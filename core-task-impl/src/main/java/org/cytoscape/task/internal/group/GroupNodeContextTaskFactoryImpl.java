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
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.edit.CollapseGroupTaskFactory;
import org.cytoscape.task.edit.ExpandGroupTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class GroupNodeContextTaskFactoryImpl extends AbstractNodeViewTaskFactory implements CollapseGroupTaskFactory,
		ExpandGroupTaskFactory {
	
	private CyGroupManager mgr;
	private boolean collapse;

	public GroupNodeContextTaskFactoryImpl(CyGroupManager mgr, boolean collapse) {
		super();
		this.mgr = mgr;
		this.collapse = collapse;
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView netView) {
		if (collapse) {
			// We're ready to collapse if the node view is in a group
			if (getExpandedGroupForNode(nodeView.getModel(), netView) != null)
				return true;
		} else {
			// We're ready to expand if the node view is a group
			CyGroup group = mgr.getGroup(nodeView.getModel(), netView.getModel());
			if (group != null && group.isCollapsed(netView.getModel()))
				return true;
		}
		return false;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		List<CyGroup> groups = new ArrayList<CyGroup>();
		final List<CyNode> selNodes = CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true);

		if (collapse) {
			// Always add the context node, whether it's selected or not
			groups.add(getExpandedGroupForNode(nodeView.getModel(), netView));
			for (CyNode node: selNodes) {
				CyGroup g = getExpandedGroupForNode(node, netView);
				if (g != null) groups.add(g);
			}
		} else  {
			groups.add(mgr.getGroup(nodeView.getModel(), netView.getModel()));
			for (CyNode node: selNodes) {
				CyGroup g = mgr.getGroup(node, netView.getModel());
				if (g != null) groups.add(g);
			}
		}

		return new TaskIterator(new CollapseGroupTask(netView.getModel(), groups, mgr, collapse));
	}

	private CyGroup getExpandedGroupForNode(CyNode node, CyNetworkView netView) {
		List<CyGroup> groups = mgr.getGroupsForNode(node); // Always add the context node
		if (groups == null || groups.size() == 0)
			return null;

		// Return the first uncollapsed group in this network
		for (CyGroup group: groups) {
			if (group.isInNetwork(netView.getModel()) && 
			    !group.isCollapsed(netView.getModel()))
				return group;
		}
		return null;
	}
}
