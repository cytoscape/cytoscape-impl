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


import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;

import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;

public class SelectTask extends AbstractSelectTask implements ObservableTask {
	private final CyApplicationManager appMgr;
	private final CyServiceRegistrar registrar;
	private List<CyNode> selectedNodes;
	private List<CyEdge> selectedEdges;

	@ContainsTunables
	public NodeAndEdgeTunable nodesAndEdges;

	// Options
	@Tunable(description="First neighbors options", 
	         longDescription="If this option is anything other than 'none', add nodes to the selection based "+
					                 "on the value of the argument.  If 'incoming', add nodes to the selection that "+
													 "have edges pointing to one of the selected nodes.  If 'output', add nodes to the selection that "+
													 "have edges that point to them from one of the selected nodes.  If 'undirected' "+
													 "add any neighbors that have undirected edges connecting to any of the selected nodes. "+
													 "Finally, if 'any', then add all first neighbors to the selection list.",
					 exampleStringValue="none",
					 context="nogui")
	public ListSingleSelection<String> firstNeighbors = 
		new ListSingleSelection<>("none", "incoming", "outgoing", "undirected", "any");

	@Tunable(description="Invert", 
	         longDescription="If this option is not 'none', then the selected nodes or edges (or both) will be "+
					                 "deselected and all other nodes or edges will be selected",
					 exampleStringValue="none",
	         context="nogui")
	public ListSingleSelection<String> invert = new ListSingleSelection<>("none", "nodes", "edges", "both");

	@Tunable(description="Extend edge selection", 
	         longDescription="If 'true', then select any nodes adjacent to any selected edges.  This happens "+
					                 "before any inversion",
					 exampleStringValue="false",
					 context="nogui")
	public boolean extendEdges = false;

	@Tunable(description="Select adjacent edges",
	         longDescription="If 'true', then select any edges adjacent to any selected nodes.  This happens "+
					                 "before any inversion",
					 exampleStringValue="false",
					 context="nogui")
	public boolean adjacentEdges = false;

	public SelectTask(final CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager,
	                  final CyEventHelper eventHelper, final CyServiceRegistrar registrar)
	{
		super(null, networkViewManager, eventHelper);
		this.appMgr = appMgr;
		this.registrar = registrar;
		nodesAndEdges = new NodeAndEdgeTunable(registrar);
	}

	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);

		network = nodesAndEdges.getNetwork();
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();

		Set<CyNode> nodes = new HashSet<CyNode>();
		Set<CyEdge> edges = new HashSet<CyEdge>();

		List<CyNode> nodeList = nodesAndEdges.getNodeList(false);
		List<CyEdge> edgeList = nodesAndEdges.getEdgeList(false);

		// If we specified nodes or edges, those override any currently
		// selected ones.  Otherwise, prime things with the current selection
		if (nodeList != null && nodeList.size() > 0) {
			nodes.addAll(nodeList);
		} else {
			nodes.addAll(CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true));
		}

		if (edgeList != null && edgeList.size() > 0) {
			edges.addAll(edgeList);
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
			selectedNodes = new ArrayList<CyNode>(newNodes);
		} else {
			selectUtils.setSelectedNodes(network, nodes, true);
			selectedNodes = new ArrayList<CyNode>(nodes);
		}

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
			selectedEdges = new ArrayList<CyEdge>(newEdges);
		} else {
			selectUtils.setSelectedEdges(network, edges, true);
			selectedEdges = new ArrayList<CyEdge>(edges);
		}

		tm.setProgress(0.6);
		tm.showMessage(TaskMonitor.Level.INFO, "Selected "+nodeCount+" nodes and "+edgeCount+" edges.");
		updateView();
		tm.setProgress(1.0);
	}

	public Object getResults(Class type) {
		List<CyIdentifiable> identifiables = new ArrayList();
		if (selectedNodes != null)
			identifiables.addAll(selectedNodes);
		if (selectedEdges != null)
			identifiables.addAll(selectedEdges);
		if (type.equals(List.class)) {
			return identifiables;
		} else if (type.equals(String.class)){
			if (identifiables.size() == 0)
				return "<none>";
			String ret = "";
			if (selectedNodes != null && selectedNodes.size() > 0) {
				ret += "Nodes selected: \n";
				for (CyNode node: selectedNodes) {
					ret += "   "+network.getRow(node).get(CyNetwork.NAME, String.class)+"\n";
				}
			}
			if (selectedEdges != null && selectedEdges.size() > 0) {
				ret += "Edges selected: \n";
				for (CyEdge edge: selectedEdges) {
					ret += "   "+network.getRow(edge).get(CyNetwork.NAME, String.class)+"\n";
				}
			}
			return ret;
		}  else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (identifiables == null || identifiables.size() == 0) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = registrar.getService(CyJSONUtil.class);
				String result = "{\"nodes\":";
				if (selectedNodes == null || selectedNodes.size() == 0)
					result += "[]";
				else
					result += cyJSONUtil.cyIdentifiablesToJson(selectedNodes);

				result += ", \"edges\":";
				if (selectedEdges == null || selectedEdges.size() == 0)
					result += "[]";
				else
					result += cyJSONUtil.cyIdentifiablesToJson(selectedEdges);
				return result+"}";
			}};
			return res;
		}
		return identifiables;
	}
	
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
