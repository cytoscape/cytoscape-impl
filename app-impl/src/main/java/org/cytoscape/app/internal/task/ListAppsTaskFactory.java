package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ListAppsTaskFactory extends AbstractTaskFactory {
	final AppManager appManager;
	final AppStatus status;

	public ListAppsTaskFactory(final AppManager appManager, final AppStatus status) {
		this.appManager = appManager;
		this.status = status;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ListAppsTask(appManager, status));
	}

	@Override
	public boolean isReady() { return true; }

}
