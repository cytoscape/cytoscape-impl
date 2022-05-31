package org.cytoscape.browser.internal.view;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetListener;

public class TableBrowserStyleMediator implements VisualStyleChangedListener, ColumnVisualStyleSetListener {

	private final CyServiceRegistrar registrar;
	
	
	public TableBrowserStyleMediator(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	

	@Override
	public void handleEvent(ColumnVisualStyleSetEvent e) {
		System.out.println("TableBrowserStyleMediator.handleEvent( ColumnVisualStyleSetEvent )");
		CyColumnView view = (CyColumnView) e.getColumnView();
		VisualStyle style = e.getVisualStyle();
		updateColumn(view, style);
	}

	
	@Override
	public void handleEvent(VisualStyleChangedEvent e) {
		System.out.println("TableBrowserStyleMediator.handleEvent( VisualStyleChangedEvent )");
		VisualStyle style = e.getSource();
		Set<CyColumnView> columns = findColumnsWithStyle(style);
		for(CyColumnView colView : columns) {
			updateColumn(colView, style);
		}
	}

	
	private void updateColumn(CyColumnView view, VisualStyle style) {
		if(style == null)
			view.clearVisualProperties();
		else
			style.apply(view);
	}
	
	
	private Set<CyColumnView> findColumnsWithStyle(VisualStyle style) {
		var tableManager = registrar.getService(CyTableManager.class);
		var visualMappingManager = registrar.getService(TableVisualMappingManager.class);
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		
		Set<CyColumnView> views = new HashSet<>();
		
		Set<CyTable> tables = tableManager.getAllTables(false);
		for(CyTable table : tables) {
			CyTableView tableView = tableViewManager.getTableView(table);
			if(tableView != null) {
				for(var colView : tableView.getColumnViews()) {
					VisualStyle colStyle = visualMappingManager.getVisualStyle(colView);
					if(colStyle != null && colStyle.equals(style)) {
						views.add((CyColumnView)colView);
					}
				}
			}
		}
		
		return views;
	}
	
}
