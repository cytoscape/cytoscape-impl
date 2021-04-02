package org.cytoscape.view.model.internal.table;

import java.util.List;

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
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.AboutToRemoveColumnViewEvent;
import org.cytoscape.view.model.events.AboutToRemoveRowViewsEvent;
import org.cytoscape.view.model.events.AddedColumnViewEvent;
import org.cytoscape.view.model.events.AddedRowViewsEvent;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class TableModelListener implements ColumnCreatedListener, ColumnDeletedListener, RowsCreatedListener, RowsDeletedListener {

	private final CyTableViewImpl tableView;
	private final CyServiceRegistrar registrar;
	private final CyEventHelper eventHelper;
	
	public TableModelListener(CyTableViewImpl tableView, CyServiceRegistrar registrar) {
		this.tableView = tableView;
		this.registrar = registrar;
		this.eventHelper = registrar.getService(CyEventHelper.class);
	}

	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;
		
		CyTable table = e.getSource();
		CyColumn column = table.getColumn(e.getColumnName());
		if(column != null) {
			CyColumnViewImpl view = tableView.addColumn(column);
			
			CyTableViewManager tableViewManager = registrar.getService(CyTableViewManager.class);
			
			// Set the gravity so that the column shows up at the right end of the table browser.
			// Don't fire an event for this.
			if(tableViewManager.getTableView(tableView.getModel()) != null && 
					tableView.getVisualLexicon() instanceof BasicTableVisualLexicon) {
				
				List<View<CyColumn>> colViews = tableView.getColumnViews();
				colViews.sort(VisualProperty.comparing(BasicTableVisualLexicon.COLUMN_GRAVITY));
				if(!colViews.isEmpty()) {
					var lastGrav = colViews.get(colViews.size()-1).getVisualProperty(BasicTableVisualLexicon.COLUMN_GRAVITY);
					if(lastGrav != null) {
						view.setLockedValue(BasicTableVisualLexicon.COLUMN_GRAVITY, lastGrav + 1.0);
					}
				}
			}
			
			if(view != null) {
				eventHelper.fireEvent(new AddedColumnViewEvent(tableView, view));
			}
		}
	}
	

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		if(tableView.getModel() != e.getSource())
			return;

		// SUID was added to API recently, we must accept that it could be null for backwards compatibility
		Long suid = e.getSUID();
		if(suid != null) {
			View<CyColumn> view = tableView.getColumnViewByDataSuid(suid);
			if(view != null) {
				eventHelper.fireEvent(new AboutToRemoveColumnViewEvent(tableView, view));
				tableView.removeColumn(suid);
			}
		} else {
			String name = e.getColumnName();
			View<CyColumn> view = tableView.getColumnViewByName(name);
			if(view != null) {
				eventHelper.fireEvent(new AboutToRemoveColumnViewEvent(tableView, view));
				tableView.removeColumn(view.getModel().getSUID());
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
