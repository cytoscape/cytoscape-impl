package org.cytoscape.commandDialog.internal.tasks;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class QuitTask extends AbstractTask {
	public String getTitle() { return "Exiting Cytoscape"; }
	CyShutdown shutdown;

	public QuitTask(CyShutdown shutdown) {
		super();
		this.shutdown = shutdown;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		shutdown.exit(0, true);
	}
}
