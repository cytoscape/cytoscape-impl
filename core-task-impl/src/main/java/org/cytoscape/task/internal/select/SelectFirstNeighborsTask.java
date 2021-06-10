package org.cytoscape.task.internal.select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

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

public class SelectFirstNeighborsTask extends AbstractSelectTask {
	
	private final Type direction;

	public SelectFirstNeighborsTask(CyNetwork net, Type direction, CyServiceRegistrar serviceRegistrar) {
		super(net, serviceRegistrar);
		this.direction = direction;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Select First Neighbors");
		tm.setProgress(0.0);
		
		if (network == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}
		
		var view = getNetworkView(network);

		serviceRegistrar.getService(UndoSupport.class).postEdit(
			new SelectionEdit("Select First-Neighbors", network, view,
			                  SelectionEdit.SelectionFilter.NODES_ONLY, serviceRegistrar));
		tm.setProgress(0.1);
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		final Set<CyNode> nodes = new HashSet<CyNode>();
		tm.setProgress(0.2);
		
		for (CyNode currentNode : selectedNodes)
			nodes.addAll(network.getNeighborList(currentNode, direction));
		
		tm.setProgress(0.4);
		selectUtils.setSelectedNodes(network, nodes, true);
		tm.setProgress(0.8);
		updateView();
		tm.setProgress(1.0);
	}
}
