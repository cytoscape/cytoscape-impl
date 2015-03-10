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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
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
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Handle the view portion of group collapse/expand
 */
public class ViewUtils {
	// Middle of the node in the X direction
	private static final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
	// Middle of the node in the Y direction
	private static final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;
	private static final String X_LOCATION_ATTR = "__xLocation";
	private static final String Y_LOCATION_ATTR = "__yLocation";
	private static final String NETWORK_SUID_ATTR = "__groupNetworks.SUID";
	private static final String ISMEMBER_EDGE_ATTR = "__isMemberEdge";
	private static final double Z_OFFSET = -100.0; // Offset for group nodes
	private static final int COMPOUND_NODE_TRANSPARENCY = 50;

	public static void applyStyle(final Collection<? extends CyIdentifiable> elements,
	                              final Collection<CyNetworkView> networkViews,
	                              final VisualMappingManager cyStyleManager) {
		if (elements == null || networkViews == null)
			return;
		
		for (CyNetworkView netView : networkViews) {
			final CyNetwork net = netView.getModel();
			final VisualStyle style = cyStyleManager.getVisualStyle(netView);
			
			for (CyIdentifiable entry : elements) {
				View<? extends CyIdentifiable> view = null;
				
				if (entry instanceof CyNode)
					view = netView.getNodeView((CyNode)entry);
				else if (entry instanceof CyEdge)
					view = netView.getEdgeView((CyEdge)entry);
				
				if (view != null)
					style.apply(net.getRow(entry), view);
			}
		}
	}

	// Careful!  In Cytoscape the X and Y locations are actually the
	// center of the node!
	public static void styleCompoundNode(CyGroup group, CyNetworkView view,
	                                     CyGroupManager groupManager,
	                                     VisualMappingManager cyStyleManager) {
		double z = Z_OFFSET;

		List<CyGroup> restyleList = new ArrayList<>();

		// First find out if any of our members are compound nodes
		for (CyNode node: group.getNodeList()) {
			if (groupManager.isGroup(node, view.getModel())) {
				// Yes -- git it's Z value
				View<CyNode> nv = view.getNodeView(node);
				if (nv != null) {
					z = nv.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)+Z_OFFSET;

					// Add it to the list to restyle
					restyleList.add(groupManager.getGroup(node, view.getModel()));
				}
			}
		}
		// TODO: Go through the list of our children and update z?
		for (CyNode node: group.getNodeList()) {
			View<CyNode> nv = view.getNodeView(group.getGroupNode());

		}

		View<CyNode> groupView = view.getNodeView(group.getGroupNode());

		/*
		System.out.println("styleCompoundNode: group node current at: "+
			groupView.getVisualProperty(xLoc)+","+
			groupView.getVisualProperty(yLoc));
		*/
		Rectangle2D bounds = calculateBounds(group.getNodeList(), view);
		// System.out.println("styleCompoundNode: bounds = "+bounds);

		double xLocation = bounds.getX()+bounds.getWidth()/2;
		double yLocation = bounds.getY()+bounds.getHeight()/2;

