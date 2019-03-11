package org.cytoscape.ding.impl;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.PrintLOD;
import org.cytoscape.ding.icon.VisualPropertyIconFactory;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.ding.impl.events.ViewportChangeListenerChain;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.SpacialIndex2DFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * DING implementation of Cytoscpae 3.
 *
 * Explain relationship to cytoscape.
 *
 * Throughout this code I am assuming that nodes or edges are never removed from
 * the underlying RootGraph. This assumption was made in the old GraphView
 * implementation. Removal from the RootGraph is the only thing that can affect
 * m_drawPersp and m_structPersp that is beyond our control.
 *
 */
public class DRenderingEngine implements RenderingEngine<CyNetwork>, Printable, ActionListener, CyNetworkViewListener {

	private static final Logger logger = LoggerFactory.getLogger(DRenderingEngine.class);
	
	
	// Size of snapshot image
	protected static int DEF_SNAPSHOT_SIZE = 400;
	

	/**
	 * Enum to identify ding canvases - used in getCanvas(Canvas canvasId)
	 */
	public enum Canvas {
		BACKGROUND_CANVAS, NETWORK_CANVAS, FOREGROUND_CANVAS;
	}

//	public enum ShapeType {
//		NODE_SHAPE, LINE_TYPE, ARROW_SHAPE;
//	}
	
	private String title;

	private final CyServiceRegistrar serviceRegistrar;
	private final CyNetworkView viewModel;
	private final DVisualLexicon lexicon;
	
	private CyNetworkViewSnapshot viewModelSnapshot;
	
	/**
	 * Common object used for synchronization.
	 *
	 * Change this to a readwrite lock!
	 */
	final DingLock m_lock = new DingLock();

//	/**
//	 * A common buffer object used to pass information about. X-Y coords of the
//	 * minimum bounding box?
//	 */
//	final float[] m_extentsBuff = new float[4];
//	final double[] m_extentsBuffD = new double[4];

	
	
//	/**
//	 * A common general path variable used for holding lots of shapes.
//	 */
//	final GeneralPath m_path = new GeneralPath();

//	/**
//	 * Holds the NodeView data for the nodes that are visible. This will change
//	 * as nodes are hidden from the view.
//	 */
//	final CySubNetwork m_drawPersp;

//	// FIXME
//	DummySpacialFactory dummySpacialFactory;

//	/**
//	 * RTree used for querying node positions.
//	 */
//	SpacialIndex2D m_spacial;
//
//	/**
//	 * RTree used for querying Edge Handle positions. Used by DNodeView,
//	 * DEdgeView, and InnerCanvas.
//	 */
//	SpacialIndex2D m_spacialA;
	
	private final NodeDetails m_nodeDetails;
	private final EdgeDetails m_edgeDetails;
//	final NodeViewDefaultSupport nodeViewDefaultSupport;
//	final EdgeViewDefaultSupport edgeViewDefaultSupport;

	/**
	 * Level of detail specific to printing. Not used for rendering.
	 */
	PrintLOD m_printLOD;

//	private final Map<CyNode, NodeView> nodeViewMap;
//	private final Map<CyEdge, EdgeView> edgeViewMap;
//
//	Long m_identifier;
//
//	final float m_defaultNodeXMin;
//	final float m_defaultNodeYMin;
//	final float m_defaultNodeXMax;
//	final float m_defaultNodeYMax;

	/**
	 * Ref to network canvas object.
	 */
	InnerCanvas m_networkCanvas;

	/**
	 * Ref to the Bird's Eye View.  Note that
	 * this may be null if we're running headless!
	 */
	BirdsEyeView m_navigationCanvas = null;

	/**
	 * Ref to background canvas object.
	 */
	ArbitraryGraphicsCanvas m_backgroundCanvas;

	/**
	 * Ref to foreground canvas object.
	 */
	ArbitraryGraphicsCanvas m_foregroundCanvas;
	
	/**
	 * Current image width
	 */
	int imageWidth = 0;
	
	/**
	 * Current image Height
	 */
	int imageHeight = 0;

	boolean m_nodeSelection = true;
	boolean m_edgeSelection = true;
	boolean m_annotationSelection = true;

//	/**
//	 * BTree of selected nodes.
//	 */
//	final LongBTree m_selectedNodes; // Positive.
//
//	/**
//	 * BTree of selected edges.
//	 */
//	final LongBTree m_selectedEdges; // Positive.

//	/**
//	 * BTree of selected anchors.
//	 */
//	final LongBTree m_selectedAnchors;

	/**
	 * Flag that indicates that the content has changed and the graph needs to be redrawn.
	 */
	private volatile boolean contentChanged;

	/**
	 * State variable for when visual properties have changed
	 */
	volatile boolean m_visualChanged;

	/**
	 * State variable for when zooming/panning have changed.
	 */
	private volatile boolean viewportChanged;

	/**
	 * List of listeners.
	 */
	final ContentChangeListener[] m_cLis = new ContentChangeListener[1];

	/**
	 * List of listeners.
	 */
	final ViewportChangeListener[] m_vLis = new ViewportChangeListener[1];

	/**
	 *
	 */
	int m_lastSize;

	/**
	 * Used for caching texture paint.
	 */
	Paint m_lastPaint;

	/**
	 * Used for caching texture paint.
	 */
	Paint m_lastTexturePaint;
	
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

	final Map<NodeViewTaskFactory, Map> nodeViewTFs;
	final Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	final Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	final Map<NetworkViewLocationTaskFactory, Map> networkViewLocationTfs;
	final Map<CyEdgeViewContextMenuFactory, Map> cyEdgeViewContextMenuFactory;
	final Map<CyNodeViewContextMenuFactory, Map> cyNodeViewContextMenuFactory;
	final Map<CyNetworkViewContextMenuFactory, Map> cyNetworkViewContextMenuFactory;

	private final Properties props;
	
	private final CyAnnotator cyAnnotator;

	private boolean annotationsLoaded;
	private boolean servicesRegistered;
	
	
	private final VisualMappingManager vmm;

//	private List<CyNode> nodeSelectionList;
//	private List<CyEdge> edgeSelectionList;
	boolean largeModel = false;
	boolean haveZOrder = false;
	
//	private boolean ignoreRowsSetEvents;

//	// Animated edges
//	Timer animationTimer;
//	Set<View<CyEdge>> animatedEdges;


	private final Timer redrawTimer;
	
	private final BendStore bendStore;
	
	public DRenderingEngine(
			final CyNetworkView view,
			final DVisualLexicon dingLexicon,
			final ViewTaskFactoryListener vtfl,
			final AnnotationFactoryManager annMgr,
			final DingGraphLOD dingGraphLOD,
			final HandleFactory handleFactory,
			final CyServiceRegistrar registrar
	) {
		this.serviceRegistrar = registrar;
		this.props = new Properties();
		this.viewModel = view;
		this.lexicon = dingLexicon;
		
		long start = System.currentTimeMillis();
		logger.debug("Phase 1: rendering start.");

		this.nodeViewTFs = vtfl.nodeViewTFs;
		this.edgeViewTFs = vtfl.edgeViewTFs;
		this.emptySpaceTFs = vtfl.emptySpaceTFs; 
		this.networkViewLocationTfs = vtfl.networkViewLocationTFs;
		this.cyEdgeViewContextMenuFactory = vtfl.cyEdgeViewContextMenuFactory;
		this.cyNodeViewContextMenuFactory = vtfl.cyNodeViewContexMenuFactory;
		this.cyNetworkViewContextMenuFactory = vtfl.cyNetworkViewContextMenuFactory;
		
		SpacialIndex2DFactory spacialIndexFactory = registrar.getService(SpacialIndex2DFactory.class);
		this.bendStore = new BendStore(this, handleFactory, spacialIndexFactory);
		
//		// New simple implementation of the graph to keep track of visible nodes/edges.
//		m_drawPersp = new MinimalNetwork(SUIDFactory.getNextSUID());

//		final SpacialIndex2DFactory spacialFactory = registrar.getService(SpacialIndex2DFactory.class);
		vmm = registrar.getService(VisualMappingManager.class);
		
//		m_spacial = spacialFactory.createSpacialIndex2D();
//		m_spacialA = spacialFactory.createSpacialIndex2D();
		m_nodeDetails = new DNodeDetails(this);
		m_edgeDetails = new DEdgeDetails(this);
//		nodeViewDefaultSupport = new NodeViewDefaultSupport(m_nodeDetails, m_lock);
//		edgeViewDefaultSupport = new EdgeViewDefaultSupport(m_edgeDetails, m_lock);
//		nodeViewMap = new ConcurrentHashMap<>(16, 0.75f, 2);
//		edgeViewMap = new ConcurrentHashMap<>(16, 0.75f, 2);
		m_printLOD = new PrintLOD();
//		m_defaultNodeXMin = 0.0f;
//		m_defaultNodeYMin = 0.0f;
//		m_defaultNodeXMax = m_defaultNodeXMin + DNodeView.DEFAULT_WIDTH;
//		m_defaultNodeYMax = m_defaultNodeYMin + DNodeView.DEFAULT_HEIGHT;
		m_networkCanvas = new InnerCanvas(m_lock, this, registrar);
		m_backgroundCanvas = new ArbitraryGraphicsCanvas(this, Canvas.BACKGROUND_CANVAS, m_networkCanvas, Color.white, true);
		addViewportChangeListener(m_backgroundCanvas);
		m_foregroundCanvas = new ArbitraryGraphicsCanvas(this, Canvas.FOREGROUND_CANVAS, m_networkCanvas, Color.white, false);
		addViewportChangeListener(m_foregroundCanvas);
//		m_selectedNodes = new LongBTree();
//		m_selectedEdges = new LongBTree();
//		m_selectedAnchors = new LongBTree();

		logger.debug("Phase 2: Canvas created: time = " + (System.currentTimeMillis() - start));

		this.title = view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class);

//		// Create view model / presentations for the graph
//		for (final CyNode nn : model.getNodeList())
//			addNodeView(nn);
//
//		for (final CyEdge ee : model.getEdgeList())
//			addEdgeView(ee);

