package org.cytoscape.group.internal.view;

/*
 * #%L
 * Cytoscape Group View Impl (group-view-impl)
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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.group.internal.CyGroupImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.data.CyGroupSettingsImpl;
import org.cytoscape.group.internal.data.GroupViewTypeChangedEvent;
import org.cytoscape.group.internal.data.GroupViewTypeChangedListener;
import org.cytoscape.group.events.GroupAddedEvent;
import org.cytoscape.group.events.GroupAddedListener;
import org.cytoscape.group.events.GroupAboutToBeDestroyedEvent;
import org.cytoscape.group.events.GroupAboutToBeDestroyedListener;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the view portion of group collapse/expand
 */
public class GroupViewCollapseHandler implements GroupAboutToCollapseListener,
                                                 GroupCollapsedListener,
                                                 SessionLoadedListener,
                                                 GroupAboutToBeDestroyedListener,
                                                 GroupViewTypeChangedListener,
                                                 GroupAddedListener
{

	private final CyGroupManagerImpl cyGroupManager;
	private final CyGroupSettingsImpl cyGroupSettings;
	private final CyEventHelper cyEventHelper;
	private final NodeChangeListener nodeChangeListener;
	private final CyApplicationManager appMgr;
	private CyNetworkManager cyNetworkManager = null;
	private CyNetworkViewManager cyNetworkViewManager = null;
	private CyNetworkViewFactory cyNetworkViewFactory = null;
	private VisualMappingManager cyStyleManager = null;
	private static final Logger logger = LoggerFactory.getLogger(GroupViewCollapseHandler.class);
	private static final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
	private static final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;

	private static final String X_LOCATION_ATTR = "__xLocation";
	private static final String Y_LOCATION_ATTR = "__yLocation";
	private static final String NETWORK_SUID_ATTR = "__groupNetworks.SUID";
	private static final String ISMEMBER_EDGE_ATTR = "__isMemberEdge";
	private static final int Z_OFFSET = -100; // Offset for group nodes

	/**
	 * 
	 * @param cyEventHelper
	 */
	public GroupViewCollapseHandler(final CyGroupManagerImpl groupManager,
	                                final CyGroupSettingsImpl groupSettings,
	                                final NodeChangeListener nodeChangeListener,
		                              final CyEventHelper cyEventHelper) {
		this.cyGroupManager = groupManager;
		this.cyGroupSettings = groupSettings;
		this.nodeChangeListener = nodeChangeListener;
		this.cyEventHelper = cyEventHelper;
		this.appMgr = cyGroupManager.getService(CyApplicationManager.class);
	}


	/**
	 * This is called when the user changes the view type for a particular
	 * group.  We want to respond and change the visualization right away,
	 * from the old visual type to the new visual type.
	 */
	public void handleEvent(GroupViewTypeChangedEvent e) {
		getServices();
		GroupViewType oldType = e.getOldType();
		GroupViewType newType = e.getNewType();
		CyGroup group = e.getGroup();

		if (group == null || oldType.equals(newType))
			return;

		if (oldType == GroupViewType.COMPOUND) {
			removeCompoundNode(group);
		} else if (oldType == GroupViewType.SINGLENODE) {
			removeCompoundNode(group);
		} else if (oldType == GroupViewType.SHOWGROUPNODE) {
			((CyGroupImpl)group).removeMemberEdges();
		}

		for (CyNetwork net: group.getNetworkSet()) {
			// Careful -- the network set includes the root
			if (net.equals(group.getRootNetwork()))
				continue;

			// If we're moving to NONE for the visualization, then
			// we want to disable the group node visibility in the
			// expanded state and we need to leave the group in the
			// collapsed state no matter what we started as.  This is
			// necessary to make sure we can update other settings.
			if (newType == GroupViewType.NONE) {
				((CyGroupImpl)group).setGroupNodeShown(net, false);
				// If we're expanded and the group node is shown, remove it
				if (!group.isCollapsed(net) && net.containsNode(group.getGroupNode())) {
					net.removeNodes(Collections.singletonList(group.getGroupNode()));
					group.collapse(net);
				}
			} else {
				// For all of the other visualization types,
				// do a collapse (if necessary)/expand to make sure
				// the group is appropriately restyled
				if (!group.isCollapsed(net)) {
					group.collapse(net);
					group.expand(net);
				} else {
					group.expand(net);
				}
			}
		}

	}

	public void handleEvent(GroupAboutToCollapseEvent e) {
		getServices();
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		CyRootNetwork rootNetwork = group.getRootNetwork();
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() == 0) {
			return;
		}

		for (CyNetworkView v: views) {
			if (v.getRendererId().equals("org.cytoscape.ding")) {
				view = v;
			}
		}

		if (view == null)
			return;

		GroupViewType groupViewType = cyGroupSettings.getGroupViewType(group);

		if (e.collapsing()) {
			// Calculate the center position of all of the
			// member nodes
			Dimension center = ViewUtils.calculateCenter(view, group.getNodeList());

			if (center != null) {
				// Save it in the groupNode attribute
				ViewUtils.updateGroupLocation(network, group, center);

				// For each member node,
				// 	calculate the offset for each member node from the center
				// 	save it in the node's attribute
				for (CyNode node: group.getNodeList()) {
					if (!network.containsNode(node)) continue;
					Dimension offset = ViewUtils.calculateOffset(center, view, node);
					ViewUtils.updateNodeOffset(network, group, node, offset);
				}
			}
		} else {
			// Get the current position of the groupNode
			View<CyNode>nView = view.getNodeView(group.getGroupNode());
			if (nView == null) return;
			double x = nView.getVisualProperty(xLoc);
			double y = nView.getVisualProperty(yLoc);
			// Save it in the groupNode attribute
			ViewUtils.updateGroupLocation(network, group, x, y);
		}
	}

	public void handleEvent(GroupCollapsedEvent e) {
		getServices();
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() == 0) {
			return;
		}

		for (CyNetworkView v: views) {
			if (v.getRendererId().equals("org.cytoscape.ding")) {
				view = v;
			}
		}

		if (view == null)
			return;

		CyRootNetwork rootNetwork = group.getRootNetwork();

		GroupViewType groupViewType = cyGroupSettings.getGroupViewType(group);

		if (e.collapsed()) {
			// System.out.println("Got collapse event for "+group);
			// Get the location to move the group node to
			Dimension d = ViewUtils.getLocation(network, group);
			if (d != null) {
				// Move it.
				ViewUtils.moveNode(view, group.getGroupNode(), d);
			}
			View<CyNode> nView = view.getNodeView(group.getGroupNode());

			if (cyGroupSettings.getUseNestedNetworks(group)) {
				// Now, if we're displaying the nested network, create it....
				CyNetwork nn = group.getGroupNetwork();
				cyNetworkManager.addNetwork(nn);
				CyNetworkView nnView = cyNetworkViewFactory.createNetworkView(nn);
				cyNetworkViewManager.addNetworkView(nnView);
				// Move the nodes around
				ViewUtils.moveNodes(group, nnView, d);

				// Allow the nested network image to be displayed
				nView.clearValueLock(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);
			} else {
				// Make sure the nested network image is not displayed
				nView.setLockedValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, Boolean.FALSE);
			}

			// If we were showing this as a compound node, we need to restyle
			if (groupViewType.equals(GroupViewType.COMPOUND)) {
				deActivateCompoundNode(group, view);
			}

			// Handle opacity
			double opacity = cyGroupSettings.getGroupNodeOpacity(group);
			if (opacity != 100.0)
				nView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, (int)(opacity*255.0/100.0));

			// Apply visual property to added graph elements
			ViewUtils.applyStyle(Collections.singleton(nView.getModel()), views, cyStyleManager);
			ViewUtils.applyStyle(network.getAdjacentEdgeList(group.getGroupNode(), Type.ANY), 
			                     views, cyStyleManager);
		} else {
			// System.out.println("Got expand event for "+group);
			CyNode groupNode = group.getGroupNode();

			// Get the location of the group node before it went away
			Dimension center = ViewUtils.getLocation(network, group);
			if (center != null)
				ViewUtils.moveNodes(group, view, center);

			// If we're asked to, show the group node
			if (!groupViewType.equals(GroupViewType.NONE)) {
				CySubNetwork subnet = (CySubNetwork)network;

				subnet.addNode(group.getGroupNode()); // Add the node back

				// Add the group nodes's edges back
				List<CyEdge> groupNodeEdges = rootNetwork.getAdjacentEdgeList(groupNode, CyEdge.Type.ANY);
				for (CyEdge edge: groupNodeEdges) {
					CyRow row = rootNetwork.getRow(edge, CyNetwork.HIDDEN_ATTRS);
					// System.out.println("Group node edge: "+edge+"("+
					//                    rootNetwork.getRow(edge).get(CyNetwork.NAME, String.class)+")");
					// Only add non-meta edges
					if (row != null && 
							(!row.isSet(CyGroupImpl.ISMETA_EDGE_ATTR) ||
							 !row.get(CyGroupImpl.ISMETA_EDGE_ATTR, Boolean.class))) {
						subnet.addEdge(edge);
					}
				}

				if (groupViewType.equals(GroupViewType.SHOWGROUPNODE)) {
					// If this is the first time, we need to add our member
					// edges in.
					addMemberEdges(group, network);

					// Style our member edges
				}

				// Flush events so that the view hears about it
				cyEventHelper.flushPayloadEvents();

				// Now, call ourselves as if we had been collapsed
				handleEvent(new GroupCollapsedEvent(group, network, true));

				if (groupViewType.equals(GroupViewType.COMPOUND) ||
				    groupViewType.equals(GroupViewType.SINGLENODE)) {
					// May be redundant, but just to make sure
					((CyGroupImpl)group).setGroupNodeShown(network, true);
					activateCompoundNode(group, view);
				}

				cyEventHelper.flushPayloadEvents();
				view.updateView();
				return;
			}

			final List<CyNode> nodeList = group.getNodeList();

			// TODO: turn off stupid nested network thing
			for (CyNode node: nodeList) {
				if (!network.containsNode(node)) continue;
				View<CyNode> nView = view.getNodeView(node);
				if (node.getNetworkPointer() != null && cyGroupManager.isGroup(node, network)) {
					if (!cyGroupSettings.getUseNestedNetworks(cyGroupManager.getGroup(node, network))) {
						nView.setLockedValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, Boolean.FALSE);
					}
				}
			}

			// Apply visual property to added graph elements
			ViewUtils.applyStyle(nodeList, views, cyStyleManager);
			ViewUtils.applyStyle(group.getInternalEdgeList(), views, cyStyleManager);
			ViewUtils.applyStyle(group.getExternalEdgeList(), views, cyStyleManager);
		}

		view.updateView();
	}

	public void handleEvent(GroupAddedEvent e) {
		getServices();
		CyGroup group = e.getGroup();
		GroupViewType groupViewType = cyGroupSettings.getGroupViewType(group);
		// System.out.println("Group added: "+group);

		try {
		// When we add a group, we may want to reflect the state
		// of the group visually immediately
		if (groupViewType.equals(GroupViewType.COMPOUND) || 
		    groupViewType.equals(GroupViewType.SINGLENODE)) {
			Set<CyNetwork> networkSet = group.getNetworkSet();
			if (networkSet == null || networkSet.size() == 0) return;

/*
			// Assume we're in the current network
			Dimension d = ViewUtils.getLocation(view.getModel(), group);
			// Move it.
			ViewUtils.moveNode(view, group.getGroupNode(), d);
*/

			for (CyNetwork network: networkSet) {
				((CyGroupImpl)group).setGroupNodeShown(network, true);
				GroupCollapsedEvent ev = new GroupCollapsedEvent(group, network, false);
				handleEvent(ev);
			}
		}
		} catch (Exception ex) {ex.printStackTrace();}
		return;
	}

	/**
	 * If a group is destroyed, and it's a compound node, we need to fix up a
	 * bunch of things.
	 */
	public void handleEvent(GroupAboutToBeDestroyedEvent e) {
		getServices();
		CyGroup group = e.getGroup();
		GroupViewType groupViewType = cyGroupSettings.getGroupViewType(group);
		if (groupViewType.equals(GroupViewType.COMPOUND)) {
			removeCompoundNode(group);
		}
	}


	/**
	 * We need to do some fixup after we load the session for compound nodes.
	 * When the session gets loaded, we don't (yet) have a view.  We need to 
	 * go through and kick update things to work properly if we have a view
	 * after the session gets loaded.
	 */
	public void handleEvent(SessionLoadedEvent e) {
		getServices();
		try {
		// For each network
		for (CyNetworkView networkView: e.getLoadedSession().getNetworkViews()) {
			CyNetwork network = networkView.getModel();
			// For each group
			for (CyGroup group: cyGroupManager.getGroupSet(network)) {
				GroupViewType groupViewType = cyGroupSettings.getGroupViewType(group);

				// If the group is a compound node and if it is expanded,
				if (groupViewType.equals(GroupViewType.COMPOUND) ||
				    groupViewType.equals(GroupViewType.SINGLENODE)) {
					if (network.containsNode(group.getGroupNode())) {
						// At this point, because of the way we create the
						// group, it will think it's collapsed.  Change
						// that.
						((CyGroupImpl)group).setCollapsed(network, false);
						((CyGroupImpl)group).setGroupNodeShown(network, true);

						try {
							// OK, now activate the group
							activateCompoundNode(group, networkView);
						} catch (Exception rtree) {
							// We may get an RTree exception under rare
							// circumstances.
							rtree.printStackTrace();
						}
					}
				}
			}
		}
		} catch (Exception ex) { ex.printStackTrace(); }
	}

	private void activateCompoundNode(CyGroup group, CyNetworkView view) {
		// System.out.println("Activating listener for "+group);
		// Style the group node
		ViewUtils.styleCompoundNode(group, view, cyGroupManager, cyStyleManager,
		                            cyGroupSettings.getGroupViewType(group));

		// Apply visual property to added graph elements
		ViewUtils.applyStyle(group.getNodeList(), Collections.singleton(view), cyStyleManager);
		ViewUtils.applyStyle(group.getInternalEdgeList(), Collections.singleton(view), cyStyleManager);
		ViewUtils.applyStyle(group.getExternalEdgeList(), Collections.singleton(view), cyStyleManager);

		// Style the member nodes (set appropriate Z)
		// Add ourselves to the listeners for node movement
		nodeChangeListener.addGroup(group, view);
	}

	private void deActivateCompoundNode(CyGroup group, CyNetworkView view) {
		// System.out.println("Deactivating listener for "+group);
		// Disable ourselves from listeners
		nodeChangeListener.removeGroup(group, view);

		// Reset the Z values
		ViewUtils.unStyleCompoundNode(group, view, cyStyleManager);
	}

	private void addMemberEdges(CyGroup group, CyNetwork network) {
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		CyNode groupNode = group.getGroupNode();
		List<CyEdge> newEdges = new ArrayList<CyEdge>();
		for (CyNode node: group.getNodeList()) {
			if (!network.containsEdge(groupNode, node)) {
				CyEdge edge = network.addEdge(groupNode, node, true);
				CyRow edgeRow = network.getRow(edge);
				CyRow edgeRootRow = rootNetwork.getRow(edge, CyRootNetwork.SHARED_ATTRS);
				// Add a name
				String name = getNodeName(groupNode, network)+" (member) "+getNodeName(node, network);
				edgeRow.set(CyNetwork.NAME, name);
				edgeRootRow.set(CyRootNetwork.SHARED_NAME, name);

				// Set the interaction
				edgeRow.set(CyEdge.INTERACTION, "member");
				edgeRootRow.set(CyRootNetwork.SHARED_INTERACTION, "member");
				newEdges.add(edge);
			}
		}
		if (newEdges.size() > 0)
			group.addEdges(newEdges);
	}

	private void removeCompoundNode(CyGroup group) {
		for (CyNetwork network: group.getNetworkSet()) {
			final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
			CyNetworkView view = null;
			if(views.size() == 0) {
				return;
			}

			for (CyNetworkView v: views) {
				if (v.getRendererId().equals("org.cytoscape.ding")) {
					view = v;
				}
			}

			if (view == null)
				continue;

			ViewUtils.unStyleCompoundNode(group, view, cyStyleManager);
			nodeChangeListener.removeGroup(group, view);
		}
	}

	private String getNodeName(CyNode node, CyNetwork network) {
		return network.getRow(node).get(CyNetwork.NAME, String.class);
	}

	// Get any services we might need, if we don't already have them
	private void getServices() {
		if (cyNetworkManager == null) {
			cyNetworkManager = cyGroupManager.getService(CyNetworkManager.class);
		}

		if (cyNetworkViewManager == null) {
			cyNetworkViewManager = cyGroupManager.getService(CyNetworkViewManager.class);
		}

		if (cyNetworkViewFactory == null) {
			cyNetworkViewFactory = cyGroupManager.getService(CyNetworkViewFactory.class);
		}

		if (cyStyleManager == null) {
			cyStyleManager = cyGroupManager.getService(VisualMappingManager.class);
		}
	}
}
