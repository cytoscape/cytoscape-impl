package org.cytoscape.task.internal.filter;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RenameFilterTaskFactory extends AbstractTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;
	
	public RenameFilterTaskFactory(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RenameFilterTask(serviceRegistrar));
	}

}
