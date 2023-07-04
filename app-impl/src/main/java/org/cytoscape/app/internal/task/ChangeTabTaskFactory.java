package org.cytoscape.app.internal.task;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ChangeTabTaskFactory extends AbstractTaskFactory {
	final AppManager appManager;
	final CyServiceRegistrar serviceRegistrar;


	public ChangeTabTaskFactory(final AppManager appManager, final CyServiceRegistrar serviceRegistrar) {
		this.appManager = appManager;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ChangeTabTask(appManager, serviceRegistrar));
	}

	@Override
	public boolean isReady() { return true; }

}
