package org.cytoscape.browser.internal.view;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;
import org.cytoscape.view.vizmap.events.VisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.VisualStyleSetListener;
import org.cytoscape.view.vizmap.events.table.ColumnAssociatedVisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.table.ColumnAssociatedVisualStyleSetListener;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetListener;

public class TableBrowserStyleMediator implements 
	VisualStyleChangedListener, VisualStyleSetListener, ColumnVisualStyleSetListener,  ColumnAssociatedVisualStyleSetListener {

	private final CyServiceRegistrar registrar;
	
	public TableBrowserStyleMediator(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	
	@Override
	public void handleEvent(VisualStyleSetEvent e) {
		// Called when the network visual style is changed for a network view.
		var netStyle = e.getVisualStyle();
		var netView = e.getNetworkView();
		
		updateColumnViews(netView, netStyle, CyNode.class);
		updateColumnViews(netView, netStyle, CyEdge.class);
	}
	
	private void updateColumnViews(CyNetworkView netView, VisualStyle netStyle, Class<? extends CyIdentifiable> tableType) {
		var tvmm = registrar.getService(TableVisualMappingManager.class);
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		
		var colStyles = tvmm.getAssociatedColumnVisualStyles(netStyle, tableType);
				
		var table = getTable(netView, tableType);
		var tableView = tableViewManager.getTableView(table);
		
		if(colStyles != null && tableView != null) {
			for(var colView : tableView.getColumnViews()) {
				// colStyle may be null, that will clear the VPs for the column
				var colStyle = colStyles.get(colView.getModel().getName()); 
				updateColumn(colView, colStyle);
			}
		}
	}
	
	
	@Override
	public void handleEvent(VisualStyleChangedEvent e) {
		VisualStyle style = e.getSource();
		
		Set<CyColumnView> columns = findColumnsWithDirectStyle(style);
		for(var colView : columns) {
			updateColumn(colView, style);
		}
		
		columns = findColumnsWithAssociatedStyle(style);
		for(var colView : columns) {
			updateColumn(colView, style);
		}
	}

	@Override
	public void handleEvent(ColumnVisualStyleSetEvent e) {
		CyColumnView view = (CyColumnView) e.getColumnView();
		VisualStyle style = e.getVisualStyle();
		updateColumn(view, style);
	}
	
	@Override
	public void handleEvent(ColumnAssociatedVisualStyleSetEvent e) {
		var association = e.getAssociation();
		Set<CyColumnView> columns = findColumnsWithAssociatedStyle(association.networkVisualStyle(), association.colName(), association.tableType());
		for(CyColumnView colView : columns) {
			updateColumn(colView, association.columnVisualStyle());
		}
	}
	
	private void updateColumn(View<CyColumn> view, VisualStyle style) {
		if(style == null)
			view.clearVisualProperties();
		else
			style.apply(view);
	}
	
	private Set<CyColumnView> findColumnsWithAssociatedStyle(VisualStyle networkVisualStyle, String colName, Class<?> tableType) {
		var networkViewManager = registrar.getService(CyNetworkViewManager.class);
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		var visualMappingManager = registrar.getService(VisualMappingManager.class);
		
		Set<CyColumnView> views = new HashSet<>();
		
		var allNetworkViews = networkViewManager.getNetworkViewSet();
		for(var networkView : allNetworkViews) {
			var netVs = visualMappingManager.getVisualStyle(networkView);
			if(netVs != null && networkVisualStyle.equals(netVs)) {
				CyTable table = getTable(networkView, tableType);
				if(table != null) {
					var tableView = tableViewManager.getTableView(table);
					if(tableView != null) {
						var colView = tableView.getColumnView(colName);
						if(colView != null) {
							views.add((CyColumnView) colView);
						}
					}
				}
			}
		}
		
		return views;
	}
	
	
	private Set<CyColumnView> findColumnsWithAssociatedStyle(VisualStyle columnVisualStyle) {
		var tableVisualMappingManager = registrar.getService(TableVisualMappingManager.class);
		
		Set<CyColumnView> views = new HashSet<>();
		
		var associations = tableVisualMappingManager.getAssociations(columnVisualStyle);
		for(var a : associations) {
			var colViews = findColumnsWithAssociatedStyle(a.networkVisualStyle(), a.colName(), a.tableType());
			views.addAll(colViews);
		}
		
		return views;
	}
	
	
	private static CyTable getTable(CyNetworkView netView, Class<?> tableType) {
		if(tableType == CyNode.class)
			return netView.getModel().getDefaultNodeTable();
		if(tableType == CyEdge.class)
			return netView.getModel().getDefaultEdgeTable();
		if(tableType == CyNetworkView.class)
			return netView.getModel().getDefaultNetworkTable();
		return null;
	}
	
	
	private Set<CyColumnView> findColumnsWithDirectStyle(VisualStyle columnStyle) {
		var tableManager = registrar.getService(CyTableManager.class);
		var visualMappingManager = registrar.getService(TableVisualMappingManager.class);
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		
		Set<CyColumnView> views = new HashSet<>();
		
		var tables = tableManager.getAllTables(false);
		for(var table : tables) {
			var tableView = tableViewManager.getTableView(table);
			if(tableView != null) {
				for(var colView : tableView.getColumnViews()) {
					var colStyle = visualMappingManager.getVisualStyle(colView);
					if(colStyle != null && colStyle.equals(columnStyle)) {
						views.add((CyColumnView)colView);
					}
				}
			}
		}
		
		return views;
	}

	
}
