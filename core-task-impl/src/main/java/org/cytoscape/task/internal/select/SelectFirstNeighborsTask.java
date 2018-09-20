package org.cytoscape.task.internal.select;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
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

public class SelectFirstNeighborsTask extends AbstractSelectTask {
	
	private final UndoSupport undoSupport;
	private final Type direction;

	public SelectFirstNeighborsTask(
			final UndoSupport undoSupport,
			final CyNetwork net,
			final CyNetworkViewManager networkViewManager,
			final CyEventHelper eventHelper,
			final Type direction
	) {
		super(net, networkViewManager, eventHelper);
		this.undoSupport = undoSupport;
		this.direction = direction;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Select First Neighbors");
		tm.setProgress(0.0);
		
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		
		if (views.size() != 0)
			view = views.iterator().next();

		undoSupport.postEdit(
			new SelectionEdit(eventHelper, "Select First-Neighbors", network, view,
			                  SelectionEdit.SelectionFilter.NODES_ONLY));
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
