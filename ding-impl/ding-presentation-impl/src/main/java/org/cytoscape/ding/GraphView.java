package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.ding.impl.DEdgeView;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

/**
 * Ding version of network view.
 */
public interface GraphView {

	public static int NODE_X_POSITION = 0;
	public static int NODE_Y_POSITION = 1;
	public static int NODE_SHAPE = 2;
	public static int NODE_PAINT = 3;
	public static int NODE_SELECTION_PAINT = 4;
	public static int NODE_BORDER_PAINT = 5;
	public static int NODE_BORDER_WIDTH = 6;
	public static int NODE_WIDTH = 7;
	public static int NODE_HEIGHT = 8;
	public static int NODE_LABEL = 9;
	public static int NODE_Z_POSITION = 10;

	public static int SOURCE_INDEX = 0;
	public static int TARGET_INDEX = 1;
	public static int EDGE_WIDTH = 2;
	public static int EDGE_LINE_TYPE = 3;
	public static int EDGE_PAINT = 4;
	public static int EDGE_SELECTION_PAINT = 5;
	public static int EDGE_SOURCE_END_TYPE = 6;
	public static int EDGE_SOURCE_END_PAINT = 7;
	public static int EDGE_SOURCE_END_SELECTED_PAINT = 8;
	public static int EDGE_TARGET_END_TYPE = 9;
	public static int EDGE_TARGET_END_PAINT = 10;
	public static int EDGE_TARGET_END_SELECTED_PAINT = 11;

	public CyNetwork getNetwork();

	public boolean nodeSelectionEnabled();

	public boolean edgeSelectionEnabled();

	public void enableNodeSelection();

	public void disableNodeSelection();

	public void enableEdgeSelection();

	public void disableEdgeSelection();

	/**
	 * @return An int array of the graph perspective indices of the selected nodes.
	 */
	public long[] getSelectedNodeIndices();

	/**
	 * @return A list of the selected NodeView.
	 */
	public List<CyNode> getSelectedNodes();

	/**
	 * @return An int array of the graph perspective indices of the selected edges.
	 */
	public long[] getSelectedEdgeIndices();

	/**
	 * @return A list of the selected EdgeView.
	 */
	public List<CyEdge> getSelectedEdges();

	/**
	 * @param paint The new Paint for the background.
	 */
	public void setBackgroundPaint(Paint paint);

	/**
	 * @return the backgroundPaint
	 */
	public Paint getBackgroundPaint();

	/**
	 * @return the java.awt.Component that can be added to most screen things
	 */
	public Component getComponent();

	/**
	 * @param node The CyNode from which the new NodeView must be created.
	 * @return The newly created NodeView.
	 */
	public NodeView addNodeView(CyNode node);

	/**
	 * @param edge The CyEdge from which the new EdgeView must be created.
	 * @return The newly created EdgeView.
	 */
	public EdgeView addEdgeView(CyEdge edge);

	/**
	 * This will entirely remove a NodeView/EdgeView from the GraphView. This is
	 * different than
	 * @see #hideGraphObject as that method simply stops showing the node/edge.
	 *      This method will destroy the object. It will be returned though, so
	 *      that a reference can be kept for undo purposes.
	 * 
	 * @param nodeView The NodeView to be removed.
	 * @return The NodeView object that was removed.
	 */
	public NodeView removeNodeView(NodeView nodeView);

	/**
	 * This will entirely remove a NodeView/EdgeView from the GraphView. This is
	 * different than
	 * @see #hideGraphObject as that method simply stops showing the node/edge.
	 *      This method will destroy the object. It will be returned though, so
	 *      that a reference can be kept for undo purposes.
	 * 
	 * @param node The CyNode of the NodeView to be removed.
	 * @return The NodeView that was removed.
	 */
	public NodeView removeNodeView(CyNode node);

	/**
	 * This will entirely remove a NodeView/EdgeView from the GraphView based on a specified node SUID.
	 * This is different than
	 * @see #hideGraphObject as that method simply stops showing the node/edge.
	 *      This method will destroy the object. It will be returned though, so
	 *      that a reference can be kept for undo purposes.
	 *      
	 * @param suid The SUID of the CyEdge associated with the NodeView to be removed.
	 * @return The NodeView object that was removed.
	 */
	public NodeView removeNodeView(long suid);

	/**
	 * This will entirely remove a NodeView/EdgeView from the GraphView. This is
	 * different than
	 * @see #hideGraphObject as that method simply stops showing the node/edge.
	 *      This method will destroy the object. It will be returned though, so
	 *      that a reference can be kept for undo purposes.
	 * 
	 * @param edgeView The EdgeView to be removed.
	 * @return The EdgeView object that was removed.
	 */
	public EdgeView removeEdgeView(EdgeView edgeView);

	/**
	 * This will entirely remove a NodeView/EdgeView from the GraphView. This is
	 * different than
	 * @see #hideGraphObject as that method simply stops showing the node/edge.
	 *      This method will destroy the object. It will be returned though, so
	 *      that a reference can be kept for undo purposes.
	 * 
	 * @param edge The CyEdge of the EdgeView to be removed.
	 * @return The EdgeView that was removed.
	 */
	public EdgeView removeEdgeView(CyEdge edge);

