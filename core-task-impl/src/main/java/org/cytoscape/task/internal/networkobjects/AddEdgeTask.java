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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class AddEdgeTask extends AbstractTask implements ObservableTask {
	CyEdge newEdge;

	@Tunable(description="Network to add a edge to", context="nogui")
	public CyNetwork network = null;

	@Tunable(description="Name of edge source node", context="nogui")
	public String sourceName = null;

	@Tunable(description="Name of edge target node", context="nogui")
	public String targetName = null;

	@Tunable(description="Is the edge directed?", context="nogui")
	public boolean isDirected = false;

	@Tunable(description="Name of the edge to add", context="nogui")
	public String name = null;

	public AddEdgeTask() {
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified for add command");
			return;
		}	

		if (sourceName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Source node name must be specified for add command");
			return;
		}	

		if (targetName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Target node name must be specified for add command");
			return;
		}	

		// Find the source and target nodes
		CyNode source = null;
		CyNode target = null;
		for (CyNode node: network.getNodeList()) {
			String nodeName = network.getRow(node).get(CyNetwork.NAME, String.class);
			if (sourceName.equals(nodeName))
				source = node;
			else if (targetName.equals(nodeName))
				target = node;

			if (source != null && target != null)
				break;
		}

		if (source == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Can't find source node named '"+sourceName+"'");
			return;
		}

		if (target == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Can't find target node named '"+targetName+"'");
			return;
		}

		newEdge = network.addEdge(source, target, isDirected);

		if (name != null) {
			network.getRow(newEdge).set(CyNetwork.NAME, name);
		}
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Added edge "+newEdge.toString()+" to network");

	}

	public Object getResults(Class type) {
		if (type.equals(CyEdge.class)) {
			return newEdge;
		} else if (type.equals(String.class)){
			if (newEdge == null)
				return "<none>";
			return newEdge.toString();
		}
		return newEdge;
	}
}
