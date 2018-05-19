package org.cytoscape.app.internal.task;

import java.awt.Container;

import javax.swing.SwingUtilities;

import org.cytoscape.app.internal.ui.AppManagerDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ShowInstalledAppsTask extends AbstractTask {
	
	private final Container parent;
	
	public ShowInstalledAppsTask(	Container parent) {
		this.parent = parent;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SwingUtilities.invokeLater(() -> {
			if (parent instanceof AppManagerDialog) {
				((AppManagerDialog) parent).changeTab(1);
			}
		});
	}

}
