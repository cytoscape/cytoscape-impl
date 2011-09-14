/*
 Copyright (c) 2006, 2007, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.ding.impl;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JLayeredPane;

import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.GraphViewChangeListener;
import org.cytoscape.ding.GraphViewObject;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.PrintLOD;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.events.GraphViewChangeListenerChain;
import org.cytoscape.ding.impl.events.GraphViewEdgesHiddenEvent;
import org.cytoscape.ding.impl.events.GraphViewEdgesRestoredEvent;
import org.cytoscape.ding.impl.events.GraphViewEdgesUnselectedEvent;
import org.cytoscape.ding.impl.events.GraphViewNodesHiddenEvent;
import org.cytoscape.ding.impl.events.GraphViewNodesRestoredEvent;
import org.cytoscape.ding.impl.events.GraphViewNodesUnselectedEvent;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.ding.impl.events.ViewportChangeListenerChain;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.Identifiable;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.spacial.SpacialEntry2DEnumerator;
import org.cytoscape.spacial.SpacialIndex2D;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.intr.IntBTree;
import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.IntHash;
import org.cytoscape.util.intr.IntStack;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.EdgeViewsChangedEvent;
import org.cytoscape.view.model.events.EdgeViewsChangedListener;
import org.cytoscape.view.model.events.FitContentEvent;
import org.cytoscape.view.model.events.FitContentEventListener;
import org.cytoscape.view.model.events.FitSelectedEvent;
import org.cytoscape.view.model.events.FitSelectedEventListener;
import org.cytoscape.view.model.events.NetworkViewChangedEvent;
import org.cytoscape.view.model.events.NetworkViewChangedListener;
import org.cytoscape.view.model.events.NodeViewsChangedEvent;
import org.cytoscape.view.model.events.NodeViewsChangedListener;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TunableInterceptor;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DING implementation of the GINY view.
 *
 * Explain relationship to cytoscape.
 *
 * Throughout this code I am assuming that nodes or edges are never removed from
 * the underlying RootGraph. This assumption was made in the old GraphView
 * implementation. Removal from the RootGraph is the only thing that can affect
 * m_drawPersp and m_structPersp that is beyond our control.
 *
 * @author Nerius Landys
 */
