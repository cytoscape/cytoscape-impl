package org.cytoscape.group.internal;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToBeDestroyedEvent;
import org.cytoscape.group.events.GroupAddedEvent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyNetworkManager.
 */
public class CyGroupManagerImpl implements CyGroupManager {
	private final CyEventHelper cyEventHelper;

	private Set<CyGroup> groupSet;
	private Map<CyRootNetwork, Set<CyGroup>> rootMap;
	private static final String GROUP_LIST_ATTRIBUTE = "__groupList.SUID";
	/**
	 * 
	 * @param cyEventHelper
	 */
	public CyGroupManagerImpl(final CyEventHelper cyEventHelper) {
		this.groupSet = new HashSet<CyGroup>();
		this.rootMap = new HashMap<CyRootNetwork, Set<CyGroup>>();
		this.cyEventHelper = cyEventHelper;
	}

	@Override
	public synchronized Set<CyGroup> getGroupSet(CyNetwork network) {
		Set<CyGroup> groupNetSet = new HashSet<CyGroup>();
		for (CyGroup group: groupSet) {
			if (group.isInNetwork(network))
				groupNetSet.add(group);
		}
		return groupNetSet;
	}

	@Override
	public synchronized void addGroup(final CyGroup group) {
		if (!groupSet.contains(group)) {
			groupSet.add(group);
			addGroupToRootMap(group);
			// updateGroupAttribute(group);
			cyEventHelper.fireEvent(new GroupAddedEvent(CyGroupManagerImpl.this, group));
		}
	}

	@Override
	public synchronized void addGroups(final List<CyGroup> groups) {
		for (CyGroup group: groups) {
			if (!groupSet.contains(group)) {
				groupSet.add(group);
				addGroupToRootMap(group);
				// updateGroupAttribute(group);
			}
		}
		// Fire GroupsAddedEvent?
		// cyEventHelper.fireEvent(new GroupAddedEvent(CyGroupManagerImpl.this, group));
	}

	@Override
	public synchronized List<CyGroup> getGroupsForNode(CyNode node) {
		List<CyGroup> returnList = new ArrayList<CyGroup>();

		// This is a little inefficient....
		for (CyGroup group: groupSet) {
			if (group.getGroupNetwork().containsNode(node))
				returnList.add(group);
		}

		return returnList;
	}

	@Override
	public synchronized List<CyGroup> getGroupsForNode(CyNode node, CyNetwork network) {
		List<CyGroup> returnList = new ArrayList<CyGroup>();
		for (CyGroup group: groupSet) {
			if (group.isInNetwork(network) &&
			    group.getGroupNetwork().containsNode(node))
				returnList.add(group);
		}
		return returnList;
	}

	@Override
	public synchronized CyGroup getGroup(CyNode node, CyNetwork network) {
		for (CyGroup group: groupSet) {
			if (group.isInNetwork(network) && group.getGroupNode().equals(node))
				return group;
		}
		return null;
	}

	@Override
	public synchronized boolean isGroup(CyNode node, CyNetwork network) {
		for (CyGroup group: groupSet) {
			if (group.isInNetwork(network) && group.getGroupNode().equals(node))
				return true;
		}

		return false;
	}

	@Override
	public synchronized void destroyGroup(CyGroup group) {
		if (!groupSet.contains(group))
			return;

		cyEventHelper.fireEvent(new GroupAboutToBeDestroyedEvent(CyGroupManagerImpl.this, group));
		if (rootMap.containsKey(group.getRootNetwork()))
			rootMap.get(group.getRootNetwork()).remove(group);
		groupSet.remove(group);
		((CyGroupImpl)group).destroyGroup();
		// updateGroupAttribute(group);
	}

	@Override
	public synchronized void reset() {
		this.groupSet = new HashSet<CyGroup>();
	}

	public Set<CyGroup> getAllGroups() {
		return groupSet;
	}

	public Set<CyGroup> getGroupSet(CyRootNetwork root) {
		if (rootMap.containsKey(root))
			return rootMap.get(root);
		return null;
	}

	/**
 	 * Get the SUIDs for all group nodes in this root network.  This is public so that
 	 * we can call it from our network added listener.
 	 */
	public List<Long> getGroupAttribute(CyRootNetwork rootNet) {
		CyRow rhRow = rootNet.getRow(rootNet, CyNetwork.HIDDEN_ATTRS); // Get the network row
		if (rhRow.getTable().getColumn(GROUP_LIST_ATTRIBUTE) == null) {
			return null;
		}
		return rhRow.getList(GROUP_LIST_ATTRIBUTE, Long.class);
	}

	private void addGroupToRootMap(CyGroup group) {
		if (rootMap.containsKey(group.getRootNetwork()))
			rootMap.get(group.getRootNetwork()).add(group);
		else {
			Set<CyGroup>groupNetSet = new HashSet<CyGroup>();
			groupNetSet.add(group);
			rootMap.put(group.getRootNetwork(),groupNetSet);
		}
	}

	private void updateGroupAttribute(CyGroup group) {
		CyRootNetwork rootNet = group.getRootNetwork();
		CyRow rhRow = rootNet.getRow(rootNet, CyNetwork.HIDDEN_ATTRS); // Get the network row
		if (rhRow.getTable().getColumn(GROUP_LIST_ATTRIBUTE) == null) {
			rhRow.getTable().createListColumn(GROUP_LIST_ATTRIBUTE, Long.class, false);
		}

		List<Long> groupSUIDs = new ArrayList<Long>();
		for (CyGroup g: getGroupSet(rootNet)) {
			groupSUIDs.add(g.getGroupNode().getSUID());
		}
		rhRow.set(GROUP_LIST_ATTRIBUTE, groupSUIDs);
	}
}
