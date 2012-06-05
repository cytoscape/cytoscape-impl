package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RenameVisualStyleTaskFactory extends AbstractTaskFactory {

	private final SelectedVisualStyleManager manager;
	private final VisualMappingManager vmm;

	public RenameVisualStyleTaskFactory(final SelectedVisualStyleManager manager, final VisualMappingManager vmm) {
		this.manager = manager;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RenameVisualStyleTask(manager, vmm));
	}
}
