package org.cytoscape.task.internal.select;

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
		selectUtils.setSelectedNodes(net, selNodes, true);
		tm.setProgress(0.8d);
		netView.updateView();
		tm.setProgress(1.0d);
	}
}
