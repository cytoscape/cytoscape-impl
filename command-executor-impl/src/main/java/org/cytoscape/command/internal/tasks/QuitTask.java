package org.cytoscape.command.internal.tasks;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class QuitTask extends AbstractTask {
	
	public String getTitle() { return "Exiting Cytoscape"; }
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public QuitTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		CyShutdown shutdown = serviceRegistrar.getService(CyShutdown.class);
		shutdown.exit(0, true);
	}
}
