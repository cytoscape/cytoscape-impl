package org.cytoscape.task.internal.network;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;

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

public class NewNetworkSelectedNodesOnlyTask extends AbstractNetworkFromSelectionTask {
	
	private Set<CyNode> nodes;
	private Set<CyEdge> edges;

	public NewNetworkSelectedNodesOnlyTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		super(net, serviceRegistrar);
	}

	/**
	 * Returns the selected nodes.
	 */
	@Override
	Set<CyNode> getNodes(final CyNetwork net) {
		if (nodes == null)
			nodes = new HashSet<>(CyTableUtil.getNodesInState(parentNetwork, CyNetwork.SELECTED, true));

		return nodes;
	}

	/**
	 * Returns all edges that connect the selected nodes.
	 */
	@Override
	Set<CyEdge> getEdges(final CyNetwork net) {
		if (edges == null) {
			edges = new HashSet<>();
			var nodes = getNodes(net);

			for (var n1 : nodes) {
				for (var n2 : nodes)
					edges.addAll(net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
			}
		}

		return edges;
	}
}
