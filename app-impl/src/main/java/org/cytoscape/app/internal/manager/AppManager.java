package org.cytoscape.app.internal.manager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.internal.event.AppsChangedEvent;
import org.cytoscape.app.internal.event.AppsChangedListener;
import org.cytoscape.app.internal.exception.AppDisableException;
import org.cytoscape.app.internal.exception.AppInstallException;
import org.cytoscape.app.internal.exception.AppLoadingException;
import org.cytoscape.app.internal.exception.AppParsingException;
import org.cytoscape.app.internal.exception.AppStartupException;
import org.cytoscape.app.internal.exception.AppUninstallException;
import org.cytoscape.app.internal.exception.AppUnloadingException;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.ui.AppManagerDialog;
import org.cytoscape.app.internal.util.DebugHelper;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.event.CyEventHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an App Manager, which is capable of maintaining a list of all currently installed and available apps. The class
 * also provides functionalities for installing and uninstalling apps.
 */
public class AppManager implements FrameworkListener {
	
	private static final Logger sysLogger = LoggerFactory.getLogger(AppManager.class);
	private static final Logger userLogger = LoggerFactory.getLogger(CyUserLog.NAME);

	
	/** Only files with these extensions are checked when looking for apps in a given subdirectory.
	 */
	private static final String[] APP_EXTENSIONS = {"jar"};
	
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
	
	/** This subdirectory in the Cytoscape installation directory is used to store core apps, */ 
	private static final String BUNDLED_APPS_DIRECTORY_NAME = "apps";
	
	/** This subdirectory in the local Cytoscape storage directory is used to store app data, as 
	 * well as installed and uninstalled apps. */
	private static final String APPS_DIRECTORY_NAME = "3" + File.separator + "apps";

	private static final int APP_START_LEVEL = 200;
	
	/** The set of all apps, represented by {@link App} objects, registered to this App Manager. */
	private Set<App> apps;
	
	private List<AppsChangedListener> appListeners;
	
	/** An {@link AppParser} object used to parse File objects and possibly URLs into {@link App} objects
	 * into a format we can more easily work with
	 */
	private AppParser appParser;
	
	/**
	 * A reference to the {@link WebQuerier} object used to make queries to the app store website.
	 */
	private WebQuerier webQuerier;
	
	/**
	 * The {@link CyEventHelper} used to fire Cytoscape events
	 */
	private CyEventHelper eventHelper;
	
	/**
	 * {@link CyVersion} service used to get the running version of Cytoscape.
	 */
	private CyVersion version;
		
	/**
	 * {@link CyApplicationConfiguration} service used to obtain the directories used to store the apps.
	 */
	private CyApplicationConfiguration applicationConfiguration;
	
	/**
	 * The {@link CySwingAppAdapter} service reference provided to the constructor of the app's {@link AbstractCyApp}-implementing class.
	 */
	private CySwingAppAdapter swingAppAdapter;
	
	private BundleContext bundleContext;
	
	private FileAlterationMonitor fileAlterationMonitor;

	private StartLevel startLevel;

	private boolean isInitialized;

	private AppManagerDialog appManagerDialog = null;

	private final Object lock = new Object();
	
	/**
	 * A {@link FileFilter} that accepts only files in the first depth level of a given directory
	 * with an extension used for apps.
	 */
	private class AppFileFilter implements FileFilter {

		private File parentDirectory;
		
		public AppFileFilter(File parentDirectory) {
			this.parentDirectory = parentDirectory;
		}
		
		@Override
		public boolean accept(File pathName) {
			if (!pathName.getParentFile().equals(parentDirectory)) {
				return false;
			} else if (pathName.isDirectory()) {
				return false;
			} 
			for(String extension: APP_EXTENSIONS) {
				if(pathName.toString().endsWith(extension))
					return true;
			}
			return false;
		}
	}
	
	public AppManager(CySwingAppAdapter swingAppAdapter, CyApplicationConfiguration applicationConfiguration, 
			CyVersion version, CyEventHelper eventHelper, final WebQuerier webQuerier, StartLevel startLevel, BundleContext bundleContext) {
		this.swingAppAdapter = swingAppAdapter;
		this.applicationConfiguration = applicationConfiguration;
		this.version = version;
		this.eventHelper = eventHelper;
		this.webQuerier = webQuerier;
		webQuerier.setAppManager(this);
		this.startLevel = startLevel;
		this.bundleContext = bundleContext;
		
		appParser = new AppParser();
		appListeners = new CopyOnWriteArrayList<AppsChangedListener>();
		
		apps = new CopyOnWriteArraySet<App>();

		// cleanKarafDeployDirectory();
		purgeTemporaryDirectories();
		initializeAppsDirectories();
		
		attemptInitialization();
	}

