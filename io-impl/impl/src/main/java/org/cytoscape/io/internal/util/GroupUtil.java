package org.cytoscape.io.internal.util;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

// TODO: this is a temporary solution, until we find a better way of saving/restoring groups
public class GroupUtil {

	// 3.x group attributes
	public static final String EXTERNAL_EDGE_ATTRIBUTE="__externalEdges.SUID";
	public static final String GROUP_COLLAPSED_ATTRIBUTE="__groupCollapsed.SUID";
	public static final String GROUP_NETWORKS_ATTRIBUTE="__groupNetworks.SUID";
	public static final String GROUP_ATTRIBUTE="__isGroup";
	public static final String ISMETA_EDGE_ATTR="__isMetaEdge";
	public static final String X_LOCATION_ATTR="__xLocation";
	public static final String Y_LOCATION_ATTR="__yLocation";
	// 2.x group attributes
	public static final String GROUP_STATE_ATTRIBUTE="__groupState";
	public static final String GROUP_ISLOCAL_ATTRIBUTE="__groupIsLocal";
	public static final String GROUP_NODEX_ATTRIBUTE="__metanodeHintX";
	public static final String GROUP_NODEY_ATTRIBUTE="__metanodeHintY";
	public static final String X_OFFSET_ATTR="__xOffset";
	public static final String Y_OFFSET_ATTR="__yOffset";
	
	private final CyGroupManager groupMgr;
	private final CyGroupFactory groupFactory;

	// Remember groups that we had to add
	private Map<CyNetwork, List<CyNode>> addedNodes = null;
	
	public GroupUtil(final CyGroupManager groupMgr, final CyGroupFactory groupFactory) {
		assert groupMgr != null;
		assert groupFactory != null;
		
		this.groupMgr = groupMgr;
		this.groupFactory = groupFactory;
	}

	public void prepareGroupsForSerialization(final Collection<CyNetwork> networks) {
		if (networks == null)
			return;

		addedNodes = new HashMap<CyNetwork, List<CyNode>>();
		
		for (CyNetwork net: networks) {
			if (!(net instanceof CySubNetwork))
				continue;

			// Get all of our groups
			Set<CyGroup> groupSet = groupMgr.getGroupSet(net);

			// For each group, save the list of external edges
			for (CyGroup group: groupSet) {
				updateExternalEdgeAttribute(group);
				updateCollapsedGroupsAttribute(group);
				updateGroupAttribute(net, group);
			}
		}
	}

	public void groupsSerialized(final Collection<CyNetwork> networks, final Collection<CyNetworkView> views) {
		if (networks == null)
			return;

		Map <CyNetwork, CyNetworkView> viewMap = new HashMap<CyNetwork, CyNetworkView>();
		if (views != null)
			for (CyNetworkView view: views)
				viewMap.put(view.getModel(), view);
		

		for (CyNetwork net: networks) {
			if (!(net instanceof CySubNetwork) || !addedNodes.containsKey(net))
				continue;

			List<CyNode> nodeList = addedNodes.get(net);
			if (nodeList != null && nodeList.size() > 0) {
				net.removeNodes(nodeList);
				// Update our view if we have one
				if (viewMap.containsKey(net))
					viewMap.get(net).updateView();
			}
		}

	}

	public List<CyNode> getExpandedGroups(final CyNetwork network) {
		// Get all of our groups in this network
		Set<CyGroup> groupSet = groupMgr.getGroupSet(network);

		// For each group see if it's expanded, but present
		List<CyNode> groupNodes = new ArrayList<CyNode>();
		for (CyGroup group: groupSet) {
			if (!group.isCollapsed(network)) {
				if (network.containsNode(group.getGroupNode())) {
					// We're expanded, but present in the network.  Remove ourselves
					groupNodes.add(group.getGroupNode());
				}
			}
		}
		return groupNodes;
	}

