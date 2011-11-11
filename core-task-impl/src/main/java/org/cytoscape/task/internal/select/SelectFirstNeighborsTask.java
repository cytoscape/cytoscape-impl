/*
 File: SelectFirstNeighborsTask.java

 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.select;


import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


public class SelectFirstNeighborsTask extends AbstractSelectTask {
	private final UndoSupport undoSupport;

	public SelectFirstNeighborsTask(final UndoSupport undoSupport, final CyNetwork net,
	                                final CyNetworkViewManager networkViewManager,
	                                final CyEventHelper eventHelper)
	{
		super(net, networkViewManager, eventHelper);
		this.undoSupport = undoSupport;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.0);
		final CyNetworkView view = networkViewManager.getNetworkView(network.getSUID());
		undoSupport.getUndoableEditSupport().postEdit(
			new SelectionEdit(eventHelper, "Select First-Neighbour Nodes", network, view,
			                  SelectionEdit.SelectionFilter.NODES_ONLY));
		tm.setProgress(0.1);
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		final Set<CyNode> nodes = new HashSet<CyNode>();
		tm.setProgress(0.2);
		for (CyNode currentNode : selectedNodes)
			nodes.addAll(network.getNeighborList(currentNode, CyEdge.Type.ANY));
		tm.setProgress(0.4);
		selectUtils.setSelectedNodes(nodes, true);
		tm.setProgress(0.8);
		updateView();
		tm.setProgress(1.0);
	}
}
