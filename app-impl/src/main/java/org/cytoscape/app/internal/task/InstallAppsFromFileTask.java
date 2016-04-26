package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallAppsFromFileTask extends AbstractTask {
	final Collection<File> appFiles;
	final AppManager appManager;
	final boolean promptToReplace;
	
	public InstallAppsFromFileTask(final Collection<File> appFiles, final AppManager appManager, final boolean promptToReplace) {
		this.appFiles = appFiles;
		this.appManager = appManager;
		this.promptToReplace = promptToReplace;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Install from File");
		taskMonitor.setTitle("");
		
		List<App> apps = new ArrayList<>();
		for(File appFile: appFiles) {
			App app = appManager.getAppParser().parseApp(appFile);
			apps.add(app);
		}
		taskMonitor.setStatusMessage("Starting install...");
		insertTasksAfterCurrentTask(new ResolveAppDependenciesTask(apps, appManager, promptToReplace));
	}

}
