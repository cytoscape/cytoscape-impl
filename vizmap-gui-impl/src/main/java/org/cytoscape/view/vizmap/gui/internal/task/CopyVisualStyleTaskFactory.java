package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class CopyVisualStyleTaskFactory extends AbstractTaskFactory {

	private final SelectedVisualStyleManager manager;
	private final VisualMappingManager vmm;
	private final VisualStyleFactory factory;

	public CopyVisualStyleTaskFactory(final VisualMappingManager vmm, final VisualStyleFactory factory,
			final SelectedVisualStyleManager manager) {
		this.manager = manager;
		this.vmm = vmm;
		this.factory = factory;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CopyVisualStyleTask(vmm, factory, manager));
	}

}
