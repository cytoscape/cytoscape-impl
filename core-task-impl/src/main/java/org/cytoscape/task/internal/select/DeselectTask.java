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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.ListSingleSelection;


public class DeselectTask extends AbstractSelectTask {
	@Tunable(description="Network to deselect nodes and edges in",context="nogui")
	public CyNetwork network;

	// Nodes
	public NodeList nodeList = new NodeList(null);
	@Tunable(description="Nodes to deselect",context="nogui")
	public NodeList getnodeList() {
		super.network = network;
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}

	// Edges
	public EdgeList edgeList = new EdgeList(null);
	@Tunable(description="Edges to deselect",context="nogui")
	public EdgeList getedgeList() {
		super.network = network;
		edgeList.setNetwork(network);
		return edgeList;
	}
	public void setedgeList(EdgeList setValue) {}

	public DeselectTask(final CyNetworkViewManager networkViewManager,
	                    final CyEventHelper eventHelper)
	{
		super(null, networkViewManager, eventHelper);
	}

	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (network == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}
		tm.setProgress(0.0);
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(!views.isEmpty())
			view = views.iterator().next();

		int edgeCount = 0;
		int nodeCount = 0;

		if (edgeList.getValue() != null && !edgeList.getValue().isEmpty()) {
			selectUtils.setSelectedEdges(network, edgeList.getValue(), false);
			edgeCount = edgeList.getValue().size();
		}

		if (nodeList.getValue() != null && !nodeList.getValue().isEmpty()) {
			selectUtils.setSelectedNodes(network, nodeList.getValue(), false);
			nodeCount = nodeList.getValue().size();
		}

		tm.setProgress(0.6);
		tm.showMessage(TaskMonitor.Level.INFO, "Deselected "+nodeCount+" nodes and "+edgeCount+" edges.");
		updateView();
		tm.setProgress(1.0);
	}
}
