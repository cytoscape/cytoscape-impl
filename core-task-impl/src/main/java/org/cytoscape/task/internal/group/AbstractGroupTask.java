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

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import org.cytoscape.task.internal.utils.DataUtils;

public abstract class AbstractGroupTask extends AbstractTask {
	CyNetwork net;
	CyGroupManager groupMgr;

	protected List<CyGroup> getGroupList(TaskMonitor tm, String groupList) {
		Set<CyGroup> allGroups = groupMgr.getGroupSet(net);
		if (groupList.equalsIgnoreCase("all")) {
			return new ArrayList<CyGroup>(allGroups);
		}
		String[] groups = DataUtils.getCSV(groupList);
		List<CyGroup> returnGroups = new ArrayList<CyGroup>();
		for (String groupName: groups) {
			CyGroup group = getGroup(groupName);
			if (group != null) {
				returnGroups.add(group);
			} else {
				tm.showMessage(TaskMonitor.Level.ERROR, "Unable to find group '"+groupName+"' in network "+net);
				return null;
			}
		}
		return returnGroups;
	}

	protected CyGroup getGroup(String groupName) {
		CyRootNetwork rootNet = ((CySubNetwork)net).getRootNetwork();
		Set<CyGroup> allGroups = groupMgr.getGroupSet(net);
		for (CyGroup group: allGroups) {
			CyRow groupRow = rootNet.getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
			if (groupName.equals(groupRow.get(CyRootNetwork.SHARED_NAME, String.class)))
				return group;
		}
		return null;
	}

	protected String getGroupName(CyGroup group) {
		CyRootNetwork rootNet = ((CySubNetwork)net).getRootNetwork();
		CyRow groupRow = rootNet.getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS);
		return groupRow.get(CyRootNetwork.SHARED_NAME, String.class);
	}

	protected String getGroupDesc(CyGroup group) {
		Long suid = group.getGroupNode().getSUID();
		int nodes = group.getNodeList().size();
		int externalEdges = group.getExternalEdgeList().size();
		int internalEdges = group.getInternalEdgeList().size();
		return "Group: "+getGroupName(group)+" (suid: "+suid+") with "+nodes+
		       " nodes, "+internalEdges+" internal edges, and "+externalEdges+" external edges";
	}

	protected Set<CyGroup>getGroups(CyNetwork net, List<CyNode>nodeList) {

		Set<CyGroup> groupList = new HashSet<CyGroup>();

		// For each node that is in a group, or is a group, add it to our list
		for (CyNode node: nodeList) {
			if (groupMgr.isGroup(node, net))
				groupList.add(groupMgr.getGroup(node, net));
			else if (groupMgr.getGroupsForNode(node, net) != null)
				groupList.addAll(groupMgr.getGroupsForNode(node, net));
		}
		return groupList;
	}
}