		logger.debug("Phase 3: All views created: time = " + (System.currentTimeMillis() - start));

		// Used to synchronize ding's internal selection state with the rest of Cytoscape.
		// new FlagAndSelectionHandler(this); 
		// TODO: Synchronize selection!
//		syncFilterAndView();

		logger.debug("Phase 4: Everything created: time = " + (System.currentTimeMillis() - start));
		setGraphLOD(dingGraphLOD);

		// Finally, intialize our annotations
		cyAnnotator = new CyAnnotator(this, annMgr, registrar);
		registrar.registerService(cyAnnotator, SessionAboutToBeSavedListener.class, new Properties());
		
		//Updating the snapshot for nested networks
		this.addContentChangeListener(new DGraphViewContentChangeListener());

//		nodeSelectionList = new ArrayList<>();
//		edgeSelectionList = new ArrayList<>();
//		
		CyNetworkViewSnapshot snapshot = view.createSnapshot();
		if (!dingGraphLOD.detail(snapshot.getNodeCount(), snapshot.getEdgeCount()))
			largeModel = true;

//		dummySpacialFactory = new DummySpacialFactory(this);

		// Animation
//		animatedEdges = Collections.newSetFromMap(new ConcurrentHashMap<>());
//
//		animationTimer = new Timer(200, this);
//		animationTimer.setRepeats(true);
		
		
		// Check if the view model has changed approximately 30 times per second
		redrawTimer = new Timer(30, e -> redraw());
		redrawTimer.setRepeats(true);
		redrawTimer.start();
	}
	
	
	/**
	 * TEMPORARY
	 * 
	 * This is being called by a Swing Timer, so the redraw() method is being run on the EDT.
	 * Painting is also done on the EDT. This is how we make sure that viewModelSnapshot does not
	 * change while a frame is being rendered.
	 * 
	 * Also the EDT will coalece paint events, so if the timer runs faster than the frame rate the
	 * EDT will take care of that.
	 * 
	 * MKTODO Move drawing off the EDT.
	 * If we move drawing off the EDT then we need another solution for ensuring that viewModelSnapshot
	 * does not get re-assigned while a frame is being drawn.
	 */
	private void redraw() {
		// Must run on EDT
		if(viewModel.isDirty()) {
			viewModelSnapshot = viewModel.createSnapshot();
			bendStore.updateSelectedEdges(viewModelSnapshot.getSelectedEdges());
			updateView();
		}
	}
	
	public BendStore getBendStore() {
		return bendStore;
	}
	
	
//	@Override
//	public CyNetwork getNetwork() {
//		return model;
//	}

//	@Override
	public boolean nodeSelectionEnabled() {
		return m_nodeSelection;
	}

//	@Override
	public boolean edgeSelectionEnabled() {
		return m_edgeSelection;
	}
	
//	@Override
	public boolean annotationSelectionEnabled() {
		return m_annotationSelection;
	}

//	@Override
	public void enableNodeSelection() {
		synchronized (m_lock) {
			m_nodeSelection = true;
		}
	}

//	@Override
	public void disableNodeSelection() {
		synchronized (m_lock) {
			m_nodeSelection = false;
		}
	}

//	@Override
	public void enableEdgeSelection() {
		synchronized (m_lock) {
			m_edgeSelection = true;
		}
	}

//	@Override
	public void disableEdgeSelection() {
		synchronized (m_lock) {
			m_edgeSelection = false;
		}
	}
	
//	@Override
	public void enableAnnotationSelection() {
		synchronized (m_lock) {
			m_annotationSelection = true;
		}
	}
	
//	@Override
	public void disableAnnotationSelection() {
		synchronized (m_lock) {
			m_annotationSelection = false;
		}
	}

//	/**
//	 * Returns an array of selected node indices.
//	 *
//	 * @return An array of selected node indices.
//	 */
//	@Override
//	public long[] getSelectedNodeIndices() {
//		// XXX: why isn't this synchronized on m_selectedNodes?
//		synchronized (m_lock) {
//			// all nodes from the btree
//			final LongEnumerator elms = m_selectedNodes.searchRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
//			final long[] returnThis = new long[elms.numRemaining()];
//
//			for (int i = 0; i < returnThis.length; i++)
//				// GINY requires all node indices to be negative (why?),
//				// hence the bitwise complement here.
//				returnThis[i] = elms.nextLong();
//
//			return returnThis;
//		}
//	}
//
//	/**
//	 * Returns a list of selected node objects.
//	 */
//	@Override
//	public List<CyNode> getSelectedNodes() {
//		synchronized (m_lock) {
//			// all nodes from the btree
//			final LongEnumerator elms = m_selectedNodes.searchRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
//			final ArrayList<CyNode> returnThis = new ArrayList<>();
//
//			while (elms.numRemaining() > 0)
//				// GINY requires all node indices to be negative (why?),
//				// hence the bitwise complement here.
//				returnThis.add(model.getNode(elms.nextLong()));
//
//			return returnThis;
//		}
//	}
//
//	/**
//	 * Returns an array of selected edge indices.
//	 *
//	 * @return An array of selected edge indices.
//	 */
//	@Override
//	public long[] getSelectedEdgeIndices() {
//		synchronized (m_lock) {
//			final LongEnumerator elms = m_selectedEdges.searchRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
//			final long[] returnThis = new long[elms.numRemaining()];
//
//			for (int i = 0; i < returnThis.length; i++)
//				returnThis[i] = elms.nextLong();
//
//			return returnThis;
//		}
//	}
//
//	/**
//	 * Returns a list of selected edge objects.
//	 *
//	 * @return A list of selected edge objects.
//	 */
//	@Override
//	public List<CyEdge> getSelectedEdges() {
//		synchronized (m_lock) {
//			final LongEnumerator elms = m_selectedEdges.searchRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
//			final ArrayList<CyEdge> returnThis = new ArrayList<>();
//
//			while (elms.numRemaining() > 0)
//				returnThis.add(model.getEdge(elms.nextLong()));
//
//			return returnThis;
//		}
//	}

	public void setBackgroundPaint(Paint paint) {
		synchronized (m_lock) {
			if (paint instanceof Color) {
				m_backgroundCanvas.setBackground((Color) paint);
				m_networkCanvas.setBackground((Color)paint); // for antialiasing...
				m_foregroundCanvas.setBackground((Color)paint); // for antialiasing...
				setContentChanged();
			} else {
				logger.debug("DGraphView.setBackgroundPaint(), Color not found.");
			}
		}
	}

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

