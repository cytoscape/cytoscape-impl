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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.internal.CyGroupSettingsImpl;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

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
                                                 GroupCollapsedListener
{

	private final CyGroupManager cyGroupManager;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final VisualMappingManager cyStyleManager;
	private final CyGroupSettingsImpl cyGroupSettings;
	private static final Logger logger = LoggerFactory.getLogger(GroupViewCollapseHandler.class);
	private static final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
	private static final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;

	private static final String X_LOCATION_ATTR = "__xLocation";
	private static final String Y_LOCATION_ATTR = "__yLocation";
	private static final String NETWORK_SUID_ATTR = "__groupNetworks.SUID";

	/**
	 * 
	 * @param cyEventHelper
	 */
	public GroupViewCollapseHandler(final CyGroupManager groupManager,
	                                final CyGroupSettingsImpl groupSettings,
	                                final CyNetworkManager netManager,
	                                final CyNetworkViewManager viewManager,
	                                final CyNetworkViewFactory viewFactory,
	                                final VisualMappingManager styleManager) {
		this.cyGroupManager = groupManager;
		this.cyGroupSettings = groupSettings;
		this.cyNetworkManager = netManager;
		this.cyNetworkViewManager = viewManager;
		this.cyStyleManager = styleManager;
		this.cyNetworkViewFactory = viewFactory;
	}

	public void handleEvent(GroupAboutToCollapseEvent e) {
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		CyRootNetwork rootNetwork = group.getRootNetwork();
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();

		if (view == null)
			return;
		
		if (e.collapsing()) {
			// Calculate the center position of all of the
			// member nodes
			Dimension center = calculateCenter(view, group.getNodeList());

			if (center != null) {
				// Save it in the groupNode attribute
				updateGroupLocation(network, group, center);

				// For each member node,
				// 	calculate the offset for each member node from the center
				// 	save it in the node's attribute
				for (CyNode node: group.getNodeList()) {
					Dimension offset = calculateOffset(center, view, node);
					updateNodeOffset(network, group, node, offset);
				}
			}
		} else {
			// Get the current position of the groupNode
			View<CyNode>nView = view.getNodeView(group.getGroupNode());
			double x = nView.getVisualProperty(xLoc);
			double y = nView.getVisualProperty(yLoc);
			// Save it in the groupNode attribute
			updateGroupLocation(network, group, getDim(x,y));
		}
	}

	public void handleEvent(GroupCollapsedEvent e) {
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();

		if (view == null)
			return;
		
		CyRootNetwork rootNetwork = group.getRootNetwork();
		VisualStyle viewStyle = cyStyleManager.getVisualStyle(view);

		if (e.collapsed()) {
			// Get the location to move the group node to
			Dimension d = getLocation(network, group);
			// Move it.
			moveNode(view, group.getGroupNode(), d);
			View<CyNode> nView = view.getNodeView(group.getGroupNode());

			if (cyGroupSettings.getUseNestedNetworks(group)) {
				// Now, if we're displaying the nested network, create it....
				CyNetwork nn = group.getGroupNetwork();
				cyNetworkManager.addNetwork(nn);
				CyNetworkView nnView = cyNetworkViewFactory.createNetworkView(nn);
				cyNetworkViewManager.addNetworkView(nnView);
				// Move the nodes around
				moveNodes(group, nnView, d);
//				// Apply our visual style
//				viewStyle.apply(nnView);
//				nnView.updateView();

				// Allow the nested network image to be displayed
				nView.clearValueLock(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);
			} else {
				// Make sure the nested network image is not displayed
				nView.setLockedValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, Boolean.FALSE);
			}
			
			// Handle opacity
			double opacity = cyGroupSettings.getGroupNodeOpacity(group);
			if (opacity != 100.0)
				nView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, (int)(opacity*255.0/100.0));

		} else {
			CyNode groupNode = group.getGroupNode();

			// Get the location of the group node before it went away
			Dimension center = getLocation(network, group);
			moveNodes(group, view, center);

			// If we're asked to, show the group node
			if (!cyGroupSettings.getHideGroupNode(group)) {
				CySubNetwork subnet = (CySubNetwork)network;

				subnet.addNode(group.getGroupNode()); // Add the node back

				// Add the group nodes's edges back
				List<CyEdge> groupNodeEdges = rootNetwork.getAdjacentEdgeList(groupNode, CyEdge.Type.ANY);
				for (CyEdge edge: groupNodeEdges)
					subnet.addEdge(edge);

//				view.updateView();

				// Now, call ourselves as if we had been collapsed
				handleEvent(new GroupCollapsedEvent(group, network, true));
				return;
			}
		}
		
