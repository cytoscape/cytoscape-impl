package org.cytoscape.group.internal.view;

import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager.DoubleClickAction;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.data.CyGroupSettingsImpl;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * Handle selection
 */
public class GroupViewDoubleClickListener extends AbstractNodeViewTaskFactory
{
	final CyGroupManagerImpl cyGroupManager;
	final CyGroupSettingsImpl cyGroupSettings;

	/**
	 * 
	 * 
	 */
	public GroupViewDoubleClickListener(final CyGroupManagerImpl groupManager, 
	                                    final CyGroupSettingsImpl groupSettings) {
		this.cyGroupManager = groupManager;
		this.cyGroupSettings = groupSettings;
	}

	@Override
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

		if (action == DoubleClickAction.EXPANDCONTRACT) {
			// Collapse/expand: if we double-click on a collapsed node, expand it.  
			// if we double-click on a node that is a member of a group, collapse
			// that group.
			return new TaskIterator(new CollapseGroupTask(nodeView, networkView));
		} else if (action == DoubleClickAction.SELECT) {
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
				String name = getName(group);
				if (group.isCollapsed(network)) {
					tm.setTitle("Expanding group \""+name+"\"");
					group.expand(network);
				} else {
					tm.setTitle("Collapsing group \""+name+"\"");
					group.collapse(network);
				}
				// Not sure how we can double click on a node that's a group, but not
				// collapsed, so just fall through
			} else {
				// Get the list of groups this node is a member of
				List<CyGroup> groups = cyGroupManager.getGroupsForNode(node);
				if (groups != null && groups.size() > 0) {
					CyGroup group = groups.get(0);
					String name = getName(group);
					tm.setTitle("Collapsing group \""+name+"\"");
					// Collapse the first one
					group.collapse(network);
				}
			}
			
			tm.setProgress(1.0d);
		}
	}

	String getName(CyGroup group) {
		CyRootNetwork rootNetwork = group.getRootNetwork();
		String name = 
					rootNetwork.getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS).
						get(CyRootNetwork.SHARED_NAME, String.class);
		return name;
	}
	
}
