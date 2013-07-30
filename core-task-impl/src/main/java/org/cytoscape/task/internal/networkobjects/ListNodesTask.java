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

import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ListNodesTask extends AbstractTask implements ObservableTask {
	@Tunable(description="Network to list nodes for", context="nogui")
	public CyNetwork network = null;

	public NodeList nodeList = new NodeList(null);
	@Tunable(description="Node specification to list", context="nogui")
	public NodeList getnodeList() {
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}

	List<CyNode> nodes = null;

	public ListNodesTask() {
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}

		if (nodeList.getValue() == null || nodeList.getValue().size() == 0) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "No nodes found");
			return;
		}

		nodes = nodeList.getValue();
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Found "+nodes.size()+" nodes");
	}

	public Object getResults(Class type) {
		if (type.equals(List.class)) {
			return nodes;
		} else if (type.equals(String.class)){
			String res = "";
			for (CyNode node: nodes) {
				res += node.toString()+" ["+getName(network, node)+"]\n";
			}
			return res.substring(0, res.length()-1);
		}
		return nodes;
	}

	String getName(CyNetwork network, CyNode node) {
		return network.getRow(node).get(CyNetwork.NAME, String.class);
	}
}
