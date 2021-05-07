package org.cytoscape.task.internal.select;

import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

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

public class DeselectNodesTask extends AbstractSelectTask {

	@Tunable(description = "Network to deselect nodes in", gravity = 1.0, context = "nogui")
	public CyNetwork network;

	public NodeList nodeList = new NodeList(null);

	@Tunable(description = "Nodes to deselect", gravity = 2.0, context = "nogui")
	public NodeList getnodeList() {
		super.network = network;
		nodeList.setNetwork(network);
		
		return nodeList;
	}

	public void setnodeList(NodeList setValue) {
	}

	public DeselectNodesTask(CyServiceRegistrar serviceRegistrar) {
		super(null, serviceRegistrar);
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		tm.showMessage(TaskMonitor.Level.INFO, "Deselecting " + nodeList.getValue().size() + " nodes");
		selectUtils.setSelectedNodes(network, nodeList.getValue(), false);
		
		tm.setProgress(0.6);
		updateView();
		
		tm.setProgress(1.0);
	}
}