//	/**
//	 * Creates a NodeView only if one doesn't already exist.
//	 */
//	@Override
//	public NodeView addNodeView(final CyNode node) {
//		final DNodeView newView;
//
//		synchronized (m_lock) {
//			newView = addNodeViewInternal(node);
//
//			// View already exists.
//			if (newView == null)
//				return nodeViewMap.get(node);
//
//			setContentChanged();
//		}
//
//		// final GraphViewChangeListener listener = m_lis[0];
//
//		// if (listener != null) {
//		// 	listener.graphViewChanged(new GraphViewNodesRestoredEvent(this,
//		// 			makeList(newView.getModel())));
//		// }
//
//		return newView;
//	}
//
//	/**
//	 * Should synchronize around m_lock.
//	 */
//	private final DNodeView addNodeViewInternal(final CyNode node) {
//		final long nodeInx = node.getSUID();
//		final NodeView oldView = nodeViewMap.get(node);
//
//		if (oldView != null)
//			return null;
//
//		m_drawPersp.addNode(node);
//
//		final DNodeView dNodeView = new DNodeView(lexicon, this, node, serviceRegistrar);
//		
//		// WARNING: DO not call the following in view creation.  This is VERY slow.
//		//Boolean selected = getModel().getRow(node).get(CyNetwork.SELECTED, Boolean.class);
//		//if (selected != null && selected)
//		//	dNodeView.select();
//
//		nodeViewMap.put(node, dNodeView);
//		m_spacial.insert(nodeInx, m_defaultNodeXMin, m_defaultNodeYMin, 
//		                  m_defaultNodeXMax, m_defaultNodeYMax, dNodeView.getZPosition());
//		
//		getEventHelper().addEventPayload((CyNetworkView) this, (View<CyNode>) dNodeView, AddedNodeViewsEvent.class);
//		
//		return dNodeView;
//	}
//
//	@Override
//	public EdgeView addEdgeView(final CyEdge edge) {
//		if (edge == null)
//			throw new NullPointerException("edge is null");
//		
//		final NodeView sourceNode;
//		final NodeView targetNode;
//		final DEdgeView dEdgeView;
//		
//		synchronized (m_lock) {
//			final EdgeView oldView = edgeViewMap.get(edge);
//
//			if (oldView != null)
//				return oldView;
//		
//			sourceNode = addNodeViewInternal(edge.getSource());
//			targetNode = addNodeViewInternal(edge.getTarget());
//
//			m_drawPersp.addEdge(edge);
//
//			dEdgeView = new DEdgeView(this, edge, handleFactory, lexicon, serviceRegistrar);
//
//			edgeViewMap.put(edge, dEdgeView);
//			setContentChanged();
//		}
//
//		// Under no circumstances should we be holding m_lock when the listener
//		// events are fired.
//		/* final GraphViewChangeListener listener = m_lis[0];
//
//		if (listener != null) {
//			// Only fire this event if either of the nodes is new. The node
//			// will be null if it already existed.
//			if ((sourceNode != null) || (targetNode != null)) {
//				long[] nodeInx;
//
//				if (sourceNode == null) {
//					nodeInx = new long[] { targetNode.getCyNode().getSUID() };
//				} else if (targetNode == null) {
//					nodeInx = new long[] { sourceNode.getCyNode().getSUID() };
//				} else {
//					nodeInx = new long[] { sourceNode.getCyNode().getSUID(),
//							targetNode.getCyNode().getSUID()};
//				}
//
//				listener.graphViewChanged(new GraphViewNodesRestoredEvent(this,
//						makeNodeList(nodeInx, this)));
//			}
//
//			listener.graphViewChanged(new GraphViewEdgesRestoredEvent(this,
//					makeList(dEdgeView.getCyEdge())));
//		}
//		*/
//
//		getEventHelper().addEventPayload((CyNetworkView) this, (View<CyEdge>) dEdgeView, AddedEdgeViewsEvent.class);
//		
//		return dEdgeView;
//	}
//
//	@Override
//	public NodeView removeNodeView(NodeView nodeView) {
//		return removeNodeView(nodeView.getCyNode().getSUID());
//	}
//
//	@Override
//	public NodeView removeNodeView(CyNode node) {
//		return removeNodeView(node.getSUID());
//	}
//
//	@Override
//	public NodeView removeNodeView(long suid) {
//		final List<CyEdge> hiddenEdgeInx;
//		final DNodeView returnThis;
//		final CyNode nnode = model.getNode(suid);
//		returnThis = (DNodeView) nodeViewMap.get(nnode);
//		
//		if (returnThis == null)
//			return null;
//		
//		getEventHelper().addEventPayload((CyNetworkView) this, (View<CyNode>) returnThis, AboutToRemoveNodeViewsEvent.class);
//		
//		synchronized (m_lock) {
//			// We have to query edges in the m_structPersp, not m_drawPersp
//			// because what if the node is hidden?
//			hiddenEdgeInx = model.getAdjacentEdgeList(nnode, CyEdge.Type.ANY);
//
//			// This isn't an error. Only if the nodeInx is invalid will
//			// getAdjacentEdgeIndicesArray
//			// return null. If there are no adjacent edges, then it will return
//			// an array of length 0.
//			if (hiddenEdgeInx == null)
//				return null;
//
//			for (final CyEdge ee : hiddenEdgeInx)
//				removeEdgeViewInternal(ee);
//
//			nodeViewMap.remove(nnode);
//			returnThis.unselectInternal();
//
//			// If this node was hidden, it won't be in m_drawPersp.
//			m_drawPersp.removeNodes(Collections.singletonList(nnode));
//			// m_structPersp.removeNode(nodeInx);
//			m_nodeDetails.unregisterNode(nnode);
//
//			// If this node was hidden, it won't be in m_spacial.
//			m_spacial.delete(suid);
//
//			setContentChanged();
//		}
//
//		/*
//		final GraphViewChangeListener listener = m_lis[0];
//
//		if (listener != null) {
//			if (hiddenEdgeInx.size() > 0) {
//				listener.graphViewChanged(new GraphViewEdgesHiddenEvent(this, hiddenEdgeInx));
//			}
//
//			listener.graphViewChanged(new GraphViewNodesHiddenEvent(this,
//					makeList(returnThis.getModel())));
//		}
//		*/
//
//		return returnThis;
//	}
//
//	@Override
//	public EdgeView removeEdgeView(EdgeView edgeView) {
//		return removeEdgeView(edgeView.getCyEdge().getSUID());
//	}
//
//	@Override
//	public EdgeView removeEdgeView(CyEdge edge) {
//		return removeEdgeView(edge.getSUID());
//	}
//
//	@Override
//	public EdgeView removeEdgeView(long suid) {
//		final DEdgeView returnThis;
//		final CyEdge edge = model.getEdge(suid);
//		
//		if (edge == null)
//			return null;
//		
//		EdgeView view = edgeViewMap.get(edge);
//		
//		if (view == null)
//			return null;
//		
//		getEventHelper().addEventPayload((CyNetworkView) this, (View<CyEdge>) view, AboutToRemoveEdgeViewsEvent.class);
//		
//		synchronized (m_lock) {
//			returnThis = removeEdgeViewInternal(edge);
//
//			if (returnThis != null)
//				setContentChanged();
//		}
//
//		/*
//		if (returnThis != null) {
//			final GraphViewChangeListener listener = m_lis[0];
//
//			if (listener != null) {
//				listener.graphViewChanged(new GraphViewEdgesHiddenEvent(this, makeList(edge)));
//			}
//		}
//		*/
//
//		return returnThis;
//	}

	
	public boolean isNodeSelected(long suid) {
		return m_nodeDetails.isSelected(getViewModelSnapshot().getNodeView(suid));
	}
	
	public boolean isEdgeSelected(long suid) {
		return m_edgeDetails.isSelected(getViewModelSnapshot().getEdgeView(suid));
	}
	
	public NodeDetails getNodeDetails() {
		return m_nodeDetails;
	}
	
	public EdgeDetails getEdgeDetails() {
		return m_edgeDetails;
	}
	
	public boolean isDirty() {
		return isContentChanged() || isViewportChanged();
	}
	
	public boolean isContentChanged() {
		return contentChanged;
	}
	
	public void setContentChanged() {
		setContentChanged(true);
	}
	
	private void setContentChanged(final boolean b) {
		contentChanged = b;
	}
	
	public boolean isViewportChanged() {
		return viewportChanged;
	}
	
	public void setViewportChanged() {
		setViewportChanged(true);
	}
	
	private void setViewportChanged(final boolean b) {
		viewportChanged = b;
	}
	
//	/**
//	 * Should synchronize around m_lock.
//	 */
//	private DEdgeView removeEdgeViewInternal(CyEdge edge) {
//		// We can't remove this yet, because the map is used
//		// later in unselectInternal...
//		final DEdgeView returnThis = (DEdgeView)edgeViewMap.get(edge);
//
//		if (returnThis == null) {
//			return returnThis;
//		}
//
//		returnThis.unselectInternal();
//
//		// Now we can remove it
//		edgeViewMap.remove(edge);
//		m_drawPersp.removeEdges(Collections.singletonList(edge)); 
//		m_edgeDetails.unregisterEdge(edge);
//
//		return returnThis;
//	}



	public double getZoom() {
		return m_networkCanvas.m_scaleFactor;
	}

	/**
	 * Set the zoom level and redraw the view.
	 */
	public void setZoom(double zoom) {
		synchronized (m_lock) {
			m_networkCanvas.m_scaleFactor = checkZoom(zoom, m_networkCanvas.m_scaleFactor);
			setViewportChanged();
		}
	}

	private void fitContent(final boolean updateView) {
		serviceRegistrar.getService(CyEventHelper.class).flushPayloadEvents();

		// MKTODO why does this have to be on the edt?
		invokeOnEDT(() -> {
			synchronized (m_lock) {
				CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
				if(netViewSnapshot.getNodeCount() == 0)
					return;
				if (m_networkCanvas.getWidth() == 0 || m_networkCanvas.getHeight() == 0)
					return;
				
				double[] extentsBuff = new double[4];
				netViewSnapshot.getSpacialIndex2D().getMBR(extentsBuff);
	
				// Adjust the content based on the foreground canvas
				m_foregroundCanvas.adjustBounds(extentsBuff);
				// Adjust the content based on the background canvas
				m_backgroundCanvas.adjustBounds(extentsBuff);
	
				CyNetworkView netView = netViewSnapshot.getMutableNetworkView();
				
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, (extentsBuff[0] + extentsBuff[2]) / 2.0d);
				
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION))
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, (extentsBuff[1] + extentsBuff[3]) / 2.0d);
	
				if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
					// Apply a factor 0.98 to zoom, so that it leaves a small border around the network and any annotations.
					final double zoom = Math.min(((double) m_networkCanvas.getWidth()) / 
					                             (extentsBuff[2] - extentsBuff[0]), 
					                              ((double) m_networkCanvas.getHeight()) / 
					                             (extentsBuff[3] - extentsBuff[1])) * 0.98;
					// Update view model.  Zoom Level should be modified.
					netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
				}
			}
			
			if (updateView)
				updateView(false);
		});
	}
	
	@Override
	public void handleFitContent() {
		fitContent(/* updateView = */ true);
	}
	
	@Override
	public void handleUpdateView() {
		updateView(true);
	}
	
	public void updateView() {
		updateView(false);
	}
	
	private void updateView(final boolean forceRedraw) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
		invokeOnEDTAndWait(() -> {
			if (forceRedraw)
				setContentChanged();
			
			m_networkCanvas.repaint();
			
			//Check if image size has changed if so, visual property needs to be changed as well
			if (m_networkCanvas.getWidth() != imageWidth || m_networkCanvas.getHeight() != imageHeight) {
				imageWidth = m_networkCanvas.getWidth();
				imageHeight = m_networkCanvas.getHeight();
				CyNetworkView netView = getViewModel();
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, (double) imageWidth);
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double) imageHeight);
			}
		});
		
		// MKTODO what is this event for ??
		eventHelper.fireEvent(new UpdateNetworkPresentationEvent(getViewModel()));
	}

