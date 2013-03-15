package org.cytoscape.task.internal.group;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2013 The Cytoscape Consortium
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

import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import org.cytoscape.view.model.CyNetworkView;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;

public class GroupNodesTask extends AbstractTask {
	private CyNetwork net;
	private CyGroupManager mgr;
	private CyGroupFactory factory;
	private UndoSupport undoSupport;

	@Tunable(description="Enter group name: ")
	public String groupName = null;

	public GroupNodesTask(UndoSupport undoSupport, CyNetworkView netView, CyGroupManager mgr, CyGroupFactory factory) {
		if (netView == null)
			throw new NullPointerException("network view is null");
		this.net = netView.getModel();
		this.mgr = mgr;
		this.factory = factory;
		this.undoSupport = undoSupport;
	}

	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);

		// Get all of the selected nodes
		final List<CyNode> selNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);

		// At some point, we'll want to seriously think about only adding 
		// those edges that are also selected, but for now....
		CyGroup group = factory.createGroup(net, selNodes, null, true);
		undoSupport.postEdit(new GroupEdit(net, mgr, factory, group));
		// Now some trickery to actually name the group.  Note that we need to change
		// both the NAME and SHARED_NAME columns
		CyRow groupRow = ((CySubNetwork)net).getRootNetwork().getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
		System.out.println("Setting group name to "+groupName);
		groupRow.set(CyRootNetwork.SHARED_NAME, groupName);
		// mgr.addGroup(group);
		tm.setProgress(1.0d);
	}
}