		groupView.setVisualProperty(xLoc, xLocation);
		groupView.setVisualProperty(yLoc, yLocation);
		groupView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, bounds.getHeight());
		groupView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, bounds.getWidth());
		groupView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, 
		                            NodeShapeVisualProperty.ROUND_RECTANGLE);
		groupView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, 
		                            COMPOUND_NODE_TRANSPARENCY); 

		groupView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, z);
		updateGroupLocation(view.getModel(), group, xLocation, yLocation); 

		// OK, now restyle any child compound nodes
		/*
		for (CyGroup childGroup: restyleList) {
			styleCompoundNode(childGroup, view, groupManager, cyStyleManager);
		}
		*/
	}

	public static void unStyleCompoundNode(CyGroup group, CyNetworkView view,
	                                       VisualMappingManager cyStyleManager) {
	}
	
	public static Dimension calculateCenter(CyNetworkView view, List<CyNode> nodeList) {
		double xCenter = 0.0d;
		double yCenter = 0.0d;
		CyNetwork network = view.getModel();

		int size = 0;
		for (CyNode node: nodeList) {
			if (!network.containsNode(node)) continue;
			View<CyNode>nView = view.getNodeView(node);
			if (nView == null) continue;
			size++;
			xCenter += (nView.getVisualProperty(xLoc));
			yCenter += (nView.getVisualProperty(yLoc));
			
		}
		return getDim(xCenter/size, yCenter/size);
	}

	public static void moveNodes(CyGroup group, CyNetworkView view, Dimension center) {
		CyNetwork net = view.getModel();
		for (CyNode node: group.getNodeList()) {
			if (net.containsNode(node)) {
				Dimension location = getOffsetLocation(net, group, node, center);
				moveNode(view, node, location);
			}
		}
	}

	public static Dimension calculateOffset(Dimension center, CyNetworkView view, CyNode node) {
		View<CyNode>nView = view.getNodeView(node);
		double xOffset = nView.getVisualProperty(xLoc) - center.getWidth();
		double yOffset = nView.getVisualProperty(yLoc) - center.getHeight();
		return getDim(xOffset, yOffset);
	}

	/**
   * Get the saved location of the group node
   */
	public static Dimension getLocation(CyNetwork network, CyGroup group) {
		CyNetwork groupNetwork = group.getGroupNetwork();
		CyTable netTable = groupNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		return getNodeLocation(netTable, network, groupNetwork.getSUID());
	}

	/**
   * Get the saved offset of a member node
   */
	public static Dimension getOffsetLocation(CyNetwork network, CyGroup group, 
	                                          CyNode node, Dimension center) {
		CyTable nodeTable = group.getGroupNetwork().getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		Dimension d = getNodeLocation(nodeTable, network, node.getSUID());
		d.setSize(d.getWidth()+center.getWidth(), d.getHeight()+center.getHeight());
		return d;
	}

	public static void updateNodeOffset(CyNetwork network, CyGroup group, CyNode node, Dimension offset) {
		CyTable nodeTable = group.getGroupNetwork().getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		updateNodeLocation(nodeTable, network, node.getSUID(), offset);
	}

	public static void updateGroupLocation(CyNetwork network, CyGroup group, 
	                                       double x, double y) {
		Dimension offset = getDim(x, y);
		updateGroupLocation(network, group, offset);
	}

	public static void updateGroupLocation(CyNetwork network, CyGroup group, Dimension offset) {
		CyNetwork groupNetwork = group.getGroupNetwork();
		CyTable netTable = groupNetwork.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		updateNodeLocation(netTable, network, groupNetwork.getSUID(), offset);
	}

	public static void moveNode(CyNetworkView view, CyNode node, Dimension location) {
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
	public static void updateNodeLocation(CyTable table, CyNetwork network, 
	                                      Long suid, Dimension location) {
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

	public static Dimension getNodeLocation(CyTable table, CyNetwork network, Long suid) {
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

	public static void createColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createColumn(name, type, false);
	}

	public static void createListColumnIfNeeded(CyTable table, String name, Class type) {
		if (table.getColumn(name) == null)
			table.createListColumn(name, type, false);
	}

	public static <T> List<T> getList(CyRow row, String column, Class<T> type) {
		List<T> l = row.getList(column, type);
		if (l == null)
			l = new ArrayList<T>();
		return l;
	}

	public static Dimension getDim(double x, double y) {
		Dimension d = new Dimension();
		d.setSize(x, y);
		return d;
	}

	public static Rectangle2D calculateBounds(List<CyNode> nodes, CyNetworkView view) {
		double x1 = Double.POSITIVE_INFINITY, y1 = Double.POSITIVE_INFINITY;
	 	double x2 = Double.NEGATIVE_INFINITY, y2 = Double.NEGATIVE_INFINITY;
		for (CyNode node: nodes) {
			View<CyNode>nv = view.getNodeView(node);
			if (nv == null) continue;

			double nw = nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			double nh = nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
			double nx = nv.getVisualProperty(xLoc) - nw/2.0;
			double ny = nv.getVisualProperty(yLoc) - nh/2.0;
			// double size = nv.getVisualProperty(BasicVisualLexicon.NODE_SIZE);
			/*
			System.out.println("Bounds for node "+node+" are "+nx+","+ny+"-"+(nx+nw)+","+(ny+nh));
			System.out.println("Location = "+nv.getVisualProperty(xLoc)+","+nv.getVisualProperty(yLoc));
			System.out.println("Size = "+nw+"x"+nh);
			*/

			x1 = Math.min(x1, nx);
			y1 = Math.min(y1, ny);
			// System.out.println("Min bounds = "+x1+","+y1);

			x2 = Math.max(x2, nx+nw);
			y2 = Math.max(y2, ny+nh);
			// System.out.println("Max bounds = "+x2+","+y2);
		}

		return new Rectangle2D.Double(x1-5.0, y1-5.0, (x2-x1)+10.0, (y2-y1)+10.0);
	}
}