//	/**
//	 * Returns an iterator of all node views, including those that are currently hidden.
//	 */
//	@Override
//	public Iterator<NodeView> getNodeViewsIterator() {
//		synchronized (m_lock) {
//			return nodeViewMap.values().iterator();
//		}
//	}
//
//	/**
//	 * Returns the count of all node views, including those that are currently hidden.
//	 */
//	@Override
//	public int getNodeViewCount() {
//		synchronized (m_lock) {
//			return nodeViewMap.size();
//		}
//	}
//
//	/**
//	 * Returns the count of all edge views, including those that are currently hidden.
//	 */
//	@Override
//	public int getEdgeViewCount() {
//		synchronized (m_lock) {
//			return edgeViewMap.size();
//		}
//	}
//
//	@Override
//	public DNodeView getDNodeView(final CyNode node) {
//		// TODO: remove cast!
//		if (node != null && nodeViewMap.containsKey(node))
//			return (DNodeView)nodeViewMap.get(node);
//		else
//			return null;
//	}
//	
//	@Override
//	public DNodeView getDNodeView(final long suid) {
//		return getDNodeView(model.getNode(suid));
//	}
//
//	@Override
//	public List<EdgeView> getEdgeViewsList() {
//		synchronized (m_lock) {
//			final ArrayList<EdgeView> returnThis = new ArrayList<>(edgeViewMap.size());
//			final Iterator<EdgeView> values = edgeViewMap.values().iterator();
//
//			while (values.hasNext())
//				returnThis.add(values.next());
//
//			return returnThis;
//		}
//	}
//
//	/**
//	 * Returns all edge views (including the hidden ones) that are either 1.
//	 * directed, having oneNode as source and otherNode as target or 2.
//	 * undirected, having oneNode and otherNode as endpoints. Note that this
//	 * behaviour is similar to that of CyNetwork.edgesList(Node, Node).
//	 */
//	@Override
//	public List<EdgeView> getEdgeViewsList(CyNode oneNode, CyNode otherNode) {
//		synchronized (m_lock) {
//			List<CyEdge> edges = model.getConnectingEdgeList(oneNode,
//					otherNode, CyEdge.Type.ANY);
//
//			if (edges == null) {
//				return null;
//			}
//
//			final ArrayList<EdgeView> returnThis = new ArrayList<>();
//			Iterator<CyEdge> it = edges.iterator();
//
//			while (it.hasNext()) {
//				CyEdge e = it.next();
//				EdgeView ev = getDEdgeView(e);
//				if (ev != null)
//					returnThis.add(ev);
//			}
//
//			return returnThis;
//		}
//	}
//
//	/**
//	 * Similar to getEdgeViewsList(Node, Node), only that one has control of whether or not to include undirected edges.
//	 */
//	@Override
//	public List<EdgeView> getEdgeViewsList(long oneNodeSUID, long otherNodeSUID, boolean includeUndirected) {
//		CyNode n1;
//		CyNode n2;
//		synchronized (m_lock) {
//			n1 = model.getNode(oneNodeSUID);
//			n2 = model.getNode(otherNodeSUID);
//		}
//		return getEdgeViewsList(n1, n2);
//	}
//
//
//	@Override
//	public DEdgeView getDEdgeView(final long suid) {
//		return getDEdgeView(model.getEdge(suid));
//	}
//
//	@Override
//	public Iterator<EdgeView> getEdgeViewsIterator() {
//		return edgeViewMap.values().iterator();
//	}
//
//	
//	@Override
//	public DEdgeView getDEdgeView(final CyEdge edge) {
//		if (edge == null) {
//			return null;
//		}
//		return (DEdgeView)edgeViewMap.get(edge);
//	}
//
//	@Override
//	public int edgeCount() {
//		return getEdgeViewCount();
//	}
//
//	@Override
//	public int nodeCount() {
//		return getNodeViewCount();
//	}
//
//	@Override
//	public boolean hideGraphObject(Object obj) {
//		return hideGraphObjectInternal(obj, true);
//	}
//
//	private boolean hideGraphObjectInternal(Object obj, boolean fireListenerEvents) {
//		if (obj instanceof DEdgeView) {
//			final DEdgeView eView = (DEdgeView) obj;
//			final CyEdge edge;
//
//			synchronized (m_lock) {
//				edge = eView.getCyEdge();
//
//				if (!m_drawPersp.removeEdges(Collections.singletonList(edge)))
//					return false;
//
//				eView.unselectInternal();
//				setContentChanged();
//			}
//
//			return true;
//		} else if (obj instanceof DNodeView) {
//			List<CyEdge> edges;
//			long nodeInx;
//			CyNode nnode;
//
//			synchronized (m_lock) {
//				final DNodeView nView = (DNodeView) obj;
//				nodeInx = nView.getCyNode().getSUID();
//				nnode = model.getNode(nodeInx);
//				
//				// If the node is already hidden, don't do anything.
//				if (m_drawPersp.getNode(nodeInx) == null)
//					return false;
//				
//				edges = m_drawPersp.getAdjacentEdgeList(nnode, CyEdge.Type.ANY);
//
//				if (edges != null) {
//					for (final CyEdge ee : edges)
//						hideGraphObjectInternal(edgeViewMap.get(ee), false);
//				}
//
//				nView.unselectInternal();
//				m_spacial.exists(nodeInx, m_extentsBuff, 0);
//				nView.m_hiddenXMin = m_extentsBuff[0];
//				nView.m_hiddenYMin = m_extentsBuff[1];
//				nView.m_hiddenXMax = m_extentsBuff[2];
//				nView.m_hiddenYMax = m_extentsBuff[3];
//				m_drawPersp.removeNodes(Collections.singletonList(nnode));
//				m_spacial.delete(nodeInx);
//				setContentChanged();
//			}
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	final boolean isHidden(final DEdgeView edgeView) {
//		// synchronized (m_lock) {
//			final long suid = edgeView.getCyEdge().getSUID();
//			return !m_drawPersp.containsEdge(m_drawPersp.getEdge(suid));
//		// }
//	}
//
//	final boolean isHidden(final DNodeView nodeView) {
//		// synchronized (m_lock) {
//			final long suid = nodeView.getCyNode().getSUID();
//			return !m_drawPersp.containsNode(m_drawPersp.getNode(suid));
//		// }
//	}
//
//	@Override
//	public boolean showGraphObject(Object obj) {
//		return showGraphObjectInternal(obj, true);
//	}
//
//	private boolean showGraphObjectInternal(Object obj, boolean fireListenerEvents) {
//		if (obj instanceof DNodeView) {
//			long nodeInx;
//			final DNodeView nView = (DNodeView) obj;
//
//			synchronized (m_lock) {
//				nodeInx = nView.getCyNode().getSUID();
//				CyNode nnode = model.getNode(nodeInx);
//
//				if (nnode == null || !m_drawPersp.addNode(nnode))
//					return false;
//
//				m_spacial.insert(nodeInx, nView.m_hiddenXMin, nView.m_hiddenYMin, nView.m_hiddenXMax,
//						nView.m_hiddenYMax, nView.getZPosition());
//				setContentChanged();
//			}
//
//			/*
//			if (fireListenerEvents) {
//				final GraphViewChangeListener listener = m_lis[0];
//
//				if (listener != null) {
//					listener.graphViewChanged(new GraphViewNodesRestoredEvent(
//							this, makeList(nView.getModel())));
//				}
//			}
//			*/
//
//			return true;
//		} else if (obj instanceof DEdgeView) {
//			CyNode sourceNode;
//			CyNode targetNode;
//			CyEdge newEdge;
//
//			synchronized (m_lock) {
//				final CyEdge edge = model.getEdge(((DEdgeView) obj).getCyEdge().getSUID());
//
//				if (edge == null)
//					return false;
//
//				// The edge exists in m_structPersp, therefore its source and target
//				// node views must also exist.
//				sourceNode = edge.getSource();
//				targetNode = edge.getTarget();
//				final DNodeView srcDnv = getDNodeView(sourceNode);
//				final DNodeView tgtDnv = getDNodeView(targetNode);
//
//				if (isHidden(srcDnv) || isHidden(tgtDnv))
//					return false;
//				
//				if (!showGraphObjectInternal(srcDnv, false))
//					sourceNode = null;
//
//				if (!showGraphObjectInternal(tgtDnv, false))
//					targetNode = null;
//
//				newEdge = edge;
//
//				if (!m_drawPersp.addEdge(newEdge))
//					return false;
//
//				setContentChanged();
//			}
//
//			return true;
//		} else {
//			return false;
//		}
//	}
//
//	@Override
//	public boolean hideGraphObjects(List<? extends GraphViewObject> objects) {
//		final Iterator<? extends GraphViewObject> it = objects.iterator();
//
//		while (it.hasNext())
//			hideGraphObject(it.next());
//
//		return true;
//	}
//
//	@Override
//	public boolean showGraphObjects(List<? extends GraphViewObject> objects) {
//		final Iterator<? extends GraphViewObject> it = objects.iterator();
//
//		while (it.hasNext())
//			showGraphObject(it.next());
//
//		return true;
//	}

	// Auxiliary methods specific to this GraphView implementation:
	
	public void setCenter(double x, double y) {
		synchronized (m_lock) {
            m_networkCanvas.setCenter(x,y);
			setViewportChanged();
			
			// Update view model
			// TODO: don't do it from here?
			CyNetworkView netView = getViewModel();
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, m_networkCanvas.m_xCenter);
			netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_networkCanvas.m_yCenter);
		}
	}

	public Point2D getCenter() {
		synchronized (m_lock) {
			return new Point2D.Double(m_networkCanvas.m_xCenter, m_networkCanvas.m_yCenter);
		}
	}
	
	@Override
	public void handleFitSelected() {
		fitSelected();
	}

	public void fitSelected() {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
//		synchronized (m_lock) {
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			SpacialIndex2D spacial = netViewSnapshot.getSpacialIndex2D();
			Collection<View<CyNode>> selectedElms = netViewSnapshot.getSelectedNodes();
			if(selectedElms.isEmpty())
				return;
			
			// MKTODO add edge selection???
//			LongEnumerator selectedElms = m_selectedNodes.searchRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false);
//
//			// Only check for selected edges if we don't have selected nodes.
//			if (selectedElms.numRemaining() == 0 && edgeSelectionEnabled()) {
//				selectedElms = getSelectedEdgeNodes();
//				if (selectedElms.numRemaining() == 0)
//					return;
//			}
			
			float[] extentsBuff = new float[4];

			float xMin = Float.POSITIVE_INFINITY;
			float yMin = Float.POSITIVE_INFINITY;
			float xMax = Float.NEGATIVE_INFINITY;
			float yMax = Float.NEGATIVE_INFINITY;

			View<CyNode> leftMost = null;
			View<CyNode> rightMost = null;

			for(View<CyNode> nodeView : selectedElms) {
				spacial.get(nodeView.getSUID(), extentsBuff);
				if (extentsBuff[0] < xMin) {
					xMin = extentsBuff[0];
					leftMost = nodeView;
				}

				if (extentsBuff[2] > xMax) {
					xMax = extentsBuff[2];
					rightMost = nodeView;
				}

				yMin = Math.min(yMin, extentsBuff[1]);
				yMax = Math.max(yMax, extentsBuff[3]);
			}

			xMin = xMin - (getLabelWidth(leftMost) / 2);
			xMax = xMax + (getLabelWidth(rightMost) / 2);

			CyNetworkView netView = netViewSnapshot.getMutableNetworkView();
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
				double zoom = Math.min(((double) m_networkCanvas.getWidth())
						/ (((double) xMax) - ((double) xMin)),
						((double) m_networkCanvas.getHeight())
								/ (((double) yMax) - ((double) yMin)));
				zoom = checkZoom(zoom, m_networkCanvas.m_scaleFactor);
				
				// Update view model.  Zoom Level should be modified.
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, zoom);
			}
			
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)) {
				double xCenter = (((double) xMin) + ((double) xMax)) / 2.0d;
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, xCenter);
			}
			
			if (!netView.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
				double yCenter = (((double) yMin) + ((double) yMax)) / 2.0d;
				netView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, yCenter);
			}
