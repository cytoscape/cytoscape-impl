package org.cytoscape.view.vizmap.gui.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class DeleteMappingFunctionTaskFactory implements TaskFactory {

	private final PropertySheetPanel panel;
	private final SelectedVisualStyleManager manager;
	private final CyApplicationManager appManager;
	
	public DeleteMappingFunctionTaskFactory(final PropertySheetPanel panel, final SelectedVisualStyleManager manager, final CyApplicationManager appManager) {
		this.panel = panel;
		this.manager = manager;
		this.appManager = appManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DeleteMappingFunctionTask(panel.getTable(), manager, appManager));
	}

}
