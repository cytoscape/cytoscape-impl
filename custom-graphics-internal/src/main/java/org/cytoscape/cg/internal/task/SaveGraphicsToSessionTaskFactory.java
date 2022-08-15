package org.cytoscape.cg.internal.task;

import java.io.File;

import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SaveGraphicsToSessionTaskFactory extends AbstractTaskFactory {

	private final File location;

	private final SessionAboutToBeSavedEvent e;

	public SaveGraphicsToSessionTaskFactory(File location, SessionAboutToBeSavedEvent e) {
		this.location = location;
		this.e = e;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// Since version 3.10, this should no longer save session images to the CytoscapeConfiguration directory!
//		return new TaskIterator(new SaveUserImagesTask(location, manager), new SaveGraphicsToSessionTask(location, e));
		return new TaskIterator(new SaveGraphicsToSessionTask(location, e));
	}
}
