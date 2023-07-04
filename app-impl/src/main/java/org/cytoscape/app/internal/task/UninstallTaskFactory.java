package org.cytoscape.app.internal.task;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class UninstallTaskFactory extends AbstractTaskFactory {
	final AppManager appManager;

	public UninstallTaskFactory(final AppManager appManager) {
		this.appManager = appManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new UninstallTask(appManager));
	}

	@Override
	public boolean isReady() { return true; }

}
