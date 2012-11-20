package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class DeleteMappingFunctionTaskFactory extends AbstractTaskFactory {

	private final PropertySheetPanel panel;
	private final VisualMappingManager vmm;

	public DeleteMappingFunctionTaskFactory(final PropertySheetPanel panel, final VisualMappingManager vmm) {
		this.panel = panel;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DeleteMappingFunctionTask(panel.getTable(), vmm));
	}
}