package org.cytoscape.ding.impl;

import static org.cytoscape.ding.internal.util.ViewUtil.isControlOrMetaDown;
import static org.cytoscape.ding.internal.util.ViewUtil.isDragSelectionKeyDown;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.ViewChangeEdit;
import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.graph.render.export.ImageImposter;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.SnapshotEdgeInfo;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.work.TaskManager;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
 * Canvas to be used for drawing actual network visualization
 */
@SuppressWarnings("serial")
public class InnerCanvas extends DingCanvas implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {

	private static final Color SELECTION_RECT_BORDER_COLOR_1 = UIManager.getColor("Focus.color");
	private static final Color SELECTION_RECT_BORDER_COLOR_2 = new Color(255, 255, 255, 160);
	
	public GraphGraphics grafx;
	private final DRenderingEngine re;
	private final CyServiceRegistrar serviceRegistrar;
	
	final DingLock dingLock;
	
	protected GraphLOD lod;
	
	double xCenter;
	double yCenter;
	double scaleFactor;
	
	private int lastRenderDetail;
	private Rectangle selectionRect;
	private ViewChangeEdit undoableEdit;
	private boolean isPrinting;
	private PopupMenuHelper popup;
	private FontMetrics fontMetrics;
	
	private boolean NodeMovement = true;

	/** for turning selection rectangle on and off */
	private boolean selecting = true;
	/** for turning camera panning on and off */
	private boolean draggingCanvas;

	private boolean enablePopupMenu = true;

	private int currMouseButton;
	private int lastXMousePos;
	private int lastYMousePos;
	private boolean button1NodeDrag;
	
	private final MousePressedDelegator mousePressedDelegator;
	private final MouseReleasedDelegator mouseReleasedDelegator;
	private final MouseDraggedDelegator mouseDraggedDelegator;
	private final AddEdgeMousePressedDelegator addEdgeMousePressedDelegator;

	private AddEdgeStateMonitor addEdgeMode;
	private Timer hideEdgesTimer;
	private Cursor moveCursor;
	
	InnerCanvas(DingLock lock, DRenderingEngine re, CyServiceRegistrar serviceRegistrar) {
		super(DRenderingEngine.Canvas.NETWORK_CANVAS);
		this.dingLock = lock;
		this.re = re;
		this.serviceRegistrar = serviceRegistrar;
		this.lod = new GraphLOD(); // Default LOD.
		this.m_backgroundColor = Color.WHITE;
		setOpaque(false);
		this.xCenter = 0.0d;
		this.yCenter = 0.0d;
		this.scaleFactor = 1.0d;

		addEdgeMode = new AddEdgeStateMonitor(this, re);
		popup = new PopupMenuHelper(re, this, serviceRegistrar);
		
		mousePressedDelegator = new MousePressedDelegator();
		mouseReleasedDelegator = new MouseReleasedDelegator();
		mouseDraggedDelegator = new MouseDraggedDelegator();
		addEdgeMousePressedDelegator = new AddEdgeMousePressedDelegator();

		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);

		// Timer to reset edge drawing
		ActionListener taskPerformer = evt -> {
			hideEdgesTimer.stop();
			lod.setDrawEdges(true);
			re.setViewportChanged();
			repaint();
		};
		hideEdgesTimer = new Timer(600, taskPerformer);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		boolean resized = (this.getWidth() != width) || (this.getHeight() != height);
        boolean moved = (this.getX() != x) || (this.getY() != y);
        
