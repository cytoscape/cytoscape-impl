package org.cytoscape.task.internal.select;

import java.util.HashSet;
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

public class SelectAdjacentEdgesTask extends AbstractSelectTask {
	
	public SelectAdjacentEdgesTask(CyNetwork net, CyServiceRegistrar serviceRegistrar) {
		super(net, serviceRegistrar);
	}

	@Override
	public void run(final TaskMonitor tm) {
		tm.setTitle("Select Adjacent Edges");
		tm.setProgress(0.0);
		
		CyNetworkView view = getNetworkView(network);

		serviceRegistrar.getService(UndoSupport.class).postEdit(new SelectionEdit(
				"Select Adjacent Edges", network, view, SelectionEdit.SelectionFilter.EDGES_ONLY, serviceRegistrar));
		
		tm.setStatusMessage("Selecting Edges...");
		tm.setProgress(0.2);
		
		final Set<CyEdge> edgeSet = new HashSet<>();

		// Get the list of selected nodes
		for (CyNode node : CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true)) {
			// Get the list of edges connected to this node
			edgeSet.addAll(network.getAdjacentEdgeList(node, CyEdge.Type.ANY));
		}
		
		tm.setProgress(0.3);
		selectUtils.setSelectedEdges(network, edgeSet, true);
		
		tm.setStatusMessage("Updating View...");
		tm.setProgress(0.9);
		updateView();
		
		tm.setProgress(1.0);
	}
}
