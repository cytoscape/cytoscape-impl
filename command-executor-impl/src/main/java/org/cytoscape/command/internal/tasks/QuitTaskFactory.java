package org.cytoscape.command.internal.tasks;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class QuitTaskFactory extends AbstractTaskFactory {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public QuitTaskFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new QuitTask(serviceRegistrar));
	}

}