        if (!resized && !moved)
            return;
		
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0)) {
			Image img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			GraphGraphics grafx = new GraphGraphics(img, false, true);

			synchronized (dingLock) {
				this.grafx = grafx;
				if (re != null)
					re.setViewportChanged();
			}
		}
	}

	@Override
	public void update(Graphics g) {
		if (grafx == null || re == null)
			return;

		// This is the magical portion of code that transfers what is in the
		// visual data structures into what's on the image.
		boolean contentChanged = false;
		boolean viewportChanged = false;
		double xCenter = 0.0d;
		double yCenter = 0.0d;
		double scaleFactor = 1.0d;

		this.fontMetrics = g.getFontMetrics();

		synchronized (dingLock) {
			if (re != null && re.isDirty()) {
				contentChanged = re.isContentChanged();
				viewportChanged = re.isViewportChanged();
				renderGraph(grafx,/* setLastRenderDetail = */ true, lod);
				xCenter = this.xCenter;
				yCenter = this.yCenter;
				scaleFactor = this.scaleFactor;
				
				// set the publicly accessible image object *after* it has been rendered
				m_img = grafx.image;
			}
		}

		// if canvas is visible, draw it
		if (isVisible()) {
			// TODO Should this be on the AWT thread?
			g.drawImage(grafx.image, 0, 0, null);
		}

		if ((selectionRect != null) && (this.isSelecting())) {
			final Graphics2D g2 = (Graphics2D) g;
			// External border
			g2.setColor(SELECTION_RECT_BORDER_COLOR_1);
			g2.draw(selectionRect);
			
			// Internal border
			if (selectionRect.width > 4 && selectionRect.height > 4) {
				g2.setColor(SELECTION_RECT_BORDER_COLOR_2);
				g2.drawRect(
						selectionRect.x + 1,
						selectionRect.y + 1,
						selectionRect.width - 2,
						selectionRect.height - 2
				);
			}
		}

		if (contentChanged && re != null) {
			re.fireContentChanged();
		}
		if (viewportChanged && re != null) {
			re.fireViewportChanged(getWidth(), getHeight(), xCenter, yCenter, scaleFactor);
		}
	}


	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void print(Graphics g) {
		isPrinting = true;
		
		final int w = getWidth();
		final int h = getHeight();
		
		if (re != null && w > 0 && h > 0)
			renderGraph(
					new GraphGraphics(new ImageImposter(g, w, h), /* debug = */ false, /* clear = */ false), 
					/* setLastRenderDetail = */ false, re.getPrintLOD());
		
		isPrinting = false;
	}

	@Override
	public void printNoImposter(Graphics g) {
		isPrinting = true;
		final Image img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (re != null)
			renderGraph(new GraphGraphics(img, false, false), /* setLastRenderDetail = */ false, re.getPrintLOD());
		
		isPrinting = false;
	}

	/**
 	 * Return true if this view is curerntly being printed (as opposed to painted on the screen)
 	 * @return true if we're currently being printed, false otherwise
 	 */
	public boolean isPrinting() { 
		return isPrinting; 
	}

	/**
	 * This method exposes the JComponent processMouseEvent so that canvases on
	 * top of us can pass events they don't want down.
	 * 
	 * @param e the MouseEvent to process
	 */
	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
	}

	// TODO: set timer and setDrawEdges to false.  Set back to true when timer expires.
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!re.getViewModelSnapshot().isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
			setHideEdges();
			adjustZoom(e.getWheelRotation());
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseDraggedDelegator.delegateMouseEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (addEdgeMode.addingEdge())
			addEdgeMode.drawRubberBand(e);
		else {
			// MKTODO this results in the RTree being constantly queried, is there a better way???
//			final String tooltipText = getToolTipText(e.getPoint());
//			final Component[] components = this.getParent().getComponents();
//			for (Component comp : components) {
//				if (comp instanceof JComponent)
//					((JComponent) comp).setToolTipText(tooltipText);
//			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) { }
	
	@Override
	public void mouseEntered(MouseEvent e) { }
	
	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseReleasedDelegator.delegateMouseEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ( addEdgeMode.addingEdge() )
			addEdgeMousePressedDelegator.delegateMouseEvent(e);
		else
			mousePressedDelegator.delegateMouseEvent(e);
		requestFocusInWindow();
	}

	/**
	 * Handles key press events. Currently used with the up/down, left/right arrow
	 * keys. Pressing any of the listed keys will move the selected nodes one pixel
	 * in that direction.
	 * @param k The key event that we're listening for.
	 */
	@Override
	public void keyPressed(KeyEvent k) {
		final int code = k.getKeyCode();
		if ( (code == KeyEvent.VK_UP) || (code == KeyEvent.VK_DOWN) || 
		     (code == KeyEvent.VK_LEFT) || (code == KeyEvent.VK_RIGHT)) {
			handleArrowKeys(k);
		} else if ( code == KeyEvent.VK_ESCAPE ) {
			handleEscapeKey();
		}
		else if ( code == KeyEvent.VK_BACK_SPACE ) 		//#1993
			handleBackspaceKey();
	}

	private void handleBackspaceKey() {		//#1993
		final TaskManager<?, ?> taskManager = serviceRegistrar.getService(TaskManager.class);
		NetworkTaskFactory taskFactory = serviceRegistrar.getService(DeleteSelectedNodesAndEdgesTaskFactory.class);
		taskManager.execute(taskFactory.createTaskIterator(re.getViewModel().getModel()));
	}

	/**
	 * Currently not used.
	 * @param k The key event that we're listening for.
	 */
	@Override
	public void keyReleased(KeyEvent k) { }

	/**
	 * Currently not used.
	 * @param k The key event that we're listening for.
	 */
	@Override
	public void keyTyped(KeyEvent k) { }

	private long getChosenNode() {
		double[] ptBuff = new double[2];
		ptBuff[0] = lastXMousePos;
		ptBuff[1] = lastYMousePos;
		re.xformComponentToNodeCoords(ptBuff);
		
		List<Long> nodes = re.getNodesIntersectingRectangle((float) ptBuff[0], (float) ptBuff[1], 
											(float) ptBuff[0], (float) ptBuff[1],
											(lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0);
		// Need to Z-sort this
		long chosenNode = -1;
		if (!nodes.isEmpty()) {
			View<CyNode> nv = null;
			
			for(Long thisNode : nodes) {
				View<CyNode> dnv = re.getViewModelSnapshot().getNodeView(thisNode);
				if (nv == null || re.getNodeDetails().getZPosition(dnv) > re.getNodeDetails().getZPosition(nv)) {
					nv = dnv;
					chosenNode = thisNode;
				}
			}
		}
		return chosenNode;
	}

	private HandleKey getChosenAnchor() {
		double[] ptBuff = new double[2];
		ptBuff[0] = lastXMousePos;
		ptBuff[1] = lastYMousePos;
		re.xformComponentToNodeCoords(ptBuff);
		
		HandleKey handleKey = re.getBendStore().pickHandle((float)ptBuff[0], (float)ptBuff[1]);
		return handleKey;
	}
	
	private long getChosenEdge() {
		List<Long> edges = computeEdgesIntersecting(lastXMousePos - 1, lastYMousePos - 1, lastXMousePos + 1, lastYMousePos + 1);
		// MKTODO should I return first element or last element???, I think probably the last
		return edges.isEmpty() ? -1 : edges.get(edges.size()-1);
	}
	
	
	private int toggleSelectedNode(long chosenNode, MouseEvent e) {
		int chosenNodeSelected = 0;
		final boolean wasSelected = re.getNodeDetails().isSelected(re.getViewModelSnapshot().getNodeView(chosenNode));
		
		// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
			chosenNodeSelected = -1;
		} else if (!wasSelected) {
			chosenNodeSelected = 1;
		}
		
		return chosenNodeSelected;
	}
	
	private void toggleChosenAnchor(HandleKey chosenAnchor, MouseEvent e) {
		final long edge = chosenAnchor.getEdgeSuid();
		View<CyEdge> ev = re.getViewModelSnapshot().getEdgeView(edge);
		
		// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
		if (e.isAltDown()) { // Remove handle
			int anchorInx = chosenAnchor.getHandleIndex();
			// Save remove handle
			undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Remove Edge Handle", serviceRegistrar);

			Bend bend = null;
			if (!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
				Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
				if (re.getEdgeDetails().getBend(ev) == defaultBend) {
					bend = new BendImpl((BendImpl) defaultBend);
				} else {
					bend = new BendImpl((BendImpl) re.getEdgeDetails().getBend(ev));
				}
			}
			
			if(bend != null) {
				View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(ev.getModel());
				if(mutableEdgeView != null) {
					mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);
				}
			}
			
			re.getBendStore().removeHandle(chosenAnchor);
			lod.setDrawEdges(true);
			// final GraphViewChangeListener listener = m_view.m_lis[0];
			// listener.graphViewChanged(new GraphViewEdgesSelectedEvent(m_view, DGraphView.makeList(ev.getCyEdge())));
		} else {
			final boolean wasSelected = re.getBendStore().isHandleSelected(chosenAnchor);
			// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
			if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
				re.getBendStore().unselectHandle(chosenAnchor);
			} else if (!wasSelected) {
				if (!e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()))
					re.getBendStore().unselectAllHandles();
				re.getBendStore().selectHandle(chosenAnchor);
			}

		}
		re.setContentChanged();	
	}
	
	private int toggleSelectedEdge(long chosenEdge, MouseEvent e) {
		int chosenEdgeSelected = 0;

		View<CyEdge> edgeView = re.getViewModelSnapshot().getEdgeView(chosenEdge);
		if(edgeView == null)
			return chosenEdgeSelected;
		
		boolean wasSelected = re.getEdgeDetails().isSelected(edgeView);
		
		// Add new Handle for Edge Bend.
		// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
		if ((e.isAltDown()) && ((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
			re.getBendStore().unselectAllHandles();
			double[] ptBuff = new double[2];
			ptBuff[0] = lastXMousePos;
			ptBuff[1] = lastYMousePos;
			re.xformComponentToNodeCoords(ptBuff);
			// Store current handle list
			undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Add Edge Handle", serviceRegistrar);
			
			Point2D newHandlePoint = new Point2D.Float((float) ptBuff[0], (float) ptBuff[1]);
			Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
			
			if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
				View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(edgeView.getSUID());
				if(mutableEdgeView != null) {
					if (defaultBend instanceof BendImpl)
						mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl) defaultBend));
					else
						mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
				}
			}
			
			HandleKey handleKey = re.getBendStore().addHandle(edgeView, newHandlePoint);
			re.getBendStore().selectHandle(handleKey);
		}

		// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
			// ((DEdgeView) m_view.getDEdgeView(chosenEdge)).unselectInternal();
			chosenEdgeSelected = -1;
		} else if (!wasSelected) {
			// ((DEdgeView) m_view.getDEdgeView(chosenEdge)).selectInternal(false);
			chosenEdgeSelected = 1;

			if ((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
				double[] ptBuff = new double[2];
				ptBuff[0] = lastXMousePos;
				ptBuff[1] = lastYMousePos;
				re.xformComponentToNodeCoords(ptBuff);

				HandleKey hit = re.getBendStore().pickHandle((float) ptBuff[0], (float) ptBuff[1]);

				if (hit != null) {
					re.getBendStore().selectHandle(hit);
				}
			}
		}

		re.setContentChanged();
		return chosenEdgeSelected;
	}
	
	private List<View<CyNode>> getAndApplySelectedNodes() {
		double[] ptBuff = new double[2];
		ptBuff[0] = selectionRect.x;
		ptBuff[1] = selectionRect.y;
		re.xformComponentToNodeCoords(ptBuff);

		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = selectionRect.x + selectionRect.width;
		ptBuff[1] = selectionRect.y + selectionRect.height;
		re.xformComponentToNodeCoords(ptBuff);

		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		boolean treatNodeShapesAsRectangle = (lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0;
		List<Long> nodesXSect = re.getNodesIntersectingRectangle(xMin, yMin, xMax, yMax, treatNodeShapesAsRectangle);

		NodeDetails nodeDetails = re.getNodeDetails();
		List<View<CyNode>> selectedNodes = new ArrayList<>(nodesXSect.size());
		for(Long suid : nodesXSect) {
			View<CyNode> node = re.getViewModelSnapshot().getNodeView(suid);
			if(!nodeDetails.isSelected(node)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedNodes.add(node);
			}
		}
		
		if(!selectedNodes.isEmpty()) {
			re.setContentChanged();
		}
		return selectedNodes;
	}
	

	private List<View<CyEdge>> getAndApplySelectedEdges() {
		if ((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
			double[] ptBuff = new double[2];
			ptBuff[0] = selectionRect.x;
			ptBuff[1] = selectionRect.y;
			re.xformComponentToNodeCoords(ptBuff);

			final float xMin = (float) ptBuff[0];
			final float yMin = (float) ptBuff[1];
			ptBuff[0] = selectionRect.x + selectionRect.width;
			ptBuff[1] = selectionRect.y + selectionRect.height;
			re.xformComponentToNodeCoords(ptBuff);

			final float xMax = (float) ptBuff[0];
			final float yMax = (float) ptBuff[1];

			SpacialIndex2DEnumerator<HandleKey> handles = re.getBendStore().queryOverlap(xMin, yMin, xMax, yMax);
			
			if (handles.hasNext()) {
				re.setContentChanged();
			}
			while (handles.hasNext()) {
				HandleKey handle = handles.next();
				re.getBendStore().selectHandle(handle);
			}
		}

		List<Long> edges = computeEdgesIntersecting(selectionRect.x, selectionRect.y,
		                         selectionRect.x + selectionRect.width,
		                         selectionRect.y + selectionRect.height);


		EdgeDetails edgeDetails = re.getEdgeDetails();
		List<View<CyEdge>> selectedEdges = new ArrayList<>(edges.size());
		for (Long edgeXSect : edges) {
			View<CyEdge> edge = re.getViewModelSnapshot().getEdgeView(edgeXSect);
			if (!edgeDetails.isSelected(edge)) {
				selectedEdges.add(edge);
			}
		}

		if (!selectedEdges.isEmpty())
			re.setContentChanged();
		
		return selectedEdges;
	}

	/**
	 * Returns the tool tip text for the specified location if any exists first
	 * checking nodes, then edges, and then returns null if it's empty space.
	 */
	private String getToolTipText(final Point p) {
		// display tips for nodes before edges
		final View<CyNode> nv = re.getPickedNodeView(p);
		if (nv != null)  {
			final String tooltip = re.getNodeDetails().getTooltipText(nv);
			return tooltip;
		}
		// only display edge tool tips if the LOD is sufficient
		if ((lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) != 0) {
			View<CyEdge> ev = re.getPickedEdgeView(p);
			if (ev != null) 
				return re.getEdgeDetails().getTooltipText(ev);
		}

		return null;
	}

	// Puts [last drawn] edges intersecting onto stack; as RootGraph indices.
	// Depends on the state of several member variables, such as m_hash.
	// Clobbers m_stack and m_ptBuff.
	// The rectangle extents are in component coordinate space.
	// IMPORTANT: Code that calls this method should be holding m_lock.
	final List<Long> computeEdgesIntersecting(final int xMini, final int yMini, final int xMaxi, final int yMaxi) {
		double[] ptBuff = new double[2];
		ptBuff[0] = xMini;
		ptBuff[1] = yMini;
		re.xformComponentToNodeCoords(ptBuff);

		final float xMin = (float) ptBuff[0];
		final float yMin = (float) ptBuff[1];
		ptBuff[0] = xMaxi;
		ptBuff[1] = yMaxi;
		re.xformComponentToNodeCoords(ptBuff);
		
		final float xMax = (float) ptBuff[0];
		final float yMax = (float) ptBuff[1];
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		
		// MKTODO this code was copied from GraphRenderer.renderGraph()
		// get viewport bounds
		float image_xMin = (float) (xCenter - ((0.5d * grafx.image.getWidth(null)) / scaleFactor));
		float image_yMin = (float) (yCenter - ((0.5d * grafx.image.getHeight(null)) / scaleFactor));
		float image_xMax = (float) (xCenter + ((0.5d * grafx.image.getWidth(null)) / scaleFactor)); 
		float image_yMax = (float) (yCenter + ((0.5d * grafx.image.getHeight(null)) / scaleFactor));

		CyNetworkViewSnapshot snapshot = re.getViewModelSnapshot();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(image_xMin, image_yMin, image_xMax, image_yMax);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		if ((lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
			// We won't need to look up arrows and their sizes.
			while(nodeHits.hasNext()) {
				long node = nodeHits.nextExtents(extentsBuff);
				
				// MKTODO make this into a utility method
				float nodeX = (extentsBuff[0] + extentsBuff[2]) / 2;
				float nodeY = (extentsBuff[1] + extentsBuff[3]) / 2;
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> e : touchingEdges) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(e);
					long edge = e.getSUID();
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					
					if(!processedNodes.contains(otherNode)) {
						snapshot.getSpacialIndex2D().get(otherNode, extentsBuff);
						float otherNodeX = (extentsBuff[0] + extentsBuff[2]) / 2;
						float otherNodeY = (extentsBuff[1] + extentsBuff[3]) / 2;
						line.setLine(nodeX, nodeY, otherNodeX, otherNodeY);
						
						if(line.intersects(xMin, yMin, xMax - xMin, yMax - yMin)) {
							resultEdges.add(edge);
						}
					}
				}
				processedNodes.add(node);
			}
		} else { // Last render high detail.
			float[] extentsBuff2 = new float[4];
			
			while(nodeHits.hasNext()) {
				long node = nodeHits.nextExtents(extentsBuff);
				View<CyNode> nodeView = snapshot.getNodeView(node);
				byte nodeShape = re.getNodeDetails().getShape(nodeView);
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> edge : touchingEdges) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
					double segThicknessDiv2 = re.getEdgeDetails().getWidth(edge) / 2.0d;
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					View<CyNode> otherNodeView = snapshot.getNodeView(otherNode);
					
					if(!processedNodes.contains(otherNode)) {
						snapshot.getSpacialIndex2D().get(otherNode, extentsBuff2);
						
						final byte otherNodeShape = re.getNodeDetails().getShape(otherNodeView);
						final byte srcShape;
						final byte trgShape;
						final float[] srcExtents;
						final float[] trgExtents;

						if (node == edgeInfo.getSourceViewSUID()) {
							srcShape = nodeShape;
							trgShape = otherNodeShape;
							srcExtents = extentsBuff;
							trgExtents = extentsBuff2;
						} else { // node == graph.edgeTarget(edge).
							srcShape = otherNodeShape;
							trgShape = nodeShape;
							srcExtents = extentsBuff2;
							trgExtents = extentsBuff;
						}

						final ArrowShape srcArrow;
						final ArrowShape trgArrow;
						final float srcArrowSize;
						final float trgArrowSize;

						final float[] floatBuff1 = new float[2];
						final float[] floatBuff2 = new float[2];
						GeneralPath path  = new GeneralPath();
						GeneralPath path2 = new GeneralPath();
						
						if ((lastRenderDetail & GraphRenderer.LOD_EDGE_ARROWS) == 0) {
							srcArrow = trgArrow = ArrowShapeVisualProperty.NONE;
							srcArrowSize = trgArrowSize = 0.0f;
						} else {
							srcArrow = re.getEdgeDetails().getSourceArrowShape(edge);
							trgArrow = re.getEdgeDetails().getTargetArrowShape(edge);
							srcArrowSize = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : re.getEdgeDetails().getSourceArrowSize(edge));
							trgArrowSize = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : re.getEdgeDetails().getTargetArrowSize(edge));
						}

						final EdgeAnchors anchors = (((lastRenderDetail
						                              & GraphRenderer.LOD_EDGE_ANCHORS) == 0)
						                             ? null : re.getEdgeDetails().getAnchors(snapshot, edge));

						if (!GraphRenderer.computeEdgeEndpoints(grafx, srcExtents, srcShape,
						                                        srcArrow, srcArrowSize, anchors,
						                                        trgExtents, trgShape, trgArrow,
						                                        trgArrowSize, floatBuff1, floatBuff2))
							continue;

						grafx.getEdgePath(srcArrow, srcArrowSize, trgArrow, trgArrowSize,
						                    floatBuff1[0], floatBuff1[1], anchors,
						                    floatBuff2[0], floatBuff2[1], path);
						GraphRenderer.computeClosedPath(path.getPathIterator(null), path2);

						if (path2.intersects(xMin - segThicknessDiv2, yMin - segThicknessDiv2,
						                       (xMax - xMin) + (segThicknessDiv2 * 2),
						                       (yMax - yMin) + (segThicknessDiv2 * 2)))
							resultEdges.add(edge.getSUID());
					}
				}

				processedNodes.add(node);
			}
		}
		return resultEdges;
	}

    /**
     * When the center is changed, this method ought to be called rather than modifying m_xCenter and m_yCenter
     * directly so that edges maintain appropriate starting points at the center of whatever node they are associated with.
     */
    void setCenter(double x, double y)  {
        double changeX = x - xCenter;
        double changeY = y - yCenter;
        xCenter = x;
        yCenter = y;

        if(addEdgeMode != null && addEdgeMode.addingEdge() ) {
            Point2D sourcePoint = AddEdgeStateMonitor.getSourcePoint(re.getViewModel());
            double newX = sourcePoint.getX() - changeX;
            double newY = sourcePoint.getY() - changeY;
            sourcePoint.setLocation(newX, newY);
            AddEdgeStateMonitor.setSourcePoint(re.getViewModel(), sourcePoint);
        }
    }

	private void adjustZoom(int notches) {
		final double factor;
		
		if (notches < 0)
			factor = 1.1; // scroll up, zoom in
		else if (notches > 0)
			factor = 0.9; // scroll down, zoom out
		else
			return;

		synchronized (dingLock) {
			scaleFactor = scaleFactor * factor;
		}

		re.setViewportChanged();
		
		// Update view model.
		re.getViewModel().setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, scaleFactor);

        //This code updates the source point so that it is better related to the selected node.
        //TODO: Center the source point on the selected node perfectly.
        if (addEdgeMode.addingEdge()) {
        	View<CyNode> nodeView = mousePressedDelegator.getPickedNodeView();

            AddEdgeStateMonitor.setSourceNode(re.getViewModel(), nodeView);
            double[] coords = new double[2];
            coords[0] = re.getNodeDetails().getXPosition(nodeView);
            coords[1] = re.getNodeDetails().getYPosition(nodeView);
            ensureInitialized();
            re.xformNodeToComponentCoords(coords);

            Point sourceP = new Point();
            sourceP.setLocation(coords[0], coords[1]);
            AddEdgeStateMonitor.setSourcePoint(re.getViewModel(), sourceP);
        }
		repaint();
	}


	public int getLastRenderDetail() {
		return lastRenderDetail;
	}

	public void setSelecting(boolean s) {
		selecting = s;
	}

	public boolean isSelecting() {
		return selecting;
	}

	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}
	
	/**
	 * Called to get the tranform matrix used by the inner canvas
	 * to move the nodes.
	 */
	public AffineTransform getAffineTransform() {
		return (grafx != null) ? grafx.getTransform() : null;
	}

	public void enableNodeMovement(){
		this.NodeMovement = true;
	}
	
	public void disableNodeMovement(){
		this.NodeMovement = false;
	}
	
	public boolean isNodeMovementDisabled(){
		return !(this.NodeMovement);
	}

	public void enablePopupMenu(){
		this.enablePopupMenu = true;
	}
	
	public void disablePopupMenu(){
		this.enablePopupMenu = false;
	}
	
	public boolean isPopupMenuDisabled(){
		return !(this.enablePopupMenu);
	}

	public void updateSubgraph(List<View<CyNode>> nodes, List<View<CyEdge>> edges) {
		renderSubgraph(grafx, false, lod, nodes, edges);
	}

	// Render just a portion of the graph.  This is used for selections when we want to overwrite
	// a limited number of nodes and edges
	private void renderSubgraph(GraphGraphics graphics, final boolean setLastRenderDetail, final GraphLOD lod, 
	                            List<View<CyNode>> nodes, List<View<CyEdge>> edges) {

		// Pass the color even though we won't use it if we actually only render the subgraph.  If we're
		// not in largeModel mode, or we're only painting a small portion of the network, we'll wind up
		// calling renderGraph anyways and we'll need to clear the image
		final Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(), m_backgroundColor.getBlue(), 0);

