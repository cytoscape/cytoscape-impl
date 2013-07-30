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


import java.util.Collection;

import org.cytoscape.command.util.NodeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;


public class SelectNodesTask extends AbstractSelectTask {
	@Tunable(description="Network to select nodes in",gravity=1.0,context="nogui")
	public CyNetwork network;

	public NodeList nodeList = new NodeList(null);

	@Tunable(description="Nodes to select",gravity=2.0,context="nogui")
	public NodeList getnodeList() {
		super.network = network;
		nodeList.setNetwork(network);
		return nodeList;
	}

	public void setnodeList(NodeList setValue) {}

	public SelectNodesTask(final CyNetworkViewManager networkViewManager,
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
		if(views.size() != 0)
			view = views.iterator().next();
		
		tm.setProgress(0.2);
		tm.showMessage(TaskMonitor.Level.INFO, "Selecting "+nodeList.getValue().size()+" nodes");
		selectUtils.setSelectedNodes(network,nodeList.getValue(), true);
		tm.setProgress(0.6);
		updateView();
		tm.setProgress(1.0);
	}
}
