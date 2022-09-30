package org.cytoscape.cg.internal.task;

import java.io.File;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SaveGraphicsToSessionTaskFactory extends AbstractTaskFactory {

	private final File location;
	private final SessionAboutToBeSavedEvent event;
	private final CyServiceRegistrar serviceRegistrar;

	public SaveGraphicsToSessionTaskFactory(
			File location,
			SessionAboutToBeSavedEvent event,
			CyServiceRegistrar serviceRegistrar
	) {
		this.location = location;
		this.event = event;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(
				new SaveUserImagesTask(location, serviceRegistrar),
				new SaveGraphicsToSessionTask(location, event)
		);
	}
}
