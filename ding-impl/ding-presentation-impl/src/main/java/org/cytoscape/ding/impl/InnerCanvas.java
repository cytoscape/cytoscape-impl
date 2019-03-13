package org.cytoscape.ding.impl;

import static org.cytoscape.ding.internal.util.ViewUtil.isControlOrMetaDown;
import static org.cytoscape.ding.internal.util.ViewUtil.isDragSelectionKeyDown;

import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.ActionEvent;
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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.ViewChangeEdit;
import org.cytoscape.ding.impl.BendStore.HandleKey;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
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
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
public class InnerCanvas extends DingCanvas implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {

	private final static long serialVersionUID = 1202416511420671L;

	// TODO This is public because BirdsEyeView needs to ensure that it isn't null and that is ridiculous. 
	public GraphGraphics m_grafx;

	final Color SELECTION_RECT_BORDER_COLOR_1;
	final Color SELECTION_RECT_BORDER_COLOR_2;
	
	// MKTODO  GET RID OF GLOBAL VARIABLES!!!!!
	final double[] m_ptBuff = new double[2];
//	final float[] m_extentsBuff2 = new float[4];
	final float[] m_floatBuff1 = new float[2];
	final float[] m_floatBuff2 = new float[2];
//	final Line2D.Float m_line = new Line2D.Float();
	final GeneralPath m_path = new GeneralPath();
	final GeneralPath m_path2 = new GeneralPath();
	
	// MKTODO get rid of these stack variables, methods should return results not take output parameters
//	final LongStack m_stack = new LongStack();
//	final LongStack m_stack2 = new LongStack();
	
	final DingLock m_lock;
	private DRenderingEngine m_re;
	final GraphLOD[] m_lod = new GraphLOD[1];
	double m_xCenter;
	double m_yCenter;
	double m_scaleFactor;
	private int m_lastRenderDetail;
	private Rectangle m_selectionRect;
	private ViewChangeEdit m_undoable_edit;
	private boolean isPrinting;
	private PopupMenuHelper popup;
	
	// MKTODO Really need to get rid of this!!!
//	private LongHash m_hash;

	FontMetrics m_fontMetrics;
	
	private boolean NodeMovement = true;

	/** for turning selection rectangle on and off */
	private boolean selecting = true;
	/** for turning camera panning on and off */
	private boolean draggingCanvas;

	private boolean enablePopupMenu = true;

	private int m_currMouseButton;
	private int m_lastXMousePos;
	private int m_lastYMousePos;
	private boolean m_button1NodeDrag;
	
	private final MousePressedDelegator mousePressedDelegator;
	private final MouseReleasedDelegator mouseReleasedDelegator;
	private final MouseDraggedDelegator mouseDraggedDelegator;
	private final AddEdgeMousePressedDelegator addEdgeMousePressedDelegator;

	private AddEdgeStateMonitor addEdgeMode;
	private Timer hideEdgesTimer;
	private Cursor moveCursor;
	
	private final CyServiceRegistrar serviceRegistrar;

	InnerCanvas(DingLock lock, DRenderingEngine re, CyServiceRegistrar serviceRegistrar) {
		super(DRenderingEngine.Canvas.NETWORK_CANVAS);
		m_lock = lock;
		m_re = re;
		this.serviceRegistrar = serviceRegistrar;
		m_lod[0] = new GraphLOD(); // Default LOD.
		m_backgroundColor = Color.WHITE;
		m_isOpaque = false;
		m_xCenter = 0.0d;
		m_yCenter = 0.0d;
		m_scaleFactor = 1.0d;
//		m_hash = new LongHash();
		
		addEdgeMode = new AddEdgeStateMonitor(this, re);
		popup = new PopupMenuHelper(re, this, serviceRegistrar);
		
		mousePressedDelegator = new MousePressedDelegator();
		mouseReleasedDelegator = new MouseReleasedDelegator();
		mouseDraggedDelegator = new MouseDraggedDelegator();
		addEdgeMousePressedDelegator = new AddEdgeMousePressedDelegator();

		SELECTION_RECT_BORDER_COLOR_1 = UIManager.getColor("Focus.color");
		SELECTION_RECT_BORDER_COLOR_2 = new Color(255, 255, 255, 160);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);

