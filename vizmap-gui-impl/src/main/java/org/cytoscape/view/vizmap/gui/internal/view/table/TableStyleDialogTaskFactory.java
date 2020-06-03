package org.cytoscape.view.vizmap.gui.internal.view.table;

import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperTableMediator;
import org.cytoscape.work.TaskIterator;

public class TableStyleDialogTaskFactory extends AbstractTableColumnTaskFactory {

	
	private final CyServiceRegistrar registrar;
	private final VizMapperTableMediator mediator;
	
	
	public TableStyleDialogTaskFactory(VizMapperTableMediator mediator, CyServiceRegistrar registrar) {
		this.mediator = mediator;
		this.registrar = registrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column) {
		return new TaskIterator(new TableStyleDialogTask(column, mediator));
	}
	
	@Override
	public boolean isReady(CyColumn column) {
		// MKTODO should this code be moved somewhere else?
		CyTableViewManager tableViewManager = registrar.getService(CyTableViewManager.class);
		var tableView = tableViewManager.getTableView(column.getTable());
		if(tableView == null)
			return false;
		
		RenderingEngineManager renderingEngineManager = registrar.getService(RenderingEngineManager.class);
		Collection<RenderingEngine<?>> renderingEngines = renderingEngineManager.getRenderingEngines(tableView);
		return renderingEngines != null && !renderingEngines.isEmpty();
	}

}
