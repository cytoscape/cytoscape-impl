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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class AddTask extends AbstractTask {
	Set<CyEdge> newEdges;
	Set<CyNode> newNodes;

	@Tunable(description="Network to add to", context="nogui")
	public CyNetwork network = null;

	// Nodes
	public NodeList nodeList = new NodeList(null);
	@Tunable(description="List of nodes to add (must be present in collection)", context="nogui")
	public NodeList getnodeList() {
		nodeList.setNetwork(((CySubNetwork)network).getRootNetwork());
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}

	// Edges
	public EdgeList edgeList = new EdgeList(null);
	@Tunable(description="List of edges to add (must be present in collection)", context="nogui")
	public EdgeList getedgeList() {
		edgeList.setNetwork(((CySubNetwork)network).getRootNetwork());
		return edgeList;
	}
	public void setedgeList(EdgeList setValue) {}

	public AddTask() {
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified for add command");
			return;
		}	

		if ((nodeList.getValue() == null|| nodeList.getValue().isEmpty()) && 
        (edgeList.getValue() == null|| edgeList.getValue().isEmpty())) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "Nothing to add");
			return;
		}	

		int nodeCount= nodeList.getValue().size();
		int edgeCount= 0;

		for (CyNode node: nodeList.getValue())
			((CySubNetwork)network).addNode(node);

		// To make this a little more sane, we only add an edge
		// if the source and destination node are already in
		// the target network.  This allows us to add a set of
		// nodes and "all" edges
		for (CyEdge edge: edgeList.getValue()) {
			if (network.containsNode(edge.getSource()) && network.containsNode(edge.getTarget())) {
				((CySubNetwork)network).addEdge(edge);
				edgeCount++;
			}
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Added "+nodeCount+" nodes and "+edgeCount+" edges to network "+network.toString());
	}
}
