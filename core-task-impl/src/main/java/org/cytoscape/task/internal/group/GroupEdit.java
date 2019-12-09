package org.cytoscape.task.internal.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.undo.AbstractCyEdit;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2018 The Cytoscape Consortium
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

public class GroupEdit extends AbstractCyEdit {
	
	private CyGroup group;
	private Set<CyGroup> groupSet;
	private final CyServiceRegistrar serviceRegistrar;

	private Map<CyGroup, List<CyNode>> nodeMap;
	private Map<CyGroup, List<CyEdge>> edgesMap;
	private Map<CyGroup, List<CyNetwork>> networkMap;
	private Map<CyGroup, List<CyNetwork>> collapseMap;

	public GroupEdit(CyGroup group, CyServiceRegistrar serviceRegistrar) {
		super("Group Nodes");
		this.group = group;
		this.groupSet = null;
		this.serviceRegistrar = serviceRegistrar;
		initMaps();
		createShadowGroup(group);
	}

	public GroupEdit(Set<CyGroup> groupSet, CyServiceRegistrar serviceRegistrar) {
		super("Ungroup Nodes");
		this.group = null;
		this.groupSet = groupSet;
		this.serviceRegistrar = serviceRegistrar;
		
		initMaps();
		
		for (CyGroup group: groupSet)
			createShadowGroup(group);
	}

	@Override
	public void undo() {
		if (group != null) {
			// Undo group nodes
			serviceRegistrar.getService(CyGroupManager.class).destroyGroup(group);
		} else if (groupSet != null) {
			Set<CyGroup>newGroups = new HashSet<>();

			// Undo ungroup nodes
			for (CyGroup group: groupSet) {
				newGroups.add(reGroup(group));
			}

			// Now recreate our maps in case we redo
			initMaps();
			groupSet = newGroups;
			for (CyGroup group: groupSet)
				createShadowGroup(group);
		}
	}

	@Override
	public void redo() {
		if (group != null) {
			// This is sort of a pain.  We need to reconfigure the
			// group from scratch...
			this.group = reGroup(group);
			initMaps();
			createShadowGroup(group);
		} else if (groupSet != null) {
			CyGroupManager groupMgr = serviceRegistrar.getService(CyGroupManager.class);
			
			for (CyGroup group: groupSet)
				groupMgr.destroyGroup(group);
		}
	}

	private CyGroup reGroup(CyGroup group) {
		List<CyNetwork> netList = networkMap.get(group);

		// network 0 is always the root network
		CyNetwork firstNet = netList.get(1);
		CyGroupFactory factory = serviceRegistrar.getService(CyGroupFactory.class);
		CyGroup newGroup = factory.createGroup(firstNet, nodeMap.get(group), edgesMap.get(group), true);

		// It should be at least 2 (rootNetwork + initial network)
		if (netList.size() > 2) {
			for (int i = 2; i < netList.size(); i++)
				newGroup.addGroupToNetwork(netList.get(i));
		}

		for (CyNetwork net: collapseMap.get(group))
			newGroup.collapse(net);
		
		return newGroup;
	}

	private void initMaps() {
		nodeMap = new HashMap<>();
		edgesMap = new HashMap<>();
		networkMap = new HashMap<>();
		collapseMap = new HashMap<>();
	}

	private void createShadowGroup(CyGroup group) {
		// Get the nodes, edges, and external edges
		nodeMap.put(group, group.getNodeList());
		List<CyEdge> edgesList = new ArrayList<>(group.getInternalEdgeList());
		edgesList.addAll(group.getExternalEdgeList());
		edgesMap.put(group, edgesList);

		// Make sure the root network is the first element
		List<CyNetwork> networks = new ArrayList<>();
		for(CyNetwork network : group.getNetworkSet()) {
			if(network instanceof CyRootNetwork)
				networks.add(0, network);
			else
				networks.add(network);
		}
		networkMap.put(group, networks);

		// Get the collapse set
		List<CyNetwork> collapseList = new ArrayList<>();
		
		for (CyNetwork net: group.getNetworkSet()) {
			if (group.isCollapsed(net))
				collapseList.add(net);
		}
		
		collapseMap.put(group, collapseList);
	}
}
