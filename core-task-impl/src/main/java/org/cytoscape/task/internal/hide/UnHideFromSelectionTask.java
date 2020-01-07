package org.cytoscape.task.internal.hide;

import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

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

public class UnHideFromSelectionTask extends AbstractNetworkViewTask {

	private final String description;
	private final boolean unhideNodes;
	private final boolean unhideEdges;
	private final boolean justSelected;
	private final CyServiceRegistrar serviceRegistrar;

	public UnHideFromSelectionTask(
			final String description,
			final boolean unhideNodes,
			final boolean unhideEdges,
			final boolean justSelected, 
			final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(view);
		this.description = description;
		this.unhideNodes = unhideNodes;
		this.unhideEdges = unhideEdges;
		this.justSelected = justSelected;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Show All Nodes and Edges");
		tm.setProgress(0.0);
		
		final CyNetwork network = view.getModel();
		List<CyNode> nodes = null;
		List<CyEdge> edges = null;

		if (unhideNodes) {
			if(justSelected)
				nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
			else
				nodes = network.getNodeList();
		}
		
		tm.setProgress(0.1);

		if (unhideEdges) {
			if(justSelected)
				edges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
			else
				edges = network.getEdgeList();
		}

		UnHideTask unHideTask = new UnHideTask(description, nodes, edges, view, serviceRegistrar);
		insertTasksAfterCurrentTask(unHideTask);
	}

}