	public List<CyEdge> getGroupNodeEdges(final CyNetwork network) {
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		// Don't need to worry about this for collapsed groups
		List<CyNode> groupNodes = getExpandedGroups(network);
		List<CyEdge> groupNodeEdges = new ArrayList<CyEdge>();
		for (CyNode groupNode : groupNodes) {
			groupNodeEdges.addAll(rootNetwork.getAdjacentEdgeList(groupNode, CyEdge.Type.ANY));
		}
		return groupNodeEdges;
	}

	public List<CyEdge> getExternalEdges(final CyNetwork network) {
		// Get all of our groups in this network
		Set<CyGroup> groupSet = groupMgr.getGroupSet(network);

		List<CyEdge> externalEdges = new ArrayList<CyEdge>();
		for (CyGroup group: groupSet) {
			// Don't need to worry about this for expanded groups
			if (group.isCollapsed(network)) {
				externalEdges.addAll(group.getExternalEdgeList());
			}
		}
		return externalEdges;
	}
	
	/**
	 * Make sure all network pointers are already set.
	 * @param networks
	 */
	public void createGroups(final Set<CyNetwork> networks, final Set<CyNetworkView> viewSet) {
		if (networks == null) return;
		
		for (final CyNetwork net : networks) {
			if (net instanceof CySubNetwork)
				createGroups((CySubNetwork) net, viewSet);
		}
	}

	public void createGroups(final CySubNetwork net, final Set<CyNetworkView> viewSet) {
		// Look for possible meta-nodes by inspecting the groups metadata in the network's hidden table
		final CyRootNetwork rootNet = net.getRootNetwork();
		final List<CyNode> nodes = net.getNodeList();

		// Iterate each node and check if they have network pointers
		for (final CyNode n : nodes) {
			final CyNetwork netPointer = n.getNetworkPointer();
			
			if (netPointer == null)
				continue;
			
			// Retrieve the internal nodes and edges
			final CyRow dnRow = net.getRow(n, CyNetwork.DEFAULT_ATTRS);
			final CyRow hnRow = net.getRow(n, CyNetwork.HIDDEN_ATTRS);
			final CyRow snRow = netPointer.getRow(netPointer, CyNetwork.HIDDEN_ATTRS);
			
			if (!dnRow.isSet(GROUP_STATE_ATTRIBUTE) && !hnRow.isSet(GROUP_STATE_ATTRIBUTE) &&
			    !hnRow.isSet(GROUP_ATTRIBUTE))
				continue;

			// Check for nested groups recursively
			createGroups((CySubNetwork) netPointer, null);
			
			boolean collapsed = false;
			boolean cy2group = false;

			if (hnRow.isSet(GROUP_STATE_ATTRIBUTE)) {
				Integer grState = hnRow.get(GROUP_STATE_ATTRIBUTE, Integer.class); // 2.x metadata
				cy2group = true;
				if (grState.intValue() == 2) collapsed = true;
			} else if (dnRow.isSet(GROUP_STATE_ATTRIBUTE)) {
				Integer grState = dnRow.get(GROUP_STATE_ATTRIBUTE, Integer.class); // 2.x metadata
				cy2group = true;
				if (grState.intValue() == 2) collapsed = true;
			} else {
				List<Long> collapsedList = snRow.getList(GROUP_COLLAPSED_ATTRIBUTE, Long.class);
				if (collapsedList != null) {
					// Are we collapsed in this network?
					for (Long suid: collapsedList) {
						if (suid.equals(net.getSUID())) {
							collapsed = true;
							break;
						}
					}
				}
			}
			
			// If we're not collapsed, remove the group node from the network before
			// we create the group
			if (!collapsed) {
				net.removeNodes(Collections.singletonList(n));

				// Add our internal edges into the network (if they aren't already)
				for (CyEdge edge: netPointer.getEdgeList()) {
					if (net.containsEdge(edge))
						continue;
					net.addEdge(edge);
				}

				// If we're a cy2 group, remember our state
				if (cy2group) {
					CyTable hnTable = rootNet.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
					if (hnTable.getColumn(GROUP_STATE_ATTRIBUTE) == null) {
						hnTable.createColumn(GROUP_STATE_ATTRIBUTE, Integer.class, false);
					}
					hnTable.getRow(n.getSUID()).set(GROUP_STATE_ATTRIBUTE, 1);
				}

			}
			
			// Create the group
			final CyGroup group;
// TODO: Do not create more than one instance of a CyGroup for the same group node.
//			if (groupMgr.isGroup(n, rootNet)) {
//				group = groupMgr.getGroup(n, rootNet);
//				group.addGroupToNetwork(net);
//				group.collapse(net);
//			} else {
				group = groupFactory.createGroup(net, n, true);
//			}
			
			// Add in the missing external edges if we're collapsed
			// CyRow groupNodeRow = net.getRow(n, CyNetwork.HIDDEN_ATTRS);
			if (snRow.isSet(EXTERNAL_EDGE_ATTRIBUTE)) {
				List<Long> externalIDs = snRow.getList(EXTERNAL_EDGE_ATTRIBUTE, Long.class);
				List<CyEdge> externalEdges = new ArrayList<CyEdge>();
				
				for (Long suid: externalIDs) {
					CyEdge newEdge = rootNet.getEdge(suid);
					if (newEdge != null) {
						// Don't add the edge if it already exists
						if (!isMeta(group, newEdge) || !metaExists(group, newEdge)) {
							externalEdges.add(newEdge);
						}
					}
				}
				
				if (cy2group && collapsed) {
					updateMetaEdges(net, n);
				}

				group.addEdges(externalEdges);
			}
			
			// TODO: restore group's settings
			// Some special handling for cy2 groups.  We need to restore the X,Y coordinates of the nodes
			// if we're collapsed.
			if (cy2group) {
				if (collapsed) {
					// Update the locations of child nodes
					updateChildOffsets(netPointer, net, n);
				}
// TODO add groups to a cached map: xgmmlreader has to access all groups and update their node locations when creating new views
				// If the group is collapsed, we need to provide the location information so we can expand it
				if (viewSet != null) {
					if (collapsed) {
						for (CyNetworkView view : viewSet) {
							if (view.getModel().equals(net))
								updateGroupNodeLocation(view, n);
								disableNestedNetworkIcon(view, n);
						}
					}
				}
			}
		}

		// TODO: If this is a 2.x group, clean up
		if (net.getDefaultNodeTable().getColumn(GROUP_STATE_ATTRIBUTE) != null)
			net.getDefaultNodeTable().deleteColumn(GROUP_STATE_ATTRIBUTE);
		if (net.getDefaultNodeTable().getColumn(GROUP_ISLOCAL_ATTRIBUTE) != null) 
			net.getDefaultNodeTable().deleteColumn(GROUP_ISLOCAL_ATTRIBUTE);
	}
	
