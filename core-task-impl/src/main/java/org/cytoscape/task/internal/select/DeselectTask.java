package org.cytoscape.task.internal.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

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

public class DeselectTask extends AbstractSelectTask implements ObservableTask {
	
	private List<CyNode> deselectedNodes;
	private List<CyEdge> deselectedEdges;

	@ContainsTunables
	public NodeAndEdgeTunable nodesAndEdges;

	public DeselectTask(CyServiceRegistrar serviceRegistrar) {
		super(null, serviceRegistrar);
		nodesAndEdges = new NodeAndEdgeTunable(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		CyNetwork network = nodesAndEdges.getNetwork();
		
		if (network == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}
		
		tm.setProgress(0.0);

		deselectedNodes = nodesAndEdges.getNodeList(false);
		deselectedEdges = nodesAndEdges.getEdgeList(false);
		int edgeCount = 0;
		int nodeCount = 0;

		if (deselectedEdges != null && deselectedEdges.size() > 0) {
			selectUtils.setSelectedEdges(network, deselectedEdges, false);
			edgeCount = deselectedEdges.size();
		}

		if (deselectedNodes != null && deselectedNodes.size() > 0) {
			selectUtils.setSelectedNodes(network, deselectedNodes, false);
			nodeCount = deselectedNodes.size();
		}

		tm.setProgress(0.6);
		tm.showMessage(TaskMonitor.Level.INFO, "Deselected " + nodeCount + " nodes and " + edgeCount + " edges.");

		updateView();
		tm.setProgress(1.0);
	}

	public Object getResults(Class type) {
		List<CyIdentifiable> identifiables = new ArrayList<>();
		
		if (deselectedNodes != null && deselectedNodes.size() > 0)
			identifiables.addAll(deselectedNodes);
		if (deselectedEdges != null && deselectedEdges.size() > 0)
			identifiables.addAll(deselectedEdges);
		
		if (type.equals(List.class)) {
			return identifiables;
		} else if (type.equals(String.class)) {
			if (identifiables.size() == 0)
				return "<none>";
			String ret = "";
			if (deselectedNodes != null && deselectedNodes.size() > 0) {
				ret += "Nodes deselected: \n";
				for (CyNode node : deselectedNodes) {
					ret += "   " + network.getRow(node).get(CyNetwork.NAME, String.class) + "\n";
				}
			}
			if (deselectedEdges != null && deselectedEdges.size() > 0) {
				ret += "Edges deselected: \n";
				for (CyEdge edge : deselectedEdges) {
					ret += "   " + network.getRow(edge).get(CyNetwork.NAME, String.class) + "\n";
				}
			}
			
			return ret;
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				
				if (identifiables == null || identifiables.size() == 0) {
					return "{}";
				} else {
					String result = "{\"nodes\":";
					
					if (deselectedNodes == null || deselectedNodes.size() == 0)
						result += "[]";
					else
						result += cyJSONUtil.cyIdentifiablesToJson(deselectedNodes);

					result += ", \"edges\":";
					if (deselectedEdges == null || deselectedEdges.size() == 0)
						result += "[]";
					else
						result += cyJSONUtil.cyIdentifiablesToJson(deselectedEdges);
					
					return result + "}";
				}
			};
			
			return res;
		}
		
		return identifiables;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
