package org.cytoscape.app.internal;


import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class AppManagerTask extends AbstractTask {
	private CySwingApplication desktop;


	AppManagerTask(CySwingApplication desktop) {
		this.desktop = desktop;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {

		System.out.println("AppManagerTask.run() ................................");
	}

	@Override
	public void cancel() {
	}
}