	public void updateGroupNodes(final CyNetworkView view) {
		if (view == null) return;

		CyNetwork network = view.getModel();
		Set<CyGroup> groupSet = groupMgr.getGroupSet(network);
		
		for (CyGroup group : groupSet) {
			if (group.isCollapsed(network))
				updateGroupNodeLocation(view, group.getGroupNode());
		}
	}
	
	public Set<CyGroup> getGroups(final Collection<CyNetwork> networks) {
		final Set<CyGroup> groups = new HashSet<CyGroup>();

		for (final CyNetwork net : networks)
			groups.addAll(groupMgr.getGroupSet(net));
		
		return groups;
	}
	
	public void destroyGroups(final Set<CyGroup> groups) {
		for (final CyGroup gr : groups)
			groupMgr.destroyGroup(gr);
	}
	
	private void updateExternalEdgeAttribute(final CyGroup group) {
		// Get the group node
		CyNode groupNode = group.getGroupNode();

		// Get the list of external edges
		Set<CyEdge> externalEdges = group.getExternalEdgeList();

		// Save the SUIDs for each edge
		List<Long> externalEdgeSUIDs = new ArrayList<Long>();
		for (CyEdge edge: externalEdges) {
			externalEdgeSUIDs.add(edge.getSUID());
		}

		// We need to use the sub network to get the row because the group might be expanded
		final CyNetwork netPointer = groupNode.getNetworkPointer();
		final CyRow snRow = netPointer.getRow(netPointer, CyNetwork.HIDDEN_ATTRS);

		// Make sure our column exists
		CyTable groupNetTable = snRow.getTable();
		if (groupNetTable.getColumn(EXTERNAL_EDGE_ATTRIBUTE) == null) {
			groupNetTable.createListColumn(EXTERNAL_EDGE_ATTRIBUTE, Long.class, false);
		}

		snRow.set(EXTERNAL_EDGE_ATTRIBUTE, externalEdgeSUIDs);
		return;
	}

