package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class DeleteVisualStyleTaskFactory implements TaskFactory {

	private final VisualMappingManager vmm;
	private final SelectedVisualStyleManager manager;

	public DeleteVisualStyleTaskFactory(final VisualMappingManager vmm,
			final SelectedVisualStyleManager manager) {
		this.manager = manager;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new DeleteVisualStyleTask(vmm, manager));
	}

}
