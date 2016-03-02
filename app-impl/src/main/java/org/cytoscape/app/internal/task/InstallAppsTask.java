package org.cytoscape.app.internal.task;

import java.util.Collection;
import java.util.Map;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallAppsTask extends AbstractTask {
	
	final Collection<App> appsToInstall;
	final Map<App, App> appsToReplace;
	final AppManager appManager;
	
	public InstallAppsTask(final Collection<App> appsToInstall, final Map<App,App> appsToReplace, final AppManager appManager) {
		this.appsToInstall = appsToInstall;
		this.appsToReplace = appsToReplace;
		this.appManager = appManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		for(App appToInstall : appsToInstall) {
			App appToReplace = appsToReplace.get(appToInstall);
			if(appToReplace != null)
				taskMonitor.setStatusMessage("Updating " + appToInstall.getAppName());
			else
				taskMonitor.setStatusMessage("Installing " + appToInstall.getAppName());
			
			appManager.installApp(appToInstall);
			if(appToReplace != null && !appToInstall.getVersion().equals(appToReplace.getVersion()))
				appManager.uninstallApp(appToReplace);
		}

	}

}