	private void updateCollapsedGroupsAttribute(final CyGroup group) {
		// Get our subnetwork
		CySubNetwork np = (CySubNetwork)group.getGroupNode().getNetworkPointer();
		CyTable hiddenTable = np.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		CyRow netRow = hiddenTable.getRow(np.getSUID());  // We use our embedded network table for this
		CyColumn stateColumn = hiddenTable.getColumn(GROUP_COLLAPSED_ATTRIBUTE);
		if (stateColumn == null)
			hiddenTable.createListColumn(GROUP_COLLAPSED_ATTRIBUTE, Long.class, true);

		List<Long> collapsedList = new ArrayList<Long>();
		for (CyNetwork net: group.getNetworkSet()) {
			if(group.isCollapsed(net))
				collapsedList.add(net.getSUID());
		}
		netRow.set(GROUP_COLLAPSED_ATTRIBUTE, collapsedList);
		return;
	}

	private void updateGroupAttribute(final CyNetwork net, final CyGroup group) {
		CyNode node = group.getGroupNode();
		List<CyNode> nodeList;
		
		// Expanded groups won't show in the network.  If it's not there, we need
		// to add it so that it will get serialized
		if (!net.containsNode(node)) {
			// Node not in this network.  Add it and mark it....
			CySubNetwork subNet = (CySubNetwork)net;
			subNet.addNode(node); // Temporarily add this to the network so we can serialize it

			// Remember it...
			if (addedNodes.containsKey(net))
				nodeList = addedNodes.get(net);
			else
				nodeList = new ArrayList<CyNode>();
			nodeList.add(node);
			addedNodes.put(net, nodeList);
		}

		CyTable hiddenTable = net.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		if (hiddenTable.getColumn(GROUP_ATTRIBUTE) == null)
			hiddenTable.createColumn(GROUP_ATTRIBUTE, Boolean.class, false);
		hiddenTable.getRow(node.getSUID()).set(GROUP_ATTRIBUTE, Boolean.TRUE);
	}
	
	private void updateChildOffsets(CyNetwork netPointer, CyNetwork net, CyNode groupNode) {
		CyTable hiddenSubTable = netPointer.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);

		// Create our columns, if we need to
		createListColumn(hiddenSubTable, X_LOCATION_ATTR, Double.class);
		createListColumn(hiddenSubTable, Y_LOCATION_ATTR, Double.class);
		createListColumn(hiddenSubTable, GROUP_NETWORKS_ATTRIBUTE, Long.class);

