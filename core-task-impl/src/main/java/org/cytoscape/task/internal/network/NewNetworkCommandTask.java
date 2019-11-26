package org.cytoscape.task.internal.network;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.command.StringToModel;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class NewNetworkCommandTask extends AbstractNetworkFromSelectionTask {
	
	private Set<CyNode> nodes;
	private Set<CyEdge> edges;
	
	@Tunable(description = "Name of new network", gravity = 1.0, context = "nogui")
	public String networkName;

	@Tunable(description = "Source network", 
	         longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
	         exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         gravity = 2.0, context = "nogui")
	public CyNetwork getsource() {
		return parentNetwork;
	}

	public void setsource(CyNetwork network) {
		parentNetwork = network;
	}

	public NodeList nodeList = new NodeList(null);

	@Tunable(description = "List of nodes for new network", 
	         longDescription = StringToModel.CY_NODE_LIST_LONG_DESCRIPTION,
	         exampleStringValue = StringToModel.CY_NODE_LIST_EXAMPLE_STRING,
	         gravity = 3.0, context = "nogui")
	public NodeList getnodeList() {
		nodeList.setNetwork(parentNetwork);
		return nodeList;
	}

	public void setnodeList(NodeList setValue) {
	}

	public EdgeList edgeList = new EdgeList(null);

	@Tunable(description = "List of edges for new network", 
	         longDescription = StringToModel.CY_EDGE_LIST_LONG_DESCRIPTION,
	         exampleStringValue = StringToModel.CY_EDGE_LIST_EXAMPLE_STRING,
	         gravity = 4.0, context = "nogui")
	public EdgeList getedgeList() {
		edgeList.setNetwork(parentNetwork);
		return edgeList;
	}

	public void setedgeList(EdgeList setValue) {
	}

	@Tunable(description = "Exclude connecting edges", 
	         longDescription = "Unless this is set to true, edges that connect nodes in the nodeList "+
					                   "are implicitly included",
	         gravity = 5.0, context = "nogui", exampleStringValue="false")
	public boolean excludeEdges;
	
	public NewNetworkCommandTask(CyServiceRegistrar serviceRegistrar) {
		super(null, serviceRegistrar);
	}

	/**
	 * Returns the selected nodes plus all nodes that connect the selected edges
	 */
	@Override
	Set<CyNode> getNodes(CyNetwork net) {
		if (nodes == null) {
			nodes = new HashSet<>(nodeList.getValue());

			if (edgeList != null && edgeList.getValue() != null) {
				var selectedEdges = edgeList.getValue();
			
				for (var e : selectedEdges) {
					nodes.add(e.getSource());
					nodes.add(e.getTarget());
				}
			}
		}
		
		return nodes;
	}
	
	/**
	 * Returns the selected edges.
	 */
	@Override
	Set<CyEdge> getEdges(final CyNetwork net) {
		if (edges == null) {
			if (edgeList != null && edgeList.getValue() != null)
				edges = new HashSet<>(edgeList.getValue());
			else
				edges = new HashSet<>();
		}

		if (!excludeEdges) {
			var nList = nodeList.getValue();
	
			for (var n1 : nList) {
				for (var n2 : nList)
					edges.addAll(net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
			}
		}
		
		return edges;
	}

	/**
 	 * Returns the name of the network if the user gave us one
 	 */
	@Override
	String getNetworkName() {
		if (networkName != null)
			return networkName;
		
		return super.getNetworkName();
	}
}
