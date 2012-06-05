package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class DeleteMappingFunctionTaskFactory extends AbstractTaskFactory {

	private final PropertySheetPanel panel;
	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;

	public DeleteMappingFunctionTaskFactory(final PropertySheetPanel panel, final VisualMappingManager vmm,
			final CyApplicationManager appManager) {
		this.panel = panel;
		this.appManager = appManager;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DeleteMappingFunctionTask(panel.getTable(), appManager, vmm));
	}

}