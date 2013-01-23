package org.cytoscape.internal.select;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsListener;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsEvent;
import org.cytoscape.view.model.events.AboutToRemoveNodeViewsListener;
import org.cytoscape.view.model.events.AddedEdgeViewsEvent;
import org.cytoscape.view.model.events.AddedEdgeViewsListener;
import org.cytoscape.view.model.events.AddedNodeViewsEvent;
import org.cytoscape.view.model.events.AddedNodeViewsListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;

public class RowViewTracker implements NetworkViewAddedListener, 
	AddedNodeViewsListener, AddedEdgeViewsListener, 
	AboutToRemoveNodeViewsListener, AboutToRemoveEdgeViewsListener,
	NetworkViewAboutToBeDestroyedListener {

	private final Map<CyNetworkView, Map<CyRow,View<?>>> rowViewMapsByNetworkView;
	private final Map<CyTable, Set<CyNetworkView>> networkViewsByTable;

	public RowViewTracker() {
		rowViewMapsByNetworkView = new IdentityHashMap<CyNetworkView, Map<CyRow,View<?>>>();
		networkViewsByTable = new IdentityHashMap<CyTable, Set<CyNetworkView>>();
	}

	public void handleEvent(final NetworkViewAddedEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetworkView view = e.getNetworkView();
				final CyNetwork net = view.getModel(); 
				Map<CyRow, View<?>> rowViewMap = getRowViewMapInternal(view);

				for ( View<CyNode> nv : view.getNodeViews() )
					rowViewMap.put( net.getRow(nv.getModel()), nv);
		
				for ( View<CyEdge> ev : view.getEdgeViews() ) 
					rowViewMap.put( net.getRow(ev.getModel()), ev);
				
				addTables(view);
			}
		});
	}
	
	protected void addTables(CyNetworkView view) {
		CyNetwork network = view.getModel();
		addTable(view, network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS));
		addTable(view, network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS));
		addTable(view, network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS));
	}

	private void addTable(CyNetworkView view, CyTable table) {
		if (table == null) {
			return;
		}
		Set<CyNetworkView> views = networkViewsByTable.get(table);
		if (views == null) {
			views = new HashSet<CyNetworkView>();
			networkViewsByTable.put(table, views);
		}
		views.add(view);
	}

	protected void removeTables(CyNetworkView view) {
		CyNetwork network = view.getModel();
		removeTable(view, network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS));
		removeTable(view, network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS));
		removeTable(view, network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS));
	}
	
	private void removeTable(CyNetworkView view, CyTable table) {
		Set<CyNetworkView> views = networkViewsByTable.get(table);
		if (views == null) {
			return;
		}
		
		views.remove(view);
		if (views.size() == 0) {
			networkViewsByTable.remove(table);
		}
	}

	public void handleEvent(final AddedNodeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetworkView view = e.getSource();
				final CyNetwork net = view.getModel(); 
				Map<CyRow, View<?>> rowViewMap = getRowViewMapInternal(view);
				
				for ( View<CyNode> v : e.getNodeViews()) 
					if (net.containsNode(v.getModel())) 
						rowViewMap.put( net.getRow(v.getModel()), v );
			}
		});
	}
	
	public void handleEvent(final AddedEdgeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final CyNetworkView view = e.getSource();
				final CyNetwork net = view.getModel(); 
				Map<CyRow, View<?>> rowViewMap = getRowViewMapInternal(view);

				for ( View<CyEdge> v : e.getEdgeViews()) 
					if (net.containsEdge(v.getModel())) 
						rowViewMap.put( net.getRow(v.getModel()), v );
			}
		});
	}
	
	public void handleEvent(final AboutToRemoveNodeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Map<CyRow, View<?>> rowViewMap = rowViewMapsByNetworkView.get(e.getSource());
				if (rowViewMap == null) {
					return;
				}
				
				Collection<View<?>> values = rowViewMap.values();
				for ( View<CyNode> v : e.getPayloadCollection()) {
					values.remove(v);
				}
			}
		});
	}
	
	public void handleEvent(final AboutToRemoveEdgeViewsEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Map<CyRow, View<?>> rowViewMap = rowViewMapsByNetworkView.get(e.getSource());
				if (rowViewMap == null) {
					return;
				}
				
				Collection<View<?>> values = rowViewMap.values();
				for ( View<CyEdge> v : e.getPayloadCollection()) 
					values.remove(v);
			}
		});
	}
	
	@Override
	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CyNetworkView view = e.getNetworkView();
				rowViewMapsByNetworkView.remove(view);
				removeTables(view);
			}
		});
	}
	
	private Map<CyRow, View<?>> getRowViewMapInternal(CyNetworkView view) {
		Map<CyRow, View<?>> rowViewMap = rowViewMapsByNetworkView.get(view);
		if (rowViewMap == null) {
			rowViewMap = new IdentityHashMap<CyRow, View<?>>();
			rowViewMapsByNetworkView.put(view, rowViewMap);
		}
		return rowViewMap;
	}

	public Map<CyRow,View<?>> getRowViewMap(CyNetworkView networkView) {
		Map<CyRow, View<?>> map = rowViewMapsByNetworkView.get(networkView);
		if (map == null) {
			map = Collections.emptyMap();
		}
		return Collections.unmodifiableMap(map);  
	}

	public Collection<CyNetworkView> getAffectedNetworkViews(CyTable cyTable) {
		Set<CyNetworkView> views = networkViewsByTable.get(cyTable);
		if (views == null) {
			return Collections.emptySet();
		}
		return views;
	}
}
