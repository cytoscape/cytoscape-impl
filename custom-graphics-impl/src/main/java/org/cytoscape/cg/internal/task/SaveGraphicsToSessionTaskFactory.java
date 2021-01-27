package org.cytoscape.cg.internal.task;

import java.io.File;

import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SaveGraphicsToSessionTaskFactory extends AbstractTaskFactory {

	private final File location;
	private final CustomGraphicsManager manager;

	private final SessionAboutToBeSavedEvent e;

	public SaveGraphicsToSessionTaskFactory(File location, CustomGraphicsManager manager, SessionAboutToBeSavedEvent e) {
		this.manager = manager;
		this.location = location;
		this.e = e;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SaveUserImagesTask(location, manager), new SaveGraphicsToSessionTask(location, e));
	}
}
