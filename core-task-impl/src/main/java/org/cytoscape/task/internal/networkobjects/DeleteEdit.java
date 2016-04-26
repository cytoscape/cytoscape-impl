package org.cytoscape.task.internal.networkobjects;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.AbstractCyEdit;



/**
 * An undoable edit that will undo and redo deletion of nodes and edges.
 */ 
final class DeleteEdit extends AbstractCyEdit {
	
	private final List<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final double[] xPos;
	private final double[] yPos;
	private final Map<CyIdentifiable, Map<VisualProperty<?>, Object>> bypassMap;
	private final CySubNetwork net;
	private final CyNetworkViewManager netViewMgr;
	private final VisualMappingManager visualMappingManager;
	private final CyEventHelper eventHelper;
	
	DeleteEdit(final CySubNetwork net, final List<CyNode> nodes, final Set<CyEdge> edges,
		   final CyNetworkViewManager netViewMgr,
		   final VisualMappingManager visualMappingManager,
		   final CyEventHelper eventHelper)
	{
		super("Delete");

		if (net == null)
			throw new NullPointerException("network is null");
		this.net = net;

		if (netViewMgr == null)
			throw new NullPointerException("network manager is null");
		this.netViewMgr = netViewMgr;

		if (nodes == null)
			throw new NullPointerException("nodes is null");
		this.nodes = nodes; 

		if (edges == null)
			throw new NullPointerException("edges is null");
		this.edges = edges; 

		if (visualMappingManager == null)
			throw new NullPointerException("visualMappingManager is null");
		this.visualMappingManager = visualMappingManager;

		if (eventHelper == null)
			throw new NullPointerException("eventHelper is null");
		this.eventHelper = eventHelper;

		// save bypass values and the positions of the nodes
		bypassMap = new HashMap<CyIdentifiable, Map<VisualProperty<?>,Object>>();
		xPos = new double[nodes.size()]; 
		yPos = new double[nodes.size()];
		final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
		CyNetworkView netView = null;
		
		if(!views.isEmpty())
			netView = views.iterator().next();

		if (netView != null) {
			final VisualLexicon lexicon = visualMappingManager.getAllVisualLexicon().iterator().next();
			final Collection<VisualProperty<?>> nodeProps = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
			final Collection<VisualProperty<?>> edgeProps = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
			
			int i = 0;
			
			for (CyNode n : nodes) {
				View<CyNode> nv = netView.getNodeView(n);
				saveLockedValues(nv, nodeProps);
				xPos[i] = nv.getVisualProperty(NODE_X_LOCATION);
				yPos[i] = nv.getVisualProperty(NODE_Y_LOCATION);
				i++;
			}
			
			for (CyEdge e : edges) {
				View<CyEdge> ev = netView.getEdgeView(e);
				saveLockedValues(ev, edgeProps);
			}
		}
	}

	@Override
	public void redo() {
		net.removeEdges(edges);
		net.removeNodes(nodes);

		final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
		CyNetworkView netView = null;
		if(!views.isEmpty())
			netView = views.iterator().next();
		
		// Manually call update presentation
		netView.updateView();
	}

	@Override
	public void undo() {
		for (CyNode n : nodes)
			net.addNode(n);
		for (CyEdge e : edges)
			net.addEdge(e);

		eventHelper.flushPayloadEvents();

		final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
		CyNetworkView netView = null;
		if(!views.isEmpty())
			netView = views.iterator().next();
		
		VisualStyle style = visualMappingManager.getVisualStyle(netView);
		if (netView != null) {
			int i = 0;
			for (final CyNode node : nodes) {
				View<CyNode> nodeView = netView.getNodeView(node);
				if (nodeView == null) continue;
				nodeView.setVisualProperty(NODE_X_LOCATION, xPos[i]);
				nodeView.setVisualProperty(NODE_Y_LOCATION, yPos[i] );
				setLockedValues(nodeView);
				style.apply(net.getRow(node), nodeView);
				i++;
			}
			for (final CyEdge edge: edges) {
				View<CyEdge> edgeView = netView.getEdgeView(edge);
				if (edgeView == null) continue;
				setLockedValues(edgeView);
				style.apply(net.getRow(edge), edgeView);
			}
		}

		netView.updateView();
	}
	
	private void saveLockedValues(final View<? extends CyIdentifiable> view,
			final Collection<VisualProperty<?>> visualProps) {
		for (final VisualProperty<?> vp : visualProps) {
			if (view.isValueLocked(vp)) {
				Map<VisualProperty<?>, Object> vpMap = bypassMap.get(view.getModel());
				
				if (vpMap == null)
					bypassMap.put(view.getModel(), vpMap = new HashMap<VisualProperty<?>, Object>());
				
				vpMap.put(vp, view.getVisualProperty(vp));
			}
		}
	}
	
	private void setLockedValues(final View<? extends CyIdentifiable> view) {
		final Map<VisualProperty<?>, Object> vpMap = bypassMap.get(view.getModel());
		
		if (vpMap != null) {
			for (final Entry<VisualProperty<?>, Object> entry : vpMap.entrySet())
				view.setLockedValue(entry.getKey(), entry.getValue());
		}
	}
}
