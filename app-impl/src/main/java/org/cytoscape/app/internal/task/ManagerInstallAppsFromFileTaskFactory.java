package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;


public class ManagerInstallAppsFromFileTaskFactory extends AbstractTaskFactory {
	final AppManager appManager;
  final TaskManager taskManager;
  final CyServiceRegistrar serviceRegistrar;

	public ManagerInstallAppsFromFileTaskFactory(final AppManager appManager, final TaskManager taskManager, final CyServiceRegistrar serviceRegistrar) {
		this.appManager = appManager;
    this.taskManager = taskManager;
    this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ManagerInstallAppsFromFileTask(appManager, taskManager, serviceRegistrar));
	}

	@Override
	public boolean isReady() { return true; }

}
