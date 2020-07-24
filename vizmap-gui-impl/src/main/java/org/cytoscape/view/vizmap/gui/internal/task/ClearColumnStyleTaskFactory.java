package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ClearColumnStyleTaskFactory extends AbstractTaskFactory {

	private final ServicesUtil servicesUtil;

	public ClearColumnStyleTaskFactory(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ClearColumnStyleTask(servicesUtil));
	}
}

