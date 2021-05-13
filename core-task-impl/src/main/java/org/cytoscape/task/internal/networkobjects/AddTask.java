package org.cytoscape.task.internal.networkobjects;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.cytoscape.command.StringToModel;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;

public class AddTask extends AbstractTask implements ObservableTask {
	final CyServiceRegistrar serviceRegistrar;
	final CyEventHelper eventHelper;

	@ContainsTunables
	public NodeAndEdgeTunable nodesAndEdges;

	List<CyNode> nodeList;
	List<CyEdge> edgeList;
	CyNetwork network;

	public AddTask(final CyServiceRegistrar cyServiceRegistrar) {
		serviceRegistrar = cyServiceRegistrar;
		eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		nodesAndEdges = new NodeAndEdgeTunable(cyServiceRegistrar, true);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		network = nodesAndEdges.getNetwork();
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified for add command");
			return;
		}	

		nodeList = nodesAndEdges.getNodeList(false);
		edgeList = nodesAndEdges.getEdgeList(false);

		if ((nodeList == null||nodeList.size() == 0) && 
        (edgeList == null||edgeList.size() == 0)) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "Nothing to add");
			return;
		}	

		int nodeCount= nodeList.size();
		int edgeCount= 0;

		for (CyNode node: nodeList)
			((CySubNetwork)network).addNode(node);

		// To make this a little more sane, we only add an edge
		// if the source and destination node are already in
		// the target network.  This allows us to add a set of
		// nodes and "all" edges
		for (CyEdge edge: edgeList) {
			if (network.containsNode(edge.getSource()) && network.containsNode(edge.getTarget())) {
				((CySubNetwork)network).addEdge(edge);
				edgeCount++;
			}
		}

		eventHelper.flushPayloadEvents();

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Added "+nodeCount+" nodes and "+edgeCount+" edges to network "+network.toString());
	}

	@Override
	@SuppressWarnings({"rawtypes","unchecked"})
	public Object getResults(Class type) {
		List<CyIdentifiable> identifiables = new ArrayList();
		if (nodeList != null)
			identifiables.addAll(nodeList);
		if (edgeList != null)
			identifiables.addAll(edgeList);
		if (type.equals(List.class)) {
			return identifiables;
		} else if (type.equals(String.class)){
			if (identifiables.size() == 0)
				return "<none>";
			String ret = "";
			if (nodeList != null && nodeList.size() > 0) {
				ret += "Nodes added: \n";
				for (CyNode node: nodeList) {
					ret += "   "+network.getRow(node).get(CyNetwork.NAME, String.class)+"\n";
				}
			}
			if (edgeList != null && edgeList.size() > 0) {
				ret += "Edges added: \n";
				for (CyEdge edge: edgeList) {
					ret += "   "+network.getRow(edge).get(CyNetwork.NAME, String.class)+"\n";
				}
			}
			return ret;
		}  else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (identifiables == null || identifiables.size() == 0) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				String result = "{\"nodes\":";
				if (nodeList == null || nodeList.size() == 0)
					result += "[]";
				else
					result += cyJSONUtil.cyIdentifiablesToJson(nodeList);

				result += ", \"edges\":";
				if (edgeList == null || edgeList.size() == 0)
					result += "[]";
				else
					result += cyJSONUtil.cyIdentifiablesToJson(edgeList);

				result += "}";
				return result;
			}};
			return res;
		}
		return identifiables;
	}
	
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
