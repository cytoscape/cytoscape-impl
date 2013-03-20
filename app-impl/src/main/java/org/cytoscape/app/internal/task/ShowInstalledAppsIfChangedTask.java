package org.cytoscape.app.internal.task;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.ui.AppManagerDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ShowInstalledAppsIfChangedTask extends AbstractTask {
	
	private final AppManager appManager;
	private final Container parent;
	private final Map<String,AppStatus> appStatuses;
	
	public ShowInstalledAppsIfChangedTask(final AppManager appManager, final Container parent) {
		this.appManager = appManager;
		this.parent = parent;
		this.appStatuses = new HashMap<String, AppStatus>();
		for(App app: appManager.getApps()) {
			appStatuses.put(app.getSha512Checksum(), app.getStatus());
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		for(App app: appManager.getApps()) {
			// if an installed app's previous status has changed or can't be found
			// show the installed apps tab in AppManagerDialog
			if(app.getStatus() != appStatuses.get(app.getSha512Checksum())) {
				SwingUtilities.invokeLater(new Runnable() {
				    public void run() {
				    	if (parent instanceof AppManagerDialog) {
			        		((AppManagerDialog) parent).changeTab(1);
			        	}
				    }
				});
				return;
			}
		}
	}

}
