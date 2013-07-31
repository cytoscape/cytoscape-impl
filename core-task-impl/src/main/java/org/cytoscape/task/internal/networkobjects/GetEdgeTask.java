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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class GetEdgeTask extends AbstractGetTask implements ObservableTask {
	@Tunable(description="Network to get edge from", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Edge to get", context="nogui")
	public String edge = null;

	@Tunable(description="Name of source node of edge to get", context="nogui")
	public String sourceNode = null;

	@Tunable(description="Name of target node of edge to get", context="nogui")
	public String targetNode = null;

	@Tunable(description="Edge type", context="nogui")
	public ListSingleSelection type = new ListSingleSelection("any", "directed", "undirected");

	private CyEdge returnedEdge = null;

	public GetEdgeTask() {
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}

		if (edge == null && (sourceNode == null || targetNode == null)) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Edge name, suid or source/target must be specified");
			return;
		}

		if (edge != null) {
			returnedEdge = getEdge(network, edge);
			return;
		}

		// Using source/destination nodes
		CyNode source = getNode(network, sourceNode);
		if (source == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Cannot find node '"+sourceNode+"'");
			return;
		}
		CyNode target = getNode(network, targetNode);
		if (target == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Cannot find node '"+targetNode+"'");
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
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "Specification yields multiple edges -- only one returned");
		} else if (edges.size() == 1) {
			returnedEdge = edges.get(0);
		} else
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "No edge matching specification found");
		return;
	}

	public Object getResults(Class type) {
		if (type.equals(CyEdge.class)) {
			return returnedEdge;
		} else if (type.equals(String.class)){
			if (returnedEdge == null)
				return "<none>";
			return returnedEdge.toString();
		}
		return returnedEdge;
	}
}
