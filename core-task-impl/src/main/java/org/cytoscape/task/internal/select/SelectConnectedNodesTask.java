package org.cytoscape.task.internal.select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

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

public class SelectConnectedNodesTask extends AbstractSelectTask {
	
	public SelectConnectedNodesTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		super(net, serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Select Connected Nodes");
		tm.setProgress(0.0);

		CyNetworkView view = getNetworkView(network);

		serviceRegistrar.getService(UndoSupport.class).postEdit(
				new SelectionEdit("Select Nodes Connected by Selected Edges", network, view,
						SelectionEdit.SelectionFilter.NODES_ONLY, serviceRegistrar));
		
		tm.setStatusMessage("Selecting Nodes...");
		tm.setProgress(0.1);
		
		final List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, "selected", true);
		final Set<CyNode> nodes = new HashSet<>();
		
		tm.setProgress(0.2);
		
		for (CyEdge edge : selectedEdges) {
			nodes.add(edge.getSource());
			nodes.add(edge.getTarget());
		}
		
		tm.setProgress(0.3);
		selectUtils.setSelectedNodes(network, nodes, true);
		
		tm.setStatusMessage("Updating View...");
		tm.setProgress(0.8);
		updateView();
		
		tm.setProgress(1.0);
	}
}
