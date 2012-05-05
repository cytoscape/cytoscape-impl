/*
 File: NetworkManager.java

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
package org.cytoscape.group.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToBeDestroyedEvent;
import org.cytoscape.group.events.GroupAddedEvent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of CyNetworkManager.
 */
public class CyGroupManagerImpl implements CyGroupManager {
	private final CyEventHelper cyEventHelper;

	private Set<CyGroup> groupSet;
	/**
	 * 
	 * @param cyEventHelper
	 */
	public CyGroupManagerImpl(final CyEventHelper cyEventHelper) {
		this.groupSet = new HashSet<CyGroup>();
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
			cyEventHelper.fireEvent(new GroupAddedEvent(CyGroupManagerImpl.this, group));
		}
	}

	@Override
	public synchronized void addGroups(final List<CyGroup> groups) {
		for (CyGroup group: groups) {
			if (!groupSet.contains(group)) {
				groupSet.add(group);
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
		((CyGroupImpl)group).destroyGroup();
		groupSet.remove(group);
	}

	@Override
	public synchronized void reset() {
		this.groupSet = new HashSet<CyGroup>();
	}

}
