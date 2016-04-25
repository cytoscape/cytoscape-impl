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
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
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
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.ModelUtils;

/**
 * Handle the view portion of group collapse/expand
 */
public class ViewUtils {
	// Middle of the node in the X direction
	private static final VisualProperty<Double> xLoc = BasicVisualLexicon.NODE_X_LOCATION;
	// Middle of the node in the Y direction
	private static final VisualProperty<Double> yLoc = BasicVisualLexicon.NODE_Y_LOCATION;
	// Middle of the node in the Z direction
	private static final VisualProperty<Double> zLoc = BasicVisualLexicon.NODE_Z_LOCATION;
	private static final String X_LOCATION_ATTR = "__xLocation";
	private static final String Y_LOCATION_ATTR = "__yLocation";
	private static final String NETWORK_SUID_ATTR = "__groupNetworks.SUID";
	private static final String ISMEMBER_EDGE_ATTR = "__isMemberEdge";
	private static final double Z_OFFSET = -100.0; // Offset for group nodes
	private static final int COMPOUND_NODE_TRANSPARENCY = 50;

	private ViewUtils() {
	}

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
	                                     VisualMappingManager cyStyleManager,
	                                     GroupViewType viewType) {
		double z = Z_OFFSET;
		if (viewType.equals(GroupViewType.SINGLENODE))
			z = -Z_OFFSET;

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
		// for (CyNode node: group.getNodeList()) {
		// 	View<CyNode> nv = view.getNodeView(group.getGroupNode());
		//
		// }

		View<CyNode> groupView = view.getNodeView(group.getGroupNode());

		// Get the visual lexicon
		VisualLexicon lex = getVisualLexicon((CyGroupManagerImpl)groupManager, view);
		VisualProperty<?> paddingProperty = lex.lookup(CyNode.class, "COMPOUND_NODE_PADDING");
		double padding = 5.0;
		if (paddingProperty != null)
			padding = (Double)groupView.getVisualProperty(paddingProperty);

		VisualProperty<?> shapeProperty = lex.lookup(CyNode.class, "COMPOUND_NODE_SHAPE");
		NodeShape shape = NodeShapeVisualProperty.ROUND_RECTANGLE;
		if (shapeProperty != null)
			shape = (NodeShape)groupView.getVisualProperty(shapeProperty);

		if (groupView != null) {

			// System.out.println("styleCompoundNode: group "+group+" currently at: "+
			// 	groupView.getVisualProperty(xLoc)+","+
			// 	groupView.getVisualProperty(yLoc));
			Rectangle2D bounds = calculateBounds(group.getNodeList(), view, padding);
			double height = bounds.getHeight();
			double width = bounds.getWidth();

			// System.out.println("styleCompoundNode: bounds = "+bounds);
			//
			double xLocation = bounds.getX()+width/2.0;
			double yLocation = bounds.getY()+height/2.0;
	
			// Adjust bounds for some of the shapes
			if (shape.equals(NodeShapeVisualProperty.ELLIPSE)||
			    shape.equals(NodeShapeVisualProperty.HEXAGON)||
					shape.equals(NodeShapeVisualProperty.OCTAGON)) {
				// Note that in general, the correct forumla is height * Math.sqrt(2), but
				// I'm fudging a little since the correct formula only applies when the
				// nodes are at the bounding box
				height = height*Math.sqrt(1.5);
				width = width*Math.sqrt(1.5);
			} else if(shape.equals(NodeShapeVisualProperty.DIAMOND)) {
				height = height*Math.sqrt(2);
				width = width*Math.sqrt(2);
			}
	
			// System.out.println("Moving to "+xLocation+","+yLocation);
			// System.out.println("Resizing to "+bounds.getWidth()+"x"+bounds.getHeight());
			groupView.setVisualProperty(xLoc, xLocation);
			groupView.setVisualProperty(yLoc, yLocation);
			groupView.setLockedValue(BasicVisualLexicon.NODE_HEIGHT, height);
			groupView.setLockedValue(BasicVisualLexicon.NODE_WIDTH, width);
			if (!viewType.equals(GroupViewType.SINGLENODE)) {
				groupView.setLockedValue(BasicVisualLexicon.NODE_SHAPE, shape);
				groupView.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 
				                         COMPOUND_NODE_TRANSPARENCY); 
			} else {
				groupView.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 10); 
				groupView.setLockedValue(BasicVisualLexicon.NODE_SHAPE,
				                         NodeShapeVisualProperty.RECTANGLE);
				groupView.setLockedValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.0);
			}
	
			groupView.setLockedValue(BasicVisualLexicon.NODE_Z_LOCATION, z);

			updateGroupLocation(view.getModel(), group, xLocation, yLocation); 
		}

		// OK, now restyle any child compound nodes
		/*
		for (CyGroup childGroup: restyleList) {
			styleCompoundNode(childGroup, view, groupManager, cyStyleManager);
		}
		*/
	}

	public static void unStyleCompoundNode(CyGroup group, CyNetworkView view,
	                                       VisualMappingManager cyStyleManager) {
		View<CyNode> groupView = view.getNodeView(group.getGroupNode());

		if (groupView != null) {
			groupView.clearValueLock(BasicVisualLexicon.NODE_HEIGHT);
			groupView.clearValueLock(BasicVisualLexicon.NODE_WIDTH);
			groupView.clearValueLock(BasicVisualLexicon.NODE_TRANSPARENCY); 
			groupView.clearValueLock(BasicVisualLexicon.NODE_SHAPE);
			groupView.clearValueLock(BasicVisualLexicon.NODE_BORDER_WIDTH);
			groupView.clearValueLock(BasicVisualLexicon.NODE_Z_LOCATION);
		}

		cyStyleManager.getVisualStyle(view).apply(view.getModel().getRow(group.getGroupNode()), groupView);
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
		return getDim(xCenter/(double)size, yCenter/(double)size);
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
		Dimension dim =  getNodeLocation(netTable, network, groupNetwork.getSUID());
		// System.out.println("Group node should be at: "+dim);
		return dim;
	}

	/**
   * Get the saved offset of a member node
   */
	public static Dimension getOffsetLocation(CyNetwork network, CyGroup group, 
	                                          CyNode node, Dimension center) {
		CyTable nodeTable = group.getGroupNetwork().getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		Dimension d = getNodeLocation(nodeTable, network, node.getSUID());
		if (d != null && center != null) 
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
		// System.out.println("Updating group node location to: "+offset);
		// if (offset.getWidth() == 0.0 && offset.getHeight() == 0.0)
		// 	Thread.dumpStack();
		updateNodeLocation(netTable, network, groupNetwork.getSUID(), offset);
	}

	public static void moveNode(CyNetworkView view, CyNode node, Dimension location) {
		View<CyNode>nView = view.getNodeView(node);
		// System.out.println("Moving node to "+location.getWidth()+","+location.getHeight());
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
		ModelUtils.createListColumnIfNeeded(table, NETWORK_SUID_ATTR, Long.class);
		ModelUtils.createListColumnIfNeeded(table, X_LOCATION_ATTR, Double.class);
		ModelUtils.createListColumnIfNeeded(table, Y_LOCATION_ATTR, Double.class);

		CyRow row = table.getRow(suid);
		networkSUIDs = ModelUtils.getList(row, NETWORK_SUID_ATTR, Long.class);
		xLocations = ModelUtils.getList(row, X_LOCATION_ATTR, Double.class);
		yLocations = ModelUtils.getList(row, Y_LOCATION_ATTR, Double.class);

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
		List<Long> networkSUIDs = ModelUtils.getList(row, NETWORK_SUID_ATTR, Long.class);
		List<Double>xLocations = ModelUtils.getList(row, X_LOCATION_ATTR, Double.class);
		List<Double>yLocations = ModelUtils.getList(row, Y_LOCATION_ATTR, Double.class);
		int index = networkSUIDs.indexOf(network.getSUID());
		if (index >= 0)
			return getDim(xLocations.get(index), yLocations.get(index));
		else if (xLocations.size() > 0) {
			// Well, do what we can...
			return getDim(xLocations.get(0), yLocations.get(0));
		} else
			return null;
	}

	public static void copyNodeLocations(CyGroup group, CyNetworkView source, CyNetworkView dest) {
		for (CyNode node: group.getNodeList()) {
			View<CyNode>nodeView = source.getNodeView(node);
			View<CyNode>nNodeView = dest.getNodeView(node);
			dest.setVisualProperty(xLoc, source.getVisualProperty(xLoc));
			dest.setVisualProperty(yLoc, source.getVisualProperty(yLoc));
			dest.setVisualProperty(zLoc, source.getVisualProperty(zLoc));
		}
	}

	public static Dimension getDim(double x, double y) {
		Dimension d = new Dimension();
		d.setSize(x, y);
		return d;
	}

	public static Rectangle2D calculateBounds(List<CyNode> nodes, CyNetworkView view, double padding) {
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

		return new Rectangle2D.Double(x1-padding, y1-padding, (x2-x1)+(padding*2), (y2-y1)+(padding*2));
	}

	public static VisualLexicon getVisualLexicon(final CyGroupManagerImpl groupMgr, final CyNetworkView netView) {
		final RenderingEngineManager rendererMgr = groupMgr.getService(RenderingEngineManager.class);
		final Collection<RenderingEngine<?>> renderingEngines = rendererMgr.getRenderingEngines(netView);

		if (renderingEngines != null && !renderingEngines.isEmpty())
			return renderingEngines.iterator().next().getVisualLexicon();

		return rendererMgr.getDefaultVisualLexicon();
	}
}
