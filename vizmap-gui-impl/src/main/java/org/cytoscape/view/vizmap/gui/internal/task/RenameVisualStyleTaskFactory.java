package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RenameVisualStyleTaskFactory extends AbstractTaskFactory {

	private final VisualMappingManager vmm;

	public RenameVisualStyleTaskFactory(final VisualMappingManager vmm) {
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RenameVisualStyleTask(vmm));
	}
}
