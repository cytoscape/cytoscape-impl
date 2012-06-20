package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CopyVisualStyleTaskFactory extends AbstractTaskFactory {

	private final VisualMappingManager vmm;
	private final VisualStyleFactory factory;
	private final CyEventHelper eventHelper;

	public CopyVisualStyleTaskFactory(final VisualMappingManager vmm,
									  final VisualStyleFactory factory,
									  final CyEventHelper eventHelper) {
		this.vmm = vmm;
		this.factory = factory;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CopyVisualStyleTask(vmm, factory, eventHelper));
	}
}
