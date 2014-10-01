package org.cytoscape.group.view.internal;

/*
 * #%L
 * Cytoscape Group View Impl (group-view-impl)
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

import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.internal.CyGroupSettingsImpl;
import org.cytoscape.group.data.internal.CyGroupSettingsImpl.DoubleClickAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Handle selection
 */
public class GroupViewDoubleClickListener extends AbstractNodeViewTaskFactory
{
	CyGroupManager cyGroupManager;
	CyGroupSettingsImpl cyGroupSettings;
	CyNetworkViewManager viewManager;
	VisualMappingManager styleManager;

	/**
	 * 
	 * 
	 */
	public GroupViewDoubleClickListener(final CyGroupManager groupManager, 
	                                    final CyGroupSettingsImpl groupSettings,
	                                    final CyNetworkViewManager viewManager,
	                                    final VisualMappingManager styleManager) {
		this.cyGroupManager = groupManager;
		this.cyGroupSettings = groupSettings;
		this.viewManager = viewManager;
		this.styleManager = styleManager;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		CyNode node = nodeView.getModel();
		CyNetwork net = networkView.getModel();
		CyGroup group = null;

		// Get the double click action for this group
		if (cyGroupManager.isGroup(node, net))
			group = cyGroupManager.getGroup(node, net);
		else {
			List<CyGroup> groups = cyGroupManager.getGroupsForNode(node);
			for (CyGroup g: groups) {
				// Make sure we're in the right network
				if (cyGroupManager.isGroup(g.getGroupNode(), net)) {
					group = g;
					break;
				}
			}
		}
		DoubleClickAction action = cyGroupSettings.getDoubleClickAction(group);

		if (action == DoubleClickAction.ExpandContract) {
			// Collapse/expand: if we double-click on a collapsed node, expand it.  
			// if we double-click on a node that is a member of a group, collapse
			// that group.
			return new TaskIterator(new CollapseGroupTask(nodeView, networkView));
		} else if (action == DoubleClickAction.Select) {
			// Select/deselect: if we double-click on a node that is a member of a group
			// and any member of the group is not selected, select all members of that group.  
			// If all members of the group are selected, deselect that group
			return new TaskIterator(new SelectGroupTask(nodeView, networkView));
		} else {
			return new TaskIterator(new NullTask());
		}
	}

	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		CyNode node = nodeView.getModel();
		CyNetwork net = networkView.getModel();

		// Do we care about this double-click?
		if (cyGroupManager.isGroup(node, net)) {
			return true;
		} else {
			List<CyGroup> groups = cyGroupManager.getGroupsForNode(node);
			for (CyGroup g: groups) {
				// Make sure we're in the right network
				if (cyGroupManager.isGroup(g.getGroupNode(), net)) {
					return true;
				}
			}
		}
		return false;
	}

	class NullTask extends AbstractTask {
		public void run(TaskMonitor tm) throws Exception {
			return;
		}
	}
	
	class SelectGroupTask extends AbstractNodeViewTask {
		public SelectGroupTask(View<CyNode> nodeView, CyNetworkView networkView) {
			super(nodeView, networkView);
		}
		
		public void run(TaskMonitor tm) throws Exception {
			CyNode node = nodeView.getModel();
			CyNetwork network = netView.getModel();
			CyTable nodeTable = network.getDefaultNodeTable();
			
			tm.setProgress(0.0);
			
			List<CyGroup> groups = cyGroupManager.getGroupsForNode(node);
			if (groups != null && groups.size() > 0) {
				for (CyGroup group: groups) {
					if (allSelected(group, nodeTable)) {
						for (CyNode member: group.getNodeList()) {
							nodeTable.getRow(member.getSUID()).set(CyNetwork.SELECTED, Boolean.FALSE);
						}
					} else {
						for (CyNode member: group.getNodeList()) {
							nodeTable.getRow(member.getSUID()).set(CyNetwork.SELECTED, Boolean.TRUE);
						}
					}
				}
			}
			
			tm.setProgress(1.0d);
		}
		
		private boolean allSelected (CyGroup group, CyTable nodeTable) {
			for (CyNode node: group.getNodeList()) {
				Boolean sel = nodeTable.getRow(node.getSUID()).get(CyNetwork.SELECTED, Boolean.class);
				if (!sel)
					return false;
			}
			return true;
		}
	}
	
	class CollapseGroupTask extends AbstractNodeViewTask {

		public CollapseGroupTask(View<CyNode> nodeView, CyNetworkView networkView) {
			super(nodeView, networkView);
		}
		
		public void run(TaskMonitor tm) throws Exception {
			CyNetwork network = netView.getModel();
			CyNode node = nodeView.getModel();
			tm.setProgress(0.0);
			
			if (cyGroupManager.isGroup(node, network)) {
				CyGroup group = cyGroupManager.getGroup(node, network);
				if (group.isCollapsed(network))
					group.expand(network);
				// Not sure how we can double click on a node that's a group, but not
				// collapsed, so just fall through
			} else {
				// Get the list of groups this node is a member of
				List<CyGroup> groups = cyGroupManager.getGroupsForNode(node);
				if (groups != null && groups.size() > 0) {
					// Collapse the first one
					groups.get(0).collapse(network);
				}
			}
			
			for (CyNetworkView view: viewManager.getNetworkViews(netView.getModel())) {
				VisualStyle style = styleManager.getVisualStyle(view);
				style.apply(view);
			}
			
			tm.setProgress(1.0d);
		}
	}
	
}
