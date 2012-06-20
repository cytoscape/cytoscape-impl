package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public final class CreateNewVisualStyleTaskFactory extends AbstractTaskFactory {

	private final VisualStyleFactory vsFactory;
	private final VisualMappingManager vmm;
	private final CyEventHelper eventHelper;

	public CreateNewVisualStyleTaskFactory(final VisualStyleFactory vsFactory,
										   final VisualMappingManager vmm,
										   final CyEventHelper eventHelper) {
		this.vsFactory = vsFactory;
		this.vmm = vmm;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateNewVisualStyleTask(vsFactory, vmm, eventHelper));
	}
}
