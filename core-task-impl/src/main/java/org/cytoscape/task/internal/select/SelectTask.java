package org.cytoscape.task.internal.select;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;


public class SelectTask extends AbstractSelectTask {
	private final CyApplicationManager appMgr;

	@Tunable(description="Network to select nodes and edges in",context="nogui")
	public CyNetwork network;

	// Nodes
	public NodeList nodeList = new NodeList(null);
	@Tunable(description="Nodes to select",context="nogui")
	public NodeList getnodeList() {
		if (network == null)
			network = appMgr.getCurrentNetwork();
		super.network = network;
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}

	// Edges
	public EdgeList edgeList = new EdgeList(null);
	@Tunable(description="Edges to select",context="nogui")
	public EdgeList getedgeList() {
		if (network == null)
			network = appMgr.getCurrentNetwork();
		super.network = network;
		edgeList.setNetwork(network);
		return edgeList;
	}
	public void setedgeList(EdgeList setValue) {}

	// Options
	@Tunable(description="First neighbors options", context="nogui")
	public ListSingleSelection firstNeighbors = new ListSingleSelection("none", "incoming", "outgoing", "undirected", "any");

	@Tunable(description="Invert", context="nogui")
	public ListSingleSelection invert = new ListSingleSelection("none", "nodes", "edges", "both");

	@Tunable(description="Extend edge selection", context="nogui")
	public boolean extendEdges = false;

	@Tunable(description="Select adjacent edges", context="nogui")
	public boolean adjacentEdges = false;

	public SelectTask(final CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager,
	                  final CyEventHelper eventHelper)
	{
		super(null, networkViewManager, eventHelper);
		this.appMgr = appMgr;
	}

	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();

		Set<CyNode> nodes = new HashSet<CyNode>();
		Set<CyEdge> edges = new HashSet<CyEdge>();

		// If we specified nodes or edges, those override any currently
		// selected ones.  Otherwise, prime things with the current selection
		if (nodeList.getValue() != null && nodeList.getValue().size() > 0) {
			nodes.addAll(nodeList.getValue());
		} else {
			nodes.addAll(CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true));
		}

		if (edgeList.getValue() != null && edgeList.getValue().size() > 0) {
			edges.addAll(edgeList.getValue());
		} else {
			edges.addAll(CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true));
		}

		int edgeCount = edges.size();
		int nodeCount = nodes.size();

		if (!firstNeighbors.getSelectedValue().equals("none")) {
			// Handle topology option
			CyEdge.Type type = CyEdge.Type.ANY;

			if (firstNeighbors.getSelectedValue().equals("incoming"))
				type = CyEdge.Type.INCOMING;
			else if (firstNeighbors.getSelectedValue().equals("outgoing"))
				type = CyEdge.Type.OUTGOING;
			else if (firstNeighbors.getSelectedValue().equals("undirected"))
				type = CyEdge.Type.UNDIRECTED;
			else if (firstNeighbors.getSelectedValue().equals("any"))
				type = CyEdge.Type.ANY;
			else {
				tm.showMessage(TaskMonitor.Level.ERROR, "Unknown edge type");
				return;
			}

			// OK, now add them in
			for (CyNode node: new ArrayList<CyNode>(nodes))
				nodes.addAll(network.getNeighborList(node, type));

			// Now, nodes has all of the nodes we specified and the appropriate first neighbors
			tm.showMessage(TaskMonitor.Level.INFO, "Found "+(nodes.size()-nodeCount)+" first neighbors");
			nodeCount = nodes.size();
		}

		if (extendEdges) {
			// Get the edges and extend them.  Note that there is an order here.  All topology
			// happens before inversion
			for (CyEdge edge: edges) {
				nodes.add(edge.getSource());
				nodes.add(edge.getTarget());
			}
			tm.showMessage(TaskMonitor.Level.INFO, "Added "+(nodes.size()-nodeCount)+" nodes from selected edges");
			nodeCount = nodes.size();
		}

		if (adjacentEdges) {
			for (CyNode node: nodes) {
				edges.addAll(network.getAdjacentEdgeList(node, CyEdge.Type.ANY));
			}
			edgeCount = edges.size();
		}

		tm.setProgress(0.2);

		// Finally, handle inversion
		if (invert.getSelectedValue().equals("nodes") || invert.getSelectedValue().equals("both")) {
			Set<CyNode> newNodes = new HashSet<CyNode>();
			for (CyNode node: network.getNodeList()) {
				if (!nodes.contains(node))
					newNodes.add(node);
			}
			selectUtils.setSelectedNodes(network, newNodes, true);
			selectUtils.setSelectedNodes(network, nodes, false);
			tm.showMessage(TaskMonitor.Level.INFO, "Inverting node selection");
			nodeCount = newNodes.size();
		} else
			selectUtils.setSelectedNodes(network, nodes, true);

		if (invert.getSelectedValue().equals("edges") || invert.getSelectedValue().equals("both")) {
			Set<CyEdge> newEdges = new HashSet<CyEdge>();
			for (CyEdge edge: network.getEdgeList()) {
				if (!edges.contains(edge))
					newEdges.add(edge);
			}
			selectUtils.setSelectedEdges(network, newEdges, true);
			selectUtils.setSelectedEdges(network, edges, false);
			tm.showMessage(TaskMonitor.Level.INFO, "Inverting edge selection");
			edgeCount = newEdges.size();
		} else
			selectUtils.setSelectedEdges(network, edges, true);

		tm.setProgress(0.6);
		tm.showMessage(TaskMonitor.Level.INFO, "Selected "+nodeCount+" nodes and "+edgeCount+" edges.");
		updateView();
		tm.setProgress(1.0);
	}
}
