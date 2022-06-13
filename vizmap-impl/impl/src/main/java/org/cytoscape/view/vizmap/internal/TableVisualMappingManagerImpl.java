package org.cytoscape.view.vizmap.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.TableViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.TableViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.vizmap.StyleAssociation;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.table.ColumnAssociatedVisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TableVisualMappingManagerImpl implements TableVisualMappingManager, TableViewAboutToBeDestroyedListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();
	
	// Styles for unassigned tables
	private final Map<View<CyColumn>, VisualStyle> column2VisualStyleMap = new WeakHashMap<>();
	
	// Styles associated with default network tables
	private final Map<VisualStyle,Map<String,VisualStyle>> associatedNodeStyles = new WeakHashMap<>();
	private final Map<VisualStyle,Map<String,VisualStyle>> associatedEdgeStyles = new WeakHashMap<>();

	
	public TableVisualMappingManagerImpl(final VisualStyleFactory factory, final CyServiceRegistrar serviceRegistrar) {
		if (serviceRegistrar == null)
			throw new NullPointerException("'serviceRegistrar' cannot be null");
		this.serviceRegistrar = serviceRegistrar;
	}

	
	@Override
	public void handleEvent(TableViewAboutToBeDestroyedEvent e) {
		CyTableView tableView = e.getTableView();
		for(View<CyColumn> colView : tableView.getColumnViews()) {
			setVisualStyle(colView, null);
		}
	}
	
	
	/**
	 * Returns an associated Visual Style for the View Model.
	 */
	@Override
	public VisualStyle getVisualStyle(View<CyColumn> colView) {
		if (colView == null) {
			logger.warn("Attempting to get the visual style for a null column view.");
			return null;
		}

		synchronized (lock) {
			return column2VisualStyleMap.get(colView);
		}
	}

	
	@Override
	public void setVisualStyle(View<CyColumn> colView, VisualStyle vs) {
		Objects.requireNonNull(colView, "Column view is null.");
		boolean changed = false;

		synchronized (lock) {
			if (vs == null) {
				changed = column2VisualStyleMap.remove(colView) != null;
			} else {
				VisualStyle previousStyle = column2VisualStyleMap.put(colView, vs);
				changed = !vs.equals(previousStyle);
			}
		}
		
		if (changed) {
			var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.fireEvent(new ColumnVisualStyleSetEvent(this, vs, colView));
		}
	}
	
	
	private Map<VisualStyle,Map<String,VisualStyle>> getAssociatedStyleMap(Class<? extends CyIdentifiable> tableType) {
		if(tableType == CyNode.class)
			return associatedNodeStyles;
		else if(tableType == CyEdge.class)
			return associatedEdgeStyles;
		else
			throw new IllegalArgumentException("tableType is not a supported type, got: " + tableType);
	}
	
	
	public void setAssociatedVisualStyle(VisualStyle networkVisualStyle, Class<? extends CyIdentifiable> tableType, String colName, VisualStyle columnVisualStyle) {
		Objects.requireNonNull(networkVisualStyle, "networkVisualStyle is null");
		Objects.requireNonNull(tableType, "tableType is null");
		Objects.requireNonNull(colName, "colName is null");
		
		var styleMap = getAssociatedStyleMap(tableType);
		boolean changed = false;

		synchronized (lock) {
			var networkAssociatedStyles = styleMap.computeIfAbsent(networkVisualStyle, k -> new HashMap<>());
			
			if (columnVisualStyle == null) {
				changed = networkAssociatedStyles.remove(colName) != null;
			} else {
				var previousStyle = networkAssociatedStyles.put(colName, columnVisualStyle);
				changed = !columnVisualStyle.equals(previousStyle);
			}
		}
		
		if (changed) {
			var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.fireEvent(new ColumnAssociatedVisualStyleSetEvent(this, networkVisualStyle, columnVisualStyle, colName, tableType));
		}
	}
	
	
	public Map<String,VisualStyle> getAssociatedColumnVisualStyles(VisualStyle networkVisualStyle, Class<? extends CyIdentifiable> tableType) {
		var styleMap = getAssociatedStyleMap(tableType);
		Map<String, VisualStyle> associatedStyles = styleMap.get(networkVisualStyle);
		if(associatedStyles == null)
			return Collections.emptyMap();
		return Collections.unmodifiableMap(associatedStyles);
	}
	

	// MKTODO Maybe use a reverse map to store this instead of searching through all the values.
	@Override
	public Set<VisualStyle> getAssociatedNetworkVisualStyles(VisualStyle columnVisualStyle) {
		Set<VisualStyle> networkStyles = new HashSet<>();
		
		synchronized (lock) {
			for(var styleMap : List.of(associatedNodeStyles, associatedEdgeStyles)) {
				for(var entry : styleMap.entrySet()) {
					var netStyle = entry.getKey();
					var colStyles = entry.getValue();
					if(colStyles.values().contains(columnVisualStyle)) {
						networkStyles.add(netStyle);
					}
				}
			}
		}
		
		return networkStyles;
	}
	
	@Override
	public Set<StyleAssociation> getAssociations(VisualStyle columnVisualStyle) {
		Objects.requireNonNull(columnVisualStyle);
		return getStyleAssociations(columnVisualStyle);
	}

	
	@Override
	public Set<StyleAssociation> getAllStyleAssociations() {
		return getStyleAssociations(null);
	}
	
	
	private Set<StyleAssociation> getStyleAssociations(VisualStyle columnVisualStyle) {
		Set<StyleAssociation> associations = new HashSet<>();
		
		synchronized (lock) {
			for(var tableType : List.of(CyNode.class, CyEdge.class)) {
				var networkMap = getAssociatedStyleMap(tableType);
				for(var netEntry : networkMap.entrySet()) {
					var netStyle = netEntry.getKey();
					var colStyles = netEntry.getValue();
					for(var colEntry : colStyles.entrySet()) {
						var colName = colEntry.getKey();
						var colStyle = colEntry.getValue();
						if(columnVisualStyle == null || colStyle.equals(columnVisualStyle)) {
							associations.add(new StyleAssociation(netStyle, tableType, colName, colStyle));
						}
					}
				}
			}
		}
		
		return associations;
	}

	@Override
	public Set<VisualStyle> getAllVisualStyles() {
		synchronized (lock) {
			return new HashSet<>(column2VisualStyleMap.values());
		}
	}
	
	@Override
	public Map<View<CyColumn>, VisualStyle> getAllVisualStylesMap() {
		synchronized (lock) {
			return new HashMap<>(column2VisualStyleMap);
		}
	}

	@Override
	public Set<VisualLexicon> getAllVisualLexicon() {
		Set<VisualLexicon> set = new LinkedHashSet<>();
		CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		for(var renderer : appManager.getTableViewRendererSet()) {
			var factory = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
			if(factory != null) {
				var lexicon = factory.getVisualLexicon();
				if(lexicon != null) {
					set.add(lexicon);
				}
			}
		}
		
		return set;
	}

}