//		synchronized (m_lock) {
			int lastRenderDetail = re.renderSubgraph(graphics, lod, backgroundColor, xCenter, yCenter, scaleFactor, nodes, edges);
			if (setLastRenderDetail)
				this.lastRenderDetail = lastRenderDetail;
//		}

		repaint();
	}
	
	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	private void renderGraph(GraphGraphics graphics, boolean setLastRenderDetail, GraphLOD lod) {
		int alpha = (isOpaque()) ? 255 : 0;
		Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(), m_backgroundColor.getBlue(), alpha);

		// long timeBegin = System.currentTimeMillis();
		int lastRenderDetail = re.renderGraph(graphics, lod, backgroundColor, xCenter, yCenter, scaleFactor);
		// System.out.println("Rendered graph in "+(System.currentTimeMillis()-timeBegin)+"ms");

		if (setLastRenderDetail)
			this.lastRenderDetail = lastRenderDetail;
		// repaint();
	}

	private void handleEscapeKey() {
		AddEdgeStateMonitor.reset(re.getViewModelSnapshot());
		repaint();
	}

	/**
	 * Arrow key handler.
	 * They are used to pan and mode nodes/edge bend handles.
	 * @param k key event
	 */
	private void handleArrowKeys(KeyEvent k) {
		final int code = k.getKeyCode();
		float move = 1.0f;

		// Adjust increment if Shift key is pressed
		if (k.isShiftDown())
			move = 15.0f;
		
		// Pan if CTR is pressed.
		if (isControlOrMetaDown(k)) {
			// Pan
			if (code == KeyEvent.VK_UP) {
				pan(0, move);
			} else if (code == KeyEvent.VK_DOWN) {
				pan(0, -move);
			} else if (code == KeyEvent.VK_LEFT) {
				pan(-move, 0);
			} else if (code == KeyEvent.VK_RIGHT) {
				pan(move, 0);
			}
			return;
		}
		
		if (re.isNodeSelectionEnabled()) {
			// move nodes
			Collection<View<CyNode>> selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
			for (View<CyNode> node : selectedNodes) {
				double xPos = re.getNodeDetails().getXPosition(node);
				double yPos = re.getNodeDetails().getYPosition(node);

				if (code == KeyEvent.VK_UP) {
					yPos -= move;
				} else if (code == KeyEvent.VK_DOWN) {
					yPos += move;
				} else if (code == KeyEvent.VK_LEFT) {
					xPos -= move;
				} else if (code == KeyEvent.VK_RIGHT) {
					xPos += move;
				}

				// MKTODO better way of doing this???
				View<CyNode> mutableNodeView = re.getViewModel().getNodeView(node.getSUID());
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xPos);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPos);
			}

			Set<HandleKey> handlesToMove = re.getBendStore().getSelectedHandles();
			
			for (HandleKey handleKey : handlesToMove) {
				View<CyEdge> ev = re.getViewModelSnapshot().getEdgeView(handleKey.getEdgeSuid());

				// MKTODO this code is copy-pasted in a few places, clean it up
				if(!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
					Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
					View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(ev.getSUID());
					if(mutableEdgeView != null) {
						if(ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
							mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)defaultBend));
						} else {
							Bend bend = re.getEdgeDetails().getBend(ev, true);
							mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)bend));
						}
					}
				}
				
				Bend bend = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
				Handle handle = bend.getAllHandles().get(handleKey.getHandleIndex());
				Point2D newPoint = handle.calculateHandleLocation(re.getViewModel(),ev);
				
				float x = (float) newPoint.getX();
				float y = (float) newPoint.getY();

				if (code == KeyEvent.VK_UP) {
					re.getBendStore().moveHandle(handleKey, x, y - move);
				} else if (code == KeyEvent.VK_DOWN) {
					re.getBendStore().moveHandle(handleKey, x, y + move);
				} else if (code == KeyEvent.VK_LEFT) {
					re.getBendStore().moveHandle(handleKey, x - move, y);
				} else if (code == KeyEvent.VK_RIGHT) {
					re.getBendStore().moveHandle(handleKey, x + move, y);
				}

			}
			repaint();
		}
	}
	
	private void pan(double deltaX, double deltaY) {
		synchronized (dingLock) {
			double newX = xCenter - (deltaX / scaleFactor);
			double newY = yCenter - (deltaY / scaleFactor);
            setCenter(newX, newY);
		}
		re.setViewportChanged();
		setHideEdges();
		repaint();
	}
	
	private void maybeDeselectAll(final MouseEvent e, long chosenNode, long chosenEdge, HandleKey chosenAnchor) {
		Collection<View<CyNode>> selectedNodes = null;
		Collection<View<CyEdge>> selectedEdges = null;
		
		// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if ((!e.isShiftDown() && !(e.isControlDown() && !e.isAltDown()) && !e.isMetaDown()) // If shift is down never unselect.
		    && (((chosenNode < 0) && (chosenEdge < 0) && (chosenAnchor == null)) // Mouse missed all.
		       // Not [we hit something but it was already selected].
		       || !( ((chosenNode >= 0) && re.isNodeSelected(chosenNode))
		             || (chosenAnchor != null) 
		             || ((chosenEdge >= 0) && re.isEdgeSelected(chosenEdge)) ))) {
				selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
				selectedEdges = re.getViewModelSnapshot().getTrackedEdges(CyNetworkViewConfig.SELECTED_EDGES);
		}
		
		// Deselect
		re.select(selectedNodes, CyNode.class, false);
		re.select(selectedEdges, CyEdge.class, false);
	}
	
	private class AddEdgeMousePressedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			Point rawPt = e.getPoint();
			double[] loc = new double[2];
			loc[0] = rawPt.getX();
			loc[1] = rawPt.getY();
			re.xformComponentToNodeCoords(loc);
			Point xformPt = new Point();
			xformPt.setLocation(loc[0],loc[1]); 
			View<CyNode> nodeView = re.getPickedNodeView(rawPt);
			
			if (nodeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
				popup.createNodeViewMenu(nodeView, e.getX(), e.getY(), "Edge");
			}
		}
	}

	private final class MousePressedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			// System.out.println("MousePressed ----> singleLeftClick");
			undoableEdit = null;
		
			currMouseButton = 1;
			lastXMousePos = e.getX();
			lastYMousePos = e.getY();
		
			long chosenNode = -1;
			long chosenEdge = -1;
			HandleKey chosenAnchor = null;
			long chosenNodeSelected = 0;
			long chosenEdgeSelected = 0;
	
			synchronized (dingLock) {
				if (re.isNodeSelectionEnabled())
					chosenNode = getChosenNode();
	
				if (re.isEdgeSelectionEnabled() && (chosenNode < 0) && ((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
					chosenAnchor = getChosenAnchor();
	
				if (re.isEdgeSelectionEnabled() && (chosenNode < 0) && (chosenAnchor == null))
					chosenEdge = getChosenEdge();
	
				if (chosenNode >= 0)
				    chosenNodeSelected = toggleSelectedNode(chosenNode, e);
	
				if (chosenAnchor != null)
					toggleChosenAnchor(chosenAnchor, e);
	
				if (chosenEdge >= 0)
					chosenEdgeSelected = toggleSelectedEdge(chosenEdge, e);
	
				if ((chosenNode >= 0 || chosenEdge >= 0) && !(e.isShiftDown() || isControlOrMetaDown(e)))
					re.getBendStore().unselectAllHandles();
				
				if (chosenNode < 0 && chosenEdge < 0 && chosenAnchor == null 
						&& (re.getCyAnnotator().getAnnotationSelection().isEmpty() || !re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION))) {
					
					button1NodeDrag = false;
					
					if (isDragSelectionKeyDown(e)) {
						selectionRect = new Rectangle(lastXMousePos, lastYMousePos, 0, 0);
						changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					} else {
						changeCursor(getMoveCursor());
					}
				} else {
					button1NodeDrag = true;
				}
			}
	
			if (chosenNode < 0 && chosenEdge < 0 && chosenAnchor == null) {
				// Save all node positions for panning
				undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.NODES, "Move", serviceRegistrar);
				lastXMousePos = e.getX();
				lastYMousePos = e.getY();
				lod.setDrawEdges(false);
			} else {
				maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
				
				if (chosenNode >= 0) {
					View<CyNode> node = re.getViewModelSnapshot().getNodeView(chosenNode);
					if (chosenNodeSelected > 0) {
						re.select(Collections.singletonList(node), CyNode.class, true);
					}
					else if (chosenNodeSelected < 0)
						re.select(Collections.singletonList(node), CyNode.class, false);
				}
				if (chosenEdge >= 0) {
					View<CyEdge> edge = re.getViewModelSnapshot().getEdgeView(chosenEdge);
					if (chosenEdgeSelected > 0)
						re.select(Collections.singletonList(edge), CyEdge.class, true);
					else if (chosenEdgeSelected < 0)
						re.select(Collections.singletonList(edge), CyEdge.class, false);
				}
			}

			repaint();
		}

		@Override
		void singleMiddleClick(MouseEvent e) {
			// Nothing to do here...
		}

        private View<CyNode> pickedNodeView = null;
        private double pickedNodeWidth = 0.0;
        private double pickedNodeHeight = 0.0;

		private View<CyNode> getPickedNodeView() {
			return pickedNodeView;
		}

		private double getPickedNodeWidth() {
			return pickedNodeWidth;
		}

		private double getPickedNodeHeight() {
			return pickedNodeHeight;
		}
	
		@Override
		void singleRightClick(MouseEvent e) {
			// System.out.println("MousePressed ----> singleRightClick");
			// Save all node positions
			undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.NODES, "Move", serviceRegistrar);
			currMouseButton = 3;
			lastXMousePos = e.getX();
			lastYMousePos = e.getY();
		
			View<CyNode> nview = re.getPickedNodeView(e.getPoint());
			if (nview != null && !InnerCanvas.this.isPopupMenuDisabled()) {
                pickedNodeView = nview;
                pickedNodeHeight = re.getNodeDetails().getHeight(pickedNodeView);
                pickedNodeWidth  = re.getNodeDetails().getWidth(pickedNodeView);
				popup.createNodeViewMenu(nview,e.getX(),e.getY(),"NEW");
			} else {
				View<CyEdge> edgeView = re.getPickedEdgeView(e.getPoint());
				if (edgeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
					popup.createEdgeViewMenu(edgeView, e.getX(), e.getY(), "NEW");
				} else {
					// Clicked on empty space...
					Point rawPt = e.getPoint();
					double[] loc = new double[2];
					loc[0] = rawPt.getX();
					loc[1] = rawPt.getY();
					re.xformComponentToNodeCoords(loc);
					Point xformPt = new Point();
					xformPt.setLocation(loc[0],loc[1]); 
					if (!InnerCanvas.this.isPopupMenuDisabled()){
						popup.createNetworkViewMenu(rawPt, xformPt, "NEW");						
					}
				}
			}
		}
	
		@Override
		void doubleLeftClick(MouseEvent e) {
			// System.out.println("MousePressed ----> doubleLeftClick");
			View<CyNode> nview = re.getPickedNodeView(e.getPoint());
			if ( nview != null && !InnerCanvas.this.isPopupMenuDisabled())
				popup.createNodeViewMenu(nview,e.getX(), e.getY(), "OPEN");
			else {
				View<CyEdge> edgeView = re.getPickedEdgeView(e.getPoint());
				if (edgeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
					popup.createEdgeViewMenu(edgeView,e.getX(),e.getY(),"OPEN");
				} else {
					Point rawPt = e.getPoint();
					double[] loc = new double[2];
					loc[0] = rawPt.getX();
					loc[1] = rawPt.getY();
					re.xformComponentToNodeCoords(loc);
					Point xformPt = new Point();
					xformPt.setLocation(loc[0],loc[1]); 
					if (!InnerCanvas.this.isPopupMenuDisabled()){
						popup.createNetworkViewMenu(rawPt, xformPt, "OPEN");
					}
				}
			}
		}
	}

	private final class MouseReleasedDelegator extends ButtonDelegator {
		@Override
		// delegate based on the originally-pressed button so as not to change actions mid-click
		public void delegateMouseEvent(MouseEvent e) {
			switch(currMouseButton) {
				case 1:
					singleLeftClick(e);
					break;
				case 2:
					singleMiddleClick(e);
					break;
				case 3:
					singleRightClick(e);
					break;
			}
			
			changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		@Override
		void singleLeftClick(MouseEvent e) {
			// System.out.println("1. MouseReleased ----> singleLeftClick");
			if (currMouseButton == 1) {
				currMouseButton = 0;
	
				if (selectionRect != null) {
					List<View<CyNode>> selectedNodes = null;
					List<View<CyEdge>> selectedEdges = null;
	
					synchronized (dingLock) {
						if (re.isNodeSelectionEnabled())
							selectedNodes = getAndApplySelectedNodes();	
						if (re.isEdgeSelectionEnabled())
							selectedEdges = getAndApplySelectedEdges();
					}
					
					selectionRect = null;

					// Update visual property value (x/y)
					// MKTODO why is it doing this? it seems to just be setting the X/Y VPs to the value they already have??!?!
//					if (selectedNodes != null){
//						for (long node : selectedNodes) {
//							View<CyNode> mutableNodeView = m_re.getViewModel().getNodeView(node);
//							if(mutableNodeView != null) {
//								mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, dNodeView.getXPosition());
//								mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, dNodeView.getYPosition());
//							}
//						}						
//					}
					
					if (!lod.getDrawEdges()) {
						lod.setDrawEdges(true);
						re.setViewportChanged();
					}
	
					if (selectedNodes != null && !selectedNodes.isEmpty())
						re.select(selectedNodes, CyNode.class, true);
					if (selectedEdges != null && !selectedEdges.isEmpty())
						re.select(selectedEdges, CyEdge.class, true);
					
					repaint();
				} else if (draggingCanvas) {
					setDraggingCanvas(false);
					
					if (undoableEdit != null)
						undoableEdit.post();

					lod.setDrawEdges(true);
					re.setViewportChanged();
					repaint();
				} else {
					long chosenNode = -1;
					long chosenEdge = -1;
					HandleKey chosenAnchor = null;
					
					if (re.isNodeSelectionEnabled())
						chosenNode = getChosenNode();
		
					if (re.isEdgeSelectionEnabled() && (chosenNode < 0) && ((lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
						chosenAnchor = getChosenAnchor();
		
					if (re.isEdgeSelectionEnabled() && (chosenNode < 0) && (chosenAnchor == null))
						chosenEdge = getChosenEdge();
					
					maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
					
					re.setContentChanged();
					repaint();
				}
			}
	
			if (undoableEdit != null)
				undoableEdit.post();
		}
	
		@Override
		void singleMiddleClick(MouseEvent e) {
			// Nothing to do here...
		}
	
		@Override
		void singleRightClick(MouseEvent e) {
			// System.out.println("MouseReleased ----> singleRightClick");
			if (currMouseButton == 3)
				currMouseButton = 0;
	
			if (undoableEdit != null)
				undoableEdit.post();
		}
	}
	
	int numFrames = 10;
	
	
	private void setDraggingCanvas(boolean draggingCanvas) {
		if(this.draggingCanvas != draggingCanvas) {
			this.draggingCanvas = draggingCanvas;
			Cursor cursor = draggingCanvas ? getMoveCursor() : Cursor.getDefaultCursor();
			changeCursor(cursor);
		}
	}

	public void changeCursor(Cursor cursor) {
		String componentName = "__CyNetworkView_" + re.getViewModel().getSUID(); // see ViewUtil.createUniqueKey(CyNetworkView)
		Container parent = this;
		while(parent != null) {
			if(componentName.equals(parent.getName())) {
				parent.setCursor(cursor);
				break;
			}
			parent = parent.getParent();
		}
	}
	
	public Cursor getMoveCursor() {
		if (moveCursor == null) {
			Cursor cursor = null;
			
			if (LookAndFeelUtil.isMac()) {
				Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(24, 24);
				Image image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();

				String icon = IconManager.ICON_ARROWS;
				JLabel label = new JLabel();
				label.setBounds(0, 0, size.width, size.height);
				label.setText(icon);
				label.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(14));
				label.paint(graphics);
				graphics.dispose();

				cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0),
						"custom:" + (int) icon.charAt(0));
			} else {
				cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				
				if (cursor == null)
					cursor = new Cursor(Cursor.MOVE_CURSOR);
			}
			
			moveCursor = cursor;
		}
		
		return moveCursor;
	}
	
	
	private final class MouseDraggedDelegator extends ButtonDelegator {
		
		// delegate based on the originally-pressed button so as not to change actions mid-click
		@Override
		public void delegateMouseEvent(MouseEvent e) {
			switch(currMouseButton) {
				case 1:
					singleLeftClick(e);
					break;
				case 2:
					singleMiddleClick(e);
					break;
			}
		}
		
		@Override
		void singleLeftClick(MouseEvent e) {
			// System.out.println("MouseDragged ----> singleLeftClick: " + m_button1NodeDrag);
			if (button1NodeDrag) {
				// save selected node and edge positions
				if (undoableEdit == null)
					undoableEdit = new ViewChangeEdit(re, ViewChangeEdit.SavedObjs.SELECTED, "Move", serviceRegistrar);
				
				synchronized (dingLock) {
					double[] ptBuff = new double[2];
					ptBuff[0] = lastXMousePos;
					ptBuff[1] = lastYMousePos;
					re.xformComponentToNodeCoords(ptBuff);
	
					final double oldX = ptBuff[0];
					final double oldY = ptBuff[1];
					lastXMousePos = e.getX();
					lastYMousePos = e.getY();
					ptBuff[0] = lastXMousePos;
					ptBuff[1] = lastYMousePos;
					re.xformComponentToNodeCoords(ptBuff);
	
					final double newX = ptBuff[0];
					final double newY = ptBuff[1];
					double deltaX = newX - oldX;
					double deltaY = newY - oldY;
	
					// If the shift key is down, then only move horizontally, vertically, or diagonally, depending on the slope.
					if (e.isShiftDown()) {
						final double slope = deltaY / deltaX;
	
						// slope of 2.41 ~ 67.5 degrees (halfway between 45 and 90)
						// slope of 0.41 ~ 22.5 degrees (halfway between 0 and 45)
						if ((slope > 2.41) || (slope < -2.41)) {
							deltaX = 0.0; // just move vertical
						} else if ((slope < 0.41) && (slope > -0.41)) {
							deltaY = 0.0; // just move horizontal
						} else {
							final double avg = (Math.abs(deltaX) + Math.abs(deltaY)) / 2.0;
							deltaX = (deltaX < 0) ? (-avg) : avg;
							deltaY = (deltaY < 0) ? (-avg) : avg;
						}
					}
	
					Collection<View<CyNode>> selectedNodes = re.getViewModelSnapshot().getTrackedNodes(CyNetworkViewConfig.SELECTED_NODES);
					
					// MKTODO rename to 'handlesToMove'
					Set<HandleKey> anchorsToMove = re.getBendStore().getSelectedHandles();
					
					if (anchorsToMove.isEmpty()) { // If we are moving anchors of edges, no need to move nodes (bug #2360).
						for (View<CyNode> node : selectedNodes) {
							View<CyNode> mutableNode = re.getViewModel().getNodeView(node.getSUID());
							if(mutableNode != null) {
								NodeDetails nodeDetails = re.getNodeDetails();
								double oldXPos = nodeDetails.getXPosition(mutableNode);
								double oldYPos = nodeDetails.getYPosition(mutableNode);
								// MKTODO Should setting VPs be done using NodeDetails as well??
								mutableNode.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, oldXPos + deltaX);
								mutableNode.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, oldYPos + deltaY);
							}
					    }
					} else {
						for (HandleKey handleKey : anchorsToMove) {
							View<CyEdge> ev = re.getViewModelSnapshot().getEdgeView(handleKey.getEdgeSuid());
	
							if (!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
								Bend defaultBend = re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
								View<CyEdge> mutableEdgeView = re.getViewModel().getEdgeView(ev.getSUID());
								if(mutableEdgeView != null) {
									if( ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend ) {
										if( defaultBend instanceof BendImpl )
											mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)defaultBend));
										else
											mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
									} else {
										Bend bend = re.getEdgeDetails().getBend(ev, true);
										mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)bend));
									}
								}
							}
							final Bend bend = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
							//TODO: Refactor to fix this ordering problem.
							//This test is necessary because in some instances, an anchor can still be present in the selected
							//anchor list, even though the anchor has been removed. A better fix would be to remove the
							//anchor from that list before this code is ever reached. However, this is not currently possible
							//under the present API, so for now we just detect this situation and continue.
							if( bend.getAllHandles().isEmpty() )
								continue;
							final Handle handle = bend.getAllHandles().get(handleKey.getHandleIndex());
							final Point2D newPoint = handle.calculateHandleLocation(re.getViewModelSnapshot(), ev);
							
							float x = (float) newPoint.getX();
							float y = (float) newPoint.getY();
							
							re.getBendStore().moveHandle(handleKey, x + (float)deltaX, y + (float)deltaY);
						}
					}
					
					if (!selectedNodes.isEmpty() || !re.getBendStore().getSelectedHandles().isEmpty()) {
						re.setContentChanged();
					}
					if (!selectedNodes.isEmpty() && re.getBendStore().getSelectedHandles().isEmpty()) {
						setHideEdges();
					}
				}
			} else if (!isDragSelectionKeyDown(e)) {
				setDraggingCanvas(true);
				double deltaX = e.getX() - lastXMousePos;
				double deltaY = e.getY() - lastYMousePos;
				lastXMousePos = e.getX();
				lastYMousePos = e.getY();
		
				synchronized (dingLock) {
					double newX = xCenter - (deltaX / scaleFactor);
					double newY = yCenter - (deltaY / scaleFactor);
	                setCenter(newX, newY);
				}
		
				re.setViewportChanged();
				re.getViewModel().setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, xCenter);
				re.getViewModel().setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, yCenter);
				
				repaint();
			}
	
			if (selectionRect != null) {
				final int x = Math.min(lastXMousePos, e.getX());
				final int y = Math.min(lastYMousePos, e.getY());
				final int w = Math.abs(lastXMousePos - e.getX());
				final int h = Math.abs(lastYMousePos - e.getY());
				selectionRect.setBounds(x, y, w, h);
			}

			repaint();
		}

		@Override
		void singleMiddleClick(MouseEvent e) {
			// Nothing to do here...
		}
	}

	public double getScaleFactor(){
		return scaleFactor;
	}

	public void dispose() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
		removeKeyListener(this);
		undoableEdit = null;
		addEdgeMode = null;
		popup.dispose();
	}
	
	public void ensureInitialized() {
		if (!grafx.isInitialized()) {
			grafx.setTransform(xCenter, yCenter, scaleFactor);
		}
	}

	public void setHideEdges() {
		hideEdgesTimer.stop();
		lod.setDrawEdges(false);
		hideEdgesTimer.start();
	}
}
