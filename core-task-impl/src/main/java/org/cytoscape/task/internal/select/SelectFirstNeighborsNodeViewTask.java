/*
 File: SelectFirstNeighborsNodeViewTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
import java.util.Set;

import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SelectFirstNeighborsNodeViewTask extends AbstractTask {

	private View<CyNode> nodeView;
	private CyNetworkView netView;
	private final Type direction;

	private final SelectUtils selectUtils;

	public SelectFirstNeighborsNodeViewTask(View<CyNode> nodeView, CyNetworkView netView, final Type direction) {
		this.nodeView = nodeView;
		this.netView = netView;
		this.direction = direction;
		this.selectUtils = new SelectUtils();
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		if (nodeView == null)
			throw new NullPointerException("node view is null");
		if (netView == null)
			throw new NullPointerException("network view is null");

		final Set<CyNode> selNodes = new HashSet<CyNode>();
		final CyNode node = nodeView.getModel();
		final CyNetwork net = netView.getModel();
		tm.setProgress(0.1d);
		selNodes.add(node);
		tm.setProgress(0.4d);
		
		selNodes.addAll(net.getNeighborList(node, direction));
		tm.setProgress(0.6d);
		selectUtils.setSelectedNodes(selNodes, true);
		tm.setProgress(0.8d);
		netView.updateView();
		tm.setProgress(1.0d);
	}
}
