package org.cytoscape.ding.customgraphicsmgr.internal;

import java.io.File;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class RestoreImageTaskFactory implements TaskFactory {
	
	private final File imageLocation;
	private final CustomGraphicsManagerImpl manager;
	private final CyEventHelper eventHelper;
	
	RestoreImageTaskFactory(final File imageLocation, final CustomGraphicsManagerImpl manager, final CyEventHelper eventHelper) {
		this.manager = manager;
		this.imageLocation = imageLocation;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RestoreImageTask(imageLocation, manager, eventHelper));
	}

}