		for (CyNode child: netPointer.getNodeList()) {
			CyRow row = hiddenSubTable.getRow(child.getSUID());

			// Load the existing information in, if any
			List<Long> networks = new ArrayList<Long>();
			List<Double> xLocation = new ArrayList<Double>();
			List<Double> yLocation = new ArrayList<Double>();

			if (row.isSet(GROUP_NETWORKS_ATTRIBUTE))
				networks = row.getList(GROUP_NETWORKS_ATTRIBUTE, Long.class);
			if (row.isSet(X_LOCATION_ATTR))
				xLocation = row.getList(X_LOCATION_ATTR, Double.class);
			if (row.isSet(Y_LOCATION_ATTR))
				yLocation = row.getList(Y_LOCATION_ATTR, Double.class);

			networks.add(net.getSUID());

			// Look for child node location information
			if ((hiddenSubTable.getColumn(GROUP_NODEX_ATTRIBUTE) != null)
			    && (row.isSet(GROUP_NODEX_ATTRIBUTE))) {
				xLocation.add(row.get(GROUP_NODEX_ATTRIBUTE, Double.class));
			} else {
				xLocation.add(0.0d);
			}

			if ((hiddenSubTable.getColumn(GROUP_NODEY_ATTRIBUTE) != null)
			    && (row.isSet(GROUP_NODEY_ATTRIBUTE))) {
				yLocation.add(row.get(GROUP_NODEY_ATTRIBUTE, Double.class));
			} else {
				yLocation.add(0.0d);
			}

			row.set(GROUP_NETWORKS_ATTRIBUTE, networks);
			row.set(X_LOCATION_ATTR, xLocation);
			row.set(Y_LOCATION_ATTR, yLocation);
		}
	}

	private void updateMetaEdges(final CyNetwork net, final CyNode groupNode) {
		CyRootNetwork rootNetwork = ((CySubNetwork)net).getRootNetwork();

		// Find all of the edges from the group node and if they are
		// meta-edges, mark them as such
		List<CyEdge> edgeList = net.getAdjacentEdgeList(groupNode, CyEdge.Type.ANY);
		for (CyEdge edge: edgeList) {
			String interaction = net.getRow(edge).get(CyEdge.INTERACTION, String.class);
			if (interaction.startsWith("meta-")) {
				createColumn(rootNetwork.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS), 
				             ISMETA_EDGE_ATTR, Boolean.class);
				rootNetwork.getRow(edge, CyNetwork.HIDDEN_ATTRS).set(ISMETA_EDGE_ATTR, Boolean.TRUE);
			}
		}
	}
	
	private void updateGroupNodeLocation(final CyNetworkView view, final CyNode groupNode) {
		if (view == null) return;

		CyRootNetwork rootNetwork = ((CySubNetwork)view.getModel()).getRootNetwork();
		View<CyNode> nView = view.getNodeView(groupNode);
		double x = nView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double y = nView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		
		// Save it
		// TODO these attributes will not work with multiple views per network!
		CyRow hRow = rootNetwork.getRow(groupNode, CyNetwork.HIDDEN_ATTRS);
		createColumn(hRow.getTable(), X_LOCATION_ATTR, Double.class);
		createColumn(hRow.getTable(), Y_LOCATION_ATTR, Double.class);
		hRow.set(X_LOCATION_ATTR, new Double(x));
		hRow.set(Y_LOCATION_ATTR, new Double(y));
	}

	public void disableNestedNetworkIcon(final CyNetworkView view, CyNode n) {
		View<CyNode> nView = view.getNodeView(n);
		nView.setLockedValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, Boolean.FALSE);
	}
	
	private void createColumn(CyTable table, String column, Class<?> type) {
		if (table.getColumn(column) == null)
			table.createColumn(column, type, false);
	}
	
	private void createListColumn(CyTable table, String column, Class<?> type) {
		if (table.getColumn(column) == null)
			table.createListColumn(column, type, false);
	}

	private boolean isMeta(CyGroup group, CyEdge edge) {
		CyRootNetwork rootNetwork = group.getRootNetwork();
		if (edge.getSource() != group.getGroupNode() &&
		    edge.getTarget() != group.getGroupNode())
			return false;

	    Boolean meta = rootNetwork.getRow(edge, CyNetwork.HIDDEN_ATTRS).
	                            get(ISMETA_EDGE_ATTR, Boolean.class, Boolean.FALSE);
	
	    return meta.booleanValue();
	}

	private boolean metaExists(CyGroup group, CyEdge edge) {
		CyRootNetwork rootNetwork = group.getRootNetwork();
		CyNode source;
		CyNode target;
		if (edge.getSource() == group.getGroupNode()) {
			source = group.getGroupNode();
			target = edge.getTarget();
		} else {
			source = edge.getSource();
			target = group.getGroupNode();
		}
		List<CyEdge> list = rootNetwork.getConnectingEdgeList(source, target, CyEdge.Type.DIRECTED);
		for (CyEdge e: list) {
			if (e != edge && isMeta(group, e))
				return true;	// Already got a meta-edge for this
		}
		return false;
	}
}
