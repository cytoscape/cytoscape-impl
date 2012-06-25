package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.karaf.features.FeaturesService;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.event.AppsChangedEvent;
import org.cytoscape.app.internal.event.AppsChangedListener;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.application.CyApplicationConfiguration;

import org.cytoscape.app.internal.net.server.LocalHttpServer;
import org.cytoscape.app.internal.util.DebugHelper;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an App Manager, which is capable of maintaining a list of all currently installed and available apps. The class
 * also provides functionalities for installing and uninstalling apps.
 */
public class AppManager {
	
	private static final Logger logger = LoggerFactory.getLogger(AppManager.class);
	
	/** Only files with these extensions are checked when looking for apps in a given subdirectory.
	 */
	private static final String[] APP_EXTENSIONS = {"jar"};
	
	/** Installed apps are copied to this subdirectory under the local app storage directory. */
	private static final String INSTALLED_APPS_DIRECTORY_NAME = "installed";
	
	/** Uninstalled apps are copied to this subdirectory under the local app storage directory. */
	private static final String UNINSTALLED_APPS_DIRECTORY_NAME = "uninstalled";
	
	/** Apps are downloaded from the web store to this subdirectory under local app storage directory. */
	private static final String DOWNLOADED_APPS_DIRECTORY_NAME = "download-temp";
	
	/** Apps that are loaded are stored in this temporary directory. */
	private static final String TEMPORARY_LOADED_APPS_DIRECTORY_NAME = ".temp-installed";
	
	/** This subdirectory in the local Cytoscape storage directory is used to store app data, as 
	 * well as installed and uninstalled apps. */
	private static final String APPS_DIRECTORY_NAME = "3.0/apps";
	
	/** The set of all apps, represented by {@link App} objects, registered to this App Manager. */
	private Set<App> apps;
	
	private Set<AppsChangedListener> appListeners;
	
	/** An {@link AppParser} object used to parse File objects and possibly URLs into {@link App} objects
	 * into a format we can more easily work with
	 */
	private AppParser appParser;
	
	/**
	 * A reference to the {@link WebQuerier} object used to make queries to the app store website.
	 */
	private WebQuerier webQuerier;
	
	/**
	 * The {@link FeaturesService} used to communicate with Apache Karaf to manage OSGi bundle based apps
	 */
	private FeaturesService featuresService;
	
	/**
	 * {@link CyApplicationConfiguration} service used to obtain the directories used to store the apps.
	 */
	private CyApplicationConfiguration applicationConfiguration;
	
	/**
	 * The {@link CySwingAppAdapter} service reference provided to the constructor of the app's {@link AbstractCyApp}-implementing class.
	 */
	private CySwingAppAdapter swingAppAdapter;
	
	private FileAlterationMonitor fileAlterationMonitor;
	
	/**
	 * A {@link FileFilter} that accepts only files in the first depth level of a given directory
	 */
	private class SingleLevelFileFilter implements FileFilter {

		private File parentDirectory;
		
		public SingleLevelFileFilter(File parentDirectory) {
			this.parentDirectory = parentDirectory;
		}
		
		@Override
		public boolean accept(File pathName) {
			if (!pathName.getParentFile().equals(parentDirectory)) {
				return false;
			} else if (pathName.isDirectory()) {
				return false;
			}
			
			return true;
		}
	}
	
	public AppManager(CySwingAppAdapter swingAppAdapter, CyApplicationConfiguration applicationConfiguration, final WebQuerier webQuerier) {
		this.applicationConfiguration = applicationConfiguration;
		this.swingAppAdapter = swingAppAdapter;
		this.webQuerier = webQuerier;
		
		apps = new HashSet<App>();

		appParser = new AppParser();
		
		purgeTemporaryDirectories();
		initializeAppsDirectories();
		
		this.appListeners = new HashSet<AppsChangedListener>();

		// Install previously enabled apps
		installAppsInDirectory(new File(getInstalledAppsPath()));
		
		// Load apps from the "uninstalled apps" directory
		Set<App> uninstalledApps = obtainAppsFromDirectory(new File(getUninstalledAppsPath()));
		apps.addAll(uninstalledApps);
		
		setupAlterationMonitor();
		
		DebugHelper.print(this, "config dir: " + applicationConfiguration.getConfigurationDirectoryLocation());
	}
	
	public FeaturesService getFeaturesService() {
		return this.featuresService;
	}
	
	public void setFeaturesService(FeaturesService featuresService) {
		this.featuresService = featuresService;
	}
	
