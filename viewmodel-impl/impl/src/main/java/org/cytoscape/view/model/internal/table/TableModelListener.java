package org.cytoscape.view.model.internal.table;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.AboutToRemoveColumnViewEvent;
import org.cytoscape.view.model.events.AboutToRemoveRowViewsEvent;
import org.cytoscape.view.model.events.AddedColumnViewEvent;
import org.cytoscape.view.model.events.AddedRowViewsEvent;

public class TableModelListener implements ColumnCreatedListener, ColumnDeletedListener, RowsCreatedListener, RowsDeletedListener {

	private final CyTableViewImpl tableView;
	private final CyEventHelper eventHelper;
	
	public TableModelListener(CyTableViewImpl tableView, CyServiceRegistrar registrar) {
		this.tableView = tableView;
		this.eventHelper = registrar.getService(CyEventHelper.class);
	}

	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;
		
		CyTable table = e.getSource();
		CyColumn column = table.getColumn(e.getColumnName());
		if(column != null) {
			View<CyColumn> view = tableView.addColumn(column);
			if(view != null) {
				eventHelper.fireEvent(new AddedColumnViewEvent(tableView, view));
			}
		}
	}
	

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;

		CyTable table = e.getSource();
		CyColumn column = table.getColumn(e.getColumnName());

		if(column != null) {
			View<CyColumn> view = tableView.getColumnView(column);
			if(view != null) {
				eventHelper.fireEvent(new AboutToRemoveColumnViewEvent(tableView, view));
				tableView.removeColumn(column);
			}
		}
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;
		
		CyTable table = e.getSource();
		
		for(var key : e.getPayloadCollection()) {
			CyRow row = table.getRow(key);
			if(row != null) {
				View<CyRow> rowView = tableView.addRow(row);
				eventHelper.addEventPayload(tableView, rowView, AddedRowViewsEvent.class);
			}
		}
		
	}
	

	@Override
	public void handleEvent(RowsDeletedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;
		
		for(var primaryKey : e.getKeys()) {
			View<CyRow> rowView = tableView.removeRow(primaryKey);
			if(rowView != null) {
				eventHelper.addEventPayload(tableView, rowView, AboutToRemoveRowViewsEvent.class);
			}
		}
	}


}
