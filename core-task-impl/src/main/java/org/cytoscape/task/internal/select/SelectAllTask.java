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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


public class SelectAllTask extends AbstractSelectTask {
	private final UndoSupport undoSupport;

	public SelectAllTask(final UndoSupport undoSupport, final CyNetwork net,
	                     final CyNetworkViewManager networkViewManager,
	                     final CyEventHelper eventHelper)
	{
		super(net, networkViewManager, eventHelper);
		this.undoSupport = undoSupport;
	}

	public void run(TaskMonitor monitor) {
		monitor.setProgress(0.0);
		final Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		undoSupport.postEdit(
			new SelectionEdit(eventHelper, "Select All Nodes and Edges", network, view,
			                  SelectionEdit.SelectionFilter.NODES_AND_EDGES));
		monitor.setProgress(0.2);
		selectUtils.setSelectedNodes(network, CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, false), true);
		monitor.setProgress(0.5);
		selectUtils.setSelectedEdges(network, CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, false), true);
		monitor.setProgress(0.8);
		updateView();
		monitor.setProgress(1.0);
	}
}
