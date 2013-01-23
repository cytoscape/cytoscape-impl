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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

public class DeleteSelectedNodesAndEdgesTask extends AbstractTask {
	private final UndoSupport undoSupport;
	private final CyNetworkViewManager networkViewManager;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager visualMappingManager;
	private final CyNetwork network;

	public DeleteSelectedNodesAndEdgesTask(final CyNetwork network,
	                                       final UndoSupport undoSupport, 
	                                       final CyNetworkViewManager networkViewManager,
	                                       final VisualMappingManager visualMappingManager, 
	                                       final CyEventHelper eventHelper) {
		this.network              = network;
		this.undoSupport          = undoSupport;
		this.networkViewManager   = networkViewManager;
		this.visualMappingManager = visualMappingManager;
		this.eventHelper          = eventHelper;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null)
			return;

		taskMonitor.setProgress(0.0);
		
		// Delete from the base network so that our changes can be undone:
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, "selected", true);
		taskMonitor.setProgress(0.1);
		final Set<CyEdge> selectedEdges = new HashSet<CyEdge>(CyTableUtil.getEdgesInState(network, "selected", true));
		taskMonitor.setProgress(0.2);
		
		// Make sure we're not loosing any edges for a possible undo!
		for (CyNode selectedNode : selectedNodes)
			selectedEdges.addAll(network.getAdjacentEdgeList(selectedNode, CyEdge.Type.ANY));

		taskMonitor.setProgress(0.3);
		
		undoSupport.postEdit(
			new DeleteEdit((CySubNetwork)network, selectedNodes, selectedEdges,
				       networkViewManager, visualMappingManager, eventHelper));

		// Delete the actual nodes and edges:
		network.removeNodes(selectedNodes);
		taskMonitor.setProgress(0.7);
		network.removeEdges(selectedEdges);
		taskMonitor.setProgress(0.9);
		
		// Update network views
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		
		for (final CyNetworkView netView : views)
			netView.updateView();
		
		taskMonitor.setProgress(1.0);
	}
}
