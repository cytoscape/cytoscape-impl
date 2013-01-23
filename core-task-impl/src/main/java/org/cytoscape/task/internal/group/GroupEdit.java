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
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.AbstractCyEdit;

public class GroupEdit extends AbstractCyEdit {
	private CyNetwork net;
	private CyGroupManager mgr;
	private CyGroupFactory factory;
	private CyGroup group;
	private Set<CyGroup> groupSet;

	private Map<CyGroup, List<CyNode>> nodeMap;
	private Map<CyGroup, List<CyEdge>> edgesMap;
	private Map<CyGroup, List<CyNetwork>> networkMap;
	private Map<CyGroup, List<CyNetwork>> collapseMap;

	public GroupEdit(CyNetwork net, CyGroupManager mgr, CyGroupFactory factory, CyGroup group) {
		super("Group Nodes");
		this.net = net;
		this.mgr = mgr;
		this.factory = factory;
		this.group = group;
		this.groupSet = null;
		initMaps();
		createShadowGroup(group);
	}

	public GroupEdit(CyNetwork net, CyGroupManager mgr, CyGroupFactory factory, Set <CyGroup> groupSet) {
		super("Ungroup Nodes");
		this.net = net;
		this.mgr = mgr;
		this.factory = factory;
		this.group = null;
		this.groupSet = groupSet;
		initMaps();
		for (CyGroup group: groupSet)
			createShadowGroup(group);
	}

	public void undo() {
		if (group != null) {
			// Undo group nodes
			mgr.destroyGroup(group);
		} else if (groupSet != null) {
			Set<CyGroup>newGroups = new HashSet<CyGroup>();

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

	public void redo() {
		if (group != null) {
			// This is sort of a pain.  We need to reconfigure the
			// group from scratch...
			this.group = reGroup(group);
			initMaps();
			createShadowGroup(group);
		} else if (groupSet != null) {
			for (CyGroup group: groupSet) {
				mgr.destroyGroup(group);
			}
		}
	}

	private CyGroup reGroup(CyGroup group) {
		List<CyNetwork> netList = networkMap.get(group);

		// network 0 is always the root network
		CyNetwork firstNet = netList.get(1);
		CyGroup newGroup = factory.createGroup(firstNet, nodeMap.get(group), edgesMap.get(group), true);

		// It should be at least 2 (rootNetwork + initial network)
		if (netList.size() > 2) {
			for (int i = 2; i < netList.size(); i++) {
				newGroup.addGroupToNetwork(netList.get(i));
			}
		}

		for (CyNetwork net: collapseMap.get(group)) {
			newGroup.collapse(net);
		}
		return newGroup;
	}

	private void initMaps() {
		nodeMap = new HashMap<CyGroup, List<CyNode>>();
		edgesMap = new HashMap<CyGroup, List<CyEdge>>();
		networkMap = new HashMap<CyGroup, List<CyNetwork>>();
		collapseMap = new HashMap<CyGroup, List<CyNetwork>>();
	}

	private void createShadowGroup(CyGroup group) {
		// Get the nodes, edges, and external edges
		nodeMap.put(group, group.getNodeList());
		List<CyEdge> edgesList = new ArrayList<CyEdge>(group.getInternalEdgeList());
		edgesList.addAll(group.getExternalEdgeList());
		edgesMap.put(group, edgesList);

		// Get the networks
		networkMap.put(group, new ArrayList<CyNetwork>(group.getNetworkSet()));

		// Get the collapse set
		List<CyNetwork> collapseList = new ArrayList<CyNetwork>();
		for (CyNetwork net: group.getNetworkSet()) {
			if (group.isCollapsed(net)) {
				collapseList.add(net);
			}
		}
		collapseMap.put(group, collapseList);
	}

}
