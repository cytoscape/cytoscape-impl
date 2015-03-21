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
import java.util.Collection;
import java.util.Collections;
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
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * An implementation of CyNetworkManager.
 */
public class CyGroupManagerImpl implements CyGroupManager, AddedEdgesListener {
	private final CyServiceRegistrar cyServiceRegistrar;
	private CyEventHelper cyEventHelper;

	private Set<CyGroup> groupSet;
	private Map<CyRootNetwork, Set<CyGroup>> rootMap;
	private static final String GROUP_LIST_ATTRIBUTE = "__groupList.SUID";
	
	private final Object lock = new Object();
	
	/**
	 * 
	 * @param cyEventHelper
	 */
	public CyGroupManagerImpl(final CyServiceRegistrar cyServiceRegistrar, 
		                        final CyEventHelper cyEventHelper) {
		this.groupSet = new HashSet<CyGroup>();
		this.rootMap = new HashMap<CyRootNetwork, Set<CyGroup>>();
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.cyEventHelper = cyEventHelper;
	}

	@Override
	public Set<CyGroup> getGroupSet(CyNetwork network) {
		synchronized (lock) {
			Set<CyGroup> groupNetSet = new HashSet<CyGroup>();
			for (CyGroup group: groupSet) {
				if (group.isInNetwork(network))
					groupNetSet.add(group);
			}
			return groupNetSet;
		}
	}

	@Override
	public void addGroup(final CyGroup group) {
		synchronized (lock) {
			if (groupSet.contains(group)) {
				return;
			} else {
				groupSet.add(group);
				addGroupToRootMap(group);
				// updateGroupAttribute(group);
			}
		}
		cyEventHelper.fireEvent(new GroupAddedEvent(CyGroupManagerImpl.this, group));
	}

	@Override
	public void addGroups(final List<CyGroup> groups) {
		synchronized (lock) {
			for (CyGroup group: groups) {
				if (!groupSet.contains(group)) {
					groupSet.add(group);
					addGroupToRootMap(group);
					// updateGroupAttribute(group);
				}
			}
		}
		// Fire GroupsAddedEvent?
		// cyEventHelper.fireEvent(new GroupAddedEvent(CyGroupManagerImpl.this, group));
	}

	@Override
	public List<CyGroup> getGroupsForNode(CyNode node) {
		synchronized (lock) {
			List<CyGroup> returnList = new ArrayList<CyGroup>();
	
			// This is a little inefficient....
			for (CyGroup group: groupSet) {
				if (group.getGroupNetwork().containsNode(node))
					returnList.add(group);
			}
	
			return returnList;
		}
	}

	@Override
	public List<CyGroup> getGroupsForNode(CyNode node, CyNetwork network) {
		synchronized (lock) {
			List<CyGroup> returnList = new ArrayList<CyGroup>();
			for (CyGroup group: groupSet) {
				if (group.isInNetwork(network) &&
				    group.getGroupNetwork().containsNode(node))
					returnList.add(group);
			}
			return returnList;
		}
	}

	@Override
	public CyGroup getGroup(CyNode node, CyNetwork network) {
		synchronized (lock) {
			for (CyGroup group: groupSet) {
				if (group.isInNetwork(network) && group.getGroupNode().equals(node))
					return group;
			}
			return null;
		}
	}

	@Override
	public boolean isGroup(CyNode node, CyNetwork network) {
		synchronized (lock) {
			for (CyGroup group: groupSet) {
				if (group.isInNetwork(network) && group.getGroupNode().equals(node))
					return true;
			}
	
			return false;
		}
	}

	@Override
	public void destroyGroup(CyGroup group) {
		synchronized (lock) {
			if (!groupSet.contains(group))
				return;
		}

		cyEventHelper.fireEvent(new GroupAboutToBeDestroyedEvent(CyGroupManagerImpl.this, group));
		
		synchronized (lock) {
			if (rootMap.containsKey(group.getRootNetwork()))
				rootMap.get(group.getRootNetwork()).remove(group);
			groupSet.remove(group);
		}
		((CyGroupImpl)group).destroyGroup();
		// updateGroupAttribute(group);
	}

	@Override
	public void reset() {
		synchronized (lock) {
			this.groupSet = new HashSet<CyGroup>();
		}
	}

	public Set<CyGroup> getAllGroups() {
		synchronized (lock) {
			return Collections.unmodifiableSet(groupSet);
		}
	}

	public Set<CyGroup> getGroupSet(CyRootNetwork root) {
		synchronized (lock) {
			return rootMap.get(root);
		}
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

	/**
	 * Method to get a service.  This is only called from within the group-impl
	 * bundle and is not part of the API.
	 */
	public <S> S getService(Class<S> serviceClass) {
		return cyServiceRegistrar.getService(serviceClass);
	}

	/**
	 * Method to get a service.  This is only called from within the group-impl
	 * bundle and is not part of the API.
	 */
	public <S> S getService(Class<S> serviceClass, String search) {
		return cyServiceRegistrar.getService(serviceClass, search);
	}

	public void handleEvent(AddedEdgesEvent addedEdgesEvent) {
		CyNetwork net = addedEdgesEvent.getSource();
		Collection<CyEdge> edges = addedEdgesEvent.getPayloadCollection();

		Set<CyGroup> groups = getGroupSet(net);
		if (groups == null || groups.size() == 0) return;

		List<CyEdge> edgesToAdd = new ArrayList<>();
		for (CyGroup group: groups) {
			CyGroupImpl gImpl = (CyGroupImpl)group;
			for (CyEdge edge: edges) {
				if (!gImpl.isMeta(edge) && gImpl.isConnectingEdge(edge))
					edgesToAdd.add(edge);
			}
			group.addEdges(edgesToAdd);
		}
	}

	private void addGroupToRootMap(CyGroup group) {
		synchronized (lock) {
			if (rootMap.containsKey(group.getRootNetwork()))
				rootMap.get(group.getRootNetwork()).add(group);
			else {
				Set<CyGroup>groupNetSet = new HashSet<CyGroup>();
				groupNetSet.add(group);
				rootMap.put(group.getRootNetwork(),groupNetSet);
			}
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
