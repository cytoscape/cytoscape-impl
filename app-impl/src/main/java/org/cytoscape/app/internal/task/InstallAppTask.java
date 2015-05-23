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
	
	public InstallAppTask(final App appToInstall, final AppManager appManager) {
		this.appToInstall = appToInstall;
		this.appManager = appManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
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