public class DGraphView implements RenderingEngine<CyNetwork>, GraphView,
		Printable, NetworkViewChangedListener,
		NodeViewsChangedListener, EdgeViewsChangedListener,
		FitContentEventListener, FitSelectedEventListener {

	private static final Logger logger = LoggerFactory.getLogger(DGraphView.class);
	
	private static enum ZOrder {
		BACKGROUND_PANE, NETWORK_PANE, FOREGROUND_PANE;
		
		int layer() {
			if (this == BACKGROUND_PANE)
				return -30000;

			if (this == NETWORK_PANE)
				return 0;

			if (this == FOREGROUND_PANE)
				return 301;

			return 0;
		}
	}

	static final float DEFAULT_ANCHOR_SIZE = 9.0f;
	static final Paint DEFAULT_ANCHOR_SELECTED_PAINT = Color.red;
	static final Paint DEFAULT_ANCHOR_UNSELECTED_PAINT = Color.black;

	boolean calledFromGetSnapshot = false;

	// Size of snapshot image
	protected static int DEF_SNAPSHOT_SIZE = 400;

	/**
	 * Enum to identify ding canvases - used in getCanvas(Canvas canvasId)
	 */
	public enum Canvas {
		BACKGROUND_CANVAS, NETWORK_CANVAS, FOREGROUND_CANVAS;
	}

	public enum ShapeType {
		NODE_SHAPE, LINE_TYPE, ARROW_SHAPE;
	}

	/**
	 * Common object used for synchronization.
	 */
	final Object m_lock = new Object();

	/**
	 * A common buffer object used to pass information about. X-Y coords of the
	 * minimum bounding box?
	 */
	final float[] m_extentsBuff = new float[4];

	/**
	 * A common general path variable used for holding lots of shapes.
	 */
	final GeneralPath m_path = new GeneralPath();

	/**
	 * The graph model that will be viewed.
	 */
	final CyNetwork networkModel;

	/**
	 * Holds the NodeView data for the nodes that are visible. This will change
	 * as nodes are hidden from the view.
	 */
	final CySubNetwork m_drawPersp;

	/**
	 * Holds all of the NodeViews, regardless of whether they're visualized.
	 */
	// CyNetwork m_structPersp;
	/**
	 * RTree used for querying node positions.
	 */
	SpacialIndex2D m_spacial;

	/**
	 * RTree used for querying Edge Handle positions. Used by DNodeView,
	 * DEdgeView, and InnerCanvas.
	 */
	SpacialIndex2D m_spacialA;

	/**
	 *
	 */
	DNodeDetails m_nodeDetails;

	/**
	 *
	 */
	DEdgeDetails m_edgeDetails;

	/**
	 * Level of detail specific to printing. Not used for rendering.
	 */
	PrintLOD m_printLOD;

	/**
	 *
	 */
	HashMap<Integer, NodeView> m_nodeViewMap;

	/**
	 *
	 */
	HashMap<Integer, EdgeView> m_edgeViewMap;

	/**
	 *
	 */
	Long m_identifier;

	/**
	 *
	 */
	final float m_defaultNodeXMin;

	/**
	 *
	 */
	final float m_defaultNodeYMin;

	/**
	 *
	 */
	final float m_defaultNodeXMax;

	/**
	 *
	 */
	final float m_defaultNodeYMax;

	/**
	 * Ref to network canvas object.
	 */
	InnerCanvas m_networkCanvas;

	/**
	 * Ref to background canvas object.
	 */
	ArbitraryGraphicsCanvas m_backgroundCanvas;

	/**
	 * Ref to foreground canvas object.
	 */
	ArbitraryGraphicsCanvas m_foregroundCanvas;

	/**
	 *
	 */
	boolean m_nodeSelection = true;

	/**
	 *
	 */
	boolean m_edgeSelection = true;

	/**
	 * BTree of selected nodes.
	 */
	final IntBTree m_selectedNodes; // Positive.

	/**
	 * BTree of selected edges.
	 */
	final IntBTree m_selectedEdges; // Positive.

	/**
	 * BTree of selected anchors.
	 */
	final IntBTree m_selectedAnchors;

	/**
	 * State variable for when nodes have moved.
	 */
	boolean m_contentChanged = false;

	/**
	 * State variable for when zooming/panning have changed.
	 */
	boolean m_viewportChanged = false;

	/**
	 * List of listeners.
	 */
	final GraphViewChangeListener[] m_lis = new GraphViewChangeListener[1];

	/**
	 * List of listeners.
	 */
	final ContentChangeListener[] m_cLis = new ContentChangeListener[1];

	/**
	 * List of listeners.
	 */
	final ViewportChangeListener[] m_vLis = new ViewportChangeListener[1];
	/**
	 * ???
	 */
	private final IntHash m_hash = new IntHash();

	/**
	 * Used for holding edge anchors.
	 */
	final float[] m_anchorsBuff = new float[2];

	/**
	 *
	 */
	int m_lastSize = 0;

	/**
	 * Used for caching texture paint.
	 */
	Paint m_lastPaint = null;

	/**
	 * Used for caching texture paint.
	 */
	Paint m_lastTexturePaint = null;
	
	/**
	 * Snapshot of current view.  Will be updated by CONTENT_CHANGED event.
	 * 
	 * This is used by a new nested network feature from 2.7.
	 */
	private BufferedImage snapshotImage;

	/**
	 * Represents current snapshot is latest version or not.
	 */
	private boolean latest;

	Map<NodeViewTaskFactory, Map> nodeViewTFs;
	Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	Map<DropNodeViewTaskFactory, Map> dropNodeViewTFs;
	Map<DropNetworkViewTaskFactory, Map> dropEmptySpaceTFs;

	TunableInterceptor interceptor;
	TaskManager manager;

	// Will be injected.
	private VisualLexicon dingLexicon;

	// This is the view model. This should be immutable.
	final CyNetworkView cyNetworkView;
	
	private final Properties props;


	/**
	 * Creates a new DGraphView object.
	 *
	 * @param perspective
	 *            The graph model that we'll be creating a view for.
	 */
	public DGraphView(final CyNetworkView view, CyTableFactory dataFactory,
			CyRootNetworkFactory cyRoot, UndoSupport undo,
			SpacialIndex2DFactory spacialFactory,
			final VisualLexicon dingLexicon,
			Map<NodeViewTaskFactory, Map> nodeViewTFs,
			Map<EdgeViewTaskFactory, Map> edgeViewTFs,
			Map<NetworkViewTaskFactory, Map> emptySpaceTFs,
			Map<DropNodeViewTaskFactory, Map> dropNodeViewTFs,
			Map<DropNetworkViewTaskFactory, Map> dropEmptySpaceTFs,
			TaskManager manager, CyEventHelper eventHelper,
			CyTableManager tableMgr) {

		if (view == null)
			throw new IllegalArgumentException(
					"Network View Model cannot be null.");
		
		logger.debug("\n\n\n************** DING Presentation *******************\n\n\n");

		this.props = new Properties();
		
		long start = System.currentTimeMillis();
		logger.debug("Phase 1: rendering start.");
		networkModel = view.getModel();
		cyNetworkView = view;

		this.dingLexicon = dingLexicon;
		this.nodeViewTFs = nodeViewTFs;
		this.edgeViewTFs = edgeViewTFs;
		this.emptySpaceTFs = emptySpaceTFs;
		this.dropNodeViewTFs = dropNodeViewTFs;
		this.dropEmptySpaceTFs = dropEmptySpaceTFs;
		this.manager = manager;

		final CyTable nodeCAM = dataFactory.createTable("node view", Identifiable.SUID, Long.class, false, false);
		tableMgr.addTable(nodeCAM);
		nodeCAM.createColumn("hidden", Boolean.class, false);
		tableMgr.getTableMap(CyNode.class, networkModel).put("VIEW", nodeCAM);

		final CyTable edgeCAM = dataFactory.createTable("edge view", Identifiable.SUID, Long.class, false, false);
		tableMgr.addTable(edgeCAM);
		edgeCAM.createColumn("hidden", Boolean.class, false);
		tableMgr.getTableMap(CyEdge.class, networkModel).put("VIEW", edgeCAM);

		// creating empty subnetworks
		m_drawPersp = cyRoot.convert(networkModel).addSubNetwork();
		eventHelper.silenceEventSource(m_drawPersp);
		m_spacial = spacialFactory.createSpacialIndex2D();
		m_spacialA = spacialFactory.createSpacialIndex2D();
		m_nodeDetails = new DNodeDetails(this);
		m_edgeDetails = new DEdgeDetails(this);
		m_nodeViewMap = new HashMap<Integer, NodeView>();
		m_edgeViewMap = new HashMap<Integer, EdgeView>();
		m_printLOD = new PrintLOD();
		m_defaultNodeXMin = 0.0f;
		m_defaultNodeYMin = 0.0f;
		m_defaultNodeXMax = m_defaultNodeXMin + DNodeView.DEFAULT_WIDTH;
		m_defaultNodeYMax = m_defaultNodeYMin + DNodeView.DEFAULT_HEIGHT;
		m_networkCanvas = new InnerCanvas(m_lock, this, undo);
		m_backgroundCanvas = new ArbitraryGraphicsCanvas(networkModel, this,
				m_networkCanvas, Color.white, true, true);
		addViewportChangeListener(m_backgroundCanvas);
		m_foregroundCanvas = new ArbitraryGraphicsCanvas(networkModel, this,
				m_networkCanvas, Color.white, true, false);
		addViewportChangeListener(m_foregroundCanvas);
		m_selectedNodes = new IntBTree();
		m_selectedEdges = new IntBTree();
		m_selectedAnchors = new IntBTree();

		logger.debug("Phase 2: Canvas created: time = "
				+ (System.currentTimeMillis() - start));

		// from DingNetworkView
		this.title = networkModel.getCyRow().get("name", String.class);

		// Create presentations for the graph objects
		for (CyNode nn : networkModel.getNodeList())
			addNodeView(nn);

		for (CyEdge ee : networkModel.getEdgeList())
			addEdgeView(ee);

		logger.debug("Phase 3: All views created: time = " + (System.currentTimeMillis() - start));
		// read in visual properties from view obj
		// FIXME TODO: this process is not necessary
		// final Collection<VisualProperty<?>> netVPs =
		// rootLexicon.getVisualProperties(NETWORK);
		// for (VisualProperty<?> vp : netVPs)
		// networkVisualPropertySet(cyNetworkView, vp,
		// cyNetworkView.getVisualProperty(vp));

		new FlagAndSelectionHandler(this, eventHelper);

		logger.debug("Phase 4: Everything created: time = " + (System.currentTimeMillis() - start));
	}

	/**
	 * Returns the graph model that this view was created for.
	 *
	 * @return The GraphPerspective that the view was created for.
	 */
	public CyNetwork getGraphPerspective() {
		return networkModel;
	}

	public CyNetwork getNetwork() {
		return networkModel;
	}

	/**
	 * Whether node selection is enabled.
	 *
	 * @return Whether node selection is enabled.
	 */
	public boolean nodeSelectionEnabled() {
		return m_nodeSelection;
	}

	/**
	 * Whether edge selection is enabled.
	 *
	 * @return Whether edge selection is enabled.
	 */
	public boolean edgeSelectionEnabled() {
		return m_edgeSelection;
	}

	/**
	 * Enabling the ability to select nodes.
	 */
	public void enableNodeSelection() {
		synchronized (m_lock) {
			m_nodeSelection = true;
		}
	}

	/**
	 * Disables the ability to select nodes.
	 */
	public void disableNodeSelection() {
		final int[] unselectedNodes;

		synchronized (m_lock) {
			m_nodeSelection = false;
			unselectedNodes = getSelectedNodeIndices();

			if (unselectedNodes.length > 0) {
				// Adding this line to speed things up from O(n*log(n)) to O(n).
				m_selectedNodes.empty();

				for (int i = 0; i < unselectedNodes.length; i++)
					((DNodeView) getNodeView(unselectedNodes[i]))
							.unselectInternal();

				m_contentChanged = true;
			}
		}

		if (unselectedNodes.length > 0) {
			final GraphViewChangeListener listener = m_lis[0];

			if (listener != null) {
				listener.graphViewChanged(new GraphViewNodesUnselectedEvent(
						this, makeNodeList(unselectedNodes, this)));
			}

			// Update the view after listener events are fired because listeners
			// may change something in the graph.
			updateView();
		}
	}

	/**
	 * Enables the ability to select edges.
	 */
	public void enableEdgeSelection() {
		synchronized (m_lock) {
			m_edgeSelection = true;
		}
	}

	/**
	 * Disables the ability to select edges.
	 */
	public void disableEdgeSelection() {
		final int[] unselectedEdges;

		synchronized (m_lock) {
			m_edgeSelection = false;
			unselectedEdges = getSelectedEdgeIndices();

			if (unselectedEdges.length > 0) {
				// Adding this line to speed things up from O(n*log(n)) to O(n).
				m_selectedEdges.empty();

				for (int i = 0; i < unselectedEdges.length; i++)
					((DEdgeView) getEdgeView(unselectedEdges[i]))
							.unselectInternal();

				m_contentChanged = true;
			}
		}

		if (unselectedEdges.length > 0) {
			final GraphViewChangeListener listener = m_lis[0];

			if (listener != null) {
				listener.graphViewChanged(new GraphViewEdgesUnselectedEvent(
						this, makeEdgeList(unselectedEdges, this)));
			}

			// Update the view after listener events are fired because listeners
			// may change something in the graph.
			updateView();
		}
	}

	/**
	 * Returns an array of selected node indices.
	 *
	 * @return An array of selected node indices.
	 */
	public int[] getSelectedNodeIndices() {
		synchronized (m_lock) {
			// all nodes from the btree
			final IntEnumerator elms = m_selectedNodes.searchRange(
					Integer.MIN_VALUE, Integer.MAX_VALUE, false);
			final int[] returnThis = new int[elms.numRemaining()];

			for (int i = 0; i < returnThis.length; i++)
				// GINY requires all node indices to be negative (why?),
				// hence the bitwise complement here.
				returnThis[i] = elms.nextInt();

			return returnThis;
		}
	}

	/**
	 * Returns a list of selected node objects.
	 *
	 * @return A list of selected node objects.
	 */
	public List<CyNode> getSelectedNodes() {
		synchronized (m_lock) {
			// all nodes from the btree
			final IntEnumerator elms = m_selectedNodes.searchRange(
					Integer.MIN_VALUE, Integer.MAX_VALUE, false);
			final ArrayList<CyNode> returnThis = new ArrayList<CyNode>();

			while (elms.numRemaining() > 0)
				// GINY requires all node indices to be negative (why?),
				// hence the bitwise complement here.
				returnThis.add(m_nodeViewMap.get(
						Integer.valueOf(elms.nextInt())).getNodeViewModel().getModel());

			return returnThis;
		}
	}

	/**
	 * Returns an array of selected edge indices.
	 *
	 * @return An array of selected edge indices.
	 */
	public int[] getSelectedEdgeIndices() {
		synchronized (m_lock) {
			final IntEnumerator elms = m_selectedEdges.searchRange(
					Integer.MIN_VALUE, Integer.MAX_VALUE, false);
			final int[] returnThis = new int[elms.numRemaining()];

			for (int i = 0; i < returnThis.length; i++)
				returnThis[i] = elms.nextInt();

			return returnThis;
		}
	}

	/**
	 * Returns a list of selected edge objects.
	 *
	 * @return A list of selected edge objects.
	 */
	public List<CyEdge> getSelectedEdges() {
		synchronized (m_lock) {
			final IntEnumerator elms = m_selectedEdges.searchRange(
					Integer.MIN_VALUE, Integer.MAX_VALUE, false);
			final ArrayList<CyEdge> returnThis = new ArrayList<CyEdge>();

			while (elms.numRemaining() > 0)
				returnThis.add(m_edgeViewMap.get(
						Integer.valueOf(elms.nextInt())).getEdge());

			return returnThis;
		}
	}

	/**
	 * Add GraphViewChangeListener to linked list of GraphViewChangeListeners.
	 * AAAAAARRRGGGGHHHHHH!!!!
	 *
	 * @param l
	 *            GraphViewChangeListener to be added to the list.
	 */
	public void addGraphViewChangeListener(GraphViewChangeListener l) {
		m_lis[0] = GraphViewChangeListenerChain.add(m_lis[0], l);
	}

	/**
	 * Remove GraphViewChangeListener from linked list of
	 * GraphViewChangeListeners. AAAAAARRRGGGGHHHHHH!!!!
	 *
	 * @param l
	 *            GraphViewChangeListener to be removed from the list.
	 */
	public void removeGraphViewChangeListener(GraphViewChangeListener l) {
		m_lis[0] = GraphViewChangeListenerChain.remove(m_lis[0], l);
	}

	/**
	 * Sets the background color on the canvas.
	 *
	 * @param paint
	 *            The Paint (color) to apply to the background.
	 */
	public void setBackgroundPaint(Paint paint) {
		synchronized (m_lock) {
			if (paint instanceof Color) {
				m_backgroundCanvas.setBackground((Color) paint);
				m_contentChanged = true;
			} else {
				logger.debug("DGraphView.setBackgroundPaint(), Color not found!");
			}
		}
	}

	/**
	 * Returns the background color on the canvas.
	 *
	 * @return The background color on the canvas.
	 */
	public Paint getBackgroundPaint() {
		return m_backgroundCanvas.getBackground();
	}

	/**
	 * Returns the InnerCanvas object. The InnerCanvas object is the actual
	 * component that the network is rendered on.
	 *
	 * @return The InnerCanvas object.
	 */
	public Component getComponent() {
		return m_networkCanvas;
	}

	/**
	 * Adds a NodeView object to the GraphView. Creates NodeView if one doesn't
	 * already exist.
	 *
	 * @param nodeInx
	 *            The index of the NodeView object to be added.
	 *
	 * @return The NodeView object that is added to the GraphView.
	 */
	public NodeView addNodeView(CyNode node) {
		NodeView newView = null;

		synchronized (m_lock) {
			newView = addNodeViewInternal(node);

			// View already exists.
			if (newView == null)
				return m_nodeViewMap.get(node.getIndex());

			m_contentChanged = true;
		}

		final GraphViewChangeListener listener = m_lis[0];

		if (listener != null) {
			listener.graphViewChanged(new GraphViewNodesRestoredEvent(this,
					makeList(newView.getNodeViewModel().getModel())));
		}

		return newView;
	}

	/**
	 * Should synchronize around m_lock.
	 */
	private NodeView addNodeViewInternal(CyNode node) {
		final int nodeInx = node.getIndex();
		final NodeView oldView = m_nodeViewMap.get(nodeInx);

		if (oldView != null)
			return null;

		m_drawPersp.addNode(node);

		final View<CyNode> nodeViewModel = cyNetworkView.getNodeView(node);
		final NodeView dNodeView = new DNodeView(dingLexicon, this, nodeInx, nodeViewModel);

		m_nodeViewMap.put(nodeInx, dNodeView);
		m_spacial.insert(nodeInx, m_defaultNodeXMin, m_defaultNodeYMin,
				m_defaultNodeXMax, m_defaultNodeYMax);

		// Set location values from View Model for reflect correct position created by layout algorithms.
		dNodeView.setVisualPropertyValue(MinimalVisualLexicon.NODE_X_LOCATION, nodeViewModel.getVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION));
		dNodeView.setVisualPropertyValue(MinimalVisualLexicon.NODE_Y_LOCATION, nodeViewModel.getVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION));
		
		return dNodeView;
	}

	/**
	 * Adds EdgeView to the GraphView.
	 *
	 * @param edgeInx
	 *            The index of EdgeView to be added.
	 *
	 * @return The EdgeView that was added.
	 */
	public EdgeView addEdgeView(final CyEdge edge) {
		NodeView sourceNode = null;
		NodeView targetNode = null;
		EdgeView dEdgeView = null;
		if (edge == null)
			throw new NullPointerException("edge is null");

		synchronized (m_lock) {
			final int edgeInx = edge.getIndex();
			final EdgeView oldView = m_edgeViewMap.get(edgeInx);

			if (oldView != null) {
				return oldView;
			}

			sourceNode = addNodeViewInternal(edge.getSource());
			targetNode = addNodeViewInternal(edge.getTarget());

			m_drawPersp.addEdge(edge);

			final View<CyEdge> edgeViewModel = cyNetworkView.getEdgeView(edge);
			dEdgeView = new DEdgeView(dingLexicon, this, edgeInx, edgeViewModel);

			m_edgeViewMap.put(Integer.valueOf(edgeInx), dEdgeView);
			m_contentChanged = true;

			// read in visual properties from view obj
			// FIXME TODO: this process is not necessary in construction
			// process.
			// final Collection<VisualProperty<?>> edgeVPs = rootLexicon
			// .getVisualProperties(EDGE);
			//
			// for (VisualProperty<?> vp : edgeVPs)
			// edgeVisualPropertySet(edgeViewModel, vp,
			// edgeViewModel.getVisualProperty(vp));

		}

		// Under no circumstances should we be holding m_lock when the listener
		// events are fired.
		final GraphViewChangeListener listener = m_lis[0];

		if (listener != null) {
			// Only fire this event if either of the nodes is new. The node
			// will be null if it already existed.
			if ((sourceNode != null) || (targetNode != null)) {
				int[] nodeInx;

				if (sourceNode == null) {
					nodeInx = new int[] { targetNode.getGraphPerspectiveIndex() };
				} else if (targetNode == null) {
					nodeInx = new int[] { sourceNode.getGraphPerspectiveIndex() };
				} else {
					nodeInx = new int[] { sourceNode.getGraphPerspectiveIndex(),
							targetNode.getGraphPerspectiveIndex() };
				}

				listener.graphViewChanged(new GraphViewNodesRestoredEvent(this,
						makeNodeList(nodeInx, this)));
			}

			listener.graphViewChanged(new GraphViewEdgesRestoredEvent(this,
					makeList(dEdgeView.getEdge())));
		}

		return dEdgeView;
	}

	/**
	 * Removes a NodeView based on specified NodeView.
	 *
	 * @param nodeView
	 *            The NodeView object to be removed.
	 *
	 * @return The NodeView object that was removed.
	 */
	public NodeView removeNodeView(NodeView nodeView) {
		return removeNodeView(nodeView.getGraphPerspectiveIndex());
	}

	/**
	 * Removes a NodeView based on specified Node.
	 *
	 * @param node
	 *            The Node object connected to the NodeView to be removed.
	 *
	 * @return The NodeView object that was removed.
	 */
	public NodeView removeNodeView(CyNode node) {
		return removeNodeView(node.getIndex());
	}

	/**
	 * Removes a NodeView based on a specified index.
	 *
	 * @param nodeInx
	 *            The index of the NodeView to be removed.
	 *
	 * @return The NodeView object that was removed.
	 */
	public NodeView removeNodeView(int nodeInx) {
		final List<CyEdge> hiddenEdgeInx;
		final DNodeView returnThis;
		final CyNode nnode;

		synchronized (m_lock) {
			nnode = networkModel.getNode(nodeInx);

			// We have to query edges in the m_structPersp, not m_drawPersp
			// because what if the node is hidden?
			hiddenEdgeInx = networkModel.getAdjacentEdgeList(nnode,
					CyEdge.Type.ANY);

			// This isn't an error. Only if the nodeInx is invalid will
			// getAdjacentEdgeIndicesArray
			// return null. If there are no adjacent edges, then it will return
			// an array of length 0.
			if (hiddenEdgeInx == null)
				return null;

			for (CyEdge ee : hiddenEdgeInx)
				removeEdgeViewInternal(ee.getIndex());

			returnThis = (DNodeView) m_nodeViewMap.remove(Integer.valueOf(nodeInx));
			returnThis.unselectInternal();

			// If this node was hidden, it won't be in m_drawPersp.
			m_drawPersp.removeNodes(Collections.singletonList(nnode));
			// m_structPersp.removeNode(nodeInx);
			m_nodeDetails.unregisterNode(nodeInx);

			// If this node was hidden, it won't be in m_spacial.
			m_spacial.delete(nodeInx);

			m_contentChanged = true;
		}

		final GraphViewChangeListener listener = m_lis[0];

		if (listener != null) {
			if (hiddenEdgeInx.size() > 0) {
				listener.graphViewChanged(new GraphViewEdgesHiddenEvent(this, hiddenEdgeInx));
			}

			listener.graphViewChanged(new GraphViewNodesHiddenEvent(this,
					makeList(returnThis.getNodeViewModel().getModel())));
		}

		return returnThis;
	}

	/**
	 * Removes an EdgeView based on an EdgeView.
	 *
	 * @param edgeView
	 *            The EdgeView to be removed.
	 *
	 * @return The EdgeView that was removed.
	 */
	public EdgeView removeEdgeView(EdgeView edgeView) {
		return removeEdgeView(edgeView.getRootGraphIndex());
	}

	/**
	 * Removes an EdgeView based on an Edge.
	 *
	 * @param edge
	 *            The Edge of the EdgeView to be removed.
	 *
	 * @return The EdgeView that was removed.
	 */
	public EdgeView removeEdgeView(CyEdge edge) {
		return removeEdgeView(edge.getIndex());
	}

	/**
	 * Removes an EdgeView based on an EdgeIndex.
	 *
	 * @param edgeInx
	 *            The edge index of the EdgeView to be removed.
	 *
	 * @return The EdgeView that was removed.
	 */
	public EdgeView removeEdgeView(int edgeInx) {
		final DEdgeView returnThis;
		final CyEdge edge; 

		synchronized (m_lock) {
			edge = networkModel.getEdge(edgeInx);
			returnThis = removeEdgeViewInternal(edgeInx);

			if (returnThis != null) {
				m_contentChanged = true;
			}
		}

		if (returnThis != null) {
			final GraphViewChangeListener listener = m_lis[0];

			if (listener != null) {
				listener.graphViewChanged(new GraphViewEdgesHiddenEvent(this, makeList(edge)));
			}
		}

		return returnThis;
	}

	/**
	 * Should synchronize around m_lock.
	 */
	private DEdgeView removeEdgeViewInternal(int edgeInx) {
		final DEdgeView returnThis = (DEdgeView) m_edgeViewMap.remove(Integer.valueOf(edgeInx));

		CyEdge eedge = networkModel.getEdge(edgeInx);

		if (returnThis == null) {
			return returnThis;
		}

		returnThis.unselectInternal();

		// If this edge view was hidden, it won't be in m_drawPersp.
		m_drawPersp.removeEdges(Collections.singletonList(eedge)); 
		// m_structPersp.hideEdge(edgeInx);
		m_edgeDetails.unregisterEdge(edgeInx);

		// m_selectedEdges.delete(edgeInx);
		returnThis.m_view = null;

		return returnThis;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Long getIdentifier() {
		return m_identifier;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param id
	 *            DOCUMENT ME!
	 */
	public void setIdentifier(Long id) {
		m_identifier = id;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public double getZoom() {
		return m_networkCanvas.m_scaleFactor;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param zoom
	 *            DOCUMENT ME!
	 */
	private void setZoom(final double zoom, final boolean updateView) {
		synchronized (m_lock) {
			m_networkCanvas.m_scaleFactor = checkZoom(zoom,m_networkCanvas.m_scaleFactor);
			m_viewportChanged = true;
		}

		if (updateView) {
			this.updateView();
		}
	}
	
	/**
	 * Set the zoom level and redraw the view.
	 */
	public void setZoom(final double zoom) {
		setZoom(zoom, /* updateView = */ true);
	}

	/**
	 * DOCUMENT ME!
	 */
	private void fitContent(final boolean updateView) {
		synchronized (m_lock) {
			if (m_spacial.queryOverlap(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
			                           Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
			                           m_extentsBuff, 0, false).numRemaining() == 0) {
				return;
			}

			m_networkCanvas.m_xCenter = (((double) m_extentsBuff[0]) + ((double) m_extentsBuff[2])) / 2.0d;
			m_networkCanvas.m_yCenter = (((double) m_extentsBuff[1]) + ((double) m_extentsBuff[3])) / 2.0d;
			final double zoom = Math.min(((double) m_networkCanvas.getWidth()) / 
			                             (((double) m_extentsBuff[2]) - 
			                              ((double) m_extentsBuff[0])), 
			                              ((double) m_networkCanvas.getHeight()) / 
			                             (((double) m_extentsBuff[3]) - 
			                              ((double) m_extentsBuff[1])));
			m_networkCanvas.m_scaleFactor = checkZoom(zoom,m_networkCanvas.m_scaleFactor);
			if (calledFromGetSnapshot) {
				calledFromGetSnapshot = false;
				m_networkCanvas.m_scaleFactor = 1.0;
			}
			m_viewportChanged = true;
			
			// Update view model.  Zoom Level should be modified.
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION, m_networkCanvas.m_xCenter);
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_networkCanvas.m_yCenter);
		}
		
		if (updateView)
			this.updateView();
	}
	
	/**
	 * Resize the network view to the size of the canvas and redraw it. 
	 */
	public void fitContent() {
		fitContent(/* updateView = */ true);
	}

	
	/**
	 * Redraw the canvas.
	 */
	public void updateView() {
		final long start = System.currentTimeMillis();
		m_networkCanvas.repaint();
		logger.debug("Repaint finised in " + (System.currentTimeMillis() - start) + " msec.");
	}

	/**
	 * Returns an iterator of all node views, including those that are currently
	 * hidden.
	 *
	 * @return DOCUMENT ME!
	 */
	public Iterator<NodeView> getNodeViewsIterator() {
		synchronized (m_lock) {
			return m_nodeViewMap.values().iterator();
		}
	}

	/**
	 * Returns the count of all node views, including those that are currently
	 * hidden.
	 *
	 * @return DOCUMENT ME!
	 */
	public int getNodeViewCount() {
		synchronized (m_lock) {
			return m_nodeViewMap.size();
		}
	}

	/**
	 * Returns the count of all edge views, including those that are currently
	 * hidden.
	 *
	 * @return DOCUMENT ME!
	 */
	public int getEdgeViewCount() {
		synchronized (m_lock) {
			return m_edgeViewMap.size();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param node
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public NodeView getNodeView(CyNode node) {
		return getNodeView(node.getIndex());
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public NodeView getNodeView(int nodeInx) {
		synchronized (m_lock) {
			return (NodeView) m_nodeViewMap.get(Integer.valueOf(nodeInx));
		}
	}

	/**
	 * Returns a list of all edge views, including those that are currently
	 * hidden.
	 *
	 * @return DOCUMENT ME!
	 */
	public List<EdgeView> getEdgeViewsList() {
		synchronized (m_lock) {
			final ArrayList<EdgeView> returnThis = new ArrayList<EdgeView>(
					m_edgeViewMap.size());
			final Iterator<EdgeView> values = m_edgeViewMap.values().iterator();

			while (values.hasNext())
				returnThis.add(values.next());

			return returnThis;
		}
	}

	/**
	 * Returns all edge views (including the hidden ones) that are either 1.
	 * directed, having oneNode as source and otherNode as target or 2.
	 * undirected, having oneNode and otherNode as endpoints. Note that this
	 * behaviour is similar to that of CyNetwork.edgesList(Node, Node).
	 *
	 * @param oneNode
	 *            DOCUMENT ME!
	 * @param otherNode
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public List<EdgeView> getEdgeViewsList(CyNode oneNode, CyNode otherNode) {
		synchronized (m_lock) {
			List<CyEdge> edges = networkModel.getConnectingEdgeList(oneNode,
					otherNode, CyEdge.Type.ANY);

			if (edges == null) {
				return null;
			}

			final ArrayList<EdgeView> returnThis = new ArrayList<EdgeView>();
			Iterator<CyEdge> it = edges.iterator();

			while (it.hasNext()) {
				CyEdge e = (CyEdge) it.next();
				EdgeView ev = getEdgeView(e);
				if (ev != null)
					returnThis.add(ev);
			}

			return returnThis;
		}
	}

	/**
	 * Similar to getEdgeViewsList(Node, Node), only that one has control of
	 * whether or not to include undirected edges.
	 *
	 * @param oneNodeInx
	 *            DOCUMENT ME!
	 * @param otherNodeInx
	 *            DOCUMENT ME!
	 * @param includeUndirected
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public List<EdgeView> getEdgeViewsList(int oneNodeInx, int otherNodeInx,
			boolean includeUndirected) {
		CyNode n1;
		CyNode n2;
		synchronized (m_lock) {
			n1 = networkModel.getNode(oneNodeInx);
			n2 = networkModel.getNode(otherNodeInx);
		}
		return getEdgeViewsList(n1, n2);
	}

	/**
	 * Returns an edge view with specified edge index whether or not the edge
	 * view is hidden; null is returned if view does not exist.
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public EdgeView getEdgeView(int edgeInx) {
		synchronized (m_lock) {
			return (EdgeView) m_edgeViewMap.get(Integer.valueOf(edgeInx));
		}
	}

	/**
	 * Returns an iterator of all edge views, including those that are currently
	 * hidden.
	 *
	 * @return DOCUMENT ME!
	 */
	public Iterator<EdgeView> getEdgeViewsIterator() {
		synchronized (m_lock) {
			return m_edgeViewMap.values().iterator();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edge
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public EdgeView getEdgeView(CyEdge edge) {
		return getEdgeView(edge.getIndex());
	}

	/**
	 * Alias to getEdgeViewCount().
	 *
	 * @return DOCUMENT ME!
	 */
	public int edgeCount() {
		return getEdgeViewCount();
	}

	/**
	 * Alias to getNodeViewCount().
	 *
	 * @return DOCUMENT ME!
	 */
	public int nodeCount() {
		return getNodeViewCount();
	}

	/**
	 * @param obj
	 *            should be either a DEdgeView or a DNodeView.
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean hideGraphObject(Object obj) {
		return hideGraphObjectInternal(obj, true);
	}

	private boolean hideGraphObjectInternal(Object obj,
			boolean fireListenerEvents) {
		if (obj instanceof DEdgeView) {
			int edgeInx;
			CyEdge edge;

			synchronized (m_lock) {
				edgeInx = ((DEdgeView) obj).getRootGraphIndex();
				edge = ((DEdgeView) obj).getEdge();

				edge.getCyRow("VIEW").set("hidden", true);
				if (!m_drawPersp.removeEdges(Collections.singletonList(edge)))
					return false;

				((DEdgeView) obj).unselectInternal();
				m_contentChanged = true;
			}

			if (fireListenerEvents) {
				final GraphViewChangeListener listener = m_lis[0];

				if (listener != null) {
					listener.graphViewChanged(new GraphViewEdgesHiddenEvent(
							this, makeList(((DEdgeView) obj).getEdge())));
				}
			}

			return true;
		} else if (obj instanceof DNodeView) {
			List<CyEdge> edges;
			int nodeInx;
			CyNode nnode;

			synchronized (m_lock) {
				final DNodeView nView = (DNodeView) obj;
				nodeInx = nView.getGraphPerspectiveIndex();
				nnode = networkModel.getNode(nodeInx);
				edges = m_drawPersp.getAdjacentEdgeList(nnode, CyEdge.Type.ANY);

				if (edges != null) {
					for (CyEdge ee : edges)
						hideGraphObjectInternal(m_edgeViewMap.get(ee.getIndex()),
									false);
				}

				nView.unselectInternal();
				m_spacial.exists(nodeInx, m_extentsBuff, 0);
				nView.m_hiddenXMin = m_extentsBuff[0];
				nView.m_hiddenYMin = m_extentsBuff[1];
				nView.m_hiddenXMax = m_extentsBuff[2];
				nView.m_hiddenYMax = m_extentsBuff[3];
				m_drawPersp.removeNodes(Collections.singletonList(nnode));
				nnode.getCyRow("VIEW").set("hidden", true);
				m_spacial.delete(nodeInx);
				m_contentChanged = true;
			}

			if (fireListenerEvents) {
				final GraphViewChangeListener listener = m_lis[0];

				if (listener != null) {
					if (edges != null && edges.size() > 0) {
						listener.graphViewChanged(new GraphViewEdgesHiddenEvent(
								this, edges));
					}

					listener.graphViewChanged(new GraphViewNodesHiddenEvent(
							this, makeList(nnode)));
				}
			}

			return true;
		} else {
			return false;
		}
	}

	final boolean isHidden(final DEdgeView edgeView) {
		synchronized (m_lock) {
			final int edgeIndex = edgeView.getRootGraphIndex();
			return m_drawPersp.containsEdge(m_drawPersp.getEdge(edgeIndex));
		}
	}

	final boolean isHidden(final DNodeView nodeView) {
		synchronized (m_lock) {
			final int nodeIndex = nodeView.getGraphPerspectiveIndex();
			return m_drawPersp.containsNode(m_drawPersp.getNode(nodeIndex));
		}
	}

	/**
	 * @param obj
	 *            should be either a DEdgeView or a DNodeView.
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean showGraphObject(Object obj) {
		return showGraphObjectInternal(obj, true);
	}

	private boolean showGraphObjectInternal(Object obj,
			boolean fireListenerEvents) {
		if (obj instanceof DNodeView) {
			int nodeInx;
			final DNodeView nView = (DNodeView) obj;

			synchronized (m_lock) {
				nodeInx = nView.getGraphPerspectiveIndex();
				CyNode nnode = networkModel.getNode(nodeInx);

				if (nnode == null) {
					return false;
				}

				nnode.getCyRow("VIEW").set("hidden", false);
				if (!m_drawPersp.addNode(nnode))
					return false;

				m_spacial.insert(nodeInx, nView.m_hiddenXMin,
						nView.m_hiddenYMin, nView.m_hiddenXMax,
						nView.m_hiddenYMax);
				m_contentChanged = true;
			}

			if (fireListenerEvents) {
				final GraphViewChangeListener listener = m_lis[0];

				if (listener != null) {
					listener.graphViewChanged(new GraphViewNodesRestoredEvent(
							this, makeList(nView.getNodeViewModel().getModel())));
				}
			}

			return true;
		} else if (obj instanceof DEdgeView) {
			CyNode sourceNode;
			CyNode targetNode;
			CyEdge newEdge;

			synchronized (m_lock) {
				final CyEdge edge = networkModel.getEdge(((DEdgeView) obj)
						.getRootGraphIndex());

				if (edge == null) {
					return false;
				}

				// The edge exists in m_structPersp, therefore its source and
				// target
				// node views must also exist.
				sourceNode = edge.getSource();

				if (!showGraphObjectInternal(getNodeView(sourceNode), false)) {
					sourceNode = null;
				}

				targetNode = edge.getTarget();

				if (!showGraphObjectInternal(getNodeView(targetNode), false)) {
					targetNode = null;
				}

				newEdge = edge;

				newEdge.getCyRow("VIEW").set("hidden", false);
				if (!m_drawPersp.addEdge(newEdge))
					return false;

				m_contentChanged = true;
			}

			if (fireListenerEvents) {
				final GraphViewChangeListener listener = m_lis[0];

				if (listener != null) {
					if (sourceNode != null) {
						listener.graphViewChanged(new GraphViewNodesRestoredEvent(
								this, makeList(sourceNode)));
					}

					if (targetNode != null) {
						listener.graphViewChanged(new GraphViewNodesRestoredEvent(
								this, makeList(targetNode)));
					}

					listener.graphViewChanged(new GraphViewEdgesRestoredEvent(
							this, makeList(newEdge)));
				}
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param objects
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean hideGraphObjects(List<? extends GraphViewObject> objects) {
		final Iterator<? extends GraphViewObject> it = objects.iterator();

		while (it.hasNext())
			hideGraphObject(it.next());

		return true;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param objects
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean showGraphObjects(List<? extends GraphViewObject> objects) {
		final Iterator<? extends GraphViewObject> it = objects.iterator();

		while (it.hasNext())
			showGraphObject(it.next());

		return true;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param data
	 *            DOCUMENT ME!
	 */
	public void setAllNodePropertyData(int nodeInx, Object[] data) {
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Object[] getAllNodePropertyData(int nodeInx) {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param data
	 *            DOCUMENT ME!
	 */
	public void setAllEdgePropertyData(int edgeInx, Object[] data) {
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Object[] getAllEdgePropertyData(int edgeInx) {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Object getNodeObjectProperty(int nodeInx, int property) {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setNodeObjectProperty(int nodeInx, int property, Object value) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Object getEdgeObjectProperty(int edgeInx, int property) {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setEdgeObjectProperty(int edgeInx, int property, Object value) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public double getNodeDoubleProperty(int nodeInx, int property) {
		return 0.0d;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param val
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setNodeDoubleProperty(int nodeInx, int property, double val) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public double getEdgeDoubleProperty(int edgeInx, int property) {
		return 0.0d;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param val
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setEdgeDoubleProperty(int edgeInx, int property, double val) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public float getNodeFloatProperty(int nodeInx, int property) {
		return 0.0f;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setNodeFloatProperty(int nodeInx, int property, float value) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public float getEdgeFloatProperty(int edgeInx, int property) {
		return 0.0f;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setEdgeFloatProperty(int edgeInx, int property, float value) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean getNodeBooleanProperty(int nodeInx, int property) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param val
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setNodeBooleanProperty(int nodeInx, int property, boolean val) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean getEdgeBooleanProperty(int edgeInx, int property) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param val
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setEdgeBooleanProperty(int edgeInx, int property, boolean val) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public int getNodeIntProperty(int nodeInx, int property) {
		return 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setNodeIntProperty(int nodeInx, int property, int value) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public int getEdgeIntProperty(int edgeInx, int property) {
		return 0;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param edgeInx
	 *            DOCUMENT ME!
	 * @param property
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean setEdgeIntProperty(int edgeInx, int property, int value) {
		return false;
	}

	// Auxiliary methods specific to this GraphView implementation:
	
	public void setCenter(double x, double y) {
		synchronized (m_lock) {
			m_networkCanvas.m_xCenter = x;
			m_networkCanvas.m_yCenter = y;
			m_viewportChanged = true;
			
			// Update view model
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION, m_networkCanvas.m_xCenter);
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_networkCanvas.m_yCenter);
		}
	}

	
	public Point2D getCenter() {
		synchronized (m_lock) {
			return new Point2D.Double(m_networkCanvas.m_xCenter, m_networkCanvas.m_yCenter);
		}
	}

	/**
	 * DOCUMENT ME!
	 */
	public void fitSelected() {
		synchronized (m_lock) {
			IntEnumerator selectedElms = m_selectedNodes.searchRange(
					Integer.MIN_VALUE, Integer.MAX_VALUE, false);

			// Only check for selected edges if we don't have selected nodes.
			if (selectedElms.numRemaining() == 0 && edgeSelectionEnabled()) {
				selectedElms = getSelectedEdgeNodes();
				if (selectedElms.numRemaining() == 0)
					return;
			}

			float xMin = Float.POSITIVE_INFINITY;
			float yMin = Float.POSITIVE_INFINITY;
			float xMax = Float.NEGATIVE_INFINITY;
			float yMax = Float.NEGATIVE_INFINITY;

			int leftMost = 0;
			int rightMost = 0;

			while (selectedElms.numRemaining() > 0) {
				final int node = selectedElms.nextInt();
				m_spacial.exists(node, m_extentsBuff, 0);
				if (m_extentsBuff[0] < xMin) {
					xMin = m_extentsBuff[0];
					leftMost = node;
				}

				if (m_extentsBuff[2] > xMax) {
					xMax = m_extentsBuff[2];
					rightMost = node;
				}

				yMin = Math.min(yMin, m_extentsBuff[1]);
				yMax = Math.max(yMax, m_extentsBuff[3]);
			}

			xMin = xMin - (getLabelWidth(leftMost) / 2);
			xMax = xMax + (getLabelWidth(rightMost) / 2);

			m_networkCanvas.m_xCenter = (((double) xMin) + ((double) xMax)) / 2.0d;
			m_networkCanvas.m_yCenter = (((double) yMin) + ((double) yMax)) / 2.0d;
			final double zoom = Math.min(((double) m_networkCanvas.getWidth())
					/ (((double) xMax) - ((double) xMin)),
					((double) m_networkCanvas.getHeight())
							/ (((double) yMax) - ((double) yMin)));
			m_networkCanvas.m_scaleFactor = checkZoom(zoom,
					m_networkCanvas.m_scaleFactor);
			m_viewportChanged = true;
			
			// Update view model.  Zoom Level should be modified.
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION, m_networkCanvas.m_xCenter);
			this.cyNetworkView.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_networkCanvas.m_yCenter);
		}
		updateView();
	}

	/**
	 * @return An IntEnumerator listing the nodes that are endpoints of the
	 *         currently selected edges.
	 */
	private IntEnumerator getSelectedEdgeNodes() {
		synchronized (m_lock) {
			final IntEnumerator selectedEdges = m_selectedEdges.searchRange(
					Integer.MIN_VALUE, Integer.MAX_VALUE, false);

			final IntHash nodeIds = new IntHash();

			while (selectedEdges.numRemaining() > 0) {
				final int edge = selectedEdges.nextInt();
				CyEdge currEdge = getEdgeView(edge).getEdge();

				CyNode source = currEdge.getSource();
				int sourceId = source.getIndex();
				nodeIds.put(sourceId);

				CyNode target = currEdge.getTarget();
				int targetId = target.getIndex();
				nodeIds.put(targetId);
			}

			return nodeIds.elements();
		}
	}

	private int getLabelWidth(int node) {
		DNodeView x = ((DNodeView) getNodeView(node));
		if (x == null)
			return 0;

		String s = x.getText();
		if (s == null)
			return 0;

		char[] lab = s.toCharArray();
		if (lab == null)
			return 0;

		if (m_networkCanvas.m_fontMetrics == null)
			return 0;

		return m_networkCanvas.m_fontMetrics.charsWidth(lab, 0, lab.length);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param lod
	 *            DOCUMENT ME!
	 */
	public void setGraphLOD(GraphLOD lod) {
		synchronized (m_lock) {
			m_networkCanvas.m_lod[0] = lod;
			m_contentChanged = true;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public GraphLOD getGraphLOD() {
		return m_networkCanvas.m_lod[0];
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param textAsShape
	 *            DOCUMENT ME!
	 */
	public void setPrintingTextAsShape(boolean textAsShape) {
		synchronized (m_lock) {
			m_printLOD.setPrintingTextAsShape(textAsShape);
		}
	}

	/**
	 * Efficiently computes the set of nodes intersecting an axis-aligned query
	 * rectangle; the query rectangle is specified in the node coordinate
	 * system, not the component coordinate system.
	 * <p>
	 * NOTE: The order of elements placed on the stack follows the rendering
	 * order of nodes; the element waiting to be popped off the stack is the
	 * node that is rendered last, and thus is "on top of" other nodes
	 * potentially beneath it.
	 * <p>
	 * HINT: To perform a point query simply set xMin equal to xMax and yMin
	 * equal to yMax.
	 *
	 * @param xMinimum
	 *            a boundary of the query rectangle: the minimum X coordinate.
	 * @param yMinimum
	 *            a boundary of the query rectangle: the minimum Y coordinate.
	 * @param xMaximum
	 *            a boundary of the query rectangle: the maximum X coordinate.
	 * @param yMaximum
	 *            a boundary of the query rectangle: the maximum Y coordinate.
	 * @param treatNodeShapesAsRectangle
	 *            if true, nodes are treated as rectangles for purposes of the
	 *            query computation; if false, true node shapes are respected,
	 *            at the expense of slowing down the query by a constant factor.
	 * @param returnVal
	 *            RootGraph indices of nodes intersecting the query rectangle
	 *            will be placed onto this stack; the stack is not emptied by
	 *            this method initially.
	 */
	public void getNodesIntersectingRectangle(double xMinimum, double yMinimum, double xMaximum,
	                                          double yMaximum, boolean treatNodeShapesAsRectangle,
	                                          IntStack returnVal) {
		synchronized (m_lock) {
			final float xMin = (float) xMinimum;
			final float yMin = (float) yMinimum;
			final float xMax = (float) xMaximum;
			final float yMax = (float) yMaximum;
			final SpacialEntry2DEnumerator under = m_spacial.queryOverlap(xMin, yMin, xMax, yMax,
			                                                              null, 0, false);
			final int totalHits = under.numRemaining();

			if (treatNodeShapesAsRectangle) {
				for (int i = 0; i < totalHits; i++)
					returnVal.push(under.nextInt());
			} else {
				final double x = xMin;
				final double y = yMin;
				final double w = ((double) xMax) - xMin;
				final double h = ((double) yMax) - yMin;

				for (int i = 0; i < totalHits; i++) {
					final int node = under.nextExtents(m_extentsBuff, 0);

					// The only way that the node can miss the intersection
					// query is
					// if it intersects one of the four query rectangle's
					// corners.
					if (((m_extentsBuff[0] < xMin) && (m_extentsBuff[1] < yMin))
					    || ((m_extentsBuff[0] < xMin) && (m_extentsBuff[3] > yMax))
					    || ((m_extentsBuff[2] > xMax) && (m_extentsBuff[3] > yMax))
					    || ((m_extentsBuff[2] > xMax) && (m_extentsBuff[1] < yMin))) {
						m_networkCanvas.m_grafx.getNodeShape(m_nodeDetails.shape(node),
						                                     m_extentsBuff[0], m_extentsBuff[1],
						                                     m_extentsBuff[2], m_extentsBuff[3],
						                                     m_path);

						if ((w > 0) && (h > 0)) {
							if (m_path.intersects(x, y, w, h))
								returnVal.push(node);
						} else {
							if (m_path.contains(x, y))
								returnVal.push(node);
						}
					} else
						returnVal.push(node);
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xMin
	 *            DOCUMENT ME!
	 * @param yMin
	 *            DOCUMENT ME!
	 * @param xMax
	 *            DOCUMENT ME!
	 * @param yMax
	 *            DOCUMENT ME!
	 * @param returnVal
	 *            DOCUMENT ME!
	 */
	public void queryDrawnEdges(int xMin, int yMin, int xMax, int yMax,
			IntStack returnVal) {
		synchronized (m_lock) {
			m_networkCanvas.computeEdgesIntersecting(xMin, yMin, xMax, yMax,
					returnVal);
		}
	}

	/**
	 * Extents of the nodes.
	 */
	public boolean getExtents(double[] extentsBuff) {
		synchronized (m_lock) {
			if (m_spacial.queryOverlap(Float.NEGATIVE_INFINITY,
					Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
					Float.POSITIVE_INFINITY, m_extentsBuff, 0, false)
					.numRemaining() == 0) {
				return false;
			}

			extentsBuff[0] = m_extentsBuff[0];
			extentsBuff[1] = m_extentsBuff[1];
			extentsBuff[2] = m_extentsBuff[2];
			extentsBuff[3] = m_extentsBuff[3];

			return true;
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param coords
	 *            DOCUMENT ME!
	 */
	public void xformComponentToNodeCoords(double[] coords) {
		synchronized (m_lock) {
			m_networkCanvas.m_grafx.xformImageToNodeCoords(coords);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param img
	 *            DOCUMENT ME!
	 * @param lod
	 *            DOCUMENT ME!
	 * @param bgPaint
	 *            DOCUMENT ME!
	 * @param xCenter
	 *            DOCUMENT ME!
	 * @param yCenter
	 *            DOCUMENT ME!
	 * @param scaleFactor
	 *            DOCUMENT ME!
	 */
	public void drawSnapshot(Image img, GraphLOD lod, Paint bgPaint, double xCenter,
	                         double yCenter, double scaleFactor) {
		synchronized (m_lock) {
			GraphRenderer.renderGraph(m_drawPersp, m_spacial, lod, m_nodeDetails,
			                          m_edgeDetails, m_hash, new GraphGraphics(img, false),
			                          bgPaint, xCenter, yCenter, scaleFactor);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void addContentChangeListener(ContentChangeListener l) {
		m_cLis[0] = ContentChangeListenerChain.add(m_cLis[0], l);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void removeContentChangeListener(ContentChangeListener l) {
		m_cLis[0] = ContentChangeListenerChain.remove(m_cLis[0], l);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void addViewportChangeListener(ViewportChangeListener l) {
		m_vLis[0] = ViewportChangeListenerChain.add(m_vLis[0], l);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void removeViewportChangeListener(ViewportChangeListener l) {
		m_vLis[0] = ViewportChangeListenerChain.remove(m_vLis[0], l);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param g
	 *            DOCUMENT ME!
	 * @param pageFormat
	 *            DOCUMENT ME!
	 * @param page
	 *            DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public int print(Graphics g, PageFormat pageFormat, int page) {
		if (page == 0) {
			((Graphics2D) g).translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			// make sure the whole image on the screen will fit to the printable
			// area of the paper
			double image_scale = Math
					.min(pageFormat.getImageableWidth()
							/ m_networkCanvas.getWidth(),
							pageFormat.getImageableHeight()
									/ m_networkCanvas.getHeight());

			if (image_scale < 1.0d) {
				((Graphics2D) g).scale(image_scale, image_scale);
			}

			// old school
			// g.clipRect(0, 0, getComponent().getWidth(),
			// getComponent().getHeight());
			// getComponent().print(g);

			// from InternalFrameComponent
			g.clipRect(0, 0, m_backgroundCanvas.getWidth(),
					m_backgroundCanvas.getHeight());
			m_backgroundCanvas.print(g);
			m_networkCanvas.print(g);
			m_foregroundCanvas.print(g);

			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}

	/**
	 * Method to return a reference to the network canvas. This method existed
	 * before the addition of background and foreground canvases, and it remains
	 * for backward compatibility.
	 *
	 * @return InnerCanvas
	 */
	public InnerCanvas getCanvas() {
		return m_networkCanvas;
	}

	/**
	 * Method to return a reference to a DingCanvas object, given a canvas id.
	 *
	 * @param canvasId
	 *            Canvas
	 * @return DingCanvas
	 */
	public DingCanvas getCanvas(Canvas canvasId) {
		if (canvasId == Canvas.BACKGROUND_CANVAS) {
			return m_backgroundCanvas;
		} else if (canvasId == Canvas.NETWORK_CANVAS) {
			return m_networkCanvas;
		} else if (canvasId == Canvas.FOREGROUND_CANVAS) {
			return m_foregroundCanvas;
		}

		// made it here
		return null;
	}

	/**
	 * Method to return a reference to an Image object,
	 * which represents the current network view.
	 *
	 * @param width Width of desired image.
	 * @param height Height of desired image.
	 * @param shrink Percent to shrink the network shown in the image.
	 * @param skipBackground If true, we don't draw the background
	 * This doesn't shrink the image, just the network shown, as if the user zoomed out.
	 * Can be between 0 and 1, if not it will default to 1.  
	 * @return Image
	 * @throws IllegalArgumentException
	 */
	private Image createImage(final int width, final int height, double shrink, final boolean skipBackground) {
		// Validate arguments
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("DGraphView.createImage(int width, int height): "
							   + "width and height arguments must be greater than zero");
		}

		if (shrink < 0 || shrink > 1.0) {
			logger.debug("DGraphView.createImage(width,height,shrink) shrink is invalid: "
			                   + shrink + "  using default of 1.0");
			shrink = 1.0;
		}

		// create image to return
		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);		
		final Graphics g = image.getGraphics();

		Dimension originalSize;

		if (!skipBackground) {
			// paint background canvas into image
			originalSize = m_backgroundCanvas.getSize();
			m_backgroundCanvas.setSize(width, height);
			m_backgroundCanvas.paint(g);
			// Restore background size
			m_backgroundCanvas.setSize(originalSize);
		}
		
		// paint inner canvas (network)
		originalSize = m_networkCanvas.getSize();
		m_networkCanvas.setSize(width, height);
		fitContent(/* updateView = */ false);
		setZoom(getZoom() * shrink, /* updateView = */ false);
		m_networkCanvas.paint(g);
		// Restore network to original size
		m_networkCanvas.setSize(originalSize);
		fitContent(/* updateView = */ false);
		
		// paint foreground canvas
		originalSize = m_foregroundCanvas.getSize();
		m_foregroundCanvas.setSize(width, height);
		m_foregroundCanvas.paint(g);
		// Restore foreground to original size
		m_foregroundCanvas.setSize(originalSize);
		
		return image;
	}

	/**
	 * Method to return a reference to an Image object,
	 * which represents the current network view.
	 *
	 * @param width Width of desired image.
	 * @param height Height of desired image.
	 * @param shrink Percent to shrink the network shown in the image. 
	 * This doesn't shrink the image, just the network shown, as if the user zoomed out.
	 * Can be between 0 and 1, if not it will default to 1.  
	 * @return Image
	 * @throws IllegalArgumentException
	 */
	public Image createImage(int width, int height, double shrink) {
		return createImage(width, height, shrink, /* skipBackground = */ false);
	}

	/**
	 * utility that returns the nodeView that is located at input point
	 *
	 * @param pt
	 */
	public NodeView getPickedNodeView(Point2D pt) {
		NodeView nv = null;
		double[] locn = new double[2];
		locn[0] = pt.getX();
		locn[1] = pt.getY();

		int chosenNode = 0;
		xformComponentToNodeCoords(locn);

		final IntStack nodeStack = new IntStack();
		getNodesIntersectingRectangle(
				(float) locn[0],
				(float) locn[1],
				(float) locn[0],
				(float) locn[1],
				(m_networkCanvas.getLastRenderDetail() & GraphRenderer.LOD_HIGH_DETAIL) == 0,
				nodeStack);

		chosenNode = (nodeStack.size() > 0) ? nodeStack.peek() : -1;

		if (chosenNode >= 0) {
			nv = getNodeView(chosenNode);
		}

		return nv;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param pt
	 *            DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public EdgeView getPickedEdgeView(Point2D pt) {
		EdgeView ev = null;
		final IntStack edgeStack = new IntStack();
		queryDrawnEdges((int) pt.getX(), (int) pt.getY(), (int) pt.getX(),
				(int) pt.getY(), edgeStack);

		int chosenEdge = 0;
		chosenEdge = (edgeStack.size() > 0) ? edgeStack.peek() : -1;

		if (chosenEdge >= 0) {
			ev = getEdgeView(chosenEdge);
		}

		return ev;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final float getAnchorSize() {
		return DEFAULT_ANCHOR_SIZE;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final Paint getAnchorSelectedPaint() {
		return DEFAULT_ANCHOR_SELECTED_PAINT;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public final Paint getAnchorUnselectedPaint() {
		return DEFAULT_ANCHOR_UNSELECTED_PAINT;
	}

	private double checkZoom(double zoom, double orig) {
		if (zoom > 0)
			return zoom;

		logger.debug("invalid zoom: " + zoom + "   using orig: " + orig);
		return orig;
	}

	private String title;

	/**
	 * DOCUMENT ME!
	 *
	 * @param title
	 *            DOCUMENT ME!
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	
	@Override public String getTitle() {
		return title;
	}

	
	/**
	 * Set selected nodes
	 * @param nodes
	 * @return
	 */
	public boolean setSelected(final CyNode[] nodes) {
		
		System.out.println("@@@@@@@@@@@@ Node selected called!!! @@@@@@@@@");
		final int size = nodes.length;
		for (int i = 0; i < size; i++)
			getNodeView(nodes[i]).select();
		
		return true;
	}
	
	
	public boolean setSelected(final CyEdge[] edges) {
		
		System.out.println("@@@@@@@@@@@@ Edge selected called!!! @@@@@@@@@");
		final int size = edges.length;
		for (int i = 0; i < size; i++)
			getEdgeView(edges[i]).select();

		return true;
	}

	
	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public List<NodeView> getNodeViewsList() {
		ArrayList<NodeView> list = new ArrayList<NodeView>(getNodeViewCount());
		for (CyNode nn : getGraphPerspective().getNodeList())
			list.add(getNodeView(nn.getIndex()));

		return list;
	}

	/**
	 * This method is used by freehep lib to export network as graphics.
	 */
	public void print(Graphics g) {
		m_backgroundCanvas.print(g);
		m_networkCanvas.print(g);
		m_foregroundCanvas.print(g);
	}

	/**
	 * This method is used by BitmapExporter to export network as graphics (png,
	 * jpg, bmp)
	 */
	public void printNoImposter(Graphics g) {
		m_backgroundCanvas.print(g);
		m_networkCanvas.printNoImposter(g);
		m_foregroundCanvas.print(g);
	}

	/**
	 * Our implementation of Component setBounds(). If we don't do this, the
	 * individual canvas do not get rendered.
	 *
	 * @param x
	 *            int
	 * @param y
	 *            int
	 * @param width
	 *            int
	 * @param height
	 *            int
	 */
	public void setBounds(int x, int y, int width, int height) {
		// call reshape on each canvas
		m_backgroundCanvas.setBounds(x, y, width, height);
		m_networkCanvas.setBounds(x, y, width, height);
		m_foregroundCanvas.setBounds(x, y, width, height);
	}
	
	public void setSize(Dimension d) {
		m_networkCanvas.setSize(d);
	}

	public Container getContainer(JLayeredPane jlp) {
		return new InternalFrameComponent(jlp, this);
	}

	public void addMouseListener(MouseListener m) {
		m_networkCanvas.addMouseListener(m);
	}

	public void addMouseMotionListener(MouseMotionListener m) {
		m_networkCanvas.addMouseMotionListener(m);
	}

	public void addKeyListener(KeyListener k) {
		m_networkCanvas.addKeyListener(k);
	}

	public void removeMouseListener(MouseListener m) {
		m_networkCanvas.removeMouseListener(m);
	}

	public void removeMouseMotionListener(MouseMotionListener m) {
		m_networkCanvas.removeMouseMotionListener(m);
	}

	public void removeKeyListener(KeyListener k) {
		m_networkCanvas.removeKeyListener(k);
	}

	static <X> List<X> makeList(X nodeOrEdge) {
		List<X> nl = new ArrayList<X>(1);
		nl.add(nodeOrEdge);
		return nl;
	}

	static List<CyNode> makeNodeList(int[] nodeids, GraphView view) {
		List<CyNode> l = new ArrayList<CyNode>(nodeids.length);
		for (int nid : nodeids)
			l.add(view.getNodeView(nid).getNodeViewModel().getModel());

		return l;
	}

	static List<CyEdge> makeEdgeList(int[] edgeids, GraphView view) {
		List<CyEdge> l = new ArrayList<CyEdge>(edgeids.length);
		for (int nid : edgeids)
			l.add(view.getEdgeView(nid).getEdge());

		return l;
	}

	@Override
	public void handleEvent(NodeViewsChangedEvent e) {
		if ( e.getSource() != cyNetworkView )
			return;
		
		for ( ViewChangeRecord<CyNode> record : e.getPayloadCollection()) {
			final Integer index = record.getView().getModel().getIndex();
			final NodeView view = m_nodeViewMap.get(index);
			if (view != null)
				view.setVisualPropertyValue(record.getVisualProperty(), record.getValue());
		}
	}

	/**
	 * This should be called from DGraphView.
	 */
	@Override
	public void handleEvent(EdgeViewsChangedEvent e) {
		if ( e.getSource() != cyNetworkView )
			return;
	
		for ( ViewChangeRecord<CyEdge> record : e.getPayloadCollection()) {
			final Integer index = record.getView().getModel().getIndex();
			final EdgeView view = m_edgeViewMap.get(index);
			if (view != null)
				view.setVisualPropertyValue(record.getVisualProperty(), record.getValue());
		}
	}


	/**
	 * Listener for all view change events.
	 */
	@Override
	public void handleEvent(NetworkViewChangedEvent e) {
		if ( e.getSource() != cyNetworkView )
			return;
	
		for ( ViewChangeRecord<CyNetwork> record : e.getPayloadCollection() ) {
			final VisualProperty<?> vp = record.getVisualProperty();
			Object value = record.getValue();
			
			if (value == null) 
				continue;

			if (vp == DVisualLexicon.NETWORK_NODE_SELECTION) {
				boolean b = ((Boolean) value).booleanValue();
				if (b)
					enableNodeSelection();
				else
					disableNodeSelection();
			} else if (vp == DVisualLexicon.NETWORK_EDGE_SELECTION) {
				boolean b = ((Boolean) value).booleanValue();
				if (b)
					enableEdgeSelection();
				else
					disableEdgeSelection();
			} else if (vp == MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT) {
				setBackgroundPaint((Paint) value);
			} else if (vp == MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION) {
				final double x = (Double) value;
				if(x != m_networkCanvas.m_xCenter)
					setCenter(x, m_networkCanvas.m_yCenter);
			} else if (vp == MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION) {
				final double y = (Double) value;
				if(y != m_networkCanvas.m_yCenter)
					setCenter(m_networkCanvas.m_xCenter, y);
			} else if (vp == MinimalVisualLexicon.NETWORK_SCALE_FACTOR) {
				setZoom(((Double) value).doubleValue());
			}
		}	
	}

	// ////// The following implements Presentation API ////////////

	public Printable createPrintable() {
		return this;
	}

	
	@Override
	public Properties getProperties() {
		return this.props;
	}
	
	/**
	 * Common API for all rendering engines.
	 */
	@Override public Image createImage(int width, int height) {
		return createImage(width, height, 1, true);
	}

	public VisualLexicon getVisualLexicon() {
		return this.dingLexicon;
	}

	public CyNetworkView getViewModel() {
		return cyNetworkView;
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int w, int h) {
		return VisualPropertyIconFactory.createIcon(vp, value, w, h);
	}

	@Override
	public void handleEvent(FitSelectedEvent e) {
		if (e.getSource().equals(cyNetworkView)) {
			logger.info("Fit Selected Called by event.");
			fitSelected();
		}
	}

	@Override
	public void handleEvent(FitContentEvent e) {
		if (e.getSource().equals(cyNetworkView)) {
			logger.info("Fit Content called by event.");
			fitContent();
		}
	}

	/**
	 * Returns the current snapshot image of this view.
	 *
	 * <p>
	 * No unnecessary image object will be created if networks in the current
	 * session does not contain any nested network, i.e., should not have
	 * performance/memory issue.
	 *
	 * @return Image of this view.  It is always up-to-date.
	 */
	TexturePaint getSnapshot(final double width, final double height) {
		if (!latest) {
			// Need to update snapshot.
			snapshotImage =
				(BufferedImage)createImage(DEF_SNAPSHOT_SIZE, DEF_SNAPSHOT_SIZE, 1,
				                           /* skipBackground = */ true);
			latest = true;
		}

		final Rectangle2D rect = new Rectangle2D.Double(-width / 2, -height / 2, width, height);
		final TexturePaint texturePaint = new TexturePaint(snapshotImage, rect);
		return texturePaint;
	}


	/**
	 * Converts a BufferedImage to a lossless PNG.
	 */
	private byte[] convertToCompressedImage(final BufferedImage bufferedImage) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(100000);
			ImageIO.write(bufferedImage, "png", baos);
			final byte[] retval = baos.toByteArray();
			return retval;
		} catch (final IOException e) {
			logger.warn("Failed to convert a BufferedImage to a PNG! (" + e + ")");
			return null;
		}
	}

	/**
	 * Converts a PNG to a BufferedImage.
	 */
	private BufferedImage convertToBufferedImage(final byte[] compressedImage) {
		try {
			final ByteArrayInputStream is = new ByteArrayInputStream(compressedImage);
			final BufferedImage retval = (BufferedImage)ImageIO.read(is);
			return retval;
		} catch (final IOException e) {
			logger.warn("Failed to convert a PNG to a BufferedImage! (" + e + ")");
			return null;
		}
	}

	/**
	 * Listener for update flag of snapshot image.
	 *
	 *
	 */
	private final class DGraphViewContentChangeListener implements ContentChangeListener {
		public void contentChanged() {
			latest = false;
		}
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		logger.debug("PrintCanvas called: " + printCanvas);
		print(printCanvas);
		logger.debug("PrintCanvas Done: ");
	}
}