	/**
	 * This will entirely remove an EdgeView from the GraphView based on a specified edge SUID.
	 * This is different than
	 * @see #hideGraphObject as that method simply stops showing the node/edge.
	 *      This method will destroy the object. It will be returned though, so
	 *      that a reference can be kept for undo purposes.
	 */
	public EdgeView removeEdgeView(long suid);

	/**
	 * @return The Unique Identifier of this GraphView
	 */
	public Long getIdentifier();

	/**
	 * @param newIdentifier The New Identifier for this GraphView.
	 */
	public void setIdentifier(Long newIdentifier);

	/**
	 * @return The Current Zoom Level
	 */
	public double getZoom();

	/**
	 * @param zoom
	 *            The New ZoomLevel
	 */
	public void setZoom(double zoom);

	/**
	 * Fits all Viewable elements onto the Graph
	 */
	public void fitContent();

	/**
	 * Do a global redraw of the entire canvas
	 */
	public void updateView();

	/**
	 * nodeViewsIterator only returns the NodeViews that are explicitly
	 * associated with this GraphView
	 */
	public Iterator<NodeView> getNodeViewsIterator();

	/**
	 * @return the number of node views present
	 */
	public int getNodeViewCount();

	/**
	 * @return The number of EdgeViews present.
	 */
	public int getEdgeViewCount();

	/**
	 * @param node The Node whose view is requested.
	 * 
	 * @return The NodeView of the given Node.
	 */
	public DNodeView getDNodeView(CyNode node);

	/**
	 * @param suid The SUID of the CyNode whose view is requested.
	 * @return The NodeView of the given Node.
	 */
	public DNodeView getDNodeView(long suid);

	/**
	 * Return All of the EdgeViews in this GraphView.
	 */
	public List<EdgeView> getEdgeViewsList();

	/**
	 * Note that this will return a list of Edge objects, the other one will return indices.
	 * 
	 * @return The list of EdgeViews connecting these two nodes. Possibly null.
	 */
	public List<EdgeView> getEdgeViewsList(CyNode oneNode, CyNode otherNode);

	/**
	 * @return A List of edge views.
	 */
	public List<EdgeView> getEdgeViewsList(long oneNodeSUID, long otherNodeSUID, boolean includeUndirectedEdges);

	/**
	 * @param suid The SUID of the CyEdge whose view is requested.
	 * @return The EdgeView that corresponds to the given SUID.
	 */
	public DEdgeView getDEdgeView(long suid);

	/**
	 * Return all of the EdgeViews in this GraphView.
	 */
	public Iterator<EdgeView> getEdgeViewsIterator();

	/**
	 * @return the EdgeView that corresponds to the given Edge.
	 */
	public DEdgeView getDEdgeView(CyEdge edge);

	/**
	 * @return the number of edges
	 */
	public int edgeCount();

	/**
	 * @return The number of Nodes, same number as the perspective.
	 */
	public int nodeCount();

	/**
	 * use this to hide a node or edge
	 */
	public boolean hideGraphObject(Object object);

	/**
	 * use this to show a node or edge
	 */
	public boolean showGraphObject(Object object);

	/**
	 * <B> Warning!!!!!!!</B><BR>
	 * Only to be used for homogenous groups!!!!
	 */
	public boolean hideGraphObjects(List<? extends GraphViewObject> objects);

	/**
	 * <B> Warning!!!!!!!</B><BR>
	 * Only to be used for homogenous groups!!!!
	 */
	public boolean showGraphObjects(List<? extends GraphViewObject> objects);

	/**
	 * Sets the Title of this View
	 */
	public void setTitle(String title);

	/**
	 * Returns the Title of this View
	 */
	public String getTitle();

	// public void redrawGraph(boolean layout, boolean vizmap);
	// public void setVisualStyle(String VSName);
	// public VisualStyle getVisualStyle();

	public void setPrintingTextAsShape(boolean textAsShape);

	public void setGraphLOD(GraphLOD lod);

	public GraphLOD getGraphLOD();

	public void fitSelected();

	// public void addTransferComponent(JComponent comp);

	// for printing
	public void setBounds(int x, int y, int width, int height);

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex);

	public void print(Graphics g);

	public void printNoImposter(Graphics g);

	public Printable createPrintable();

	// ??
	public Point2D getCenter();

	public void setCenter(double x, double y);

	public void setSize(Dimension d);

	// for export
	public Image createImage(int width, int height, double shrink);

	// used by editor
	public NodeView getPickedNodeView(Point2D pt);

	public EdgeView getPickedEdgeView(Point2D pt);

	public void xformComponentToNodeCoords(double[] coords);

	public void addMouseListener(MouseListener m);

	public void addMouseMotionListener(MouseMotionListener m);

	public void addKeyListener(KeyListener k);

	public void removeMouseListener(MouseListener m);

	public void removeMouseMotionListener(MouseMotionListener m);

	public void removeKeyListener(KeyListener k);

	public CyNetworkView getViewModel();
}
