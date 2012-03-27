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
import java.util.Collection;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;

import org.cytoscape.view.model.CyNetworkView;
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
	private final CyNetworkViewManager cyNetworkViewManager;
	private final VisualMappingManager cyStyleManager;
	private static final Logger logger = LoggerFactory.getLogger(GroupViewCollapseHandler.class);
	private static final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
	private static final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;

	private static final String X_OFFSET_ATTR = "__xOffset";
	private static final String Y_OFFSET_ATTR = "__yOffset";
	private static final String X_LOCATION_ATTR = "__xLocation";
	private static final String Y_LOCATION_ATTR = "__yLocation";

	/**
	 * 
	 * @param cyEventHelper
	 */
	public GroupViewCollapseHandler(final CyGroupManager groupManager,
	                                final CyNetworkViewManager viewManager,
	                                final VisualMappingManager styleManager) {
		this.cyGroupManager = groupManager;
		this.cyNetworkViewManager = viewManager;
		this.cyStyleManager = styleManager;
	}

	public void handleEvent(GroupAboutToCollapseEvent e) {
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		CyRootNetwork rootNetwork = group.getRootNetwork();
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		if (e.collapsing()) {
			// Calculate the center position of all of the
			// member nodes
			Dimension center = calculateCenter(view, group.getNodeList());

			// Save it in the groupNode attribute
			updateGroupLocation(rootNetwork, group.getGroupNode(), center);

			// For each member node,
			// 	calculate the offset for each member node from the center
			// 	save it in the node's attribute
			for (CyNode node: group.getNodeList()) {
				Dimension offset = calculateOffset(center, view, node);
				updateNodeOffset(rootNetwork, node, offset);
			}
		} else {
			// Get the current position of the groupNode
			View<CyNode>nView = view.getNodeView(group.getGroupNode());
			double x = nView.getVisualProperty(xLoc);
			double y = nView.getVisualProperty(yLoc);
			// Save it in the groupNode attribute
			updateGroupLocation(rootNetwork, group.getGroupNode(), getDim(x,y));
		}
	}

	public void handleEvent(GroupCollapsedEvent e) {
		CyNetwork network = e.getNetwork();
		CyGroup group = e.getSource();
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
		CyRootNetwork rootNetwork = group.getRootNetwork();
		VisualStyle viewStyle = cyStyleManager.getVisualStyle(view);

		if (e.collapsed()) {
			// Get the location to move the group node to
			Dimension d = getLocation(rootNetwork, group.getGroupNode());
			// Move it.
			moveNode(view, group.getGroupNode(), d);
		} else {
			// Get the location of the group node before it went away
			Dimension center = getLocation(rootNetwork, group.getGroupNode());
			// Now, get the offsets for each of the member nodes and move them
			for (CyNode node: group.getNodeList()) {
				Dimension location = getOffsetLocation(rootNetwork, node, center);
				moveNode(view, node, location);
			}
		}
		viewStyle.apply(view);
	}

	private Dimension calculateCenter(CyNetworkView view, List<CyNode> nodeList) {
		double xCenter = 0.0d;
		double yCenter = 0.0d;

		for (CyNode node: nodeList) {
			View<CyNode>nView = view.getNodeView(node);
			double x = nView.getVisualProperty(xLoc);
			double y = nView.getVisualProperty(yLoc);
			xCenter += (nView.getVisualProperty(xLoc)) / nodeList.size();
			yCenter += (nView.getVisualProperty(yLoc)) / nodeList.size();
			
		}
		return getDim(xCenter, yCenter);
	}

	private Dimension calculateOffset(Dimension center, CyNetworkView view, CyNode node) {
		View<CyNode>nView = view.getNodeView(node);
		double xOffset = nView.getVisualProperty(xLoc) - center.getWidth();
		double yOffset = nView.getVisualProperty(yLoc) - center.getHeight();
		return getDim(xOffset, yOffset);
	}

	private Dimension getLocation(CyNetwork network, CyNode node) {
		CyRow nodeRow = network.getRow(node, CyNetwork.HIDDEN_ATTRS);
		double x = nodeRow.get(X_LOCATION_ATTR, Double.class);
		double y = nodeRow.get(Y_LOCATION_ATTR, Double.class);
		return getDim(x,y);
	}

	private Dimension getOffsetLocation(CyNetwork network, CyNode node, Dimension center) {
		CyRow nodeRow = network.getRow(node, CyNetwork.HIDDEN_ATTRS);
		double xLocation = nodeRow.get(X_OFFSET_ATTR, Double.class) + center.getWidth();
		double yLocation = nodeRow.get(Y_OFFSET_ATTR, Double.class) + center.getHeight();
		return getDim(xLocation, yLocation);
	}

	private void updateNodeOffset(CyNetwork network, CyNode node, Dimension offset) {
		CyRow nodeRow = network.getRow(node, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNeeded(nodeRow.getTable(), X_OFFSET_ATTR, Double.class);
		createColumnIfNeeded(nodeRow.getTable(), Y_OFFSET_ATTR, Double.class);
		nodeRow.set(X_OFFSET_ATTR, new Double(offset.getWidth()));
		nodeRow.set(Y_OFFSET_ATTR, new Double(offset.getHeight()));
	}

	private void updateGroupLocation(CyNetwork network, CyNode node, Dimension offset) {
		CyRow nodeRow = network.getRow(node, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNeeded(nodeRow.getTable(), X_LOCATION_ATTR, Double.class);
		createColumnIfNeeded(nodeRow.getTable(), Y_LOCATION_ATTR, Double.class);
		nodeRow.set(X_LOCATION_ATTR, new Double(offset.getWidth()));
		nodeRow.set(Y_LOCATION_ATTR, new Double(offset.getHeight()));
	}

	private void moveNode(CyNetworkView view, CyNode node, Dimension location) {
		View<CyNode>nView = view.getNodeView(node);
		nView.setVisualProperty(xLoc, location.getWidth());
		nView.setVisualProperty(yLoc, location.getHeight());
	}

	private void createColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createColumn(name, type, false);
	}

	private Dimension getDim(double x, double y) {
		Dimension d = new Dimension();
		d.setSize(x, y);
		return d;
	}
}
