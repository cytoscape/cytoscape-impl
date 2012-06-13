/*
  File: DeleteSelectedNodesAndEdgesTask.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.task.internal.networkobjects;

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
