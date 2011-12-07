package org.cytoscape.app.internal;


import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class PluginManagerTask extends AbstractTask {
	private CySwingApplication desktop;


	PluginManagerTask(CySwingApplication desktop) {
		this.desktop = desktop;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {

		System.out.println("PluginManagerTask.run() ................................");
	}

	@Override
	public void cancel() {
	}
}
