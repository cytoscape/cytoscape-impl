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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.edit.UnGroupNodesTaskFactory;
import org.cytoscape.task.edit.UnGroupTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class UnGroupNodesTaskFactoryImpl implements NetworkViewTaskFactory, 
                                                    UnGroupTaskFactory, UnGroupNodesTaskFactory, TaskFactory {
	
	private CyApplicationManager appMgr;
	private CyGroupFactory factory;
	private CyGroupManager mgr;
	private UndoSupport undoSupport;

	public UnGroupNodesTaskFactoryImpl(CyApplicationManager appMgr, CyGroupManager mgr, 
	                                   CyGroupFactory factory, UndoSupport undoSupport) {
		this.mgr = mgr;
		this.factory = factory;
		this.appMgr = appMgr;
		this.undoSupport = undoSupport;
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		if (nodeView == null || netView == null) {
			return false;
		}
		
		List<CyNode> nodeList = new ArrayList<>();
		nodeList.add(nodeView.getModel());

		CyNetwork net = netView.getModel();
		if (getGroups(net, nodeList).size() > 0)
			return true;
		return false; 
	}

	public boolean isReady(CyNetworkView netView) {
		if (netView == null) {
			return false;
		}
		
		// Get all of the selected nodes
		CyNetwork net = netView.getModel();
		final List<CyNode> selNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
		if (getGroups(net, selNodes).size() > 0)
			return true;
		return false; 
	}

	public boolean isReady() { return true; }
		

	public TaskIterator createTaskIterator(View<CyNode> nodeView, 
	                                       CyNetworkView netView) {
		List<CyNode> nodeList = new ArrayList<>();
		CyNetwork net = netView.getModel();

		nodeList.add(nodeView.getModel());
		nodeList.addAll(CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true));
		Set<CyGroup> groups = getGroups(net, nodeList);

		return new TaskIterator(new UnGroupNodesTask(undoSupport, net, factory, groups, mgr, netView));
	}

	public TaskIterator createTaskIterator(CyNetworkView netView) {
		CyNetwork net = netView.getModel();
		final List<CyNode> selNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
		Set<CyGroup> groups = getGroups(net, selNodes);
		return new TaskIterator(new UnGroupNodesTask(undoSupport, netView.getModel(), factory, groups, mgr, netView));
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new UnGroupNodesTask(appMgr, mgr));
	}

	private Set<CyGroup>getGroups(CyNetwork net, List<CyNode>nodeList) {

		Set<CyGroup> groupList = new HashSet<>();

		// For each node that is in a group, or is a group, add it to our list
		for (CyNode node: nodeList) {
			if (mgr.isGroup(node, net))
				groupList.add(mgr.getGroup(node, net));
			else if (mgr.getGroupsForNode(node, net) != null)
				groupList.addAll(mgr.getGroupsForNode(node, net));
		}
		return groupList;
	}
}
