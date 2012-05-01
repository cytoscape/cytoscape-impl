package org.cytoscape.editor.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public class ClipboardManagerImpl {
	CyNetworkView sourceView;
	List<View<CyNode>> nodeViews;
	List<View<CyEdge>> edgeViews;

	// Row maps
	Map<CyIdentifiable, CyRow> oldDefaultRowMap;
	Map<CyIdentifiable, CyRow> oldHiddenRowMap;

	double xCenter, yCenter;

	public ClipboardManagerImpl() {
	}

	public boolean clipboardHasData() {
		if (nodeViews != null && nodeViews.size() > 0)
			return true;
		return false;
	}

	public void copy(CyNetworkView networkView, List<CyNode> nodes, List<CyEdge> edges) {
		this.sourceView = networkView;
		CyNetwork sourceNetwork = sourceView.getModel();
		nodeViews = new ArrayList<View<CyNode>>();
		edgeViews = new ArrayList<View<CyEdge>>();
		oldDefaultRowMap = new HashMap<CyIdentifiable, CyRow>();
		oldHiddenRowMap = new HashMap<CyIdentifiable, CyRow>();

		xCenter = 0.0;
		yCenter = 0.0;
		for (CyNode node: nodes) {
			View<CyNode> nodeView = networkView.getNodeView(node);
			nodeViews.add(nodeView);
			xCenter += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			yCenter += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			oldDefaultRowMap.put(node, sourceNetwork.getRow(node, CyNetwork.DEFAULT_ATTRS));
			oldHiddenRowMap.put(node, sourceNetwork.getRow(node, CyNetwork.HIDDEN_ATTRS));
		}
		xCenter = xCenter / nodes.size();
		yCenter = yCenter / nodes.size();

		for (CyEdge edge: edges) {
			edgeViews.add(networkView.getEdgeView(edge));
			oldDefaultRowMap.put(edge, sourceNetwork.getRow(edge, CyNetwork.DEFAULT_ATTRS));
			oldHiddenRowMap.put(edge, sourceNetwork.getRow(edge, CyNetwork.HIDDEN_ATTRS));
		}
	}


	public void cut(CyNetworkView networkView, List<CyNode> nodes, List<CyEdge> edges) {
		copy(networkView, nodes, edges);
		networkView.getModel().removeEdges(edges);
		networkView.getModel().removeNodes(nodes);
	}

	public List<CyIdentifiable> paste(CyNetworkView targetView, double x, double y, boolean createColumns) {
		List<CyIdentifiable> pastedObjects = new ArrayList<CyIdentifiable>();
		Map<CyRow, CyRow> rowMap = new HashMap<CyRow, CyRow>();

		CyNetwork sourceNetwork = sourceView.getModel();

		CyRootNetwork sourceRoot = ((CySubNetwork)sourceNetwork).getRootNetwork();

		CySubNetwork targetNetwork = (CySubNetwork)targetView.getModel();
		CyRootNetwork targetRoot = targetNetwork.getRootNetwork();

		// We need to do this in two passes.  In pass 1, we'll add all of the nodes
		// and store their (possibly new) SUID.  In pass 2, we'll reposition the
		// nodes and add the edges.

		// Pass 1: add the nodes 
		Map<CyNode, CyNode> newNodeMap = new HashMap<CyNode, CyNode>();
		for (View<CyNode> nodeView: nodeViews) {
			CyNode node = nodeView.getModel();
			CyNode newNode = null;

			// Three cases:
			// 1) We're copying nodes to a new network in a different network tree
			// 2) We're copying nodes to a new network in the same network tree
			// 3) We're copying nodes to a new location in the same network
			if (sourceRoot != targetRoot || targetNetwork.containsNode(node)) {
				newNode = targetNetwork.addNode();
				// Copy the attributes over
				rowMap.put(oldDefaultRowMap.get(node),
				           targetNetwork.getRow(newNode, CyNetwork.DEFAULT_ATTRS));
				rowMap.put(oldHiddenRowMap.get(node),
				           targetNetwork.getRow(newNode, CyNetwork.HIDDEN_ATTRS));
			} else {
				// Same node: no need to copy the attributes
				targetNetwork.addNode(node);
				newNode = node;
			}

			// Save the original node and it's new node
			newNodeMap.put(node, newNode);
			pastedObjects.add(newNode);
		}

		// Pass 2: add the edges in
		for (View<CyEdge> edgeView: edgeViews) {
			CyEdge edge = edgeView.getModel();
			CyEdge newEdge = null;
			if (sourceRoot != targetRoot || targetNetwork.containsEdge(edge)) {
				CyNode sourceNode = edge.getSource();
				CyNode targetNode = edge.getTarget();
				if (!newNodeMap.containsKey(sourceNode) || !newNodeMap.containsKey(targetNode))
					continue;  // Maybe a dangling edge

				// Create the edge
				newEdge = targetNetwork.addEdge(newNodeMap.get(sourceNode), 
				                                newNodeMap.get(targetNode), edge.isDirected());

				// Copy the attributes over
				rowMap.put(oldDefaultRowMap.get(edge),
				           targetNetwork.getRow(newEdge, CyNetwork.DEFAULT_ATTRS));
				rowMap.put(oldHiddenRowMap.get(edge),
				           targetNetwork.getRow(newEdge, CyNetwork.HIDDEN_ATTRS));
			} else {
				targetNetwork.addEdge(edge);
				newEdge = edge;
			}
			pastedObjects.add(newEdge);
		}

		copyRows(rowMap, createColumns);
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

	private void copyRows(Map<CyRow,CyRow> rowMap, boolean createColumns) {
		if (rowMap == null || rowMap.size() == 0) return;

		for (CyRow sourceRow: rowMap.keySet()) {
			CyRow targetRow = rowMap.get(sourceRow);
			CyTable destTable = targetRow.getTable();
			CyTable sourceTable = sourceRow.getTable();

			Map<String, Object> oldDataMap = sourceRow.getAllValues();
			for (String colName: oldDataMap.keySet()) {
				CyColumn column = destTable.getColumn(colName);
				if (column == null && createColumns) {
					CyColumn sourceColumn = sourceTable.getColumn(colName);
					if (sourceColumn.getType() == List.class) {
						destTable.createListColumn(colName, sourceColumn.getListElementType(), 
						                           sourceColumn.isImmutable());
					} else {
						destTable.createColumn(colName, sourceColumn.getType(), 
						                       sourceColumn.isImmutable());
					}
				} else if (column == null || column.isPrimaryKey()) continue;

				targetRow.set(colName, oldDataMap.get(colName));
			}
		}
	}
}
