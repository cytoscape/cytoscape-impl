package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.application.CyVersion;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallAppFromJarTask extends AbstractTask {
	
	private final File appFile;
	private final AppManager appManager;
	private final boolean promptToReplace;
	
	public InstallAppFromJarTask(final File appFile, final AppManager appManager, final boolean promptToReplace) {
		this.appFile = appFile;
		this.appManager = appManager;
		this.promptToReplace = promptToReplace;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Parse app
		App appToInstall = appManager.getAppParser().parseApp(appFile);
		CyVersion version = appManager.getCyVersion();
		if(!appToInstall.isCompatible(version))
			throw new Exception("Unable to install " + appToInstall.getAppName() + 
					".\nIt is incompatible with this version of Cytoscape (" + version.getVersion() +
					").");
		
		taskMonitor.setStatusMessage("Resolving dependencies for " + appToInstall.getAppName() + "...");
		taskMonitor.setProgress(-1);
		insertTasksAfterCurrentTask(new InstallAppTask(appToInstall, appManager, promptToReplace));

		// Download dependencies
		final Set<WebApp> webApps = appManager.getWebQuerier().getAllApps();
		
		if (webApps != null && appToInstall.getDependencies() != null) {
			// Only attempt to install dependencies if the app has dependencies and
			// the App Manager can connect to the App Store.
			appManager.getWebQuerier().checkWebAppInstallStatus(webApps, appManager);
			for (final App.Dependency dep : appToInstall.getDependencies()) {
				final WebApp webApp = findWebAppForDep(dep, webApps);
				if (webApp == null) {
					taskMonitor.showMessage(TaskMonitor.Level.WARN, "App may not work. Could not find dependency through the App Store: " + dep);
				} else {
					if (webApp.getCorrespondingApp() == null ||
							WebQuerier.compareVersions(webApp.getCorrespondingApp().getVersion(), dep.getVersion()) > 0) {
						// install/update dependency
						insertTasksAfterCurrentTask(new InstallAppFromNetworkTask(webApp, appManager.getWebQuerier(), appManager));
					} else {
						taskMonitor.showMessage(TaskMonitor.Level.INFO, "App dependency already satisfied: " + dep);
					}
				}
			}
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
}