//		viewStyle.apply(view);
//		view.updateView();
	}

	private Dimension calculateCenter(CyNetworkView view, List<CyNode> nodeList) {
		double xCenter = 0.0d;
		double yCenter = 0.0d;

		for (CyNode node: nodeList) {
			View<CyNode>nView = view.getNodeView(node);
			if (nView == null) continue;
			double x = nView.getVisualProperty(xLoc);
			double y = nView.getVisualProperty(yLoc);
			xCenter += (nView.getVisualProperty(xLoc)) / nodeList.size();
			yCenter += (nView.getVisualProperty(yLoc)) / nodeList.size();
			
		}
		return getDim(xCenter, yCenter);
	}

	private void moveNodes(CyGroup group, CyNetworkView view, Dimension center) {
		CyNetwork net = view.getModel();
		for (CyNode node: group.getNodeList()) {
			Dimension location = getOffsetLocation(net, group, node, center);
			moveNode(view, node, location);
		}
	}

	private Dimension calculateOffset(Dimension center, CyNetworkView view, CyNode node) {
		View<CyNode>nView = view.getNodeView(node);
		double xOffset = nView.getVisualProperty(xLoc) - center.getWidth();
		double yOffset = nView.getVisualProperty(yLoc) - center.getHeight();
		return getDim(xOffset, yOffset);
	}

	/**
   * Get the saved location of the group node
   */
	private Dimension getLocation(CyNetwork network, CyGroup group) {
		CyNetwork groupNetwork = group.getGroupNetwork();
		CyTable netTable = groupNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		return getNodeLocation(netTable, network, groupNetwork.getSUID());
	}

	/**
   * Get the saved offset of a member node
   */
	private Dimension getOffsetLocation(CyNetwork network, CyGroup group, CyNode node, Dimension center) {
		CyTable nodeTable = group.getGroupNetwork().getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		Dimension d = getNodeLocation(nodeTable, network, node.getSUID());
		d.setSize(d.getWidth()+center.getWidth(), d.getHeight()+center.getHeight());
		return d;
	}

	private void updateNodeOffset(CyNetwork network, CyGroup group, CyNode node, Dimension offset) {
		CyTable nodeTable = group.getGroupNetwork().getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		updateNodeLocation(nodeTable, network, node.getSUID(), offset);
	}

	private void updateGroupLocation(CyNetwork network, CyGroup group, Dimension offset) {
		CyNetwork groupNetwork = group.getGroupNetwork();
		CyTable netTable = groupNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		updateNodeLocation(netTable, network, groupNetwork.getSUID(), offset);
	}

	private void moveNode(CyNetworkView view, CyNode node, Dimension location) {
		View<CyNode>nView = view.getNodeView(node);
		nView.setVisualProperty(xLoc, location.getWidth());
		nView.setVisualProperty(yLoc, location.getHeight());
	}

	/**
	 * Saves the location of a group node or a member node as part of a list associated with
	 * a particular network.  The idea is that the group node or member nodes might have different
	 * locations in different networks, so we need to keep a mapping.  We store all of this information
	 * in the CySubnetwork that represents the group so that it will get serialized properly
	 *
	 * @param table group node locations are stored in the network table, 
	 *              member nodes are in the node table
	 * @param network the network we're updating the location for
	 * @param suid if this is a group node, suid will be the SUID of the 
	 *             group subnetwork, otherwise it's the SUID of the node.
	 * @param location the location of the node to save
	 */
	private void updateNodeLocation(CyTable table, CyNetwork network, Long suid, Dimension location) {
		List<Long> networkSUIDs;
		List<Double> xLocations;
		List<Double> yLocations;

		// Make sure our three columns are available
		createListColumnIfNeeded(table, NETWORK_SUID_ATTR, Long.class);
		createListColumnIfNeeded(table, X_LOCATION_ATTR, Double.class);
		createListColumnIfNeeded(table, Y_LOCATION_ATTR, Double.class);

		CyRow row = table.getRow(suid);
		networkSUIDs = getList(row, NETWORK_SUID_ATTR, Long.class);
		xLocations = getList(row, X_LOCATION_ATTR, Double.class);
		yLocations = getList(row, Y_LOCATION_ATTR, Double.class);

		int index = networkSUIDs.indexOf(network.getSUID());
		if (index == -1) {
			networkSUIDs.add(network.getSUID());
			xLocations.add(location.getWidth());
			yLocations.add(location.getHeight());
		} else {
			xLocations.set(index, location.getWidth());
			yLocations.set(index, location.getHeight());
		}

		row.set(NETWORK_SUID_ATTR, networkSUIDs);
		row.set(X_LOCATION_ATTR, xLocations);
		row.set(Y_LOCATION_ATTR, yLocations);
	}

	private Dimension getNodeLocation(CyTable table, CyNetwork network, Long suid) {
		if (!table.rowExists(suid))
			return null;

		CyRow row = table.getRow(suid);
		List<Long> networkSUIDs = getList(row, NETWORK_SUID_ATTR, Long.class);
		List<Double>xLocations = getList(row, X_LOCATION_ATTR, Double.class);
		List<Double>yLocations = getList(row, Y_LOCATION_ATTR, Double.class);
		int index = networkSUIDs.indexOf(network.getSUID());
		if (index == -1)
			return null;
		return getDim(xLocations.get(index), yLocations.get(index));
	}

	private void createColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createColumn(name, type, false);
	}

	private void createListColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createListColumn(name, type, false);
	}

	private <T> List<T> getList(CyRow row, String column, Class<T> type) {
		List<T> l = row.getList(column, type);
		if (l == null)
			l = new ArrayList<T>();
		return l;
	}

	private Dimension getDim(double x, double y) {
		Dimension d = new Dimension();
		d.setSize(x, y);
		return d;
	}
}