	public void setAppManagerDialog(AppManagerDialog dialog) {
		this.appManagerDialog = dialog;
	}

	public AppManagerDialog getAppManagerDialog() {
		return appManagerDialog;
	}
	
	@Override
	public void frameworkEvent(FrameworkEvent event) {
		// Defer initialization until we reach the right start level.
		if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
			attemptInitialization();
		}
	}
	
	void attemptInitialization() {
		synchronized (lock ) {
			if (!isInitialized && startLevel.getStartLevel() >= APP_START_LEVEL) {
				// Initialize the apps list and start apps
				initializeApps();
				isInitialized = true;
			}
		}
	}
	
	void initializeApps() {
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
		
		Set<App> disabledApps = obtainAppsFromDirectory(new File(getDisabledAppsPath()), false);
		for (App app: disabledApps) {
			try {
				boolean appRegistered = false;
				for (App regApp : apps) {
					if (regApp.heuristicEquals(app))
						appRegistered = true;
				}
				if (!appRegistered) {
					apps.add(app);
					app.setStatus(AppStatus.DISABLED);
				} else {
					// Delete the copy
					FileUtils.deleteQuietly(app.getAppFile());
					app.setAppFile(null);
				}		
			} catch (Throwable e) {
			}
		}
		
		Set<App> uninstalledApps = obtainAppsFromDirectory(new File(getUninstalledAppsPath()), false);
		for (App app: uninstalledApps) {
			try {
				boolean appRegistered = false;
				for (App regApp : apps) {
					if (regApp.heuristicEquals(app))
						appRegistered = true;
				}
				if (!appRegistered) {
					apps.add(app);
					app.setStatus(AppStatus.UNINSTALLED);
				} else {
					// Delete the copy
					FileUtils.deleteQuietly(app.getAppFile());
					app.setAppFile(null);
				}
			} catch (Throwable e) {
			}
		}
		
		
		Set<App> installedApps = obtainAppsFromDirectory(getBundledAppsPath(), true);
		installedApps.addAll(obtainAppsFromDirectory(new File(getInstalledAppsPath()), false));
		
		Map<String, App> appsToStart = new HashMap<String, App>();
		for(App app: installedApps) {
			boolean appRegistered = false;
			for (App regApp : apps) {
				if (regApp.heuristicEquals(app))
					appRegistered = true;
			}
			if (!appRegistered) {
				apps.add(app);
				String appName = app.getAppName().toLowerCase();
				App currentVersion = appsToStart.get(appName);
				if(app.isCompatible(version) && (currentVersion == null ||  
						WebQuerier.compareVersions(currentVersion.getVersion(), app.getVersion()) > 0))
					appsToStart.put(appName, app);
			}
			else {
				// Delete the copy
				FileUtils.deleteQuietly(app.getAppFile());
				app.setAppFile(null);
			}
		}
		
		Set<App> coreAppsToStart = new HashSet<App>();
		App coreAppsMetaApp = appsToStart.get("core apps");
		if(coreAppsMetaApp != null && coreAppsMetaApp.getDependencies() != null) {
			for(App.Dependency dep: coreAppsMetaApp.getDependencies()) {
				String appName = dep.getName().toLowerCase();
				App app = appsToStart.get(appName);
				if(app != null) {
					coreAppsToStart.add(app);
				}
			}
			coreAppsToStart.add(coreAppsMetaApp);
		}
		if (!startApps(coreAppsToStart))
			userLogger.warn("One or more core apps failed to load or start");
		eventHelper.fireEvent(new CyStartEvent(this));
		
		Set<App> otherAppsToStart = new HashSet<App>(appsToStart.values());
		otherAppsToStart.removeAll(coreAppsToStart);
		
		if(!startApps(otherAppsToStart))
			userLogger.warn("One or more apps failed to load or start");		
		eventHelper.fireEvent(new AppsFinishedStartingEvent(this));
	}
	
	private boolean startApps(Collection<App> apps) {
		boolean success = true;
		for(Iterator<App> i = apps.iterator(); i.hasNext();) {
			App app = i.next();
			try {
				app.load(this);
			} catch (AppLoadingException e) {
				i.remove();
				success = false;
				app.setStatus(AppStatus.FAILED_TO_LOAD);
				userLogger.error("Failed to load app " + app.getAppName(), e);
			}
		}
		
		for(App app: apps) {
			try {
				app.start(this);
				app.setStatus(AppStatus.INSTALLED);
			} catch (AppStartupException e) {
				success = false;
				app.setStatus(AppStatus.FAILED_TO_START);
				userLogger.error("Failed to start app " + app.getAppName(), e);
			}
		}
		return success;
		
	}
	
	private void setupAlterationMonitor() {
		// Set up the FileAlterationMonitor to install/uninstall apps when apps are moved in/out of the 
		// installed/uninstalled app directories
		fileAlterationMonitor = new FileAlterationMonitor(2000L);
		
		File installedAppsPath = new File(getInstalledAppsPath());
		
		FileAlterationObserver installAlterationObserver = new FileAlterationObserver(
				installedAppsPath, new AppFileFilter(installedAppsPath), IOCase.SYSTEM);
		
		final AppManager appManager = this;
		
		// Listen for events on the "installed apps" folder
		installAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(File file) {
				
				App parsedApp = null;
				try {
					parsedApp = appParser
							.parseApp(file);
				} catch (AppParsingException e) {
					return;
				}
				boolean startApp = parsedApp.isCompatible(version);
				App registeredApp = null;
				for (App app : apps) {
					if (parsedApp.heuristicEquals(app)) {
						registeredApp = app;
						
						// Delete old file if it was still there
						File oldFile = registeredApp
								.getAppFile();
						
						if (oldFile != null && oldFile.exists() && !registeredApp
								.getAppFile().equals(parsedApp
										.getAppFile())) {
							FileUtils.deleteQuietly(oldFile);
						}
						
						// Update file reference to reflect file having been moved
						registeredApp.setAppFile(file);
						registeredApp.setStatus(AppStatus.INACTIVE);
					}
					else if(parsedApp.isCompatible(version) && parsedApp.getAppName().equals(app.getAppName())) {
						try {
							if(!app.isDetached() && app.isCompatible(version)) {
								if(WebQuerier.compareVersions(parsedApp.getVersion(), app.getVersion()) > 0)
									startApp = false;
								else {
									app.unload(AppManager.this);
									app.setStatus(AppStatus.INACTIVE);
								}
							}
						} catch (AppUnloadingException e) {
							// TODO Auto-generated catch block
							userLogger.warn("Failed to unload app " + app.getAppName(), e);
						}
					}
				}
				App app = null;
				
				if (registeredApp == null) {
					app = parsedApp;
					apps.add(app);
				} else {
					app = registeredApp;
				}
				try {
					if(startApp) {
						app.load(appManager);
						app.start(appManager);
						app.setStatus(AppStatus.INSTALLED);
					}
				}
				catch (AppLoadingException e) {
					app.setStatus(AppStatus.FAILED_TO_LOAD);
					userLogger.error("Failed to load app " + app.getAppName(), e);
				}
				catch (AppStartupException e) {
					app.setStatus(AppStatus.FAILED_TO_START);
					userLogger.error("Failed to start app " + app.getAppName(), e);
				}

				fireAppsChangedEvent();
			}
			
			@Override
			public void onFileChange(File file) {
				// Can treat file replacements/changes as old file deleted, new file added
				this.onFileDelete(file);
				this.onFileCreate(file);
				
				fireAppsChangedEvent();
			}
			
			@Override
			public void onFileDelete(File file) {
				// System.out.println(file + " on delete");
				
				DebugHelper.print(this + " installObserverDelete", file.getAbsolutePath() + " deleted.");
				App registeredApp = null;
				for (App app : apps) {
					if (file.equals(app.getAppFile())) {
						app.setAppFile(null);
						registeredApp = app;
						break;
					}
				}
				
				if (registeredApp == null) return;
				
				try {
					registeredApp.unload(appManager);
					registeredApp.setStatus(AppStatus.FILE_MOVED);
				}
				catch (AppUnloadingException e) {
					userLogger.warn("Failed to unload app " + registeredApp.getAppName(), e);
				}

				//Do this so that we don't reload an old app when responding to change events
				if (file.exists()) {
					App parsedApp = null;
					try {
						parsedApp = appParser
								.parseApp(file);
					} catch (AppParsingException e) {
						return;
					}
					if(parsedApp.isCompatible(version) && 
							registeredApp.getAppName().equalsIgnoreCase(parsedApp.getAppName()))
						return;
				}

				App appToStart = null;
				for (App app: apps) {
					if(!app.isDetached() && app.isCompatible(version) && 
							app.getAppName().equalsIgnoreCase(registeredApp.getAppName())) {
						if(appToStart == null || 
								WebQuerier.compareVersions(appToStart.getVersion(), app.getVersion()) > 0) 
							appToStart = app;
					}
				}

				if(appToStart != null) {
					try {
						appToStart.load(appManager);
						appToStart.start(appManager);
						appToStart.setStatus(AppStatus.INSTALLED);
					}
					catch (AppLoadingException e) {
						appToStart.setStatus(AppStatus.FAILED_TO_LOAD);
						userLogger.error("Failed to load app " + appToStart.getAppName(), e);
					}
					catch (AppStartupException e) {
						appToStart.setStatus(AppStatus.FAILED_TO_START);
						userLogger.error("Failed to start app " + appToStart.getAppName(), e);
					}
				}
			fireAppsChangedEvent();
			}
		});
		
		FileAlterationObserver disableAlterationObserver = new FileAlterationObserver(
				getDisabledAppsPath(), new AppFileFilter(new File(getDisabledAppsPath())), IOCase.SYSTEM);
		
		// Listen for events on the "disabled apps" folder
		disableAlterationObserver.addListener( new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(File file) {
				App parsedApp = null;
				try {
					parsedApp = appParser.parseApp(file);
				} catch (AppParsingException e) {
					return;
				}
				
				DebugHelper.print(this + " disableObserver Create", parsedApp.getAppName() + " parsed");
				
				App registeredApp = null;
				for (App app : apps) {
					if (parsedApp.heuristicEquals(app)) {
						registeredApp = app;
						
						// Delete old file if it was still there
						// TODO: Possible rename from filename-2 to filename?
						File oldFile = registeredApp.getAppFile();
						
						if (oldFile != null && oldFile.exists() && !registeredApp.getAppFile().equals(parsedApp.getAppFile())) {
							DebugHelper.print(this + " disableObserverCreate", 
									registeredApp.getAppName() + " moved from " 
									+ registeredApp.getAppFile().getAbsolutePath() + " to " 
									+ parsedApp.getAppFile().getAbsolutePath() + ". deleting: " + oldFile);
							
							FileUtils.deleteQuietly(oldFile);
						}
						
						// Update file reference to reflect file having been moved
						registeredApp.setAppFile(file);
					}
				}
				App app = null;
				
				if (registeredApp == null) {
					app = parsedApp;
					apps.add(app);
				} else {
					app = registeredApp;
				}
				
				app.setStatus(AppStatus.DISABLED);	
				fireAppsChangedEvent();
				
				// System.out.println(file + " on create");
			}
			
			@Override
			public void onFileChange(File file) {
				// Can treat file replacements/changes as old file deleted, new file added
				this.onFileDelete(file);
				this.onFileCreate(file);
				
				fireAppsChangedEvent();
			}
			
			@Override
			public void onFileDelete(File file) {
				// System.out.println(file + " on delete");
				
				DebugHelper.print(this + " disableObserverDelete", file.getAbsolutePath() + " deleted.");
				
				for (App app : apps) {
					// System.out.println("checking " + app.getAppFile().getAbsolutePath());
					if (file.equals(app.getAppFile())) {
						app.setAppFile(null);
						app.setStatus(AppStatus.FILE_MOVED);
						break;
					}
				}
				
				fireAppsChangedEvent();
			}
		});
		
		FileAlterationObserver uninstallAlterationObserver = new FileAlterationObserver(
				getUninstalledAppsPath(), new AppFileFilter(new File(getUninstalledAppsPath())), IOCase.SYSTEM);
		
		// Listen for events on the "uninstalled apps" folder
		uninstallAlterationObserver.addListener(new FileAlterationListenerAdaptor() {
			@Override
			public void onFileCreate(File file) {
				App parsedApp = null;
				try {
					parsedApp = appParser.parseApp(file);
				} catch (AppParsingException e) {
					return;
				}
				
				DebugHelper.print(this + " uninstallObserverCreate", parsedApp.getAppName() + " parsed");
				
				App registeredApp = null;
				for (App app : apps) {
					if (parsedApp.heuristicEquals(app)) {
						registeredApp = app;
						
						// Delete old file if it was still there
						// TODO: Possible rename from filename-2 to filename?
						File oldFile = registeredApp.getAppFile();
						
						if (oldFile != null && oldFile.exists() && !registeredApp.getAppFile().equals(parsedApp.getAppFile())) {
							DebugHelper.print(this + " uninstallObserverCreate", 
									registeredApp.getAppName() + " moved from " 
									+ registeredApp.getAppFile().getAbsolutePath() + " to " 
									+ parsedApp.getAppFile().getAbsolutePath() + ". deleting: " + oldFile);
							
							FileUtils.deleteQuietly(oldFile);
						}
						
						// Update file reference to reflect file having been moved
						registeredApp.setAppFile(file);
					}
				}
				App app = null;
				
				if (registeredApp == null) {
					app = parsedApp;
					apps.add(app);
				} else {
					app = registeredApp;
				}

				app.setStatus(AppStatus.UNINSTALLED);
				fireAppsChangedEvent();
				
				// System.out.println(file + " on create");
			}
			
			@Override
			public void onFileChange(File file) {
				// Can treat file replacements/changes as old file deleted, new file added
				this.onFileDelete(file);
				this.onFileCreate(file);
				
				fireAppsChangedEvent();
			}
			
			@Override
			public void onFileDelete(File file) {
				// System.out.println(file + " on delete");
				
				DebugHelper.print(this + " uninstallObserverDelete", file.getAbsolutePath() + " deleted.");
				
				for (App app : apps) {
					// System.out.println("checking " + app.getAppFile().getAbsolutePath());
					if (file.equals(app.getAppFile())) {
						app.setAppFile(null);
						app.setStatus(AppStatus.FILE_MOVED);
						break;
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
	
	public BundleContext getBundleContext() {
		return bundleContext;
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
		
		if(app.isBundledApp()) return;
		
		boolean installOnRestart = false;

		if(app instanceof SimpleApp){
			for (App regApp : apps) {
				if (app.getAppName().equalsIgnoreCase(regApp.getAppName()) && !app.isDetached()) {
					installOnRestart = true;
				}
			}
		}
		
		if (installOnRestart) {
			try {
				app.moveAppFile(this, new File(getInstallOnRestartAppsPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new AppInstallException("Unable to move app file" ,e);
			}
			checkForFileChanges();
			
			apps.add(app);
			app.setStatus(AppStatus.TO_BE_INSTALLED);
			fireAppsChangedEvent();
		}
		else {
			try {
				app.moveAppFile(this, new File(getInstalledAppsPath()));
			} catch (IOException e) {
				throw new AppInstallException("Unable to move app file", e);
			}
			checkForFileChanges();
		}
	}
	
	/**
	 * Uninstalls an app. If it was located in the subdirectory containing currently installed apps in the
	 * local storage directory, it will be moved to the subdirectory containing currently uninstalled apps.
	 * 
	 * @param app The app to be uninstalled.
	 * @throws AppUninstallException If there was an error while attempting to uninstall the app such as
	 * attempting to uninstall an app that isn't installed, or being unable to move the app to the uninstalled
	 * apps directory
	 */
	public void uninstallApp(App app) throws AppUninstallException {
		if(app.isBundledApp()) return; 
		
		try {
			app.moveAppFile(this, new File(getUninstalledAppsPath()));
		} catch (IOException e) {
			throw new AppUninstallException("Unable to move app file", e);
		}
		checkForFileChanges();
	}

    public void disableApp(App app) throws AppDisableException {
    	if(app.isBundledApp()) return;
    	
    	try {
			app.moveAppFile(this, new File(getDisabledAppsPath()));
		} catch (IOException e) {
			throw new AppDisableException("Unable to move app file", e);
		}
    	checkForFileChanges();
    }
    
//    /**
//     * Attempts to remove uninstalled apps so that they don't take up space on the UI.
//     * If you wish to display uninstalled apps for purposes of completion, avoid calling this function.
//     */
//    public void clearUninstalledApps() {
//    	Set<App> appsToBeRemoved = new HashSet<App>();
//    	
//    	for (App app : apps) {
//    		if (app.getStatus() == AppStatus.UNINSTALLED
//    				|| app.getStatus() == AppStatus.FILE_MOVED) {
//    			
//    			appsToBeRemoved.add(app);
//    		}
//    	}
//    	
//    	this.apps.removeAll(appsToBeRemoved);
//    }
	
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
	 * Return the set of installed apps.
	 * @return The set of all installed apps.
	 */
	public Set<App> getInstalledApps() {
		final Set<App> installedApps = new HashSet<App>();
		
		for (App app : apps) {
			if (app.getStatus() != AppStatus.INACTIVE
					&& !app.isDetached()) {
				installedApps.add(app);
			}
		}
		return installedApps;
	}
	
	/**
	 * Return the CyVersion for this Cytoscape instance.
	 * @return The CyVersion.
	 */
	public CyVersion getCyVersion() {
		return version;
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
			throw new RuntimeException("Unabled to obtain canonical path for Cytoscape local storage directory", e);
		}
		
		return baseAppPath;
	}
	
	/**
	 * Return the path of the directory containing apps bundled with Cytoscape.
	 * @return The path of the root directory containing apps bundled with Cytoscape.
	 */
	private File getBundledAppsPath() {
		File path = null;
		
		// TODO: At time of writing, CyApplicationConfiguration always returns the home directory for directory location.
		try {
			path = new File(applicationConfiguration.getInstallationDirectoryLocation().getCanonicalPath() 
					+ File.separator + BUNDLED_APPS_DIRECTORY_NAME);
		} catch (IOException e) {
			throw new RuntimeException("Unable to obtain canonical path for Cytoscape installation directory", e);
		}
		
		return path;
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
			sysLogger.warn("Failed to obtain path to installed apps directory");
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
			sysLogger.warn("Failed to obtain path to disabled apps directory");
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
			sysLogger.warn("Failed to obtain canonical path to the temporary installed apps directory");
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
			sysLogger.warn("Failed to obtain path to uninstalled apps directory");
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
			sysLogger.warn("Failed to obtain path to downloaded apps directory");
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
			sysLogger.warn("Failed to obtain path to directory containing apps to install on restart");
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
			sysLogger.warn("Unable to completely remove temporary directories for downloaded, loaded, and uninstalled apps.");
		}
	}
	
	/**
	 * Obtain a set of {@link App} objects through attempting to parse files found in the first level of the given directory.
	 * @param directory The directory used to parse {@link App} objects
	 * @return A set of all {@link App} objects that were successfully parsed from files in the given directory
	 */
	private Set<App> obtainAppsFromDirectory(File directory, boolean isBundled) {
		// Obtain all files in the given directory with supported extensions, perform a non-recursive search
		Collection<File> files = FileUtils.listFiles(directory, APP_EXTENSIONS, false); 
		
		Set<App> parsedApps = new HashSet<App>();
		
		App app;
		for (File file : files) {
			
			app = null;
			try {
				app = appParser.parseApp(file);
				app.setBundledApp(isBundled);
			} catch (AppParsingException e) {
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
			sysLogger.info("Creating " + appDirectory + ". Success? " + created);
		}
		
		File installedDirectory = new File(getInstalledAppsPath());
		if (!installedDirectory.exists()) {
			created = created && installedDirectory.mkdirs();
			sysLogger.info("Creating " + installedDirectory + ". Success? " + created);
		}
		
		File disabledDirectory = new File(getDisabledAppsPath());
		if (!disabledDirectory.exists()) {
			created = created && disabledDirectory.mkdirs();
			sysLogger.info("Creating " + disabledDirectory + ". Success? " + created);
		}
		
		File temporaryInstallDirectory = new File(getTemporaryInstallPath());
		if (!temporaryInstallDirectory.exists()) {
			created = created && temporaryInstallDirectory.mkdirs();
			sysLogger.info("Creating " + temporaryInstallDirectory + ". Success? " + created);
		}
		
		File uninstalledDirectory = new File(getUninstalledAppsPath());
		if (!uninstalledDirectory.exists()) {
			created = created && uninstalledDirectory.mkdirs();
			sysLogger.info("Creating " + uninstalledDirectory + ". Success? " + created);
		}
		
		File downloadedDirectory = new File(getDownloadedAppsPath());
		if (!downloadedDirectory.exists()) {
			created = created && downloadedDirectory.mkdirs();
		}
		
		File installRestartDirectory = new File(getInstallOnRestartAppsPath());
		if (!installRestartDirectory.exists()) {
			created = created && installRestartDirectory.mkdirs();
			sysLogger.info("Creating " + installRestartDirectory + ". Success? " + created);
		}
		
		if (!created) {
			sysLogger.error("Failed to create local app storage directories.");
		}
	}
	
	public void addAppListener(AppsChangedListener appListener) {
		if (appListeners.contains(appListener)) {
			return;
		}
		appListeners.add(appListener);
	}
	
	public void removeAppListener(AppsChangedListener appListener) {
		appListeners.remove(appListener);
	}
}
