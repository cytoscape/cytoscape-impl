package org.cytoscape.app.internal.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.karaf.features.FeaturesService;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.internal.event.AppsChangedEvent;
import org.cytoscape.app.internal.event.AppsChangedListener;
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.application.CyApplicationConfiguration;

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
	private static final String[] APP_EXTENSIONS = {"jar", "kar"};
	
	/** Installed apps are moved to this subdirectory under the local app storage directory. */
	private static final String INSTALLED_APPS_DIRECTORY_NAME = "installed";
	
	/** Uninstalled apps are moved to this subdirectory under the local app storage directory. */
	private static final String UNINSTALLED_APPS_DIRECTORY_NAME = "uninstalled";
	
	/** Disabled apps are moved to this subdirectory under the local app storage directory. */
	private static final String DISABLED_APPS_DIRECTORY_NAME = "disabled";
	
	/** Apps are downloaded from the web store to this subdirectory under local app storage directory. */
	private static final String DOWNLOADED_APPS_DIRECTORY_NAME = "download-temp";
	
	/** Apps that are loaded are stored in this temporary directory. */
	private static final String TEMPORARY_LOADED_APPS_DIRECTORY_NAME = ".temp-installed";
	
	/** Apps that are to be installed on restart are stored in this directory. */
	private static final String INSTALL_RESTART_DIRECTORY_NAME = "install-on-restart";
	
	/** This subdirectory in the local Cytoscape storage directory is used to store app data, as 
	 * well as installed and uninstalled apps. */
	private static final String APPS_DIRECTORY_NAME = "3.0" + File.separator + "apps";
	
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
	
	
	// private KarService karService;
	
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
	
	public AppManager(CySwingAppAdapter swingAppAdapter, CyApplicationConfiguration applicationConfiguration, 
			final WebQuerier webQuerier, FeaturesService featuresService) {
		this.applicationConfiguration = applicationConfiguration;
		this.swingAppAdapter = swingAppAdapter;
		this.webQuerier = webQuerier;
		this.featuresService = featuresService;
		
		apps = new HashSet<App>();
		appParser = new AppParser();
		appListeners = new HashSet<AppsChangedListener>();
		
		// cleanKarafDeployDirectory();
		purgeTemporaryDirectories();
		initializeAppsDirectories();
		
		// Move apps from install-on-restart directory to install directory
		Set<App> installOnRestartApps = obtainAppsFromDirectory(new File(getInstallOnRestartAppsPath()), false);
		for (App app: installOnRestartApps) {
			try {
				app.moveAppFile(this, new File(getInstalledAppsPath()));
			} catch (IOException e) {
			}
		}
		
		// Remove the install-on-restart directory after apps were moved
		try {
			FileUtils.deleteDirectory(new File(getInstallOnRestartAppsPath()));
		} catch (IOException e) {
		}
		
		setupAlterationMonitor();
		
		// Obtain previously disabled, installed apps
		
		Set<App> disabledFolderApps = obtainAppsFromDirectory(new File(getDisabledAppsPath()), false);
		for (App app: disabledFolderApps) {
			try {
				boolean appRegistered = false;
				for (App regApp : apps) {
					if (regApp.heuristicEquals(app))
						appRegistered = true;
				}
				if (!appRegistered) {
					apps.add(app);
					app.disable(this);
				} else {
					// Delete the copy
					FileUtils.deleteQuietly(app.getAppFile());
					app.setAppFile(null);
				}		
			} catch (AppDisableException e) {
			}
		}
		
		Set<App> uninstalledFolderApps = obtainAppsFromDirectory(new File(getUninstalledAppsPath()), false);
		for (App app: uninstalledFolderApps) {
			try {
				boolean appRegistered = false;
				for (App regApp : apps) {
					if (regApp.heuristicEquals(app))
						appRegistered = true;
				}
				if (!appRegistered) {
					apps.add(app);
					app.uninstall(this);
				} else {
					// Delete the copy
					FileUtils.deleteQuietly(app.getAppFile());
					app.setAppFile(null);
				}
			} catch (AppUninstallException e) {
			}
		}
		
		Set<App> installedFolderApps = obtainAppsFromDirectory(new File(getInstalledAppsPath()), false);
		for (App app: installedFolderApps) {
			try {
				boolean appRegistered = false;
				for (App regApp : apps) {
					if (regApp.heuristicEquals(app))
						appRegistered = true;
				}
				if (!appRegistered) {
					apps.add(app);
					app.install(this);
				} else {
					// Delete the copy
					FileUtils.deleteQuietly(app.getAppFile());
					app.setAppFile(null);
				}
			} catch (AppInstallException e) {
				logger.warn("Failed to initially install app, " + e);
			}
		}
		
		//installAppsInDirectory(new File(getKarafDeployDirectory()), false);
		//installAppsInDirectory(new File(getInstalledAppsPath()), true);
		
		// Load apps from the "uninstalled apps" directory
		//Set<App> uninstalledApps = obtainAppsFromDirectory(new File(getUninstalledAppsPath()), true);
		//apps.addAll(uninstalledApps);
		
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
		fileAlterationMonitor = new FileAlterationMonitor(2000L);
		
		File installedAppsPath = new File(getInstalledAppsPath());
		
		FileAlterationObserver installAlterationObserver = new FileAlterationObserver(
				installedAppsPath, new SingleLevelFileFilter(installedAppsPath), IOCase.SYSTEM);
		
		final AppManager appManager = this;
		
		// Listen for events on the "installed apps" folder
		installAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(File file) {
				DebugHelper.print("Install directory file created");
				
				App parsedApp = null;
				try {
					parsedApp = appParser.parseApp(file);
				} catch (AppParsingException e) {
					logger.warn(e.getMessage());
					return;
				}
				
				App registeredApp = null;
				for (App app : apps) {
					if (parsedApp.heuristicEquals(app)) {
						registeredApp = app;
						
						// Delete old file if it was still there
						// TODO: Possible rename from filename-2 to filename?
						File oldFile = registeredApp.getAppFile();
						
						if (oldFile.exists()) {
							FileUtils.deleteQuietly(oldFile);
						}
						
						// Update file reference to reflect file having been moved
						registeredApp.setAppFile(file);
					}
				}
				
				try {
					if (registeredApp == null) {
						apps.add(parsedApp);
						parsedApp.install(appManager);
					} else {
						registeredApp.install(appManager);
					}
					
					fireAppsChangedEvent();
				} catch (AppInstallException e) {
				}
				
				// System.out.println(file + " on create");
			}
			
			@Override
			public void onFileDelete(File file) {
				// System.out.println(file + " on delete");
				
				for (App app : apps) {
					// System.out.println("checking " + app.getAppFile().getAbsolutePath());
					if (app.getAppFile().equals(file)) {
						// System.out.println(app + " moved");
						app.setStatus(AppStatus.FILE_MOVED);
					}
				}
				
				fireAppsChangedEvent();
			}
		});
		
		FileAlterationObserver disableAlterationObserver = new FileAlterationObserver(
				getDisabledAppsPath(), new SingleLevelFileFilter(new File(getDisabledAppsPath())), IOCase.SYSTEM);
		
		// Listen for events on the "disabled apps" folder
		disableAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(File file) {
				App parsedApp = null;
				try {
					parsedApp = appParser.parseApp(file);
				} catch (AppParsingException e) {
					return;
				}
				
				App registeredApp = null;
				for (App app : apps) {
					if (parsedApp.heuristicEquals(app)) {
						registeredApp = app;
						
						// Delete old file if it was still there
						// TODO: Possible rename from filename-2 to filename?
						File oldFile = registeredApp.getAppFile();
						
						if (oldFile.exists()) {
							FileUtils.deleteQuietly(oldFile);
						}
						
						// Update file reference to reflect file having been moved
						registeredApp.setAppFile(file);
					}
				}
				
				try {
					if (registeredApp == null) {
						apps.add(parsedApp);
						parsedApp.disable(appManager);
					} else {
						registeredApp.disable(appManager);
					}
					
					fireAppsChangedEvent();
				} catch (AppDisableException e) {
				}
				
				// System.out.println(file + " on create");
			}
			
			@Override
			public void onFileDelete(File file) {
				// System.out.println(file + " on delete");
				
				for (App app : apps) {
					// System.out.println("checking " + app.getAppFile().getAbsolutePath());
					if (app.getAppFile().equals(file)) {
						// System.out.println(app + " moved");
						app.setStatus(AppStatus.FILE_MOVED);
					}
				}
				
				fireAppsChangedEvent();
			}
		});
		
		FileAlterationObserver uninstallAlterationObserver = new FileAlterationObserver(
				getUninstalledAppsPath(), new SingleLevelFileFilter(new File(getUninstalledAppsPath())), IOCase.SYSTEM);
		
		// Listen for events on the "disabled apps" folder
		uninstallAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(File file) {
				App parsedApp = null;
				try {
					parsedApp = appParser.parseApp(file);
				} catch (AppParsingException e) {
					return;
				}
				
				App registeredApp = null;
				for (App app : apps) {
					if (parsedApp.heuristicEquals(app)) {
						registeredApp = app;
						
						// Delete old file if it was still there
						// TODO: Possible rename from filename-2 to filename?
						File oldFile = registeredApp.getAppFile();
						
						if (oldFile.exists()) {
							FileUtils.deleteQuietly(oldFile);
						}
						
						// Update file reference to reflect file having been moved
						registeredApp.setAppFile(file);
					}
				}
				
				try {
					if (registeredApp == null) {
						apps.add(parsedApp);
						parsedApp.uninstall(appManager);
					} else {
						registeredApp.uninstall(appManager);
					}
					
					fireAppsChangedEvent();
				} catch (AppUninstallException e) {
				}
				
				// System.out.println(file + " on create");
			}
			
			@Override
			public void onFileDelete(File file) {
				// System.out.println(file + " on delete");
				
				for (App app : apps) {
					// System.out.println("checking " + app.getAppFile().getAbsolutePath());
					if (app.getAppFile().equals(file)) {
						// System.out.println(app + " moved");
						app.setStatus(AppStatus.FILE_MOVED);
					}
				}
				
				fireAppsChangedEvent();
			}
		});
		
		
		// setupKarafDeployMonitor(fileAlterationMonitor);
		
		try {
			installAlterationObserver.initialize();
			fileAlterationMonitor.addObserver(installAlterationObserver);
			
			disableAlterationObserver.initialize();
			fileAlterationMonitor.addObserver(disableAlterationObserver);
			
			uninstallAlterationObserver.initialize();
			fileAlterationMonitor.addObserver(uninstallAlterationObserver);
			
			fileAlterationMonitor.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void checkForFileChanges() {
		for (FileAlterationObserver observer : fileAlterationMonitor.getObservers()) {
			observer.checkAndNotify();
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

		/*
		try {
			System.out.println(getChecksum(app.getAppFile()));
		} catch (ChecksumException e) {
			System.out.println(e.getMessage());
		}
		*/
		
		try {
			app.moveAppFile(this, new File(getInstalledAppsPath()));
		} catch (IOException e) {
			throw new AppInstallException("Unable to move app file, " + e.getMessage());
		}
		
		checkForFileChanges();
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
		
		try {
			app.moveAppFile(this, new File(getUninstalledAppsPath()));
		} catch (IOException e) {
			throw new AppUninstallException("Unable to move app file, " + e.getMessage());
		}

		checkForFileChanges();
	}

    public void disableApp(App app) throws AppDisableException {
		
    	try {
			app.moveAppFile(this, new File(getDisabledAppsPath()));
		} catch (IOException e) {
			throw new AppDisableException("Unable to move app file, " + e.getMessage());
		}

		checkForFileChanges();
    }
	
	public void fireAppsChangedEvent() {
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
	 * Return the canonical path of the subdirectory in the local storage directory containing disabled apps.
	 * @return The canonical path of the subdirectory in the local storage directory containing disabled apps,
	 * or <code>null</code> if there was an error obtaining the canonical path.
	 */
	public String getDisabledAppsPath() {
		File path = new File(getBaseAppPath() + File.separator + DISABLED_APPS_DIRECTORY_NAME);
		
		try {
			// Create the directory if it doesn't exist	
			if (!path.exists()) {
				path.mkdirs();
			}
			
			return path.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Failed to obtain path to disabled apps directory");
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
	 * Return the canonical path of the subdirectory in the local storage directory used to contain apps that
	 * are installed on restart.
	 * @return The canonical path of the subdirectory in the local storage directory containing
	 * apps to install on restart.
	 */
	public String getInstallOnRestartAppsPath() {
		File path = new File(getBaseAppPath() + File.separator + INSTALL_RESTART_DIRECTORY_NAME);
		
		try {
			// Create the directory if it doesn't exist
			if (!path.exists()) {
				path.mkdirs();
			}
			
			return path.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Failed to obtain path to directory containing apps to install on restart");
			return path.getAbsolutePath();
		}
	}
	
	private boolean checkIfCytoscapeApp(File file) {
		JarFile jarFile = null;
		
		try {
			jarFile = new JarFile(file);
			
			Manifest manifest = jarFile.getManifest();
			
			// Check the manifest file 
			if (manifest != null) {
				if (manifest.getMainAttributes().getValue("Cytoscape-App-Name") != null) {

					jarFile.close();
					return true;
				}
			}
			
			jarFile.close();
		} catch (ZipException e) {
			// Do nothing; skip file
			// e.printStackTrace();
		} catch (IOException e) {
			// Do nothing; skip file
			// e.printStackTrace();
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
				}
			}
		}

		return false;
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
	
	private void installAppsInDirectory(File directory, boolean ignoreDuplicateBundleApps) {
        // Temporary fix to get the App Manager working--this should be removed later (Samad)
        if (!directory.exists()) {
            logger.error("Attempting to load from a directory that does not exist: " + directory.getAbsolutePath());
            return;
        }

		// Parse App objects from the given directory
		Set<App> parsedApps = obtainAppsFromDirectory(directory, ignoreDuplicateBundleApps);
		
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
	private Set<App> obtainAppsFromDirectory(File directory, boolean ignoreDuplicateBundleApps) {
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
				app = null;
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
		
		File disabledDirectory = new File(getDisabledAppsPath());
		if (!disabledDirectory.exists()) {
			created = created && disabledDirectory.mkdirs();
			logger.info("Creating " + disabledDirectory + ". Success? " + created);
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
		
		File installRestartDirectory = new File(getInstallOnRestartAppsPath());
		if (!installRestartDirectory.exists()) {
			created = created && installRestartDirectory.mkdirs();
			logger.info("Creating " + installRestartDirectory + ". Success? " + created);
		}
		
		if (!created) {
			logger.error("Failed to create local app storage directories.");
		}
	}
	
	public void addAppListener(AppsChangedListener appListener) {
		appListeners.add(appListener);
	}
	
	public void removeAppListener(AppsChangedListener appListener) {
		appListeners.remove(appListener);
	}
}
