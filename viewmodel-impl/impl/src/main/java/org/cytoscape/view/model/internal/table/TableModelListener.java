package org.cytoscape.view.model.internal.table;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;

public class TableModelListener implements ColumnCreatedListener, ColumnDeletedListener {

	private final CyServiceRegistrar registrar;
	private final CyTableViewImpl tableView;
	
	
	public TableModelListener(CyTableViewImpl tableView, CyServiceRegistrar registrar) {
		this.registrar = registrar;
		this.tableView = tableView;
	}

	
	private CyEventHelper getEventHelper() {
		return registrar.getService(CyEventHelper.class);
	}
	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;
		
		CyTable table = e.getSource();
		CyColumn column = table.getColumn(e.getColumnName());
		if(column != null) {
			View<CyColumn> view = tableView.addColumn(column);
			
//			if(view != null) {
//				getEventHelper().addEventPayload(tableView, view, AddedColumnViewsEvent.class);
//			}
		}
	}
	

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;

		CyTable table = e.getSource();
		CyColumn column = table.getColumn(e.getColumnName());

		if(column != null) {
			View<CyColumn> view = tableView.removeColumn(column);
//			if(view != null) {
//				getEventHelper().addEventPayload(tableView, view, AboutToRemoveColumnViewsEvent.class);
//			}
		}
	}
	
	
	

}
