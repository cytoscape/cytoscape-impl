package org.cytoscape.app.internal.task;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.manager.BundleApp;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallAppTask extends AbstractTask {
	
	private final App appToInstall;
	private final AppManager appManager;
	private final boolean promptToReplace;
	
	public InstallAppTask(final App appToInstall, final AppManager appManager, final boolean promptToReplace) {
		this.appToInstall = appToInstall;
		this.appManager = appManager;
		this.promptToReplace = promptToReplace;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Check for name collisions
		taskMonitor.setStatusMessage("Installing " + appToInstall.getAppName() + "...");
		Set<App> conflictingApps = checkAppNameCollision(appToInstall.getAppName());
		if (conflictingApps.size() == 1) {
			App conflictingApp = conflictingApps.iterator().next();
			if(promptToReplace)
				insertTasksAfterCurrentTask(new ResolveAppInstallationConflictTask(appToInstall, conflictingApp, appManager));
			else {
				appManager.installApp(appToInstall);
				appManager.uninstallApp(conflictingApp);
			}
		}
		else
		{
			appManager.installApp(appToInstall);
		}
	}
	
	private Set<App> checkAppNameCollision(String appName) {
		Set<App> collidingApps = new HashSet<App>();
		
		for (App app : appManager.getInstalledApps()) {
			if (appName.equalsIgnoreCase(app.getAppName())) {
				collidingApps.add(app);
			}
		}
		
		return collidingApps;
	}
}
