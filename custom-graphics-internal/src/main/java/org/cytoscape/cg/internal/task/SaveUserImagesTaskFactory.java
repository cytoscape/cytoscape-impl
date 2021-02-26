package org.cytoscape.cg.internal.task;

import java.io.File;

import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SaveUserImagesTaskFactory extends AbstractTaskFactory {

	private final File location;
	private final CustomGraphicsManager manager;

	public SaveUserImagesTaskFactory(File location, CustomGraphicsManager manager) {
		this.manager = manager;
		this.location = location;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SaveUserImagesTask(location, manager));
	}
}