//		}
			
		updateView(false);
	}

//	/**
//	 * @return An LongEnumerator listing the nodes that are endpoints of the currently selected edges.
//	 */
//	private LongEnumerator getSelectedEdgeNodes() {
//		synchronized (m_lock) {
//			final LongEnumerator selectedEdges = m_selectedEdges.searchRange(Integer.MIN_VALUE,Integer.MAX_VALUE,false);
//
//			final LongHash nodeIds = new LongHash();
//
//			while (selectedEdges.numRemaining() > 0) {
//				final long edge = selectedEdges.nextLong();
//				CyEdge currEdge = model.getEdge(edge); 
//
//				CyNode source = currEdge.getSource();
//				long sourceId = source.getSUID();
//				nodeIds.put(sourceId);
//
//				CyNode target = currEdge.getTarget();
//				long targetId = target.getSUID();
//				nodeIds.put(targetId);
//			}
//
//			return nodeIds.elements();
//		}
//	}

	private int getLabelWidth(View<CyNode> nodeView) {
		if (nodeView == null)
			return 0;

		String s = m_nodeDetails.getLabelText(nodeView);
		if (s == null)
			return 0;

		char[] lab = s.toCharArray();
		if (lab == null)
			return 0;

		if (m_networkCanvas.m_fontMetrics == null)
			return 0;

		return m_networkCanvas.m_fontMetrics.charsWidth(lab, 0, lab.length);
	}

	public void setGraphLOD(GraphLOD lod) {
		synchronized (m_lock) {
			m_networkCanvas.m_lod[0] = lod;
			setContentChanged();
		}
	}

	public GraphLOD getGraphLOD() {
		return m_networkCanvas.m_lod[0];
	}

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
	public List<Long> getNodesIntersectingRectangle(double xMinimum, double yMinimum, double xMaximum,
	                                          double yMaximum, boolean treatNodeShapesAsRectangle) {
//		synchronized (m_lock) {
		
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			
			final float xMin = (float) xMinimum;
			final float yMin = (float) yMinimum;
			final float xMax = (float) xMaximum;
			final float yMax = (float) yMaximum;
			
			SpacialIndex2DEnumerator<Long> under = netViewSnapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
			if(!under.hasNext())
				return Collections.emptyList();
			
			List<Long> returnVal = new ArrayList<>(under.size());
			
			if (treatNodeShapesAsRectangle) {
				while(under.hasNext()) {
					returnVal.add(under.next());
				}
			} else {
				final double x = xMin;
				final double y = yMin;
				final double w = ((double) xMax) - xMin;
				final double h = ((double) yMax) - yMin;

				float[] extentsBuff = new float[4];
				
				while (under.hasNext()) {
					final long suid = under.nextExtents(extentsBuff);
					View<CyNode> cyNode = netViewSnapshot.getNodeView(suid);

					// The only way that the node can miss the intersection
					// query is
					// if it intersects one of the four query rectangle's
					// corners.
					if (((extentsBuff[0] < xMin) && (extentsBuff[1] < yMin))
					    || ((extentsBuff[0] < xMin) && (extentsBuff[3] > yMax))
					    || ((extentsBuff[2] > xMax) && (extentsBuff[3] > yMax))
					    || ((extentsBuff[2] > xMax) && (extentsBuff[1] < yMin))) {
						
						GeneralPath path = new GeneralPath();
						m_networkCanvas.m_grafx.getNodeShape(m_nodeDetails.getShape(cyNode),
								extentsBuff[0], extentsBuff[1],
								extentsBuff[2], extentsBuff[3], path);

						if ((w > 0) && (h > 0)) {
							if (path.intersects(x, y, w, h))
								returnVal.add(suid);
						} else {
							if (path.contains(x, y))
								returnVal.add(suid);
						}
					} else
						returnVal.add(suid);
				}
			}
			return returnVal;
//		}
	}

	public List<Long> queryDrawnEdges(int xMin, int yMin, int xMax, int yMax) {
		synchronized (m_lock) {
			return m_networkCanvas.computeEdgesIntersecting(xMin, yMin, xMax, yMax);
		}
	}

	/**
	 * Extents of the nodes.
	 * Called by the birds-eye-view
	 */
	public boolean getExtents(double[] extentsBuff) {
//		synchronized (m_lock) {
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			if(netViewSnapshot.getNodeCount() == 0) {
				Arrays.fill(extentsBuff, 0.0);
				return false;
			}
			netViewSnapshot.getSpacialIndex2D().getMBR(extentsBuff);
			return true;
//		}
	}

	public void xformComponentToNodeCoords(double[] coords) {
		synchronized (m_lock) {
			if (m_networkCanvas != null && m_networkCanvas.m_grafx != null)
				m_networkCanvas.m_grafx.xformImageToNodeCoords(coords);
		}
	}
	
	public void xformNodeToComponentCoords(double[] coords) {
		synchronized (m_lock) {
			if (m_networkCanvas != null && m_networkCanvas.m_grafx != null)
				m_networkCanvas.m_grafx.xformNodetoImageCoords(coords);
		}
	}

	/**
	 * This method is called by the BirdsEyeView to get a snapshot of the graphics.
	 */
	//TODO: Need to fix up scaling and sizing.  
	public void drawSnapshot(VolatileImage img, GraphLOD lod, Paint bgPaint, 
	                         double xMin, double yMin, double xCenter,
	                         double yCenter, double scaleFactor) {
		// First paint the background
		m_backgroundCanvas.drawCanvas(img, xMin, yMin, xCenter, yCenter, scaleFactor);
		
		// final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
		final Set<VisualPropertyDependency<?>> dependencies =
				vmm.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();

		// synchronized (m_lock) {
		try {
			GraphRenderer.renderGraph(getViewModelSnapshot(), lod, m_nodeDetails,
			                          m_edgeDetails, new GraphGraphics(img, false, false),
			                          bgPaint, xCenter, yCenter, scaleFactor, haveZOrder, dependencies);
		} catch (Exception e) { 
			// We probably had a node or edge view removed out from underneath us.  Just quietly return, but
			// set content changed so we redraw again
			setContentChanged();
		}
		// }

		// Finally, draw the foreground
		m_foregroundCanvas.drawCanvas(img, xMin, yMin, xCenter, yCenter, scaleFactor);
	}

	public void drawSnapshot(
		VolatileImage img,
		GraphLOD lod,
		Paint bgPaint, 
		double xMin,
		double yMin,
		double xCenter,
		double yCenter,
		double scaleFactor,
		List<View<CyNode>> nodes,
		List<View<CyEdge>> edges
	) {
		if (!largeModel) { // || (nodes.size() + edges.size()) >= (m_drawPersp.getNodeCount() + m_drawPersp.getEdgeCount()) / 4) {
			drawSnapshot(img, lod, bgPaint, xMin, yMin, xCenter, yCenter, scaleFactor);
			return;
		}

		try {
			synchronized (m_lock) {
				renderSubgraph(new GraphGraphics(img, false, false), lod, bgPaint, xCenter, yCenter, scaleFactor, nodes, edges);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int renderSubgraph(GraphGraphics graphics, final GraphLOD lod, 
	                   Paint bgColor, double xCenter, double yCenter, double scale,
	                   List<View<CyNode>> nodeList, List<View<CyEdge>> edgeList) {

		// If we're updateing more then 1/4 of the nodes or edges, just redraw the entire network to avoid
		// the overhead of creating the SpacialIndex2D and CySubNetwork
//		if (!largeModel) // || ((nodeList.size() + edgeList.size()) >= (m_drawPersp.getNodeCount() + m_drawPersp.getEdgeCount())/4))
			return renderGraph(graphics, lod, bgColor, xCenter, yCenter, scale);
//
////		// Make a copy of the nodes and edges arrays to avoid a conflict with selection events
////		// The assumption here is that these arrays are relatively small
////		List<CyNode> nodes = new ArrayList<>(nodeList);
////		List<CyEdge> edges = new ArrayList<>(edgeList);
//
//		// Make sure the graphics is initialized
//		if (!graphics.isInitialized())
//			graphics.clear(bgColor, xCenter, yCenter, scale);
//
//		Color bg = (Color)bgColor;
//		
//		if (bg != null)
//			bg = new Color(bg.getRed(), bg.getBlue(), bg.getGreen(), 0);
//
//		// Create our private spacial index.
////		final SpacialIndex2DFactory spacialFactory = serviceRegistrar.getService(SpacialIndex2DFactory.class);
////		SpacialIndex2D sub_spacial = spacialFactory.createSpacialIndex2D();
//
//		// And our private subnetwork
////		CySubNetwork net = new MinimalNetwork(SUIDFactory.getNextSUID());
//
//		for (View<CyEdge> edge : edgeList) {
//			SnapshotEdgeInfo edgeInfo = getViewModelSnapshot().getEdgeInfo(edge);
//			nodeList.add(edgeInfo.getTargetNodeView());
//			nodeList.add(edgeInfo.getSourceNodeView());
//		}
//		
////		for (CyNode node: nodes) {
////			long idx = node.getSUID();
////			if (m_spacial.exists(idx, m_extentsBuff, 0)) {
////				if (!sub_spacial.exists(idx, new float[4], 0))
////					sub_spacial.insert(idx, m_extentsBuff[0], m_extentsBuff[1], m_extentsBuff[2], m_extentsBuff[3], 0.0);
//////				net.addNode(node);
////			}
////		}
//		
////		for (CyEdge edge: edges) {
////			net.addEdge(edge);
////		}
//
//		int lastRenderDetail = 0;
//		
//		try {
//			// final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
//			final Set<VisualPropertyDependency<?>> dependencies =
//					vmm.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();
//			
////			synchronized (m_lock) {
//				lastRenderDetail = GraphRenderer.renderGraph(this, sub_spacial, lod, m_nodeDetails, m_edgeDetails, hash,
//						graphics, null, xCenter, yCenter, scale, haveZOrder, dependencies);
////			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		setContentChanged(false);
//		setViewportChanged(false);
//		m_visualChanged = true;
//
//		return lastRenderDetail;
	}
	
	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	int renderGraph(GraphGraphics graphics, final GraphLOD lod, Paint bgColor, double xCenter, double yCenter, double scale) {
		int lastRenderDetail = 0;
		
		try {
//			synchronized (m_lock) {
				// final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
			Set<VisualPropertyDependency<?>> dependencies = vmm.getVisualStyle(getViewModel()).getAllVisualPropertyDependencies();
				
			lastRenderDetail = GraphRenderer.renderGraph(getViewModelSnapshot(),
			  						     lod,
			  						     m_nodeDetails,
			  						     m_edgeDetails,
			  						     graphics, bgColor, xCenter,
			  						     yCenter, scale, haveZOrder,
			  						     dependencies);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		setContentChanged(false);
		setViewportChanged(false);
		m_visualChanged = true;
		
		return lastRenderDetail;
	}

	public void addContentChangeListener(ContentChangeListener l) {
		m_cLis[0] = ContentChangeListenerChain.add(m_cLis[0], l);
	}

	public void removeContentChangeListener(ContentChangeListener l) {
		m_cLis[0] = ContentChangeListenerChain.remove(m_cLis[0], l);
	}

	public ContentChangeListener getContentChangeListener() {
		return m_cLis[0];
	}

	public void addViewportChangeListener(ViewportChangeListener l) {
		m_vLis[0] = ViewportChangeListenerChain.add(m_vLis[0], l);
	}

	public void removeViewportChangeListener(ViewportChangeListener l) {
		m_vLis[0] = ViewportChangeListenerChain.remove(m_vLis[0], l);
	}

	@Override
	public int print(Graphics g, PageFormat pageFormat, int page) {
		if (page == 0) {
			((Graphics2D) g).translate(pageFormat.getImageableX(),
					pageFormat.getImageableY());

			// make sure the whole image on the screen will fit to the printable
			// area of the paper
			double image_scale = Math.min(pageFormat.getImageableWidth()  / m_networkCanvas.getWidth(),
										  pageFormat.getImageableHeight() / m_networkCanvas.getHeight());

			if (image_scale < 1.0d) {
				((Graphics2D) g).scale(image_scale, image_scale);
			}

			// old school
			// g.clipRect(0, 0, getComponent().getWidth(),
			// getComponent().getHeight());
			// getComponent().print(g);

			// from InternalFrameComponent
			g.clipRect(0, 0, m_backgroundCanvas.getWidth(), m_backgroundCanvas.getHeight());
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
	private Image createImage(int width, final int height, double shrink, final boolean skipBackground) {
		// Validate arguments
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("DGraphView.createImage(int width, int height): "
							   + "width and height arguments must be greater than zero");

		if (shrink < 0 || shrink > 1.0) {
			logger.debug("DGraphView.createImage(width,height,shrink) shrink is invalid: "
			                   + shrink + "  using default of 1.0");
			shrink = 1.0;
		}

		final double scale = shrink;
		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		invokeOnEDTAndWait(() -> {
			// Save current sizes, zoom and viewport position
			Dimension originalBgSize = m_backgroundCanvas.getSize();
			Dimension originalNetSize = m_networkCanvas.getSize();
			Dimension originalFgSize = m_foregroundCanvas.getSize();
			double zoom = getZoom();
			CyNetworkViewSnapshot netVewSnapshot = getViewModelSnapshot();
			
			Double centerX = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
			Double centerY = netVewSnapshot.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
			
			// Create image to return
			final Graphics g = image.getGraphics();
	
			if (!skipBackground) {
				// Paint background canvas into image
				m_backgroundCanvas.setSize(width, height);
				m_backgroundCanvas.paint(g);
			}
			
			// Paint inner canvas (network)
			m_networkCanvas.setSize(width, height);
			fitContent(/* updateView = */ false);
			setZoom(getZoom() * scale);
			m_networkCanvas.paint(g);
			
			// Paint foreground canvas
			m_foregroundCanvas.setSize(width, height);
			m_foregroundCanvas.paint(g);
			
			// Restore to to original size, zoom and viewport
			m_backgroundCanvas.setSize(originalBgSize);
			m_networkCanvas.setSize(originalNetSize);
			m_foregroundCanvas.setSize(originalFgSize);
			setZoom(zoom);
			
			// MKTODO why do we need to set these VPs here???
//			setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, centerX);
//			setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, centerY);
		});
		
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
	 */
	public View<CyNode> getPickedNodeView(Point2D pt) {
		View<CyNode> nv = null;
		double[] locn = new double[2];
		locn[0] = pt.getX();
		locn[1] = pt.getY();

		xformComponentToNodeCoords(locn);
		float x = (float) locn[0];
		float y = (float) locn[1];

		boolean treatNodeShapesAsRectangle = (m_networkCanvas.getLastRenderDetail() & GraphRenderer.LOD_HIGH_DETAIL) == 0;
		List<Long> suids = getNodesIntersectingRectangle(x, y, x, y, treatNodeShapesAsRectangle);

		CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
		
		// return node with topmost Z
		for(Long suid : suids) {
			View<CyNode> dnv = netViewSnapshot.getNodeView(suid);
			if (nv == null || m_nodeDetails.getZPosition(dnv) > m_nodeDetails.getZPosition(nv)) {
				nv = dnv;
			}
		}
		return nv;
	}

	public View<CyEdge> getPickedEdgeView(Point2D pt) {
		View<CyEdge> ev = null;
		List<Long> edges = queryDrawnEdges((int) pt.getX(), (int) pt.getY(), (int) pt.getX(), (int) pt.getY());

		long chosenEdge = edges.isEmpty() ? -1 : edges.get(edges.size()-1);
		if (chosenEdge >= 0) {
			CyNetworkViewSnapshot netViewSnapshot = getViewModelSnapshot();
			ev = netViewSnapshot.getEdgeView(chosenEdge);
		}
		return ev;
	}
	

	private double checkZoom(double zoom, double orig) {
		if (zoom > 0)
			return zoom;

		logger.debug("invalid zoom: " + zoom + "   using orig: " + orig);
		return orig;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

//	public boolean setSelected(final CyNode[] nodes) {
//		for ( CyNode node : nodes )
//			getDNodeView(node).select();
//		// TODO: update model
//
//		return true;
//	}
//	
//	public boolean setSelected(final CyEdge[] edges) {
//		for ( CyEdge edge : edges )
//			getDEdgeView(edge).setSelected(true);
//		// TODO: update model
//		
//		return true;
//	}
//
//	public List<NodeView> getNodeViewsList() {
//		ArrayList<NodeView> list = new ArrayList<>(getNodeViewCount());
//		for (CyNode nn : getNetwork().getNodeList())
//			list.add(getDNodeView(nn.getSUID()));
//
//		return list;
//	}

	/**
	 * This method is used by freehep lib to export network as graphics.
	 */
	public void print(Graphics g) {
		m_backgroundCanvas.print(g);
		m_networkCanvas.print(g);
		m_foregroundCanvas.print(g);
	}

	/**
	 * This method is used by BitmapExporter to export network as graphics (png, jpg, bmp)
	 */
	public void printNoImposter(Graphics g) {
		m_backgroundCanvas.print(g);
		m_networkCanvas.printNoImposter(g);
		m_foregroundCanvas.print(g);
	}

	/**
	 * Our implementation of Component setBounds(). If we don't do this, the
	 * individual canvas do not get rendered.
	 */
	public void setBounds(int x, int y, int width, int height) {
		// call reshape on each canvas
		m_backgroundCanvas.setBounds(x, y, width, height);
		m_networkCanvas.setBounds(x, y, width, height);
		m_foregroundCanvas.setBounds(x, y, width, height);
	
		// If this is the first call to setBounds, load any annotations
		if (!annotationsLoaded) {
			annotationsLoaded = true;
			cyAnnotator.loadAnnotations();
		}
	}

	public void setSize(Dimension d) {
		m_networkCanvas.setSize(d);
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
		List<X> nl = new ArrayList<>(1);
		nl.add(nodeOrEdge);
		return nl;
	}
//
//	static List<CyNode> makeNodeList(long[] nodeids, GraphView view) {
//		List<CyNode> l = new ArrayList<>(nodeids.length);
//		
//		for (long nid : nodeids)
//			l.add(((DNodeView)view.getDNodeView(nid)).getModel());
//
//		return l;
//	}
//
//	static List<CyEdge> makeEdgeList(long[] edgeids, GraphView view) {
//		if (view == null || edgeids == null || edgeids.length == 0)
//			return new ArrayList<>();
//		
//		List<CyEdge> l = new ArrayList<>(edgeids.length);
//		
//		for (long nid : edgeids) {
//			if (view.getDEdgeView(nid) != null)
//				l.add(view.getDEdgeView(nid).getCyEdge());
//		}
//
//		return l;
//	}

	@Override
	public Printable createPrintable() {
		return this;
	}
	
	@Override
	public Properties getProperties() {
		return this.props;
	}
	
	@Override 
	public Image createImage(int width, int height) {
		return createImage(width, height, 1, false);
	}

	@Override
	public DVisualLexicon getVisualLexicon() {
		return lexicon;
	}


	// For now the viewModelSnapshot should only be re-assigned on the EDT.
	public CyNetworkViewSnapshot getViewModelSnapshot() {
		return viewModelSnapshot;
	}
	
	@Override
	public CyNetworkView getViewModel() {
		return viewModel;
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int w, int h) {
		return VisualPropertyIconFactory.createIcon(value, w, h);
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

		// Handle non-square images
		// Get the height and width of the image
		int imageWidth = snapshotImage.getWidth();
		int imageHeight = snapshotImage.getHeight();
		double ratio = (double)imageHeight / (double) imageWidth;
		int adjustedWidth = (int)((double)width/ratio)+1;

		final Rectangle2D rect = new Rectangle2D.Double(-adjustedWidth / 2, -height / 2, adjustedWidth, height);
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
			logger.warn("Failed to convert a BufferedImage to a PNG. (" + e + ")");
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
			logger.warn("Failed to convert a PNG to a BufferedImage. (" + e + ")");
			return null;
		}
	}

	/**
	 * Listener for update flag of snapshot image.
	 */
	private final class DGraphViewContentChangeListener implements ContentChangeListener {
		public void contentChanged() {
			latest = false;
		}
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		final boolean contentChanged = isContentChanged();
		final boolean viewportChanged = isViewportChanged();
		
		// Check properties related to printing:
		boolean exportAsShape = false;
		final String exportAsShapeString = props.getProperty("exportTextAsShape");
		
		if (exportAsShapeString != null)
			exportAsShape = Boolean.parseBoolean(exportAsShapeString);
		
		setPrintingTextAsShape(exportAsShape);
		print(printCanvas);
		
		// Keep previous dirty flags, otherwise the actual view canvas may not be updated next time.
		// (this method is usually only used to export the View as image, create thumbnails, etc,
		// therefore it should not flag the Graph View as updated, because the actual view canvas
		// may still have to be redrawn after this).
		setContentChanged(contentChanged);
		setViewportChanged(viewportChanged);
	}

//
//
//	@Override
//	protected <T, V extends T> void applyVisualProperty(final VisualProperty<? extends T> vpOriginal, final V value) {
//		if (value == null) 
//			return;
//
//		//  
//		//  WARNING!!!!!!!
//		//  
//		//  No calls to other methods from this method should trigger calls to updateView().
//		//  Allowing this can cause deadlocks.  The expectation is that anyone using
//		//  setVisualProperty() should call updateView() themselves.
//		//  
//		
//		final VisualProperty<?> vp = vpOriginal;
//		
//		if (vp == DVisualLexicon.NETWORK_NODE_SELECTION) {
//			boolean b = ((Boolean) value).booleanValue();
//			if (b)
//				enableNodeSelection();
//			else
//				disableNodeSelection();
//		} else if (vp == DVisualLexicon.NETWORK_EDGE_SELECTION) {
//			boolean b = ((Boolean) value).booleanValue();
//			if (b)
//				enableEdgeSelection();
//			else
//				disableEdgeSelection();
//		} else if (vp == DVisualLexicon.NETWORK_ANNOTATION_SELECTION) {
//			boolean b = ((Boolean) value).booleanValue();
//			if (b)
//				enableAnnotationSelection();
//			else
//				disableAnnotationSelection();
//		} else if (vp == BasicVisualLexicon.NETWORK_BACKGROUND_PAINT) {
//			setBackgroundPaint((Paint) value);
//		} else if (vp == BasicVisualLexicon.NETWORK_CENTER_X_LOCATION) {
//			final double x = (Double) value;
//			if (x != m_networkCanvas.m_xCenter)
//				setCenter(x, m_networkCanvas.m_yCenter);
//		} else if (vp == BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION) {
//			final double y = (Double) value;
//			if (y != m_networkCanvas.m_yCenter)
//				setCenter(m_networkCanvas.m_xCenter, y);
//		} else if (vp == BasicVisualLexicon.NETWORK_SCALE_FACTOR) {
//			setZoom(((Double) value).doubleValue());
//		} else if (vp == BasicVisualLexicon.NETWORK_WIDTH) {
//			// This actually sets the size on the canvas, so we need to make sure
//			// this runs on the AWT thread
//			invokeOnEDT(() -> {
//				m_networkCanvas.setSize(((Double)value).intValue(), m_networkCanvas.getHeight());
//			});
//		} else if (vp == BasicVisualLexicon.NETWORK_HEIGHT) {
//			// This actually sets the size on the canvas, so we need to make sure
//			// this runs on the AWT thread
//			invokeOnEDT(() -> {
//				m_networkCanvas.setSize(m_networkCanvas.getWidth(), ((Double)value).intValue());
//			});
//		}
//	}
//
//	@Override
//	public void clearValueLock(final VisualProperty<?> vp) {
//		synchronized (m_lock) {
//			directLocks.remove(vp);
//			allLocks.remove(vp);
//			applyVisualProperty(vp, visualProperties.get(vp)); // always apply the regular vp
//		}
//	}
//	
//	@Override
//	public <T> T getVisualProperty(final VisualProperty<T> vp) {
//		Object value = null;
//		
//		if (vp == DVisualLexicon.NETWORK_NODE_SELECTION) {
//			value = nodeSelectionEnabled();
//		} else if (vp == DVisualLexicon.NETWORK_EDGE_SELECTION) {
//			value = edgeSelectionEnabled();
//		} else if (vp == BasicVisualLexicon.NETWORK_BACKGROUND_PAINT) {
//			value = getBackgroundPaint();
//		} else if (vp == BasicVisualLexicon.NETWORK_CENTER_X_LOCATION) {
//			value = getCenter().getX();
//		} else if (vp == BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION) {
//			value = getCenter().getY();
//		} else if (vp == BasicVisualLexicon.NETWORK_SCALE_FACTOR) {
//			value = getZoom();
//		} else {
//			value = super.getVisualProperty(vp);
//		}
//		
//		return (T) value;
//	}

//	@Override
//	public View<CyNode> getNodeView(final CyNode node) {
//		return (View<CyNode>) getDNodeView(node.getSUID());
//	}
//
//	@Override
//	public View<CyEdge> getEdgeView(final CyEdge edge) {
//		return (View<CyEdge>) getDEdgeView(edge.getSUID());
//	}
//
//	@Override
//	public void handleEvent(FitSelectedEvent e) {
//		if (e.getSource().equals(this)) {
//			fitSelected();
//		}
//	}
//
//	@Override
//	public void handleEvent(FitContentEvent e) {
//		if (e.getSource().equals(this)) {
//			fitContent();
//		}
//	}

//	@Override
//	@SuppressWarnings("unchecked")
//	public <T, V extends T> void setViewDefault(VisualProperty<? extends T> vp, V defaultValue) {
//		if (vp.shouldIgnoreDefault())
//			return;
//		
//		final Class<?> targetType = vp.getTargetDataType();
//		
//		// Visibility should be applied directly to each edge view.
//		if (vp == BasicVisualLexicon.EDGE_VISIBLE) {
//			// TODO: Run in parallel.  fork/join?
//			applyToAllEdges(vp, defaultValue);
//			return;
//		}
//		
//		// In DING, there is no default W, H, and D.
//		// Also, visibility should be applied directly to each node view.
//		if (vp == BasicVisualLexicon.NODE_SIZE 
//				|| vp == BasicVisualLexicon.NODE_WIDTH
//				|| vp == BasicVisualLexicon.NODE_HEIGHT
//				|| vp == BasicVisualLexicon.NODE_VISIBLE
//				|| CG_SIZE_PATTERN.matcher(vp.getIdString()).matches()) {
//			// TODO: Run in parallel.  fork/join?
//			applyToAllNodes(vp, defaultValue);
//			return;
//		}
//		
//		if ((VisualProperty<?>) vp instanceof CustomGraphicsVisualProperty) {
//			if (defaultValue != NullCustomGraphics.getNullObject()) {
//				applyToAllNodes(vp, defaultValue);
//				return;
//			}
//		}
//		
//		if (vp != DVisualLexicon.NODE_LABEL_POSITION && defaultValue instanceof ObjectPosition) {
//			// This is a CustomGraphicsPosition.
//			if (defaultValue.equals(ObjectPosition.DEFAULT_POSITION) == false) {
//				applyToAllNodes(vp, defaultValue);
//				return;
//			}
//		}
//		
//		if (targetType == CyNode.class) {
//			m_nodeDetails.clear();
//			nodeViewDefaultSupport.setViewDefault((VisualProperty<V>)vp, defaultValue);
//		} else if (targetType == CyEdge.class) {
//			// XXX: Why do we have to clear the edge details?
//			m_edgeDetails.clear();
//			edgeViewDefaultSupport.setViewDefault((VisualProperty<V>)vp, defaultValue);
//		} else if (targetType == CyNetwork.class) {
//			// For networks, just set as regular visual property value.  (No defaults)
//			this.setVisualProperty(vp, defaultValue);
//		}
//	}
//	
//	
//	private final <T, V extends T> void applyToAllNodes(final VisualProperty<? extends T> vp, final V defaultValue) {
//		final Collection<NodeView> nodes = this.nodeViewMap.values();
//		for (final NodeView n : nodes)
//			((DNodeView) n).setVisualProperty(vp, defaultValue);
//	}
//	
//	private final <T, V extends T> void applyToAllEdges(final VisualProperty<? extends T> vp, final V defaultValue) {
//		final Collection<EdgeView> edges = this.edgeViewMap.values();
//		for (final EdgeView e : edges)
//			((DEdgeView) e).setVisualProperty(vp, defaultValue);
//	}

	public CyAnnotator getCyAnnotator() {
		return cyAnnotator;
	}

//	@Override
//	public String toString() {
//		return "DGraphView: suid=" + suid + ", model=" + model;
//	}

	public void registerServices() {
		synchronized (this) {
			if (servicesRegistered)
				return;
			
			serviceRegistrar.registerAllServices(this, new Properties());
			servicesRegistered = true;
		}
	}
	
	@Override
	public void dispose() {
		synchronized (this) {
			redrawTimer.stop();
			
			if (!servicesRegistered)
				return;
			
			serviceRegistrar.unregisterAllServices(this);
			servicesRegistered = false;
			
			// m_lis[0] = null;
			cyAnnotator.dispose();
			serviceRegistrar.unregisterAllServices(cyAnnotator);
			m_networkCanvas.dispose();
		}
	}


//	@Override
//	protected <T, V extends T> V getDefaultValue(VisualProperty<T> vp) {
//		return null;
//	}
	
	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}
	
//	@Override
//	protected DGraphView getDGraphView() {
//		return this;
//	}

//	@Override
//	public void handleEvent(RowsSetEvent e) {	
//		if (!ignoreRowsSetEvents && e.containsColumn(CyNetwork.SELECTED)) {
//			Class<? extends CyIdentifiable> targetType = null;
//			
//			if (e.getSource() == getModel().getDefaultNodeTable())
//				targetType = CyNode.class;
//			else if (e.getSource() == getModel().getDefaultEdgeTable())
//				targetType = CyEdge.class;
//			
//			if (targetType != null)
//				updateSelection(targetType, e.getColumnRecords(CyNetwork.SELECTED));
//		}
//	}
//
//	public void setHaveZOrder(boolean z) {
//		haveZOrder = z;
//	}
//
//	// This is expensive and should only be called at initialization!!!
//	private void syncFilterAndView() {
//		final List<CyNode> flaggedNodes = CyTableUtil.getNodesInState(getModel(), CyNetwork.SELECTED, true);
//		final List<CyEdge> flaggedEdges = CyTableUtil.getEdgesInState(getModel(), CyNetwork.SELECTED, true);
//
//		final List<CyNode> selectedNodes = getSelectedNodes();
//		final List<CyEdge> selectedEdges = getSelectedEdges();
//
//		// select all nodes that are flagged but not currently selected
//		for (final CyNode node : flaggedNodes) {
//			final NodeView nv = getDNodeView(node);
//
//			if ((nv == null) || nv.isSelected())
//				continue;
//
//			nv.setSelected(true);
//		}
//
//		// select all edges that are flagged but not currently selected
//		for (final CyEdge edge : flaggedEdges) {
//			final EdgeView ev = getDEdgeView(edge);
//
//			if ((ev == null) || ev.isSelected())
//				continue;
//
//			ev.setSelected(true);
//		}
//
//		// flag all nodes that are selected but not currently flagged
//		select(selectedNodes, CyNode.class, true);
//
//		// flag all edges that are selected but not currently flagged
//		select(selectedEdges, CyEdge.class, true);
//	}

	/******************************************************
	 * Animation handling.  Currently, this only supports *
	 * edge marquee, but could be extended in the future  *
	 * to support other kinds of animations.              *
	 *****************************************************/
	@Override
	public void actionPerformed(ActionEvent e) {
		// If we're not even drawing dashed edges, no sense in trying to do marquee
		if ((m_networkCanvas.getLastRenderDetail() & GraphRenderer.LOD_DASHED_EDGES) == 0) {
			return;
		}
		
		// MKTODO make this work again
//
//		List<DEdgeView> removeMe = new ArrayList<>();
//		for (DEdgeView edgeView: animatedEdges) {
//			CyEdge edge = edgeView.getModel();
//			Stroke s = m_edgeDetails.getStroke(edge);
//			
//			if (s != null && s instanceof AnimatedStroke) { 
//				Stroke as = ((AnimatedStroke)s).newInstanceForNextOffset();
//				
//				synchronized (m_lock) {
//					m_edgeDetails.overrideStroke(edge, as);
//					setContentChanged();
//				}
//			} else if (s == null) {
//				removeMe.add(edgeView);
//			}
//		}
//
//		// We do this this way to avoid the overhead of concurrent maps since this should be relatively rare
//		if (removeMe.size() != 0) {
//			for (DEdgeView edgeView: removeMe)
//				animatedEdges.remove(edgeView);
//		}

		// Redraw?
		m_networkCanvas.repaint();
		//updateView();
	}
//
//	public void removeAnimatedEdge(DEdgeView edgeView) {
//		if (animatedEdges.contains(edgeView)) {
//			animatedEdges.remove(edgeView);
//
//			if (animatedEdges.size() == 0 && animationTimer.isRunning()) {
//				animationTimer.stop();
//			}
//		}
//	}
//
//	public void addAnimatedEdge(DEdgeView edgeView) {
//		if (!animatedEdges.contains(edgeView)) {
//			animatedEdges.add(edgeView);
//
//			if (!animationTimer.isRunning()) {
//				animationTimer.start();
//			}
//		}
//	}
	
	
	
	public void selectBySuid(Collection<Long> suids, Class<? extends CyIdentifiable> type, boolean selected) {
		if (suids == null || suids.isEmpty())
			return;
		
		List<RowSetRecord> records = new ArrayList<>();
		CyNetwork model = getViewModel().getModel();
		CyTable table = type.equals(CyNode.class) ? model.getDefaultNodeTable() : model.getDefaultEdgeTable();
		
		// Disable events
		// MKTODO With the new event coalesce logic in the event helper this shouldn't be necessary, but I'll leave it in for now.
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.silenceEventSource(table);

		try {
			for (Long suid : suids) {
				CyRow row = table.getRow(suid);
				row.set(CyNetwork.SELECTED, selected);		
				records.add(new RowSetRecord(row, CyNetwork.SELECTED, selected, selected));
			}
		} finally {
			eventHelper.unsilenceEventSource(table);
		}
		
		// Update the selection before firing the event to prevent race conditions
//		updateSelection(type, records);
		
		// Only now it can fire the RowsSetEvent
		fireRowsSetEvent(table, records, eventHelper);
	}
	
	
	public <T extends CyIdentifiable> void select(Collection<View<T>> nodesOrEdgeViews, Class<T> type, boolean selected) {
		if (nodesOrEdgeViews == null || nodesOrEdgeViews.isEmpty())
			return;
		
		List<RowSetRecord> records = new ArrayList<>();
		CyNetwork model = getViewModel().getModel();
		CyTable table = type.equals(CyNode.class) ? model.getDefaultNodeTable() : model.getDefaultEdgeTable();
		
		// Disable events
		// MKTODO With the new event coalesce logic in the event helper this shouldn't be necessary, but I'll leave it in for now.
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.silenceEventSource(table);

		try {
			for (View<? extends CyIdentifiable> nodeOrEdgeView : nodesOrEdgeViews) {
				CyRow row = model.getRow(nodeOrEdgeView.getModel());
				row.set(CyNetwork.SELECTED, selected);		
				
				// Add to paylod
				records.add(new RowSetRecord(row, CyNetwork.SELECTED, selected, selected));
			}
		} finally {
			eventHelper.unsilenceEventSource(table);
		}
		
		// Update the selection before firing the event to prevent race conditions
//		updateSelection(type, records);
		
		// Only now it can fire the RowsSetEvent
		fireRowsSetEvent(table, records, eventHelper);
	}

	private void fireRowsSetEvent(CyTable table, List<RowSetRecord> records, CyEventHelper eventHelper) {
		// Make sure the event is not fired on the EDT,
		// otherwise selecting many nodes and edges may lock up the UI momentarily
		if (SwingUtilities.isEventDispatchThread()) {
			new Thread(() -> {
				fireRowsSetEvent(table, records, eventHelper);
			}).start();
		} else {
			eventHelper.fireEvent(new RowsSetEvent(table, records));
		}
	}
	
//	private void updateSelection(Class<? extends CyIdentifiable> type, Collection<RowSetRecord> records) {
//		if (type == null)
//			return;
//		
//		synchronized (m_lock) {
//			nodeSelectionList.clear();
//			edgeSelectionList.clear();
//		}
//		
//		if (type == CyNode.class) {
//			for (RowSetRecord rec : records) {
//				// Get the SUID
//				Long suid = rec.getRow().get(CyNetwork.SUID, Long.class);
//				CyNode node = getModel().getNode(suid);
//
//				if (node == null)
//					continue;
//
//				DNodeView nv = (DNodeView) getNodeView(node);
//
//				if (nv == null)
//					continue;
//
//				Boolean value = rec.getRow().get(CyNetwork.SELECTED, Boolean.class);
//
//				synchronized (m_lock) {
//					if (Boolean.TRUE.equals(value))
//						nv.selectInternal();
//					else
//						nv.unselectInternal();
//					
//					nodeSelectionList.add(node);
//				}
//			}
//		} else if (type == CyEdge.class) {
//			for (RowSetRecord rec : records) {
//				// Get the SUID
//				Long suid = rec.getRow().get(CyNetwork.SUID, Long.class);
//				CyEdge edge = getModel().getEdge(suid);
//				
//				if (edge == null)
//					continue;
//
//				DEdgeView ev = (DEdgeView) getEdgeView(edge);
//				
//				if (ev == null)
//					continue;
//				
//				Boolean value = rec.getRow().get(CyNetwork.SELECTED, Boolean.class);
//				
//				synchronized (m_lock) {
//					if (Boolean.TRUE.equals(value))
//						ev.selectInternal(false);
//					else
//						ev.unselectInternal();
//					
//					edgeSelectionList.add(edge);
//				}
//			}
//		}
//
//		if (nodeSelectionList.size() > 0 || edgeSelectionList.size() > 0) {
//			// Update renderings
//			m_networkCanvas.updateSubgraph(nodeSelectionList, edgeSelectionList);
//			
//			if (m_navigationCanvas != null)
//				m_navigationCanvas.updateSubgraph(nodeSelectionList, edgeSelectionList);
//		}
//	}
}