		// Timer to reset edge drawing
		ActionListener taskPerformer = (ActionEvent evt) -> {
			hideEdgesTimer.stop();
			m_lod[0].setDrawEdges(true);
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
			final Image img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			GraphGraphics grafx = new GraphGraphics(img, false, true);

			synchronized (m_lock) {
				m_grafx = grafx;
				
				if (m_re != null)
					m_re.setViewportChanged();
			}
		}
	}

	@Override
	public void update(Graphics g) {
		if (m_grafx == null || m_re == null)
			return;

		// This is the magical portion of code that transfers what is in the
		// visual data structures into what's on the image.
		boolean contentChanged = false;
		boolean viewportChanged = false;
		double xCenter = 0.0d;
		double yCenter = 0.0d;
		double scaleFactor = 1.0d;

		m_fontMetrics = g.getFontMetrics();

		synchronized (m_lock) {
			if (m_re != null && m_re.isDirty()) {
				contentChanged = m_re.isContentChanged();
				viewportChanged = m_re.isViewportChanged();
				renderGraph(m_grafx,/* setLastRenderDetail = */ true, m_lod[0]);
				xCenter = m_xCenter;
				yCenter = m_yCenter;
				scaleFactor = m_scaleFactor;
				
				// set the publicly accessible image object *after* it has been rendered
				m_img = m_grafx.image;
			}
		}

		// if canvas is visible, draw it
		if (isVisible()) {
			// TODO Should this be on the AWT thread?
			g.drawImage(m_grafx.image, 0, 0, null);
		}

		if ((m_selectionRect != null) && (this.isSelecting())) {
			final Graphics2D g2 = (Graphics2D) g;
			// External border
			g2.setColor(SELECTION_RECT_BORDER_COLOR_1);
			g2.draw(m_selectionRect);
			
			// Internal border
			if (m_selectionRect.width > 4 && m_selectionRect.height > 4) {
				g2.setColor(SELECTION_RECT_BORDER_COLOR_2);
				g2.drawRect(
						m_selectionRect.x + 1,
						m_selectionRect.y + 1,
						m_selectionRect.width - 2,
						m_selectionRect.height - 2
				);
			}
		}

		if (contentChanged && m_re != null) {
			final ContentChangeListener lis = m_re.m_cLis[0];

			if (lis != null)
				lis.contentChanged();
		}

		if (viewportChanged && m_re != null) {
			final ViewportChangeListener lis = m_re.m_vLis[0];

			if (lis != null)
				lis.viewportChanged(getWidth(), getHeight(), xCenter, yCenter, scaleFactor);
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
		
		if (m_re != null && w > 0 && h > 0)
			renderGraph(
					new GraphGraphics(new ImageImposter(g, w, h), /* debug = */ false, /* clear = */ false), 
					/* setLastRenderDetail = */ false, m_re.m_printLOD);
		
		isPrinting = false;
	}


	@Override
	public void printNoImposter(Graphics g) {
		isPrinting = true;
		final Image img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (m_re != null)
			renderGraph(new GraphGraphics(img, false, false), /* setLastRenderDetail = */ false, m_re.m_printLOD);
		
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
		if (!m_re.getViewModelSnapshot().isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
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
			final String tooltipText = getToolTipText(e.getPoint());
			final Component[] components = this.getParent().getComponents();
			for (Component comp : components) {
				if (comp instanceof JComponent)
					((JComponent) comp).setToolTipText(tooltipText);
			}
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
		taskManager.execute(taskFactory.createTaskIterator(m_re.getViewModel().getModel()));
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
		m_ptBuff[0] = m_lastXMousePos;
		m_ptBuff[1] = m_lastYMousePos;
		m_re.xformComponentToNodeCoords(m_ptBuff);
		
		List<Long> nodes = m_re.getNodesIntersectingRectangle((float) m_ptBuff[0], (float) m_ptBuff[1], 
											(float) m_ptBuff[0], (float) m_ptBuff[1],
											(m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0);
		// Need to Z-sort this
		long chosenNode = -1;
		if (!nodes.isEmpty()) {
			View<CyNode> nv = null;
			
			for(Long thisNode : nodes) {
				View<CyNode> dnv = m_re.getViewModelSnapshot().getNodeView(thisNode);
				if (nv == null || m_re.getNodeDetails().getZPosition(dnv) > m_re.getNodeDetails().getZPosition(nv)) {
					nv = dnv;
					chosenNode = thisNode;
				}
			}
		}
		return chosenNode;
	}

	private HandleKey getChosenAnchor() {
		m_ptBuff[0] = m_lastXMousePos;
		m_ptBuff[1] = m_lastYMousePos;
		m_re.xformComponentToNodeCoords(m_ptBuff);
		
		HandleKey handleKey = m_re.getBendStore().pickHandle((float)m_ptBuff[0], (float)m_ptBuff[1]);
		return handleKey;
	}
	
	private long getChosenEdge() {
		List<Long> edges = computeEdgesIntersecting(m_lastXMousePos - 1, m_lastYMousePos - 1, m_lastXMousePos + 1, m_lastYMousePos + 1);
		// MKTODO should I return first element or last element???, I think probably the last
		return edges.isEmpty() ? -1 : edges.get(edges.size()-1);
	}
	
//	/**
//	 * @return an array of indices of unselected nodes
//	 */
//	private long[] getUnselectedNodes() {
//		long [] unselectedNodes;
//		// MKTODO
////		if (m_re.m_nodeSelection) { // Unselect all selected nodes.
////			unselectedNodes = m_re.getSelectedNodeIndices();
////		} else
//			unselectedNodes = new long[0];
//		return unselectedNodes;
//
//	}
//	
//	private long[] getUnselectedEdges() {
//		long[] unselectedEdges;
//		// MKTODO
////		if (m_re.m_edgeSelection) { // Unselect all selected edges.
////			unselectedEdges = m_re.getSelectedEdgeIndices();
////		} else
//			unselectedEdges = new long[0];
//		return unselectedEdges;
//	}
	
	
	private int toggleSelectedNode(long chosenNode, MouseEvent e) {
		int chosenNodeSelected = 0;
		final boolean wasSelected = m_re.getNodeDetails().isSelected(m_re.getViewModelSnapshot().getNodeView(chosenNode));
		
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
		View<CyEdge> ev = m_re.getViewModelSnapshot().getEdgeView(edge);
		
		// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
		if (e.isAltDown()) { // Remove handle
			int anchorInx = chosenAnchor.getHandleIndex();
			// Save remove handle
			m_undoable_edit = new ViewChangeEdit(m_re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Remove Edge Handle", serviceRegistrar);

			Bend bend = null;
			if (!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
				Bend defaultBend = m_re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
				if (m_re.getEdgeDetails().getBend(ev) == defaultBend) {
					bend = new BendImpl((BendImpl) defaultBend);
				} else {
					bend = new BendImpl((BendImpl) m_re.getEdgeDetails().getBend(ev));
				}
			}
			
			if(bend != null) {
				View<CyEdge> mutableEdgeView = m_re.getViewModel().getEdgeView(ev.getModel());
				if(mutableEdgeView != null) {
					mutableEdgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bend);
				}
			}
			
			m_re.getBendStore().removeHandle(chosenAnchor);
			m_lod[0].setDrawEdges(true);
			// final GraphViewChangeListener listener = m_view.m_lis[0];
			// listener.graphViewChanged(new GraphViewEdgesSelectedEvent(m_view, DGraphView.makeList(ev.getCyEdge())));
		} else {
			final boolean wasSelected = m_re.getBendStore().isHandleSelected(chosenAnchor);
			// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
			if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
				m_re.getBendStore().unselectHandle(chosenAnchor);
			} else if (!wasSelected) {
				if (!e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()))
					m_re.getBendStore().unselectAllHandles();
				m_re.getBendStore().selectHandle(chosenAnchor);
			}

		}
		m_re.setContentChanged();	
	}
	
	private int toggleSelectedEdge(long chosenEdge, MouseEvent e) {
		int chosenEdgeSelected = 0;

		View<CyEdge> edgeView = m_re.getViewModelSnapshot().getEdgeView(chosenEdge);
		boolean wasSelected = m_re.getEdgeDetails().isSelected(edgeView);
		
		// Add new Handle for Edge Bend.
		// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
		if ((e.isAltDown()) && ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
			m_re.getBendStore().unselectAllHandles();
			m_ptBuff[0] = m_lastXMousePos;
			m_ptBuff[1] = m_lastYMousePos;
			m_re.xformComponentToNodeCoords(m_ptBuff);
			// Store current handle list
			m_undoable_edit = new ViewChangeEdit(m_re, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Add Edge Handle", serviceRegistrar);
			
			Point2D newHandlePoint = new Point2D.Float((float) m_ptBuff[0], (float) m_ptBuff[1]);
			Bend defaultBend = m_re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
			
			if (edgeView.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
				if (defaultBend instanceof BendImpl)
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl) defaultBend));
				else
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
			}
			
			HandleKey handleKey = m_re.getBendStore().addHandle(edgeView, newHandlePoint);
			m_re.getBendStore().selectHandle(handleKey);
		}

		// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
			// ((DEdgeView) m_view.getDEdgeView(chosenEdge)).unselectInternal();
			chosenEdgeSelected = -1;
		} else if (!wasSelected) {
			// ((DEdgeView) m_view.getDEdgeView(chosenEdge)).selectInternal(false);
			chosenEdgeSelected = 1;

			if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
				m_ptBuff[0] = m_lastXMousePos;
				m_ptBuff[1] = m_lastYMousePos;
				m_re.xformComponentToNodeCoords(m_ptBuff);

				HandleKey hit = m_re.getBendStore().pickHandle((float) m_ptBuff[0], (float) m_ptBuff[1]);

				if (hit != null) {
					m_re.getBendStore().selectHandle(hit);
				}
			}
		}

		m_re.setContentChanged();
		return chosenEdgeSelected;
	}
	
	private List<View<CyNode>> getAndApplySelectedNodes() {
		m_ptBuff[0] = m_selectionRect.x;
		m_ptBuff[1] = m_selectionRect.y;
		m_re.xformComponentToNodeCoords(m_ptBuff);

		final float xMin = (float) m_ptBuff[0];
		final float yMin = (float) m_ptBuff[1];
		m_ptBuff[0] = m_selectionRect.x + m_selectionRect.width;
		m_ptBuff[1] = m_selectionRect.y + m_selectionRect.height;
		m_re.xformComponentToNodeCoords(m_ptBuff);

		final float xMax = (float) m_ptBuff[0];
		final float yMax = (float) m_ptBuff[1];
		
		boolean treatNodeShapesAsRectangle = (m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0;
		List<Long> nodesXSect = m_re.getNodesIntersectingRectangle(xMin, yMin, xMax, yMax, treatNodeShapesAsRectangle);

		NodeDetails nodeDetails = m_re.getNodeDetails();
		List<View<CyNode>> selectedNodes = new ArrayList<>(nodesXSect.size());
		for(Long suid : nodesXSect) {
			View<CyNode> node = m_re.getViewModelSnapshot().getNodeView(suid);
			if(!nodeDetails.isSelected(node)) { // MKTODO is this check necessary? so what if it re-selects a node
				selectedNodes.add(node);
			}
		}
		
		if(!selectedNodes.isEmpty()) {
			m_re.setContentChanged();
		}
		return selectedNodes;
	}
	
	
	private List<View<CyEdge>> getAndApplySelectedEdges() {
		if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
			m_ptBuff[0] = m_selectionRect.x;
			m_ptBuff[1] = m_selectionRect.y;
			m_re.xformComponentToNodeCoords(m_ptBuff);

			final float xMin = (float) m_ptBuff[0];
			final float yMin = (float) m_ptBuff[1];
			m_ptBuff[0] = m_selectionRect.x + m_selectionRect.width;
			m_ptBuff[1] = m_selectionRect.y + m_selectionRect.height;
			m_re.xformComponentToNodeCoords(m_ptBuff);

			final float xMax = (float) m_ptBuff[0];
			final float yMax = (float) m_ptBuff[1];

			SpacialIndex2DEnumerator<HandleKey> handles = m_re.getBendStore().queryOverlap(xMin, yMin, xMax, yMax);
			
			if (handles.hasNext()) {
				m_re.setContentChanged();
			}
			while (handles.hasNext()) {
				HandleKey handle = handles.next();
				m_re.getBendStore().selectHandle(handle);
			}
		}

		List<Long> edges = computeEdgesIntersecting(m_selectionRect.x, m_selectionRect.y,
		                         m_selectionRect.x + m_selectionRect.width,
		                         m_selectionRect.y + m_selectionRect.height);


		EdgeDetails edgeDetails = m_re.getEdgeDetails();
		List<View<CyEdge>> selectedEdges = new ArrayList<>(edges.size());
		for (Long edgeXSect : edges) {
			View<CyEdge> edge = m_re.getViewModelSnapshot().getEdgeView(edgeXSect);
			if (!edgeDetails.isSelected(edge)) {
				selectedEdges.add(edge);
			}
		}

		if (!selectedEdges.isEmpty())
			m_re.setContentChanged();
		
		return selectedEdges;
	}

	/**
	 * Returns the tool tip text for the specified location if any exists first
	 * checking nodes, then edges, and then returns null if it's empty space.
	 */
	private String getToolTipText(final Point p) {
		// display tips for nodes before edges
		final View<CyNode> nv = m_re.getPickedNodeView(p);
		if (nv != null)  {
			final String tooltip = m_re.getNodeDetails().getTooltipText(nv);
			return tooltip;
		}
		// only display edge tool tips if the LOD is sufficient
		if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) != 0) {
			View<CyEdge> ev = m_re.getPickedEdgeView(p);
			if (ev != null) 
				return m_re.getEdgeDetails().getTooltipText(ev);
		}

		return null;
	}

	// Puts [last drawn] edges intersecting onto stack; as RootGraph indices.
	// Depends on the state of several member variables, such as m_hash.
	// Clobbers m_stack and m_ptBuff.
	// The rectangle extents are in component coordinate space.
	// IMPORTANT: Code that calls this method should be holding m_lock.
	final List<Long> computeEdgesIntersecting(final int xMini, final int yMini, final int xMaxi, final int yMaxi) {
		m_ptBuff[0] = xMini;
		m_ptBuff[1] = yMini;
		m_re.xformComponentToNodeCoords(m_ptBuff);

		final float xMin = (float) m_ptBuff[0];
		final float yMin = (float) m_ptBuff[1];
		m_ptBuff[0] = xMaxi;
		m_ptBuff[1] = yMaxi;
		m_re.xformComponentToNodeCoords(m_ptBuff);
		
		final float xMax = (float) m_ptBuff[0];
		final float yMax = (float) m_ptBuff[1];
		
		Line2D.Float line = new Line2D.Float();
		float[] extentsBuff = new float[4];
		float[] extentsBuff2 = new float[4];
		
		CyNetworkViewSnapshot snapshot = m_re.getViewModelSnapshot();
		SpacialIndex2DEnumerator<Long> nodeHits = snapshot.getSpacialIndex2D().queryOverlap(xMin, yMin, xMax, yMax);
		
		Set<Long> processedNodes = new HashSet<>();
		List<Long> resultEdges = new ArrayList<>();
		
		if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
			// We won't need to look up arrows and their sizes.
			while(nodeHits.hasNext()) {
				Long node = nodeHits.nextExtents(extentsBuff);
				
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
						float otherNodeX = extentsBuff[0] + extentsBuff[2] / 2;
						float otherNodeY = extentsBuff[1] + extentsBuff[3] / 2;
						line.setLine(nodeX, nodeY, otherNodeX, otherNodeY);
						
						if(line.intersects(xMin, yMin, xMax - xMin, yMax - yMin)) {
							resultEdges.add(edge);
						}
					}
				}
				processedNodes.add(node);
			}
		} else { // Last render high detail.
			
			while(nodeHits.hasNext()) {
				Long node = nodeHits.nextExtents(extentsBuff);
				View<CyNode> nodeView = snapshot.getNodeView(node);
				byte nodeShape = m_re.getNodeDetails().getShape(nodeView);
				
				Iterable<View<CyEdge>> touchingEdges = snapshot.getAdjacentEdgeIterable(node);
				
				for(View<CyEdge> edge : touchingEdges) {
					SnapshotEdgeInfo edgeInfo = snapshot.getEdgeInfo(edge);
					double segThicknessDiv2 = m_re.getEdgeDetails().getWidth(edge) / 2.0d;
					long otherNode = node ^ edgeInfo.getSourceViewSUID() ^ edgeInfo.getTargetViewSUID();
					View<CyNode> otherNodeView = snapshot.getNodeView(otherNode);
					
					if(!processedNodes.contains(otherNode)) {
						snapshot.getSpacialIndex2D().get(otherNode, extentsBuff);
						
						final byte otherNodeShape = m_re.getNodeDetails().getShape(otherNodeView);
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

						if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ARROWS) == 0) {
							srcArrow = trgArrow = ArrowShapeVisualProperty.NONE;
							srcArrowSize = trgArrowSize = 0.0f;
						} else {
							srcArrow = m_re.getEdgeDetails().getSourceArrowShape(edge);
							trgArrow = m_re.getEdgeDetails().getTargetArrowShape(edge);
							srcArrowSize = ((srcArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : m_re.getEdgeDetails().getSourceArrowSize(edge));
							trgArrowSize = ((trgArrow == ArrowShapeVisualProperty.NONE) ? 0.0f : m_re.getEdgeDetails().getTargetArrowSize(edge));
						}

						final EdgeAnchors anchors = (((m_lastRenderDetail
						                              & GraphRenderer.LOD_EDGE_ANCHORS) == 0)
						                             ? null : m_re.getEdgeDetails().getAnchors(snapshot, edge));

						if (!GraphRenderer.computeEdgeEndpoints(m_grafx, srcExtents, srcShape,
						                                        srcArrow, srcArrowSize, anchors,
						                                        trgExtents, trgShape, trgArrow,
						                                        trgArrowSize, m_floatBuff1,
						                                        m_floatBuff2))
							continue;

						m_grafx.getEdgePath(srcArrow, srcArrowSize, trgArrow, trgArrowSize,
						                    m_floatBuff1[0], m_floatBuff1[1], anchors,
						                    m_floatBuff2[0], m_floatBuff2[1], m_path);
						GraphRenderer.computeClosedPath(m_path.getPathIterator(null), m_path2);

						if (m_path2.intersects(xMin - segThicknessDiv2, yMin - segThicknessDiv2,
						                       (xMax - xMin) + (segThicknessDiv2 * 2),
						                       (yMax - yMin) + (segThicknessDiv2 * 2)))
							resultEdges.add(edge.getSUID().longValue());
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
    void setCenter(double x, double y)
    {
        double changeX = x - m_xCenter;
        double changeY = y - m_yCenter;
        m_xCenter = x;
        m_yCenter = y;

        if(addEdgeMode != null && addEdgeMode.addingEdge() )
        {
            Point2D sourcePoint = AddEdgeStateMonitor.getSourcePoint(m_re.getViewModel());
            double newX = sourcePoint.getX() - changeX;
            double newY = sourcePoint.getY() - changeY;
            sourcePoint.setLocation(newX, newY);
            AddEdgeStateMonitor.setSourcePoint(m_re.getViewModel(), sourcePoint);
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

		synchronized (m_lock) {
			m_scaleFactor = m_scaleFactor * factor;
		}

		m_re.setViewportChanged();
		
		// Update view model.
		m_re.getViewModel().setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, m_scaleFactor);

        //This code updates the source point so that it is better related to the selected node.
        //TODO: Center the source point on the selected node perfectly.
        if (addEdgeMode.addingEdge()) {
        	View<CyNode> nodeView = mousePressedDelegator.getPickedNodeView();

            AddEdgeStateMonitor.setSourceNode(m_re.getViewModel(), nodeView);
            double[] coords = new double[2];
            coords[0] = m_re.getNodeDetails().getXPosition(nodeView);
            coords[1] = m_re.getNodeDetails().getYPosition(nodeView);
            ensureInitialized();
            m_re.xformNodeToComponentCoords(coords);

            Point sourceP = new Point();
            sourceP.setLocation(coords[0], coords[1]);
            AddEdgeStateMonitor.setSourcePoint(m_re.getViewModel(), sourceP);
        }
		repaint();
	}


	public int getLastRenderDetail() {
		return m_lastRenderDetail;
	}

	public void setSelecting(boolean s) {
		selecting = s;
	}

	public boolean isSelecting() {
		return selecting;
	}

	// 

	/**
	 * Called to get the tranform matrix used by the inner canvas
	 * to move the nodes.
	 *
	 * @return AffineTransform
	 */
	public AffineTransform getAffineTransform() {
		return (m_grafx != null) ? m_grafx.getTransform() : null;
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
		renderSubgraph(m_grafx, false, m_lod[0], nodes, edges);
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
			int lastRenderDetail = m_re.renderSubgraph(graphics, lod, backgroundColor, m_xCenter, m_yCenter, m_scaleFactor, nodes, edges);
			if (setLastRenderDetail)
				m_lastRenderDetail = lastRenderDetail;
//		}

		repaint();
	}
	
	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	private void renderGraph(GraphGraphics graphics, boolean setLastRenderDetail, GraphLOD lod) {
		int alpha = (m_isOpaque) ? 255 : 0;
		Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(), m_backgroundColor.getBlue(), alpha);

		// long timeBegin = System.currentTimeMillis();
		int lastRenderDetail = m_re.renderGraph(graphics, lod, backgroundColor, m_xCenter, m_yCenter, m_scaleFactor);
		// System.out.println("Rendered graph in "+(System.currentTimeMillis()-timeBegin)+"ms");

		if (setLastRenderDetail)
			m_lastRenderDetail = lastRenderDetail;
		// repaint();
	}

	private void handleEscapeKey() {
		AddEdgeStateMonitor.reset(m_re.getViewModelSnapshot());
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
		
		if (m_re.m_nodeSelection) {
			// move nodes
			Collection<View<CyNode>> selectedNodes = m_re.getViewModelSnapshot().getSelectedNodes();
			for (View<CyNode> node : selectedNodes) {
				double xPos = m_re.getNodeDetails().getXPosition(node);
				double yPos = m_re.getNodeDetails().getYPosition(node);

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
				View<CyNode> mutableNodeView = m_re.getViewModel().getNodeView(node.getSUID());
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, xPos);
				mutableNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, yPos);
			}

			Set<HandleKey> handlesToMove = m_re.getBendStore().getSelectedHandles();
			
			for (HandleKey handleKey : handlesToMove) {
				View<CyEdge> ev = m_re.getViewModelSnapshot().getEdgeView(handleKey.getEdgeSuid());

				// MKTODO this code is copy-pasted in a few places, clean it up
				if(!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
					Bend defaultBend = m_re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
					if(ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
						ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)defaultBend));
					} else {
						Bend bend = m_re.getEdgeDetails().getBend(ev, true);
						ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)bend));
					}
				}
				
				Bend bend = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
				Handle handle = bend.getAllHandles().get(handleKey.getHandleIndex());
				Point2D newPoint = handle.calculateHandleLocation(m_re.getViewModel(),ev);
				
				m_floatBuff1[0] = (float) newPoint.getX();
				m_floatBuff1[1] = (float) newPoint.getY();

				if (code == KeyEvent.VK_UP) {
					m_re.getBendStore().moveHandle(handleKey, m_floatBuff1[0], m_floatBuff1[1] - move);
				} else if (code == KeyEvent.VK_DOWN) {
					m_re.getBendStore().moveHandle(handleKey, m_floatBuff1[0], m_floatBuff1[1] + move);
				} else if (code == KeyEvent.VK_LEFT) {
					m_re.getBendStore().moveHandle(handleKey, m_floatBuff1[0] - move, m_floatBuff1[1]);
				} else if (code == KeyEvent.VK_RIGHT) {
					m_re.getBendStore().moveHandle(handleKey, m_floatBuff1[0] + move, m_floatBuff1[1]);
				}

			}
			repaint();
		}
	}
	
	private void pan(double deltaX, double deltaY) {
		synchronized (m_lock) {
			double newX = m_xCenter - (deltaX / m_scaleFactor);
			double newY = m_yCenter - (deltaY / m_scaleFactor);
            setCenter(newX, newY);
		}
		m_re.setViewportChanged();
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
		       || !( ((chosenNode >= 0) && m_re.isNodeSelected(chosenNode))
		             || (chosenAnchor != null) 
		             || ((chosenEdge >= 0) && m_re.isEdgeSelected(chosenEdge)) ))) {
				selectedNodes = m_re.getViewModelSnapshot().getSelectedNodes();
				selectedEdges = m_re.getViewModelSnapshot().getSelectedEdges();
		}
		
		// Deselect
		m_re.select(selectedNodes, CyNode.class, false);
		m_re.select(selectedEdges, CyEdge.class, false);
	}
	
	private class AddEdgeMousePressedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			Point rawPt = e.getPoint();
			double[] loc = new double[2];
			loc[0] = rawPt.getX();
			loc[1] = rawPt.getY();
			m_re.xformComponentToNodeCoords(loc);
			Point xformPt = new Point();
			xformPt.setLocation(loc[0],loc[1]); 
			View<CyNode> nodeView = m_re.getPickedNodeView(rawPt);
			
			if (nodeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
				popup.createNodeViewMenu(nodeView, e.getX(), e.getY(), "Edge");
			}
		}
	}

	private final class MousePressedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			// System.out.println("MousePressed ----> singleLeftClick");
			m_undoable_edit = null;
		
			m_currMouseButton = 1;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
		
			long chosenNode = -1;
			long chosenEdge = -1;
			HandleKey chosenAnchor = null;
			long chosenNodeSelected = 0;
			long chosenEdgeSelected = 0;
	
			synchronized (m_lock) {
				if (m_re.m_nodeSelection)
					chosenNode = getChosenNode();
	
				if (m_re.m_edgeSelection && (chosenNode < 0) && ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
					chosenAnchor = getChosenAnchor();
	
				if (m_re.m_edgeSelection && (chosenNode < 0) && (chosenAnchor == null))
					chosenEdge = getChosenEdge();
	
				if (chosenNode >= 0)
				    chosenNodeSelected = toggleSelectedNode(chosenNode, e);
	
				if (chosenAnchor != null)
					toggleChosenAnchor(chosenAnchor, e);
	
				if (chosenEdge >= 0)
					chosenEdgeSelected = toggleSelectedEdge(chosenEdge, e);
	
				if ((chosenNode >= 0 || chosenEdge >= 0) && !(e.isShiftDown() || isControlOrMetaDown(e)))
					m_re.getBendStore().unselectAllHandles();
				
				if (chosenNode < 0 && chosenEdge < 0 && chosenAnchor == null 
						&& (m_re.getCyAnnotator().getAnnotationSelection().isEmpty() || !m_re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION))) {
					
					m_button1NodeDrag = false;
					
					if (isDragSelectionKeyDown(e)) {
						m_selectionRect = new Rectangle(m_lastXMousePos, m_lastYMousePos, 0, 0);
						changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					} else {
						changeCursor(getMoveCursor());
					}
				} else {
					m_button1NodeDrag = true;
				}
			}
	
			if (chosenNode < 0 && chosenEdge < 0 && chosenAnchor == null) {
				// Save all node positions for panning
				m_undoable_edit = new ViewChangeEdit(m_re, ViewChangeEdit.SavedObjs.NODES, "Move", serviceRegistrar);
				m_lastXMousePos = e.getX();
				m_lastYMousePos = e.getY();
				m_lod[0].setDrawEdges(false);
			} else {
				maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
				
				if (chosenNode >= 0) {
					View<CyNode> node = m_re.getViewModelSnapshot().getNodeView(chosenNode);
					if (chosenNodeSelected > 0) {
						m_re.select(Collections.singletonList(node), CyNode.class, true);
					}
					else if (chosenNodeSelected < 0)
						m_re.select(Collections.singletonList(node), CyNode.class, false);
				}
				if (chosenEdge >= 0) {
					View<CyEdge> edge = m_re.getViewModelSnapshot().getEdgeView(chosenEdge);
					if (chosenEdgeSelected > 0)
						m_re.select(Collections.singletonList(edge), CyEdge.class, true);
					else if (chosenEdgeSelected < 0)
						m_re.select(Collections.singletonList(edge), CyEdge.class, false);
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
			m_undoable_edit = new ViewChangeEdit(m_re, ViewChangeEdit.SavedObjs.NODES, "Move", serviceRegistrar);
			m_currMouseButton = 3;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
		
			View<CyNode> nview = m_re.getPickedNodeView(e.getPoint());
			if (nview != null && !InnerCanvas.this.isPopupMenuDisabled()) {
                pickedNodeView = nview;
                pickedNodeHeight = m_re.getNodeDetails().getHeight(pickedNodeView);
                pickedNodeWidth  = m_re.getNodeDetails().getWidth(pickedNodeView);
				popup.createNodeViewMenu(nview,e.getX(),e.getY(),"NEW");
			} else {
				View<CyEdge> edgeView = m_re.getPickedEdgeView(e.getPoint());
				if (edgeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
					popup.createEdgeViewMenu(edgeView, e.getX(), e.getY(), "NEW");
				} else {
					// Clicked on empty space...
					Point rawPt = e.getPoint();
					double[] loc = new double[2];
					loc[0] = rawPt.getX();
					loc[1] = rawPt.getY();
					m_re.xformComponentToNodeCoords(loc);
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
			View<CyNode> nview = m_re.getPickedNodeView(e.getPoint());
			if ( nview != null && !InnerCanvas.this.isPopupMenuDisabled())
				popup.createNodeViewMenu(nview,e.getX(), e.getY(), "OPEN");
			else {
				View<CyEdge> edgeView = m_re.getPickedEdgeView(e.getPoint());
				if (edgeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
					popup.createEdgeViewMenu(edgeView,e.getX(),e.getY(),"OPEN");
				} else {
					Point rawPt = e.getPoint();
					double[] loc = new double[2];
					loc[0] = rawPt.getX();
					loc[1] = rawPt.getY();
					m_re.xformComponentToNodeCoords(loc);
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
			switch(m_currMouseButton) {
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
			if (m_currMouseButton == 1) {
				m_currMouseButton = 0;
	
				if (m_selectionRect != null) {
					List<View<CyNode>> selectedNodes = null;
					List<View<CyEdge>> selectedEdges = null;
	
					synchronized (m_lock) {
						if (m_re.m_nodeSelection)
							selectedNodes = getAndApplySelectedNodes();	
						if (m_re.m_edgeSelection)
							selectedEdges = getAndApplySelectedEdges();
					}
					
					m_selectionRect = null;

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
					
					if (!m_lod[0].getDrawEdges()) {
						m_lod[0].setDrawEdges(true);
						m_re.setViewportChanged();
					}
	
					if (selectedNodes != null && !selectedNodes.isEmpty())
						m_re.select(selectedNodes, CyNode.class, true);
	
					if (selectedEdges != null && !selectedEdges.isEmpty())
						m_re.select(selectedEdges, CyEdge.class, true);
					
					repaint();
				} else if (draggingCanvas) {
					setDraggingCanvas(false);
					
					if (m_undoable_edit != null)
						m_undoable_edit.post();

					m_lod[0].setDrawEdges(true);
					m_re.setViewportChanged();
					repaint();
				} else {
					long chosenNode = -1;
					long chosenEdge = -1;
					HandleKey chosenAnchor = null;
					
					if (m_re.m_nodeSelection)
						chosenNode = getChosenNode();
		
					if (m_re.m_edgeSelection && (chosenNode < 0) && ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
						chosenAnchor = getChosenAnchor();
		
					if (m_re.m_edgeSelection && (chosenNode < 0) && (chosenAnchor == null))
						chosenEdge = getChosenEdge();
					
					maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
					
					m_re.setContentChanged();
					repaint();
				}
			}
	
			if (m_undoable_edit != null)
				m_undoable_edit.post();
		}
	
		@Override
		void singleMiddleClick(MouseEvent e) {
			// Nothing to do here...
		}
	
		@Override
		void singleRightClick(MouseEvent e) {
			// System.out.println("MouseReleased ----> singleRightClick");
			if (m_currMouseButton == 3)
				m_currMouseButton = 0;
	
			if (m_undoable_edit != null)
				m_undoable_edit.post();
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
		String componentName = "__CyNetworkView_" + m_re.getViewModel().getSUID(); // see ViewUtil.createUniqueKey(CyNetworkView)
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
			switch(m_currMouseButton) {
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
			if (m_button1NodeDrag) {
				// save selected node and edge positions
				if (m_undoable_edit == null)
					m_undoable_edit = new ViewChangeEdit(m_re, ViewChangeEdit.SavedObjs.SELECTED, "Move", serviceRegistrar);
				
				synchronized (m_lock) {
					m_ptBuff[0] = m_lastXMousePos;
					m_ptBuff[1] = m_lastYMousePos;
					m_re.xformComponentToNodeCoords(m_ptBuff);
	
					final double oldX = m_ptBuff[0];
					final double oldY = m_ptBuff[1];
					m_lastXMousePos = e.getX();
					m_lastYMousePos = e.getY();
					m_ptBuff[0] = m_lastXMousePos;
					m_ptBuff[1] = m_lastYMousePos;
					m_re.xformComponentToNodeCoords(m_ptBuff);
	
					final double newX = m_ptBuff[0];
					final double newY = m_ptBuff[1];
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
	
					Collection<View<CyNode>> selectedNodes = m_re.getViewModelSnapshot().getSelectedNodes();
					// MKTODO rename to 'handlesToMove'
					Set<HandleKey> anchorsToMove = m_re.getBendStore().getSelectedHandles();
					
					if (!anchorsToMove.isEmpty()) { // If we are moving anchors of edges, no need to move nodes (bug #2360).
//					    for (int i = 0; i < selectedNodes.length; i++) {
//						    final NodeView dNodeView = m_view.getDNodeView(selectedNodes[i]);
						for (View<CyNode> node : selectedNodes) {
							View<CyNode> mutableNode = m_re.getViewModel().getNodeView(node.getSUID());
							if(mutableNode != null) {
								NodeDetails nodeDetails = m_re.getNodeDetails();
								double oldXPos = nodeDetails.getXPosition(node);
								double oldYPos = nodeDetails.getYPosition(node);
								// MKTODO Should setting VPs be done using NodeDetails as well??
								node.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, oldXPos + deltaX);
								node.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, oldXPos + deltaY);
							}
					    }
					}
	
					for (HandleKey handleKey : anchorsToMove) {
						View<CyEdge> ev = m_re.getViewModelSnapshot().getEdgeView(handleKey.getEdgeSuid());

						if (!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
							
							Bend defaultBend = m_re.getViewModelSnapshot().getViewDefault(BasicVisualLexicon.EDGE_BEND);
							if( ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend ) {
								if( defaultBend instanceof BendImpl )
									ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)defaultBend));
								else
									ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
							} else {
								Bend bend = m_re.getEdgeDetails().getBend(ev, true);
								ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl)bend));
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
						final Point2D newPoint = handle.calculateHandleLocation(m_re.getViewModel(), ev);
						m_floatBuff1[0] = (float) newPoint.getX();
						m_floatBuff1[1] = (float) newPoint.getY();
						
						m_re.getBendStore().moveHandle(handleKey,  m_floatBuff1[0] + (float)deltaX, m_floatBuff1[1] + (float)deltaY);
					}
	
					if (!selectedNodes.isEmpty() || !m_re.getBendStore().getSelectedHandles().isEmpty()) {
						m_re.setContentChanged();
					}
					if (!selectedNodes.isEmpty() && m_re.getBendStore().getSelectedHandles().isEmpty()) {
						setHideEdges();
					}
				}
			} else if (!isDragSelectionKeyDown(e)) {
				setDraggingCanvas(true);
				double deltaX = e.getX() - m_lastXMousePos;
				double deltaY = e.getY() - m_lastYMousePos;
				m_lastXMousePos = e.getX();
				m_lastYMousePos = e.getY();
		
				synchronized (m_lock) {
					double newX = m_xCenter - (deltaX / m_scaleFactor);
					double newY = m_yCenter - (deltaY / m_scaleFactor);
	                setCenter(newX, newY);
				}
		
				m_re.setViewportChanged();
				m_re.getViewModel().setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, m_xCenter);
				m_re.getViewModel().setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_yCenter);
				
				repaint();
			}
	
			if (m_selectionRect != null) {
				final int x = Math.min(m_lastXMousePos, e.getX());
				final int y = Math.min(m_lastYMousePos, e.getY());
				final int w = Math.abs(m_lastXMousePos - e.getX());
				final int h = Math.abs(m_lastYMousePos - e.getY());
				m_selectionRect.setBounds(x, y, w, h);
			}

			repaint();
		}

		@Override
		void singleMiddleClick(MouseEvent e) {
			// Nothing to do here...
		}
	}

	public double getScaleFactor(){
		return m_scaleFactor;
	}

	public void dispose() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		removeMouseWheelListener(this);
		removeKeyListener(this);
		m_re = null;
		m_undoable_edit = null;
		addEdgeMode = null;
		popup.dispose();
	}
	
	public void ensureInitialized() {
		if (!m_grafx.isInitialized()) {
			m_grafx.setTransform(m_xCenter, m_yCenter, m_scaleFactor);
		}
	}

	public void setHideEdges() {
		hideEdgesTimer.stop();
		m_lod[0].setDrawEdges(false);
		hideEdgesTimer.start();
	}
}