	private void setupAlterationMonitor() {
		// Set up the FileAlterationMonitor to install/uninstall apps when apps are moved in/out of the 
		// installed/uninstalled app directories
		fileAlterationMonitor = new FileAlterationMonitor(600);
		
		File installedAppsPath = new File(getInstalledAppsPath());
		File uninstalledAppsPath = new File(getUninstalledAppsPath());
		
		FileAlterationObserver installAlterationObserver = new FileAlterationObserver(
				installedAppsPath, new SingleLevelFileFilter(installedAppsPath), IOCase.SYSTEM);
		FileAlterationObserver uninstallAlterationObserver = new FileAlterationObserver(
				uninstalledAppsPath, new SingleLevelFileFilter(uninstalledAppsPath), IOCase.SYSTEM);
		
		// Listen for events on the "installed apps" folder
		installAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileDelete(File file) {
				DebugHelper.print("Install directory file deleted");
				
				try {
					String canonicalPath = file.getCanonicalPath();
					
					for (App app : apps) {
						File appFile = app.getAppFile();
						
						if (appFile != null 
								&& appFile.getCanonicalPath().equals(canonicalPath)) {

							app.setAppFile(new File(getUninstalledAppsPath() + File.separator + appFile.getName()));
							
							try {
								uninstallApp(app);
							} catch (AppUninstallException e) {

								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFileCreate(File file) {
				DebugHelper.print("Install directory file created");
				
				App parsedApp = null;
				try {
					parsedApp = appParser.parseApp(file);
					installApp(parsedApp);

					DebugHelper.print("Installed: " + parsedApp.getAppName());
				} catch (AppParsingException e) {
					DebugHelper.print("Failed to parse: " + file.getName());
				} catch (AppInstallException e) {
					DebugHelper.print("Failed to install: " + parsedApp.getAppName());
				}
			
			}
			
			@Override
			public void onFileChange(File file) {
			}
		});
		
		
		/*
		uninstallAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileDelete(File file) {
				
				try {
					String canonicalPath = file.getCanonicalPath();
					
					Set<App> appsToBeRemoved = new HashSet<App>();
					
					for (App app : apps) {
						File appFile = app.getAppFile();
						
						// If the app was uninstalled and was moved from the uninstalled apps
						// directory, remove it from the app manager
						if (appFile != null 
								&& appFile.getCanonicalPath().equals(canonicalPath)) {
							
							//app.setAppFile(null);
							app.setAppFile(new File(getInstalledAppsPath() + File.separator + appFile.getName()));
							
							if (app.getStatus() == AppStatus.UNINSTALLED) {
								appsToBeRemoved.add(app);
							}
								
						}
						
						// TODO: Currently keeps the app registered to the app manager
						// if its state was about-to-uninstall, perhaps need to 
						// disable app re-installing as the file is no longer there.
						// Possibly do by calling app.setFile(null)
					}
					
					for (App appToBeRemoved : appsToBeRemoved) {
						removeApp(appToBeRemoved);
					}
					
					if (appsToBeRemoved.size() > 0) {
						fireAppsChangedEvent();	
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFileCreate(File file) {
				try {
					App parsedApp = appParser.parseApp(file);
					
					// If no installed app has the same name, register it and make it available
					boolean nameConflict = false;
					
					for (App registeredApp : apps) {
						if (registeredApp.getAppName().equalsIgnoreCase(parsedApp.getAppName())) {
							nameConflict = true;
							break;
						}
					}
					
					if (!nameConflict) {
						addApp(parsedApp);
						fireAppsChangedEvent();
					}
					
				} catch (AppParsingException e) {
				
				}
				
				// Do nothing if a file is added to the uninstalled apps directory
			}
			
			@Override
			public void onFileChange(File file) {
			}
		});
		*/
		
		try {
			installAlterationObserver.initialize();
			uninstallAlterationObserver.initialize();
			// fileAlterationMonitor.addObserver(installAlterationObserver);
			// fileAlterationMonitor.addObserver(uninstallAlterationObserver);
			fileAlterationMonitor.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CySwingAppAdapter getSwingAppAdapter() {
		return swingAppAdapter;
	}
	
	public AppParser getAppParser() {
		return appParser;
	}
	
	public WebQuerier getWebQuerier() {
		return webQuerier;
	}
	
	/**
	 * Registers an app to this app manager.
	 * @param app The app to register to this manager.
	 */
	public void addApp(App app) {
		apps.add(app);
		
		/*
		// Let the listeners know that an app has changed
		for (AppsChangedListener appListener : appListeners) {
			AppsChangedEvent appEvent = new AppsChangedEvent(this);
			appListener.appsChanged(appEvent);
		}
		*/
	}
	
	/**
	 * Removes an app from this app manager.
	 * @param app The app to remove
	 */
	public void removeApp(App app) {
		apps.remove(app);
	}
	
	/**
	 * Attempts to install an app. Makes a copy of the app file and places it in the directory 
	 * used to hold all installed and uninstalled apps, if it was not already present there. Then, the 
	 * app is created by instancing its class that extends {@link AbstractCyApp}.
	 * 
	 * Before the app is installed, it is checked if it contains valid packaging by its isAppValidated() method.
	 * Apps that have not been validated are ignored. Also, apps that are already installed are left alone.
	 * 
	 * @param app The {@link App} object representing and providing information about the app to install
	 * @throws AppInstallException If there was an error while attempting to install the app such as being
	 * unable to copy the app to the installed apps directory or to instance the app's entry point class
	 */
	public void installApp(App app) throws AppInstallException {
		
		try {
			app.install(this);
		} catch (AppInstallException e) {
			if (app.getAppFile() != null) {
				app.getAppFile().delete();
			}
			
			throw new AppInstallException(e.getMessage());
		}
		
		// Let the listeners know that an app has been installed
		fireAppsChangedEvent();
	}
	
	/**
	 * Uninstalls an app. If it was located in the subdirectory containing currently installed apps in the
	 * local storage directory, it will be moved to the subdirectory containing currently uninstalled apps.
	 * 
	 * The app will only be uninstalled if it is currently installed.
	 * 
	 * @param app The app to be uninstalled.
	 * @throws AppUninstallException If there was an error while attempting to uninstall the app such as
	 * attempting to uninstall an app that isn't installed, or being unable to move the app to the uninstalled
	 * apps directory
	 */
	public void uninstallApp(App app) throws AppUninstallException {
		
		app.uninstall(this);
		
		// Let the listeners know that an app has been uninstalled
		fireAppsChangedEvent();
	}
	
	private void fireAppsChangedEvent() {
		AppsChangedEvent appEvent = new AppsChangedEvent(this);
		for (AppsChangedListener appListener : appListeners) {
			appListener.appsChanged(appEvent);
		}
	}
	
	/**
	 * Return the set of all apps registered to this app manager.
	 * @return The set of all apps registered to this app manager.
	 */
	public Set<App> getApps() {
		return apps;
	}
	
	/**
	 * Return the path of the directory used to contain all apps.
	 * @return The path of the root directory containing all installed and uninstalled apps.
	 */
	private File getBaseAppPath() {
		File baseAppPath = null;
		
		// TODO: At time of writing, CyApplicationConfiguration always returns the home directory for directory location.
		try {
			baseAppPath = new File(applicationConfiguration.getConfigurationDirectoryLocation().getCanonicalPath() 
					+ File.separator + APPS_DIRECTORY_NAME);
		} catch (IOException e) {
			throw new RuntimeException("Unabled to obtain canonical path for Cytoscape local storage directory: " + e.getMessage());
		}
		
		return baseAppPath;
	}
	
	/**
	 * Return the canonical path of the subdirectory in the local storage directory containing installed apps.
	 * @return The canonical path of the subdirectory in the local storage directory containing currently installed apps,
	 * or <code>null</code> if there was an error obtaining the canonical path.
	 */
	public String getInstalledAppsPath() {
		File path = new File(getBaseAppPath() + File.separator + INSTALLED_APPS_DIRECTORY_NAME);
		
		try {
			// Create the directory if it doesn't exist	
			if (!path.exists()) {
				path.mkdirs();
			}
			
			return path.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Failed to obtain path to installed apps directory");
			return path.getAbsolutePath();
		}
	}

	/**
	 * Return the canonical path of the temporary directory in the local storage directory used to contain apps that
	 * are currently loaded.
	 * @return The canonical path of the temporary directory containing apps with classes that are loaded.
	 */
	public String getTemporaryInstallPath() {
		File path = new File(getBaseAppPath() + File.separator + TEMPORARY_LOADED_APPS_DIRECTORY_NAME);
		
		try {
			// Create the directory if it doesn't exist
			if (!path.exists()) {
				path.mkdirs();
			}
			
			return path.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Failed to obtain canonical path to the temporary installed apps directory");
			return path.getAbsolutePath();
		}
	}
	
	/**
	 * Return the canonical path of the subdirectory in the local storage directory containing uninstalled apps.
	 * @return The canonical path of the subdirectory in the local storage directory containing uninstalled apps,
	 * or <code>null</code> if there was an error obtaining the canonical path.
	 */
	public String getUninstalledAppsPath() {
		File path = new File(getBaseAppPath() + File.separator + UNINSTALLED_APPS_DIRECTORY_NAME);
		
		try {
			// Create the directory if it doesn't exist
			if (!path.exists()) {
				path.mkdirs();
			}
			
			return path.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Failed to obtain path to uninstalled apps directory");
			return path.getAbsolutePath();
		}
	}
	
	/**
	 * Return the canonical path of the subdirectory in the local storage directory used to temporarily store
	 * apps downloaded from the app store.
	 * @return The canonical path of the subdirectory in the local storage directory temporarily
	 * storing apps downloaded from the app store.
	 */
	public String getDownloadedAppsPath() {
		File path = new File(getBaseAppPath() + File.separator + DOWNLOADED_APPS_DIRECTORY_NAME);
		
		try {
			// Create the directory if it doesn't exist
			if (!path.exists()) {
				path.mkdirs();
			}
			
			return path.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Failed to obtain path to downloaded apps directory");
			return path.getAbsolutePath();
		}
	}
	
	/**
	 * Removes the temporary app download directory and the directory used to store uninstalled apps.
	 */
	public void purgeTemporaryDirectories() {
		File downloaded = new File(getDownloadedAppsPath());
		File uninstalled = new File(getUninstalledAppsPath());
		File temporaryInstall = new File(getTemporaryInstallPath());
		
		try {
			FileUtils.deleteDirectory(downloaded);
			FileUtils.deleteDirectory(uninstalled);
			FileUtils.deleteDirectory(temporaryInstall);
		} catch (IOException e) {
			logger.warn("Unable to completely remove temporary directories for downloaded, loaded, and uninstalled apps.");
		}
	}
	
	private void installAppsInDirectory(File directory) {

		// Parse App objects from the given directory
		Set<App> parsedApps = obtainAppsFromDirectory(directory);
		
		// Install each app
		for (App parsedApp : parsedApps) {
			try {
				installApp(parsedApp);
			} catch (AppInstallException e) {
				logger.warn("Unable to install app from installed apps directory: " + e.getMessage());
			}
		}
		
		DebugHelper.print("Number of apps installed from directory: " + parsedApps.size());
	}
	
	/**
	 * Obtain a set of {@link App} objects through attempting to parse files found in the first level of the given directory.
	 * @param directory The directory used to parse {@link App} objects
	 * @return A set of all {@link App} objects that were successfully parsed from files in the given directory
	 */
	private Set<App> obtainAppsFromDirectory(File directory) {
		// Obtain all files in the given directory with supported extensions, perform a non-recursive search
		Collection<File> files = FileUtils.listFiles(directory, APP_EXTENSIONS, false); 
		
		Set<App> parsedApps = new HashSet<App>();
		
		App app;
		for (File potentialApp : files) {
			app = null;
			try {
				app = appParser.parseApp(potentialApp);
			} catch (AppParsingException e) {
				DebugHelper.print("Failed to parse " + potentialApp + ", error: " + e.getMessage());
			} finally {
				if (app != null) {
					parsedApps.add(app);
					
					DebugHelper.print("App parsed: " + app);
				}
			}
		}
		
		return parsedApps;
	}
	
	/**
	 * Create app storage directories if they don't already exist.
	 */
	private void initializeAppsDirectories() {
		boolean created = true;
		
		File appDirectory = getBaseAppPath();
		if (!appDirectory.exists()) {
			created = created && appDirectory.mkdirs();
			logger.info("Creating " + appDirectory + ". Success? " + created);
		}
		
		File installedDirectory = new File(getInstalledAppsPath());
		if (!installedDirectory.exists()) {
			created = created && installedDirectory.mkdirs();
			logger.info("Creating " + installedDirectory + ". Success? " + created);
		}
		
		File temporaryInstallDirectory = new File(getTemporaryInstallPath());
		if (!temporaryInstallDirectory.exists()) {
			created = created && temporaryInstallDirectory.mkdirs();
			logger.info("Creating " + temporaryInstallDirectory + ". Success? " + created);
		}
		
		File uninstalledDirectory = new File(getUninstalledAppsPath());
		if (!uninstalledDirectory.exists()) {
			created = created && uninstalledDirectory.mkdirs();
			logger.info("Creating " + uninstalledDirectory + ". Success? " + created);
		}
		
		File downloadedDirectory = new File(getDownloadedAppsPath());
		if (!downloadedDirectory.exists()) {
			created = created && downloadedDirectory.mkdirs();
		}
		
		if (!created) {
			logger.error("Failed to create local app storage directories.");
		}
	}
	
	public void addAppListener(AppsChangedListener appListener) {
		appListeners.add(appListener);
	}
	
	public  void removeAppListener(AppsChangedListener appListener) {
		appListeners.remove(appListener);
	}
	
	/**
	 * Install apps from the local storage directory containing previously installed apps.
	 */
	public void installAppsFromDirectory() {
		installAppsInDirectory(new File(getInstalledAppsPath()));
	}
}
