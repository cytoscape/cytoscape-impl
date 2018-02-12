package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class UpdateTaskFactory extends AbstractTaskFactory {
	final AppManager appManager;
	final UpdateManager updateManager;

	public UpdateTaskFactory(final AppManager appManager, final UpdateManager updateManager) {
		this.appManager = appManager;
		this.updateManager = updateManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new UpdateTask(appManager, updateManager));
	}

	@Override
	public boolean isReady() { return true; }

}
