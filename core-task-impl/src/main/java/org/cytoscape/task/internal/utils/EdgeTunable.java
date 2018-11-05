package org.cytoscape.task.internal.utils;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class EdgeTunable {
	
	private final CyServiceRegistrar serviceRegistrar;

	@Tunable(description = "Network", context = "nogui", longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;

	public EdgeList edgeList = new EdgeList(null);

	@Tunable(description = "List of edges", context = "nogui", longDescription = StringToModel.CY_EDGE_LIST_LONG_DESCRIPTION, exampleStringValue = StringToModel.CY_EDGE_LIST_EXAMPLE_STRING)
	public EdgeList getedgeList() {
		if (network == null)
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		edgeList.setNetwork(network);
		
		return edgeList;
	}

	public void setedgeList(EdgeList setValue) {
	}

	public EdgeTunable(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public CyNetwork getNetwork() {
		if (network == null)
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

		return network;
	}

	public List<CyEdge> getEdgeList() {
		if (edgeList == null || edgeList.getValue() == null)
			return getNetwork().getEdgeList();

		return edgeList.getValue();
	}
}
