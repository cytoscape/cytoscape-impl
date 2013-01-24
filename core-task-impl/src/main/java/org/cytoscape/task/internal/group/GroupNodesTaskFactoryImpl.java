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

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.edit.GroupNodesTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class GroupNodesTaskFactoryImpl extends AbstractNetworkViewTaskFactory 
                                       implements NodeViewTaskFactory, GroupNodesTaskFactory {
	private CyGroupManager mgr;
	private CyGroupFactory groupFactory;
	private UndoSupport undoSupport;

	public GroupNodesTaskFactoryImpl(CyGroupManager mgr, CyGroupFactory groupFactory, UndoSupport undoSupport) {
		super();
		this.mgr = mgr;
		this.groupFactory = groupFactory;
		this.undoSupport = undoSupport;
	}

	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(new GroupNodesTask(undoSupport, view, mgr, groupFactory));
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView view) {
		return new TaskIterator(new GroupNodesTask(undoSupport, view, mgr, groupFactory));
	}

	public boolean isReady(CyNetworkView netView) {
		if (netView == null) 
			return false;

		// Get all of the selected nodes
		CyNetwork net = netView.getModel();
		final List<CyNode> selNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
		if (selNodes.size() > 1)
			return true;
		return false; 
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		if (nodeView == null || netView == null) {
			return false;
		}

		// Get all of the selected nodes
		CyNetwork net = netView.getModel();
		final List<CyNode> selNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
		if (selNodes.size() > 1)
			return true;
		return false; 
	}
}
