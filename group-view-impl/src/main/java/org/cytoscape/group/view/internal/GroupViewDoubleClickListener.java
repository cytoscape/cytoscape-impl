/*
 File: GroupViewCollapseHandler.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.group.view.internal;

import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupSettings;
import org.cytoscape.group.data.CyGroupSettings.DoubleClickAction;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * Handle selection
 */
public class GroupViewDoubleClickListener extends AbstractNodeViewTaskFactory
{
	CyGroupManager cyGroupManager;
	CyGroupSettings cyGroupSettings;

	/**
	 * 
	 * 
	 */
	public GroupViewDoubleClickListener(final CyGroupManager groupManager, final CyGroupSettings groupSettings) {
		this.cyGroupManager = groupManager;
		this.cyGroupSettings = groupSettings;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		DoubleClickAction action = cyGroupSettings.getDoubleClickAction();

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
			tm.setProgress(1.0d);
		}
	}
	
}
