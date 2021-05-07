package org.cytoscape.task.internal.networkobjects;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.CoreImplDocumentationConstants;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class GetEdgeTask extends AbstractGetTask implements ObservableTask {
	
	@Tunable(description="Network to get edge from", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network;

	@Tunable(description="Edge to match", context="nogui", longDescription=CoreImplDocumentationConstants.EDGE_LONG_DESCRIPTION + " If this parameter is set, all other edge matching parameters are ignored.", exampleStringValue="Node 1 (interacts with) Node 2")
		
	public String edge;

	@Tunable(description="Name of source node to match", context="nogui", longDescription="Selects a node by name, or, if the parameter has the prefix ```suid:```, selects a node by SUID. Specifies that the edge matched must have this node as its source. This parameter must be used with the ```targetNode``` parameter to produce results.", exampleStringValue="Node 1")
	public String sourceNode;

	@Tunable(description="Name of target node to match", context="nogui", longDescription="Selects a node by name, or, if the parameter has the prefix ```suid:```, selects a node by SUID. Specifies that the edge matched must have this node as its target. This parameter must be used with the ```sourceNode``` parameter to produce results.", exampleStringValue="Node 2")
	public String targetNode;

	@Tunable(description="Edge type to match", context="nogui", longDescription="Specifies that the edge matched must be of the specified type. This parameter must be used with the ```sourceNode``` and ```targetNode``` parameters to produce results.", exampleStringValue="any")
	public ListSingleSelection type = new ListSingleSelection("any", "directed", "undirected");

	private CyEdge returnedEdge;
	private final CyServiceRegistrar serviceRegistrar;
	
	protected GetEdgeTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(final TaskMonitor tm) {
		if (network == null) {
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if (network == null) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
				return;
			}
		}

		if (edge == null && (sourceNode == null || targetNode == null)) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Edge name, suid or source/target must be specified");
			return;
		}

		if (edge != null) {
			returnedEdge = getEdge(network, edge);
			return;
		}

		// Using source/destination nodes
		CyNode source = getNode(network, sourceNode);
		if (source == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Cannot find node '"+sourceNode+"'");
			return;
		}
		CyNode target = getNode(network, targetNode);
		if (target == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Cannot find node '"+targetNode+"'");
			return;
		}
		CyEdge.Type edgeType = CyEdge.Type.ANY;
		if (type.getSelectedValue().equals("directed"))
			edgeType = CyEdge.Type.DIRECTED;
		else if (type.getSelectedValue().equals("undirected"))
			edgeType = CyEdge.Type.UNDIRECTED;

		List<CyEdge> edges = network.getConnectingEdgeList(source, target, edgeType);
		
		// If we got multiple, choose the first one and warn the user
		if (edges.size() > 1) {
			returnedEdge = edges.get(0);
			tm.showMessage(TaskMonitor.Level.WARN, "Specification yields multiple edges -- only one returned");
		} else if (edges.size() == 1) {
			returnedEdge = edges.get(0);
		} else
			tm.showMessage(TaskMonitor.Level.WARN, "No edge matching specification found");
		return;
	}
	
	@Override
	public Object getResults(Class type) {
		if (type.equals(CyEdge.class)) {
			return returnedEdge;
		} else if (type.equals(String.class)){
			if (returnedEdge == null)
				return "<none>";
			return returnedEdge.toString();
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (returnedEdge == null) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return "{\"edge\":"+cyJSONUtil.toJson(returnedEdge)+"}";
			}};
			return res;
		}
		return returnedEdge;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyEdge.class, String.class, JSONResult.class);
	}
}
