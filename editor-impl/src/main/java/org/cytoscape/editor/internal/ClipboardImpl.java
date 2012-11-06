package org.cytoscape.editor.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public class ClipboardImpl {
	private CyNetworkView sourceView;
	private List<View<CyNode>> nodeViews;
	private List<View<CyEdge>> edgeViews;
	private List<CyNode> nodes;
	private List<CyEdge> edges;

	// Row maps
	private Map<CyIdentifiable, CyRow> oldSharedRowMap;
	private Map<CyIdentifiable, CyRow> oldLocalRowMap;
	private Map<CyIdentifiable, CyRow> oldHiddenRowMap;

	private double xCenter, yCenter;

	public ClipboardImpl(CyNetworkView networkView, List<CyNode> nodes, List<CyEdge> edges) {
		this.sourceView = networkView;
		this.nodes = nodes;
		this.edges = edges;

		CyNetwork sourceNetwork = sourceView.getModel();
		nodeViews = new ArrayList<View<CyNode>>();
		edgeViews = new ArrayList<View<CyEdge>>();
		oldSharedRowMap = new WeakHashMap<CyIdentifiable, CyRow>();
		oldLocalRowMap = new WeakHashMap<CyIdentifiable, CyRow>();
		oldHiddenRowMap = new WeakHashMap<CyIdentifiable, CyRow>();

		// We need the root network to get the shared attributes
		CyRootNetwork sourceRootNetwork = ((CySubNetwork)sourceNetwork).getRootNetwork();

		xCenter = 0.0;
		yCenter = 0.0;
		for (CyNode node: nodes) {
			View<CyNode> nodeView = networkView.getNodeView(node);
			nodeViews.add(nodeView);
			xCenter += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			yCenter += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			addRows(node, sourceRootNetwork, sourceNetwork);
		}
		xCenter = xCenter / nodes.size();
		yCenter = yCenter / nodes.size();

		for (CyEdge edge: edges) {
			edgeViews.add(networkView.getEdgeView(edge));
			addRows(edge, sourceRootNetwork, sourceNetwork);
		}
	}

	public List<View<CyNode>> getNodeViews() { return nodeViews; }
	public List<View<CyEdge>> getEdgeViews() { return edgeViews; }
	public List<CyNode> getNodes() { return nodes; }
	public List<CyEdge> getEdges() { return edges; }

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

		// We need to do this in two passes.  In pass 1, we'll add all of the nodes
		// and store their (possibly new) SUID.  In pass 2, we'll reposition the
		// nodes and add the edges.

		// Pass 1: add the nodes 
		final Map<CyNode, CyNode> newNodeMap = new HashMap<CyNode, CyNode>();
		for (View<CyNode> nodeView: nodeViews) {
			CyNode node = nodeView.getModel();
			CyNode newNode = pasteNode(sourceView, targetView, node, rowMap);
			newNodeMap.put(node, newNode);
			pastedObjects.add(newNode);
		}

		// Pass 2: add the edges in
		for (View<CyEdge> edgeView: edgeViews) {
			CyEdge edge = edgeView.getModel();
			CyEdge newEdge = pasteEdge(sourceView, targetView, edge, rowMap, newNodeMap);

			pastedObjects.add(newEdge);
		}

		copyRows(rowMap);
		targetView.updateView();

		// Finally, Pass 3: reposition the nodes and update our view
		double xOffset = xCenter - x;
		double yOffset = yCenter - y;
		for (View<CyNode> nodeView: nodeViews) {
			double nodeX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION) - xOffset;
			double nodeY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION) - yOffset;

			if (!newNodeMap.containsKey(nodeView.getModel())) continue; // Shouldn't happen

			// Now, get the node view
			View<CyNode> newNodeView = targetView.getNodeView(newNodeMap.get(nodeView.getModel()));
			if (newNodeView != null) {
				newNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, nodeX);
				newNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, nodeY);
			}
		}

		return pastedObjects;
	}

	private CyEdge pasteEdge(CyNetworkView sourceView, CyNetworkView targetView, 
	                         CyEdge edge, Map<CyRow, CyRow> rowMap, Map<CyNode, CyNode> newNodeMap) {
		CyNetwork sourceNetwork = sourceView.getModel();
		CyRootNetwork sourceRoot = ((CySubNetwork)sourceNetwork).getRootNetwork();

		CySubNetwork targetNetwork = (CySubNetwork)targetView.getModel();
		CyRootNetwork targetRoot = targetNetwork.getRootNetwork();

		CyEdge newEdge = null;
		CyNode sourceNode = edge.getSource();
		CyNode targetNode = edge.getTarget();

		// Same three cases as pasteNode, but we need to be careful to add missing nodes.  If
		// we paste and edge, but there's no corresponding node, we need to copy the
		// node in.
		//
		// Three cases:
		// 1) We're copying edges to a new network in a different network tree
		// 2) We're copying edges to a new network in the same network tree
		// 3) We're copying edges to a new location in the same network
		if (sourceRoot != targetRoot) {
			// Case 1: different root

			if (!newNodeMap.containsKey(sourceNode)) {
				addRows(sourceNode, sourceRoot, sourceNetwork);
				newNodeMap.put(sourceNode, pasteNode(sourceView, targetView, sourceNode, rowMap));
			}

			if (!newNodeMap.containsKey(targetNode)) {
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
				addRows(sourceNode, sourceRoot, sourceNetwork);
				if (targetNetwork.containsNode(sourceNode)) {
					newNodeMap.put(sourceNode, sourceNode);
				} else {
					newNodeMap.put(sourceNode, pasteNode(sourceView, targetView, sourceNode, rowMap));
				}
			}

			if (!newNodeMap.containsKey(targetNode)) {
				addRows(targetNode, sourceRoot, sourceNetwork);
				if (targetNetwork.containsNode(targetNode)) {
					newNodeMap.put(targetNode, targetNode);
				} else {
					newNodeMap.put(targetNode, pasteNode(sourceView, targetView, targetNode, rowMap));
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
			newNode = targetNetwork.addNode();
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
				boolean isVirtual = false;

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

	private String printVirtualColumnInfo(VirtualColumnInfo info) {
		String s = "{source="+info.getSourceColumn()+", sourceJoinKey="+info.getSourceJoinKey();
		s += ", targetJoinKey="+info.getTargetJoinKey()+", sourceTable="+info.getSourceTable();
		return s;
	}
}
