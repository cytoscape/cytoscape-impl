package org.cytoscape.ding.customgraphicsmgr.internal;

import java.io.File;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class RestoreImageTaskFactory extends AbstractTaskFactory {

	private final File imageLocation;
	private final CustomGraphicsManagerImpl manager;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	
	private final CyApplicationManager applicationManager;

	RestoreImageTaskFactory(final File imageLocation, final CustomGraphicsManagerImpl manager,
			final CyEventHelper eventHelper, final VisualMappingManager vmm, final CyApplicationManager applicationManager) {
		this.manager = manager;
		this.imageLocation = imageLocation;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.applicationManager = applicationManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		final RestoreImageTask firstTask = new RestoreImageTask(imageLocation, manager, eventHelper);
		final TaskIterator itr = new TaskIterator(firstTask);
		
		itr.insertTasksAfter(firstTask, new ReplaceDummyGraphicsTask(vmm, manager, applicationManager));
		return itr;
	}

}
