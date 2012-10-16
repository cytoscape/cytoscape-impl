/*
 Copyright (c) 2008, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToBeRemovedEvent;
import org.cytoscape.group.events.GroupAddedToNetworkEvent;
import org.cytoscape.group.events.GroupNodesAddedEvent;
import org.cytoscape.group.events.GroupNodesRemovedEvent;
import org.cytoscape.group.events.GroupEdgesAddedEvent;
import org.cytoscape.group.events.GroupEdgesRemovedEvent;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;


// TODO: Update attributes

class CyGroupImpl implements CyGroup {
	final private static String CHILDREN_ATTR = "NumChildren";
	final private static String DESCENDENTS_ATTR = "NumDescendents";
	final private static String ISMETA_EDGE_ATTR = "__isMetaEdge";

	final private CyEventHelper cyEventHelper;
	final private CyGroupManager mgr;

	private CyNode groupNode;
	private Set<CyEdge> externalEdges;
	private Set<CyEdge> metaEdges;
    private Set<CyEdge> memberEdges;
	private CyRootNetwork rootNetwork = null;
	private Set<CyNetwork> networkSet = null;
	private Set<Long> collapseSet = null;
	private boolean nodeProvided = false;  // We'll need this when we destroy ourselves

	CyGroupImpl(final CyEventHelper eventHelper, 
	            final CyGroupManager mgr, CyNetwork network, CyNode node,
	            List<CyNode>nodes, List<CyEdge>edges) {
		this.cyEventHelper = eventHelper;
		this.mgr = mgr;

		this.rootNetwork = ((CySubNetwork)network).getRootNetwork();
		if (node == null)
			this.groupNode = this.rootNetwork.addNode();
		else {
			nodeProvided = true;
			this.groupNode = node;
		}

		this.externalEdges = new HashSet<CyEdge>();
		this.metaEdges = new HashSet<CyEdge>();
		this.memberEdges = new HashSet<CyEdge>();
		this.networkSet = new HashSet<CyNetwork>();
		this.collapseSet = new HashSet<Long>();

		networkSet.add(rootNetwork);
		networkSet.add(network);

		if (nodes == null)
			nodes = new ArrayList<CyNode>();

        // This is merely a copy of the "nodes" list but as a set,
        // so it's fast to call the contains() method.
		Set<CyNode> nodeMap = new HashSet<CyNode>(nodes);

        // This block of code makes the distinction between internal and external edges
        // based on the edges we were given. If "edges" is null, it's our responsibility
        // to build the edge list from the parent network's edges in our group.
		if (edges != null) {
			List<CyEdge> intEdges = new ArrayList<CyEdge>();
			// Remove those edges in the list that aren't attached to nodes in
			// the list.  Otherwise, we'll wind up adding nodes to the group
			// that the user didn't request.
			for (CyEdge e: edges) {
				if (nodeMap.contains(e.getSource()) && nodeMap.contains(e.getTarget())) {
					intEdges.add(e);
                } else if(e.getSource().equals(node) || e.getTarget().equals(node)) {
                    memberEdges.add(e);
				} else {
					externalEdges.add(e);
				}
			}
			edges = intEdges;
		} else if (edges == null) {
			// Create the edge lists
			edges = new ArrayList<CyEdge>();

			// Get all of the edges and put them in the right lists
			for (CyNode n: nodes) {
				List<CyEdge> aEdges = network.getAdjacentEdgeList(n, CyEdge.Type.ANY);
				for (CyEdge e: aEdges) {
					if (nodeMap.contains(e.getSource()) && nodeMap.contains(e.getTarget())) {
						edges.add(e);
                    } else if(e.getSource().equals(node) || e.getTarget().equals(node)) {
                        memberEdges.add(e);
					} else {
						// This is an external edge, which means that we need to create
						// a corresponding meta-edge
						externalEdges.add(e);
					}
				}
			}
		}

        // The group node must have a network pointer for expanding and collapsing.
        // If we were given a network pointer with the group node,
        // reflect our own internal data structures with what's in the network pointer.
        // If we were not given a network pointer, make a new subnetwork with our
        // group's contents.
		CySubNetwork np = (CySubNetwork)groupNode.getNetworkPointer();
		// If we already have a network pointer and we didn't get
		// nodes or edges, and the network pointer points to the same
		// root network, then it may have been provided by the session loader
		if (np != null && nodeProvided && 
		    edges.size() == 0 && nodes.size() == 0 &&
		    np.getRootNetwork().equals(this.rootNetwork)) {
			CySubNetwork groupNet = np;

			// See if we're already collapsed
			if (network.containsNode(groupNode)) {
				// Yes, note it
				collapseSet.add(network.getSUID());
			}
		} else {
			// Create the subnetwork
			CySubNetwork groupNet = rootNetwork.addSubNetwork(nodes, edges);
			groupNode.setNetworkPointer(groupNet);
		}

		// Update our meta-edges
		updateMetaEdges(true);

		// Initialize our attributes
		updateCountAttributes(rootNetwork);

		// printGroup();
	}

	/**
	 * @see org.cytoscape.group.CyGroup#getGroupNode()
	 */
	@Override
	public CyNode getGroupNode() {
		return groupNode;
	}

	/**
	 * @see org.cytoscape.group.CyGroup#getNodeList()
	 */
	@Override
	public List<CyNode> getNodeList() {
		return getGroupNetwork().getNodeList();
	}

	/**
	 * @see org.cytoscape.group.CyGroup#getInternalEdgeList()
	 */
	@Override
	public List<CyEdge> getInternalEdgeList() {
		return getGroupNetwork().getEdgeList();
	}

	/**
	 * @see org.cytoscape.group.CyGroup#getExternalEdgeList()
	 */
	@Override
	public Set<CyEdge> getExternalEdgeList() {
		return externalEdges;
	}

	/**
	 * @see org.cytoscape.group.CyGroup#getGroupNetwork()
	 */
	@Override
	public CySubNetwork getGroupNetwork() {
		return (CySubNetwork)groupNode.getNetworkPointer();
	}

	/**
	 * @see org.cytoscape.group.CyGroup#addNode()
	 */
	private synchronized void addNode(CyNode node) {
		if (!rootNetwork.containsNode(node))
			throwIllegalArgumentException("Can only add a node in the same network tree");
		getGroupNetwork().addNode(node);
	}

	/**
	 * @see org.cytoscape.group.CyGroup#addInternalEdge()
	 */
	private synchronized void addInternalEdge(CyEdge edge) {
		if (!rootNetwork.containsEdge(edge))
			throwIllegalArgumentException("Can only add an edge in the same network tree");
		getGroupNetwork().addEdge(edge);
	}

	/**
	 * @see org.cytoscape.group.CyGroup#addExternalEdge()
	 */
	protected synchronized void addExternalEdge(CyEdge edge) {
		if (!rootNetwork.containsEdge(edge))
			throwIllegalArgumentException("Can only add an edge in the same network tree");
		if (!externalEdges.contains(edge))
			externalEdges.add(edge);
	}

    private synchronized void addMemberEdge(CyEdge edge) {
		if (!rootNetwork.containsEdge(edge))
			throwIllegalArgumentException("Can only add an edge in the same network tree");
        if (groupNode == null)
			throwIllegalArgumentException("Cannot add member edge without a group node");
        if (!edge.getSource().equals(groupNode) && !edge.getTarget().equals(groupNode))
			throwIllegalArgumentException("Cannot member edge whose source or target is not the group node");
        if (!memberEdges.contains(edge))
            memberEdges.add(edge);
    }

	/**
	 * @see org.cytoscape.group.CyGroup#addNodes()
	 */
	@Override
	public synchronized void addNodes(List<CyNode> nodes) {
		Set<CyEdge> edgeSet = new HashSet<CyEdge>();
		for (CyNode n: nodes) {
			if (!rootNetwork.containsNode(n))
				throwIllegalArgumentException("Can only add a node in the same network tree");

			addNode(n);
			edgeSet.addAll(rootNetwork.getAdjacentEdgeList(n, CyEdge.Type.ANY));
		}

		for (CyEdge e: edgeSet) {
            final CyNode source = e.getSource();
            final CyNode target = e.getTarget();
			if (getGroupNetwork().containsNode(source) && getGroupNetwork().containsNode(target)) {
				addInternalEdge(e);
            } else if (groupNode != null && (source.equals(groupNode) || target.equals(groupNode))) {
                addMemberEdge(e);
			} else {
				addExternalEdge(e);
			}
		}
		updateMetaEdges(false);
		for (CyNetwork net: networkSet) {
			updateCountAttributes(net);
		}
		cyEventHelper.fireEvent(new GroupNodesAddedEvent(CyGroupImpl.this, nodes));
	}

	/**
	 * @see org.cytoscape.group.CyGroup#addEdges()
	 */
	@Override
	public synchronized void addEdges(List<CyEdge> edges) {
		boolean updateMeta = false;
		for (CyEdge edge: edges) {
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			if(getGroupNetwork().containsNode(source) && getGroupNetwork().containsNode(target)) {
				getGroupNetwork().addEdge(edge);
            } else if (groupNode != null && (source.equals(groupNode) || target.equals(groupNode))) {
                memberEdges.add(edge);
			} else if (getGroupNetwork().containsNode(source) || getGroupNetwork().containsNode(target)) {
				if (!metaEdges.contains(edge)) {
					externalEdges.add(edge);
					updateMeta = true;
				}
			} else
				throwIllegalArgumentException("Attempted to add an edge that has no node in the group");
		}
		if (updateMeta) {
			updateMetaEdges(true);
		}
		// printGroup();
		cyEventHelper.fireEvent(new GroupEdgesAddedEvent(CyGroupImpl.this, edges));
	}

	/**
	 * @see org.cytoscape.group.CyGroup#removeNodes()
	 */
	@Override
	public synchronized void removeNodes(List<CyNode> nodes) {
		List<CyEdge> netEdges = new ArrayList<CyEdge>();
		for (CyNode node: nodes) {
			List<CyEdge> edges = rootNetwork.getAdjacentEdgeList(node, CyEdge.Type.ANY);
			for (CyEdge edge: edges) {
				if (externalEdges.contains(edge))
					externalEdges.remove(edge);
                else if (memberEdges.contains(edge))
                    memberEdges.remove(edge);
				else {
					netEdges.add(edge);
				}
			}
		}
		if (netEdges.size() > 0)
			getGroupNetwork().removeEdges(netEdges);
		getGroupNetwork().removeNodes(nodes);
		updateMetaEdges(false);
		for (CyNetwork net: networkSet) {
			updateCountAttributes(net);
		}
		cyEventHelper.fireEvent(new GroupNodesRemovedEvent(CyGroupImpl.this, nodes));
	}

	/**
	 * @see org.cytoscape.group.CyGroup#removeEdges()
	 */
	@Override
	public synchronized void removeEdges(List<CyEdge> edges) {
		List<CyEdge> netEdges = new ArrayList<CyEdge>();
		for (CyEdge edge: edges) {
			if (getGroupNetwork().containsEdge(edge))
				netEdges.add(edge);
			else if (externalEdges.contains(edge))
				externalEdges.remove(edge);
			else if (metaEdges.contains(edge))
				metaEdges.remove(edge);
            else if (memberEdges.contains(edge))
                memberEdges.remove(edge);
		}
		cyEventHelper.fireEvent(new GroupEdgesRemovedEvent(CyGroupImpl.this, edges));
	}

	/**
	 * @see org.cytoscape.group.CyGroup#getRootNetwork()
	 */
	@Override
	public CyRootNetwork getRootNetwork() {
		return this.rootNetwork;
	}

	/**
 	 * @see org.cytoscape.group.CyGroup#getNetworkSet()
 	 */
	@Override
	public Set<CyNetwork> getNetworkSet() {
		return networkSet;
	}

	/**
 	 * @see org.cytoscape.group.CyGroup#isInNetwork()
 	 */
	@Override
	public boolean isInNetwork(CyNetwork network) {
		return networkSet.contains(network);
	}

	/**
 	 * @see org.cytoscape.group.CyGroup#addGroupToNetwork()
 	 */
	@Override
	public synchronized void addGroupToNetwork(CyNetwork network) {
		// First, we need to make sure this network is in the same
		// root network as the group node
		if (!inSameRoot(network))
			throwIllegalArgumentException("Network not in same root network as group");

		if(!networkSet.contains(network))
			networkSet.add(network);

		// Notify
		cyEventHelper.fireEvent(new GroupAddedToNetworkEvent(CyGroupImpl.this, network));
	}

	/**
 	 * @see org.cytoscape.group.CyGroup#removeGroupFromNetwork()
 	 */
	@Override
	public synchronized void removeGroupFromNetwork(CyNetwork network) {
		// Notify
		cyEventHelper.fireEvent(new GroupAboutToBeRemovedEvent(CyGroupImpl.this, network));

		if(networkSet.contains(network))
			networkSet.remove(network);
	}

	/**
	 * @see org.cytoscape.group.CyGroup#collapse()
	 */
	@Override
	public void collapse(CyNetwork net) {
		// printGroup();
		if (isCollapsed(net))
			return; // Already collapsed

		if (!networkSet.contains(net))
			return; // We're not in that network

		CySubNetwork subnet = (CySubNetwork) net;

		// First collapse any children that are groups
		for (CyNode node: getNodeList()) {
			// Is this a group?
			if (mgr.isGroup(node, net)) {
				// Yes, collapse it
				mgr.getGroup(node,net).collapse(net);
			}
		}

		// Now collapse ourselves
		cyEventHelper.fireEvent(new GroupAboutToCollapseEvent(CyGroupImpl.this, net, true));

		// Deselect all of the nodes
		for (CyNode node: getNodeList()) {
			net.getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);
		}

		// Deselect all of our internal edges
		for (CyEdge edge: getInternalEdgeList()) {
			net.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
		}

		// Deselect all of our member edges
		for (CyEdge edge: memberEdges) {
			net.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
		}

		// Deselect all of our external edges
		for (CyEdge edge: getExternalEdgeList()) {
			net.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
		}

		// Remove all of the nodes from the target network
		subnet.removeNodes(getNodeList());

		// Add the group node and its edges to the target network.
        // If we have member edges, the group node didn't
        // go away so we don't have to add it back in here
        if (memberEdges.size() == 0) {
            subnet.addNode(groupNode);
            // Now add the edges for the group node
            List<CyEdge> groupNodeEdges = rootNetwork.getAdjacentEdgeList(groupNode, CyEdge.Type.ANY);
            for (CyEdge e: groupNodeEdges) {
                // I have no idea why this would be necessary, but it is....
                if (subnet.containsEdge(e))
                    subnet.removeEdges(Collections.singletonList(e));
                subnet.addEdge(e);
            }
        }

		Set<CyNode> memberNodes = new HashSet<CyNode>(getNodeList());

		// Add the meta-edges
		for (CyEdge e: getMetaEdgeList()) {
			subnet.addEdge(e);
		}

		collapseSet.add(net.getSUID());

		// Update attributes
		updateCountAttributes(net);

		cyEventHelper.flushPayloadEvents();  // Make sure the view "hears" about all of the changes...

		// OK, all done
		cyEventHelper.fireEvent(new GroupCollapsedEvent(CyGroupImpl.this, net, true));
	}

	/**
	 * @see org.cytoscape.group.CyGroup#expand()
	 */
	@Override
	public void expand(CyNetwork net) {
		if (!isCollapsed(net))
			return; // Already expanded

		if (!networkSet.contains(net))
			return; // We're not in that network

		cyEventHelper.fireEvent(new GroupAboutToCollapseEvent(CyGroupImpl.this, net, false));

		CySubNetwork subnet = (CySubNetwork) net;

		// Expand it.

		// Remove the group node from the target network only if
        // there are no member edges. If there were member edges,
        // the group node did not go away.
        if (memberEdges.size() == 0)
            subnet.removeNodes(Collections.singletonList(groupNode));

		// Add all of the member nodes and edges in
		for (CyNode n: getNodeList())
			subnet.addNode(n);

		// Add all of the interior edges in
		for (CyEdge e: getInternalEdgeList())
			subnet.addEdge(e);

		// Add all of the exterior edges in
		for (CyEdge e: getExternalEdgeList()) {
			// We need to be careful to only add the edge in
			// if both the edge and the target are available
			// since the target node might have been part of a
			// collapsed group
			if (subnet.containsNode(e.getSource()) && subnet.containsNode(e.getTarget())) {
				subnet.addEdge(e);
			}
		}

        // Add all of the member edges in
		for (CyEdge e: memberEdges)
			subnet.addEdge(e);

		collapseSet.remove(net.getSUID());

		// Finish up
		cyEventHelper.fireEvent(new GroupCollapsedEvent(CyGroupImpl.this, net, false));
	}

	/**
	 * @see org.cytoscape.group.CyGroup#isCollapsed()
	 */
	@Override
	public boolean isCollapsed(CyNetwork net) {
		return collapseSet.contains(net.getSUID());
	}

	/**
 	 * Destroy this group. This will destroy the subnetwork, all metaEdges, and 
 	 * the group node (if we created it).  This is meant to be called from the
 	 * CyGroupManager, only.
 	 */
	public void destroyGroup() {
		for (CyNetwork net: networkSet)
			expand(net);

		// Destroy the subNetwork
		rootNetwork.removeSubNetwork(getGroupNetwork());
		groupNode.setNetworkPointer(null);

		// Release all of our external edges
		externalEdges = null;

		// Remove all of our metaEdges from the root network
		rootNetwork.removeEdges(metaEdges);

		// If our group node was not provided, destroy it if it doesn't have any member edges
		if (!nodeProvided && rootNetwork.containsNode(groupNode) && memberEdges.size() == 0) {
			rootNetwork.removeNodes(Collections.singletonList(groupNode));
		}

		networkSet = null;
		collapseSet = null;
	}

	protected synchronized void addMetaEdge(CyEdge edge) {
		if (!metaEdges.contains(edge))
			metaEdges.add(edge);
	}

	protected synchronized void removeMetaEdge(CyEdge edge) {
		if (!metaEdges.contains(edge))
			metaEdges.remove(edge);
	}

	protected Set<CyEdge> getMetaEdgeList() {
		return metaEdges;
	}

	@Override
	public String toString() {
		return "Group suid: " + groupNode.getSUID() + " node: " + groupNode;
	}

	private boolean inSameRoot(CyNetwork network) {
		CyRootNetwork root = ((CySubNetwork) network).getRootNetwork();
		if (!root.equals(rootNetwork))
			return false;
		return true;
	}

	private	void throwIllegalArgumentException(String message) {
		throw new IllegalArgumentException(message);
	}

  /**
	 * This method is the central method for the creation and maintenance of a
	 * meta-node.  Essentially, it is responsible for creating all of the meta-edges
	 * that connect this meta-node to external nodes.
	 *
	 * Basic approach:
	 *  for each external edge:
	 *    add a meta-edge to the parter
	 *    if the partner is a group and the group is in our network:
	 *      add ourselves to the group's outer edges list (recursively)
	 *      add ourselves to the partner's meta edge list
	 *    if the partner is in a group:
	 *      add ourselves to the group's meta edge list
	 */
	private void updateMetaEdges(boolean ignoreMetaEdges) {
		metaEdges = new HashSet<CyEdge>();

		// We need to use a list iterator because we might need to add new
		// edges to our outer edge list and we want to add them to the
		// iterator to re-examine them
		ListIterator<CyEdge> iterator = (new ArrayList<CyEdge>(externalEdges)).listIterator();
		while (iterator.hasNext()) {
			CyEdge edge = iterator.next();
			CyNode node = getPartner(edge);

			if (ignoreMetaEdges && isMeta(edge)) {
				// this.addMetaEdge(edge);
				continue;
			}

			// If the edge is already on our group node, don't create a metaedge for it
			if (edge.getSource() == groupNode || edge.getTarget() == groupNode)
				continue;

			// If our group node already points to this node, and we're collapsed, we may
			// be adding a new edge from the XGMML reader.  Not a clean special case,
			// but there you have it...
			if (rootNetwork.containsEdge(edge.getSource(), groupNode)
				  && metaAlreadyExists(edge.getSource(), groupNode))
					continue;

			if (rootNetwork.containsEdge(groupNode, edge.getTarget())
				  && metaAlreadyExists(groupNode, edge.getTarget()))
					continue;

			// Create the meta-edge to the external node, but maintain the directionality
			// of the original edge
			CyEdge metaEdge = createMetaEdge(edge, node, groupNode);
			this.addMetaEdge(metaEdge);

			for (CyNetwork net: networkSet) {
				CyGroup metaPartner = mgr.getGroup(node, net);
				if (metaPartner != null) {
					// Recursively add links to the appropriate children
					addPartnerEdges(metaPartner, net);
					((CyGroupImpl)metaPartner).addMetaEdge(metaEdge);
				}

				// Now, handle the case where the partner is a member of one or more groups
				List<CyGroup> nodeGroups = mgr.getGroupsForNode(node);
				if (nodeGroups != null && nodeGroups.size() > 0) {
					addPartnerMetaEdges(net, edge, node, metaEdge);
				}
			}
		}
	}

	protected int getDescendents(CyNetwork net) {
		int nDescendents = getGroupNetwork().getNodeCount();
		for (CyNode node: getGroupNetwork().getNodeList()) {
			CyGroup group = mgr.getGroup(node, net);
			if (group != null)
				nDescendents += ((CyGroupImpl)group).getDescendents(net);
		}
		return nDescendents;
	}

	// Find the edge in our partner that links to us
	protected void addPartnerEdges(CyGroup metaPartner, CyNetwork net) {
		Set<CyEdge> partnerEdges = metaPartner.getExternalEdgeList();
		Set<CyEdge> newEdges = new HashSet<CyEdge>();
		for (CyEdge edge: partnerEdges) {
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			CyNode partner = null;
			boolean directed = edge.isDirected();
			if (getGroupNetwork().containsNode(target)) {
				source = groupNode;
				partner = target;
			} else if (getGroupNetwork().containsNode(source)) {
				target = groupNode;
				partner = source;
			} else {
				continue;
			}

			// Create a new edge
			CyEdge newEdge = rootNetwork.addEdge(source, target, directed);
			newEdges.add(newEdge);

			externalEdges.add(edge);

			CyGroup partnerMeta = mgr.getGroup(partner, net);
			if (partnerMeta != null)
				addPartnerEdges(partnerMeta, net);

			metaEdges.add(newEdge);
		}

		for (CyEdge edge: newEdges) { ((CyGroupImpl)metaPartner).addExternalEdge(edge); }
	}

	private CyEdge createMetaEdge(CyEdge edge, CyNode node, CyNode groupNode) {
		CyEdge metaEdge = null;
		if (isIncoming(edge))
			metaEdge = rootNetwork.addEdge(node, groupNode, edge.isDirected());
		else
			metaEdge = rootNetwork.addEdge(groupNode, node, edge.isDirected());

		// Add the name and mark this as a meta-edge
		String edgeName = rootNetwork.getRow(edge).get(CyNetwork.NAME, String.class);
		rootNetwork.getRow(metaEdge).set(CyNetwork.NAME, "meta-"+edgeName);
		createIfNecessary(metaEdge, CyNetwork.HIDDEN_ATTRS, ISMETA_EDGE_ATTR, Boolean.class);
		rootNetwork.getRow(metaEdge, CyNetwork.HIDDEN_ATTRS).set(ISMETA_EDGE_ATTR, Boolean.TRUE);

		return metaEdge;
	}

	private void addPartnerMetaEdges(CyNetwork net, CyEdge connectingEdge, 
	                                 CyNode partnerNode, CyEdge metaEdge) {
		for (CyGroup partnerGroup: mgr.getGroupsForNode(partnerNode)) {
			// Are we partners in this network?
			if (!partnerGroup.getNetworkSet().contains(net))
				continue;

			CyEdge metaMetaEdge = null;
			CyGroupImpl partner = (CyGroupImpl)partnerGroup;
			if (isIncoming(connectingEdge)) {
				metaMetaEdge = createMetaEdge(connectingEdge, partnerGroup.getGroupNode(), this.groupNode);
			} else {
				metaMetaEdge = createMetaEdge(connectingEdge, this.groupNode, partnerGroup.getGroupNode());
			}

			partner.addMetaEdge(metaMetaEdge);
			partner.addMetaEdge(metaEdge);

			// Now, get our partner's metaEdges and add any that point to our children
			for (CyEdge outerEdge: partner.getMetaEdgeList()) {
				if (isConnectingEdge(outerEdge))
					addExternalEdge(outerEdge);
			}
			
		}
	}

	private boolean isMeta(CyEdge edge) {
		Boolean meta = rootNetwork.getRow(edge, CyNetwork.HIDDEN_ATTRS).
		                        get(ISMETA_EDGE_ATTR, Boolean.class, Boolean.FALSE);

		return meta.booleanValue();
	}

	private boolean isConnectingEdge(CyEdge edge) {
		CyNode source = edge.getSource();
		CyNode target = edge.getTarget();
		if (getGroupNetwork().containsNode(source) || getGroupNetwork().containsNode(target))
			return true;
		return false;
	}

	private boolean isIncoming(CyEdge edge) {
		CyNode source = edge.getSource();
		if (source.equals(groupNode) || getGroupNetwork().containsNode(source))
			return false;
		return true;
	}

	private CyNode getPartner(CyEdge edge) {
		CyNode source = edge.getSource();
		CyNode target = edge.getTarget();
		if (source.equals(groupNode) || getGroupNetwork().containsNode(source))
			return target;
		return source;
	}

	private void createIfNecessary(CyIdentifiable entry, String tableName, 
	                               String attribute, Class type) {
		CyTable table = rootNetwork.getRow(entry, tableName).getTable();
		if (table.getColumn(attribute) == null)
			table.createColumn(attribute, type, false);

		return;
	}

	public void updateCountAttributes(CyNetwork net) {
		CyTable nodeTable = net.getDefaultNodeTable();
		CyColumn childrenColumn = nodeTable.getColumn(CHILDREN_ATTR);
		if (childrenColumn == null) {
			nodeTable.createColumn(CHILDREN_ATTR, Integer.class, true);
		}

		if (!nodeTable.rowExists(groupNode.getSUID())) {
			// Shouldn't happen!
			return;
		}
		CyRow groupRow = nodeTable.getRow(groupNode.getSUID());
		groupRow.set(CHILDREN_ATTR, getGroupNetwork().getNodeCount());

		CyColumn descendentsColumn = nodeTable.getColumn(DESCENDENTS_ATTR);
		if (descendentsColumn == null) {
			nodeTable.createColumn(DESCENDENTS_ATTR, Integer.class, true);
		}

		int nDescendents = getGroupNetwork().getNodeCount();
		for (CyNode node: getGroupNetwork().getNodeList()) {
			if (mgr.isGroup(node, rootNetwork)) {
				Integer d = nodeTable.getRow(node.getSUID()).get(DESCENDENTS_ATTR, Integer.class);
				if (d != null)
					nDescendents += d.intValue();
			}
		}
		groupRow.set(DESCENDENTS_ATTR, nDescendents);
	}

	public boolean metaAlreadyExists(CyNode source, CyNode target) {
		List<CyEdge> edges = rootNetwork.getConnectingEdgeList(source, target, CyEdge.Type.ANY);
		for (CyEdge groupEdge: edges) {
			if (isMeta(groupEdge)) {
				return true;
			}
		}
		return false;
	}

	private void printGroup() {
		System.out.println("Group "+this+" created");
		System.out.println("Nodes:");
		for (CyNode n: getNodeList()) {
			System.out.println("    "+n);
		}
		System.out.println("Internal edges:");
		for (CyEdge edge: getInternalEdgeList()) {
			System.out.println("    "+edge);
		}
		System.out.println("External edges:");
		for (CyEdge edge: getExternalEdgeList()) {
			System.out.println("    "+edge);
		}
		System.out.println("Meta edges:");
		for (CyEdge edge: getMetaEdgeList()) {
			System.out.println("    "+edge);
		}
	}
}
