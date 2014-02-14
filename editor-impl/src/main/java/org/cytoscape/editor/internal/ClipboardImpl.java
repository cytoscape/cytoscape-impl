package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public class ClipboardImpl {
	
	private final CyNetworkView sourceView;
	private final Set<View<CyNode>> nodeViews;
	private final Set<View<CyEdge>> edgeViews;
	private final Set<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final Map<CyNode, Map<VisualProperty<?>, Object>> nodeBypass;
	private final Map<CyEdge, Map<VisualProperty<?>, Object>> edgeBypassMap;

	// Row maps
	private Map<CyIdentifiable, CyRow> oldSharedRowMap;
	private Map<CyIdentifiable, CyRow> oldLocalRowMap;
	private Map<CyIdentifiable, CyRow> oldHiddenRowMap;

	private double xCenter, yCenter;
	
	private final CyEventHelper eventHelper;

	public ClipboardImpl(final CyNetworkView networkView,
						 final Set<CyNode> nodes,
						 final Set<CyEdge> edges,
						 final VisualLexicon lexicon,
			CyEventHelper eventHelper) {
		this.sourceView = networkView;
		this.nodes = nodes;
		this.edges = edges;
		this.eventHelper = eventHelper;

		CyNetwork sourceNetwork = sourceView.getModel();
		nodeViews = new HashSet<View<CyNode>>();
		edgeViews = new HashSet<View<CyEdge>>();
		oldSharedRowMap = new WeakHashMap<CyIdentifiable, CyRow>();
		oldLocalRowMap = new WeakHashMap<CyIdentifiable, CyRow>();
		oldHiddenRowMap = new WeakHashMap<CyIdentifiable, CyRow>();

		// We need the root network to get the shared attributes
		CyRootNetwork sourceRootNetwork = ((CySubNetwork)sourceNetwork).getRootNetwork();

		// save bypass values and the positions of the nodes
		nodeBypass = new HashMap<CyNode, Map<VisualProperty<?>,Object>>();
		edgeBypassMap = new HashMap<CyEdge, Map<VisualProperty<?>,Object>>();
		final Collection<VisualProperty<?>> nodeProps = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
		final Collection<VisualProperty<?>> edgeProps = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		
		xCenter = 0.0;
		yCenter = 0.0;
		
		for (CyNode node: nodes) {
			if (networkView != null) {
				View<CyNode> nodeView = networkView.getNodeView(node);
				if (nodeView == null) continue;
				nodeViews.add(nodeView);
				xCenter += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				yCenter += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				saveLockedValues(nodeView, nodeProps, nodeBypass);
			}
			
			addRows(node, sourceRootNetwork, sourceNetwork);
		}
		
		if (nodes.size() > 0) {
			xCenter = xCenter / nodes.size();
			yCenter = yCenter / nodes.size();
		}

		for (CyEdge edge: edges) {
			if (networkView != null) {
				View<CyEdge> edgeView = networkView.getEdgeView(edge);
				if (edgeView == null) continue;
				edgeViews.add(edgeView);
				saveLockedValues(edgeView, edgeProps, edgeBypassMap);
			}
			
			addRows(edge, sourceRootNetwork, sourceNetwork);
		}
	}

	public Set<CyNode> getNodes() { return nodes; }
	public Set<CyEdge> getEdges() { return edges; }

	public double getCenterX() { return xCenter; }
	public double getCenterY() { return yCenter; }

	public boolean clipboardHasData() {
		if (nodeViews != null && nodeViews.size() > 0)
			return true;
		if (edgeViews != null && edgeViews.size() > 0)
			return true;
		return false;
	}

	public List<CyIdentifiable> paste(CyNetworkView targetView, double x, double y) {
		List<CyIdentifiable> pastedObjects = new ArrayList<CyIdentifiable>();
		final Map<CyRow, CyRow> rowMap = new HashMap<CyRow, CyRow>();

		// We need to do this in 4 passes.
		// In pass 1, we'll add all of the nodes and store their (possibly new) SUID.
		// In pass 2, we'll add the edges.
		// In pass 3, we'll reposition the nodes and paste any locked visual properties node views.
		// In pass 4, we'll paste any locked visual properties to edge views
		// Note that if we add any nodes, we'll only add edges to nodes that exist.  

		// Pass 1: add the nodes 
		final Map<CyNode, CyNode> newNodeMap = new HashMap<CyNode, CyNode>();
		
		for (View<CyNode> nodeView: nodeViews) {
			CyNode node = nodeView.getModel();
			CyNode newNode = pasteNode(sourceView, targetView, node, rowMap);
			newNodeMap.put(node, newNode);
			pastedObjects.add(newNode);
		}

		// Pass 2: add the edges
		final Map<CyEdge, CyEdge> newEdgeMap = new HashMap<CyEdge, CyEdge>();
		
		for (View<CyEdge> edgeView: edgeViews) {
			CyEdge edge = edgeView.getModel();
			CyEdge newEdge = pasteEdge(sourceView, targetView, edge, rowMap, newNodeMap);
			
			if (newEdge != null) {
				newEdgeMap.put(edge, newEdge);
				pastedObjects.add(newEdge);
			}
		}

		copyRows(rowMap);
		
		eventHelper.flushPayloadEvents(); // Make sure node/edge views were created
		targetView.updateView();

		// Pass 3: paste locked visual properties and reposition the new node views
		double xOffset = xCenter - x;
		double yOffset = yCenter - y;
		
		for (View<CyNode> nodeView: nodeViews) {
			double nodeX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) - xOffset;
			double nodeY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) - yOffset;

			if (!newNodeMap.containsKey(nodeView.getModel())) continue; // Shouldn't happen

			// Now, get the new node view
			View<CyNode> newNodeView = targetView.getNodeView(newNodeMap.get(nodeView.getModel()));
			
			if (newNodeView != null) {
				newNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, nodeX);
				newNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, nodeY);
				setLockedValues(newNodeView, nodeView.getModel(), nodeBypass);
			}
		}
		
		// Pass 4: paste locked visual properties to new edge views
		if (!edgeBypassMap.isEmpty()) {
			for (CyEdge edge: edgeBypassMap.keySet()) {
				// Now, get the new edge view
				CyEdge newEdge = newEdgeMap.get(edge);
				View<CyEdge> newEdgeView = newEdge != null ? targetView.getEdgeView(newEdge) : null;
				
				if (newEdgeView != null)
					setLockedValues(newEdgeView, edge, edgeBypassMap);
			}
		}

		return pastedObjects;
	}

	private CyEdge pasteEdge(final CyNetworkView sourceView,
							 final CyNetworkView targetView, 
	                         final CyEdge edge,
	                         final Map<CyRow, CyRow> rowMap,
	                         final Map<CyNode, CyNode> newNodeMap) {
		CyNetwork sourceNetwork = sourceView.getModel();
		CyRootNetwork sourceRoot = ((CySubNetwork)sourceNetwork).getRootNetwork();

		CySubNetwork targetNetwork = (CySubNetwork)targetView.getModel();
		CyRootNetwork targetRoot = targetNetwork.getRootNetwork();
		boolean addedNodes = newNodeMap.size() > 0;

		CyEdge newEdge = null;
		CyNode sourceNode = edge.getSource();
		CyNode targetNode = edge.getTarget();

		// Same three cases as pasteNode, but we need to be careful to add missing nodes.  If
		// we paste an edge, but there's no corresponding node, we need to copy the
		// node in if we're copying a bare edge.
		//
		// Three cases:
		// 1) We're copying edges to a new network in a different network tree
		// 2) We're copying edges to a new network in the same network tree
		// 3) We're copying edges to a new location in the same network
		if (sourceRoot != targetRoot) {
			// Case 1: different root

			if (!newNodeMap.containsKey(sourceNode)) {
				if (addedNodes) return null;
				addRows(sourceNode, sourceRoot, sourceNetwork);
				newNodeMap.put(sourceNode, pasteNode(sourceView, targetView, sourceNode, rowMap));
			}

			if (!newNodeMap.containsKey(targetNode)) {
				if (addedNodes) return null;
				addRows(targetNode, sourceRoot, sourceNetwork);
				newNodeMap.put(targetNode, pasteNode(sourceView, targetView, targetNode, rowMap));
			}

			// Create the edge
			newEdge = targetNetwork.addEdge(newNodeMap.get(sourceNode), 
			                                newNodeMap.get(targetNode), edge.isDirected());

			// Copy the attributes over
			rowMap.put(oldSharedRowMap.get(edge),
			           targetNetwork.getRow(newEdge, CyNetwork.DEFAULT_ATTRS));
			rowMap.put(oldLocalRowMap.get(edge),
			           targetNetwork.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(edge),
			           targetNetwork.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		} else {
			// Case 2: different network, same root and
			// Case 3: same network

			// First, see if we already have the nodes
			if (!newNodeMap.containsKey(sourceNode)) {
				if (targetNetwork.containsNode(sourceNode)) {
					newNodeMap.put(sourceNode, sourceNode);
				} else if (!addedNodes) {
					addRows(sourceNode, sourceRoot, sourceNetwork);
					newNodeMap.put(sourceNode, pasteNode(sourceView, targetView, sourceNode, rowMap));
				} else {
					// If this network doesn't contain this node and we added nodes in our paste,
					// skip this edge
					return null;
				}
			}

			if (!newNodeMap.containsKey(targetNode)) {
				if (targetNetwork.containsNode(targetNode)) {
					newNodeMap.put(targetNode, targetNode);
				} else if (!addedNodes) {
					addRows(targetNode, sourceRoot, sourceNetwork);
					newNodeMap.put(targetNode, pasteNode(sourceView, targetView, targetNode, rowMap));
				} else {
					// If this network doesn't contain this node and we added nodes in our paste,
					// skip this edge
					return null;
				}
			}

			// We want to create another copy of the edge
			// Create the edge
			newEdge = targetNetwork.addEdge(newNodeMap.get(sourceNode), 
			                                newNodeMap.get(targetNode), edge.isDirected());

			// Copy the attributes over
			rowMap.put(oldLocalRowMap.get(edge),
			           targetNetwork.getRow(newEdge, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(edge),
			           targetNetwork.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
		}
		
		return newEdge;
	}

	// TODO: Need to figure out how to copy LOCAL_ATTRS, SHARED_ATTRS, and HIDDEN_ATTRS
	// The latter is easy.  The second two are both part of the DEFAULT_ATTRS, but it's
	// not clear how to create a local attribute specifically....
	private CyNode pasteNode(CyNetworkView sourceView, CyNetworkView targetView, 
	                         CyNode node, Map<CyRow, CyRow> rowMap) {

		CyNetwork sourceNetwork = sourceView.getModel();
		CyRootNetwork sourceRoot = ((CySubNetwork)sourceNetwork).getRootNetwork();

		CySubNetwork targetNetwork = (CySubNetwork)targetView.getModel();
		CyRootNetwork targetRoot = targetNetwork.getRootNetwork();

		CyNode newNode = null;

		// Three cases:
		// 1) We're copying nodes to a new network in a different network tree
		// 2) We're copying nodes to a new network in the same network tree
		// 3) We're copying nodes to a new location in the same network
		if (sourceRoot != targetRoot) {
			// Case 1: Different roots
			newNode = targetNetwork.addNode();
			// Copy the attributes over
			rowMap.put(oldSharedRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.DEFAULT_ATTRS));
			rowMap.put(oldLocalRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		} else if (!targetNetwork.containsNode(node)) {
			// Case 2: different subnetwork, same root
			targetNetwork.addNode(node);
			newNode = node;
			rowMap.put(oldLocalRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.LOCAL_ATTRS));
			rowMap.put(oldHiddenRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
		} else {
			// Case 3: Copying the node to the same network
			newNode = targetNetwork.addNode();
			// Copy in the hidden attributes
			rowMap.put(oldHiddenRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
			// Copy in the local attributes
			rowMap.put(oldLocalRowMap.get(node),
			           targetNetwork.getRow(newNode, CyNetwork.LOCAL_ATTRS));
			targetNetwork.addNode(node);
		}
		
		return newNode;
	}

	private void copyRows(Map<CyRow,CyRow> rowMap) {
		if (rowMap == null || rowMap.size() == 0) return;

		for (CyRow sourceRow: rowMap.keySet()) {
			CyRow targetRow = rowMap.get(sourceRow);
			CyTable destTable = targetRow.getTable();
			CyTable sourceTable = sourceRow.getTable();

			Map<String, Object> oldDataMap = sourceRow.getAllValues();
			for (String colName: oldDataMap.keySet()) {
				CyColumn column = destTable.getColumn(colName);

				if (column == null) {
					CyColumn sourceColumn = sourceTable.getColumn(colName);
					if (sourceColumn.getType() == List.class) {
						destTable.createListColumn(colName, sourceColumn.getListElementType(), 
						                           sourceColumn.isImmutable());
					} else {
						destTable.createColumn(colName, sourceColumn.getType(), 
						                       sourceColumn.isImmutable());
					}
				} else if (column.isPrimaryKey()) {
					continue;
				} else {
					// Column already exists.  We need to check for virtual columns that don't join
					// on SUID (since that's the only thing we're changing).  If they don't, we need to
					// skip them.
					VirtualColumnInfo virtualInfo = column.getVirtualColumnInfo();
					if (virtualInfo.isVirtual() && !virtualInfo.getTargetJoinKey().equals(CyNetwork.SUID))
						continue;
				}

				// We need to be careful of the facade table.  If the sourceTable and
				// the targetTable are the same, and this column is virtual, we don't
				// want to copy it.
				try {
					targetRow.set(colName, oldDataMap.get(colName));
				} catch (IllegalArgumentException e) {
					// Log a warning
				} 
			}
		}
	}

	public void addRows(CyIdentifiable object, CyRootNetwork sourceRootNetwork, CyNetwork sourceNetwork) {
		oldSharedRowMap.put(object, sourceRootNetwork.getRow(object, CyRootNetwork.SHARED_ATTRS));
		oldLocalRowMap.put(object, sourceNetwork.getRow(object, CyNetwork.LOCAL_ATTRS));
		oldHiddenRowMap.put(object, sourceNetwork.getRow(object, CyNetwork.HIDDEN_ATTRS));
	}

	static <T extends CyIdentifiable> void saveLockedValues(final View<T> view,
			final Collection<VisualProperty<?>> visualProps, final Map<T, Map<VisualProperty<?>, Object>> bypassMap) {
		for (final VisualProperty<?> vp : visualProps) {
			if (view.isValueLocked(vp)) {
				Map<VisualProperty<?>, Object> vpMap = bypassMap.get(view.getModel());
				
				if (vpMap == null)
					bypassMap.put(view.getModel(), vpMap = new HashMap<VisualProperty<?>, Object>());
				
				vpMap.put(vp, view.getVisualProperty(vp));
			}
		}
	}
	
	static <T extends CyIdentifiable> void setLockedValues(final View<T> target, final T orginalModel,
			final Map<T, Map<VisualProperty<?>, Object>> bypassMap) {
		final Map<VisualProperty<?>, Object> vpMap = bypassMap.get(orginalModel);
		
		if (vpMap != null) {
			for (final Entry<VisualProperty<?>, Object> entry : vpMap.entrySet())
				target.setLockedValue(entry.getKey(), entry.getValue());
		}
	}
}
