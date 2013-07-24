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

import org.cytoscape.command.util.EdgeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;


public class DeselectEdgesTask extends AbstractSelectTask {
	@Tunable(description="Network to deselect edges in",gravity=1.0,context="nogui")
	public CyNetwork network;

	public EdgeList edgeList = new EdgeList(null);

	@Tunable(description="Edges to deselect",gravity=2.0,context="nogui")
	public EdgeList getedgeList() {
		super.network = network;
		edgeList.setNetwork(network);
		return edgeList;
	}

	public void setedgeList(EdgeList setValue) {}

	public DeselectEdgesTask(final CyNetworkViewManager networkViewManager,
	                       final CyEventHelper eventHelper)
	{
		super(null, networkViewManager, eventHelper);
	}

	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		tm.setProgress(0.2);
		tm.showMessage(TaskMonitor.Level.INFO, "Deselecting "+edgeList.getValue().size()+" edges");
		selectUtils.setSelectedEdges(network,edgeList.getValue(), true);
		tm.setProgress(0.6);
		updateView();
		tm.setProgress(1.0);
	}
}
