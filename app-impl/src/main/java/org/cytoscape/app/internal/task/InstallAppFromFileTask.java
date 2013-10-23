package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.manager.BundleApp;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallAppFromFileTask extends AbstractTask {
	
	private final File appFile;
	private final AppManager appManager;
	
	public InstallAppFromFileTask(final File appFile, final AppManager appManager) {
		this.appFile = appFile;
		this.appManager = appManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Installing app from file: " + appFile);
		// Parse app
		App appToInstall = appManager.getAppParser().parseApp(appFile);

		// Download dependencies
		final Set<WebApp> webApps = appManager.getWebQuerier().getAllApps();
		if (webApps != null && appToInstall.getDependencies() != null) {
			// Only attempt to install dependencies if the app has dependencies and
			// the App Manager can connect to the App Store.
			final Set<App> installedApps = appManager.getApps();
			for (final App.Dependency dep : appToInstall.getDependencies()) {
				final WebApp webApp = findWebAppForDep(dep, webApps);
				if (webApp == null) {
					taskMonitor.showMessage(TaskMonitor.Level.WARN, "App may not work. Could not find dependency through the App Store: " + dep);
				} else {
					if (webApp.getCorrespondingApp() == null || webApp.getCorrespondingApp().isDetached()) {
						insertTasksAfterCurrentTask(new InstallAppFromAppStoreTask(webApp, appManager.getWebQuerier(), appManager));
					} else {
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "App dependency already satisfied: " + dep);
					}
				}
			}
		}
		
		// Check for name collisions
		Set<App> conflictingApps = checkAppNameCollision(appToInstall.getAppName());
		if (conflictingApps.size() == 1 && conflictingApps.iterator().next() instanceof BundleApp) {
			App conflictingApp = conflictingApps.iterator().next();
			insertTasksAfterCurrentTask(new ResolveAppInstallationConflictTask(appToInstall, conflictingApp, appManager));
		}
		else
		{
			appManager.installApp(appToInstall);
		}
	}

	private WebApp findWebAppForDep(final App.Dependency dep, final Set<WebApp> webApps) {
		// We search web apps (instead of the App Manager's list of all apps) because web apps
		// represent the list of apps we can download and install from the App Store.
		for (final WebApp webApp : webApps) {
			if (webApp.getFullName().equalsIgnoreCase(dep.getName())) {
				return webApp;
			}
		}
		return null;
	}
	
	private Set<App> checkAppNameCollision(String appName) {
		Set<App> collidingApps = new HashSet<App>();
		
		for (App app : appManager.getApps()) {
			if (appName.equalsIgnoreCase(app.getAppName())) {
				
				if (app.isDetached() == false) {
					collidingApps.add(app);
				}
			}
		}
		
		return collidingApps;
	}

}
