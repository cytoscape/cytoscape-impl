package org.cytoscape.app.internal.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.Update;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class InstallUpdatesTask extends AbstractTask {
	
	private final Collection<Update> updates;
	private final AppManager appManager;
	
	public InstallUpdatesTask(final Collection<Update> updates, final AppManager appManager) {
		this.updates = updates;
		this.appManager = appManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Install Updates");
		taskMonitor.setTitle("");
		List<WebApp> webApps = new ArrayList<>();
        for(Update update: updates) {
        	webApps.add(update.getWebApp());
        }
        insertTasksAfterCurrentTask(new InstallAppsFromWebAppTask(webApps, appManager, false));
	}

}
