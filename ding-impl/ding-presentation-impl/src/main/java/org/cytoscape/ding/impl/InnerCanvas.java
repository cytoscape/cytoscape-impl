package org.cytoscape.ding.impl;

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
import java.awt.event.InputEvent;
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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ViewChangeEdit;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.graph.render.export.ImageImposter;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.util.intr.LongEnumerator;
import org.cytoscape.util.intr.LongHash;
import org.cytoscape.util.intr.LongStack;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.work.undo.UndoSupport;

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
 * Canvas to be used for drawing actual network visualization
 */
public class InnerCanvas extends DingCanvas implements MouseListener, MouseMotionListener,
		KeyListener, MouseWheelListener {

	private final static long serialVersionUID = 1202416511420671L;

	// TODO This is public because BirdsEyeView needs to ensure that it isn't null and that is ridiculous. 
	public GraphGraphics m_grafx;

	final Color SELECTION_RECT_BORDER_COLOR_1;
	final Color SELECTION_RECT_BORDER_COLOR_2;
	
	final double[] m_ptBuff = new double[2];
	final float[] m_extentsBuff2 = new float[4];
	final float[] m_floatBuff1 = new float[2];
	final float[] m_floatBuff2 = new float[2];
	final Line2D.Float m_line = new Line2D.Float();
	final GeneralPath m_path = new GeneralPath();
	final GeneralPath m_path2 = new GeneralPath();
	final LongStack m_stack = new LongStack();
	final LongStack m_stack2 = new LongStack();
	final Object m_lock;
	DGraphView m_view;
	final GraphLOD[] m_lod = new GraphLOD[1];
	double m_xCenter;
	double m_yCenter;
	double m_scaleFactor;
	private int m_lastRenderDetail;
	private Rectangle m_selectionRect;
	private ViewChangeEdit m_undoable_edit;
	private boolean isPrinting;
	private PopupMenuHelper popup;
	private LongHash m_hash;

	FontMetrics m_fontMetrics;
	
	private boolean NodeMovement = true;

	/** for turning selection rectangle on and off */
	private boolean selecting = true;
	/** for turning camera panning on and off */
	private boolean draggingCanvas;

	private boolean enablePopupMenu = true;

	private UndoSupport m_undo;
	private IconManager m_iconManager;

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
	

	InnerCanvas(Object lock, DGraphView view, UndoSupport undo, IconManager iconManager) {
		m_lock = lock;
		m_view = view;
		m_undo = undo;
		m_iconManager = iconManager;
		m_lod[0] = new GraphLOD(); // Default LOD.
		m_backgroundColor = Color.WHITE;
		m_isOpaque = false;
		m_xCenter = 0.0d;
		m_yCenter = 0.0d;
		m_scaleFactor = 1.0d;
		m_hash = new LongHash();
		
		addEdgeMode = new AddEdgeStateMonitor(this, m_view);
		popup = new PopupMenuHelper(m_view, this);
		
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
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// System.out.println("hideEdgesTimer expired");
				hideEdgesTimer.stop();
				m_lod[0].setDrawEdges(true);
				m_view.setViewportChanged();
				repaint();
			}
		};
		hideEdgesTimer = new Timer(600, taskPerformer);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0)) {
			final Image img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			GraphGraphics grafx = new GraphGraphics(img, false, true);

			synchronized (m_lock) {
				m_grafx = grafx;
				
				if (m_view != null)
					m_view.setViewportChanged();
			}
		}
	}

	@Override
	public void update(Graphics g) {
		if (m_grafx == null || m_view == null)
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
			if (m_view.isDirty()) {
				contentChanged = m_view.isContentChanged();
				viewportChanged = m_view.isViewportChanged();
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

		if (contentChanged) {
			final ContentChangeListener lis = m_view.m_cLis[0];

			if (lis != null)
				lis.contentChanged();
		}

		if (viewportChanged) {
			final ViewportChangeListener lis = m_view.m_vLis[0];

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
		
		if (m_view != null && w > 0 && h > 0)
			renderGraph(
					new GraphGraphics(new ImageImposter(g, w, h), /* debug = */ false, /* clear = */ false), 
					/* setLastRenderDetail = */ false, m_view.m_printLOD);
		
		isPrinting = false;
	}


	@Override
	public void printNoImposter(Graphics g) {
		isPrinting = true;
		final Image img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		if (m_view != null)
			renderGraph(new GraphGraphics(img, false, false), /* setLastRenderDetail = */ false, m_view.m_printLOD);
		
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
		if (!m_view.isValueLocked(BasicVisualLexicon.NETWORK_SCALE_FACTOR)) {
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


	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }

	public void mouseReleased(MouseEvent e) {
		mouseReleasedDelegator.delegateMouseEvent(e);
	}

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
	public void keyPressed(KeyEvent k) {
		final int code = k.getKeyCode();
		if ( (code == KeyEvent.VK_UP) || (code == KeyEvent.VK_DOWN) || 
		     (code == KeyEvent.VK_LEFT) || (code == KeyEvent.VK_RIGHT)) {
			handleArrowKeys(k);
		} else if ( code == KeyEvent.VK_ESCAPE ) {
			handleEscapeKey();
		}
	}

	/**
	 * Currently not used.
	 * @param k The key event that we're listening for.
	 */
	public void keyReleased(KeyEvent k) { }

	/**
	 * Currently not used.
	 * @param k The key event that we're listening for.
	 */
	public void keyTyped(KeyEvent k) { }


	private long getChosenNode() {
		m_ptBuff[0] = m_lastXMousePos;
		m_ptBuff[1] = m_lastYMousePos;
		m_view.xformComponentToNodeCoords(m_ptBuff);
		m_stack.empty();
		m_view.getNodesIntersectingRectangle((float) m_ptBuff[0], (float) m_ptBuff[1],
		                                     (float) m_ptBuff[0], (float) m_ptBuff[1],
		                                     (m_lastRenderDetail
		                                     & GraphRenderer.LOD_HIGH_DETAIL) == 0,
		                                     m_stack);
		// Need to Z-sort this
		long chosenNode = -1;
		if (m_stack.size() > 0) {
			LongEnumerator le = m_stack.elements();
			DNodeView nv = null;
			while (le.numRemaining() > 0) {
				long thisNode = le.nextLong();
				DNodeView dnv = m_view.getDNodeView(thisNode);
				if (nv == null || dnv.getZPosition() > nv.getZPosition()) {
					nv = dnv;
					chosenNode = thisNode;
				}
			}
		}
		return chosenNode;
	}

	private long getChosenAnchor() {
		m_ptBuff[0] = m_lastXMousePos;
		m_ptBuff[1] = m_lastYMousePos;
		m_view.xformComponentToNodeCoords(m_ptBuff);

		final LongEnumerator hits = m_view.m_spacialA.queryOverlap((float) m_ptBuff[0],
		                                                          (float) m_ptBuff[1],
		                                                          (float) m_ptBuff[0],
		                                                          (float) m_ptBuff[1],
		                                                          null, 0, false);
		long chosenAnchor = (hits.numRemaining() > 0) ? hits.nextLong() : (-1);
		return chosenAnchor;
	}
	
	private long getChosenEdge() {
		computeEdgesIntersecting(m_lastXMousePos - 1, m_lastYMousePos - 1,
                m_lastXMousePos + 1, m_lastYMousePos + 1, m_stack2);
        long chosenEdge = (m_stack2.size() > 0) ? m_stack2.peek() : -1;
        return chosenEdge;
	}
	
	/**
	 * @return an array of indices of unselected nodes
	 */
	private long[] getUnselectedNodes() {
		long [] unselectedNodes;
		if (m_view.m_nodeSelection) { // Unselect all selected nodes.
			unselectedNodes = m_view.getSelectedNodeIndices();
		} else
			unselectedNodes = new long[0];
		return unselectedNodes;

	}
	
	private long[] getUnselectedEdges() {
		long[] unselectedEdges;
		if (m_view.m_edgeSelection) { // Unselect all selected edges.
			unselectedEdges = m_view.getSelectedEdgeIndices();
		} else
			unselectedEdges = new long[0];
		return unselectedEdges;
	}
	
	private int toggleSelectedNode(long chosenNode, MouseEvent e) {
		int chosenNodeSelected = 0;
		final boolean wasSelected = m_view.getDNodeView(chosenNode).isSelected();
		
		//Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
			chosenNodeSelected = -1;
		} else if (!wasSelected) {
			chosenNodeSelected = 1;
		}

		m_button1NodeDrag = true;
		
		return chosenNodeSelected;
	}
	
	
	private void toggleChosenAnchor(long chosenAnchor, MouseEvent e) {
		final long edge = chosenAnchor >>> 6;
		DEdgeView ev = m_view.getDEdgeView(edge);
		// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
		if (e.isAltDown()) {
			// Remove handle
			final int anchorInx = (int)(chosenAnchor & 0x000000000000003f);
			// Save remove handle
			m_undoable_edit = new ViewChangeEdit(m_view,ViewChangeEdit.SavedObjs.SELECTED_EDGES,"Remove Edge Handle",m_undo);

			if (!ev.isValueLocked(BasicVisualLexicon.EDGE_BEND)) {
				Bend defaultBend = ev.getDefaultValue(BasicVisualLexicon.EDGE_BEND);
				if (ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend) {
					ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl) defaultBend));
				} else {
					ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl((BendImpl) ev.getBend()));
				}
			}
			
			ev.removeHandle(anchorInx);
			m_button1NodeDrag = false;
			m_lod[0].setDrawEdges(true);
			// final GraphViewChangeListener listener = m_view.m_lis[0];
			// listener.graphViewChanged(new GraphViewEdgesSelectedEvent(m_view, DGraphView.makeList(ev.getCyEdge())));
		} else {
			final boolean wasSelected = m_view.m_selectedAnchors.count(chosenAnchor) > 0;
			//Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
			if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
				m_view.m_selectedAnchors.delete(chosenAnchor);
			} else if (!wasSelected) {
				if (!e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()))
					m_view.m_selectedAnchors.empty();

				m_view.m_selectedAnchors.insert(chosenAnchor);
			}

			m_button1NodeDrag = true;
		}
		m_view.setContentChanged();	
	}
	
	private int toggleSelectedEdge(long chosenEdge, MouseEvent e) {
		int chosenEdgeSelected = 0;

		final boolean wasSelected = m_view.getDEdgeView(chosenEdge).isSelected();
		
		// Add new Handle for Edge Bend.
		// Linux users should use Ctrl-Alt since many window managers capture Alt-drag to move windows
		if ((e.isAltDown()) && ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
			
			m_view.m_selectedAnchors.empty();
			m_ptBuff[0] = m_lastXMousePos;
			m_ptBuff[1] = m_lastYMousePos;
			m_view.xformComponentToNodeCoords(m_ptBuff);
			// Store current handle list
			m_undoable_edit = new ViewChangeEdit(m_view, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Add Edge Handle", m_undo);
			final Point2D newHandlePoint = new Point2D.Float((float) m_ptBuff[0], (float) m_ptBuff[1]);
			DEdgeView edgeView = m_view.getDEdgeView(chosenEdge);
			Bend defaultBend = edgeView.getDefaultValue(BasicVisualLexicon.EDGE_BEND);
			if( edgeView.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend )
			{
				if( defaultBend instanceof BendImpl )
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl( (BendImpl)defaultBend));
				else
					edgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
			}
			DEdgeView ev = m_view.getDEdgeView(chosenEdge);
			final int chosenInx = ev.addHandlePoint(newHandlePoint);
			
			m_view.m_selectedAnchors.insert(((chosenEdge) << 6) | chosenInx);
		}

		//Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if (wasSelected && (e.isShiftDown() || (isControlOrMetaDown(e) && !e.isAltDown()))) {
			// ((DEdgeView) m_view.getDEdgeView(chosenEdge)).unselectInternal();
			chosenEdgeSelected = -1;
		} else if (!wasSelected) {
			// ((DEdgeView) m_view.getDEdgeView(chosenEdge)).selectInternal(false);
			chosenEdgeSelected = 1;

			if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
				m_ptBuff[0] = m_lastXMousePos;
				m_ptBuff[1] = m_lastYMousePos;
				m_view.xformComponentToNodeCoords(m_ptBuff);

				final LongEnumerator hits = m_view.m_spacialA.queryOverlap((float) m_ptBuff[0], (float) m_ptBuff[1],
						(float) m_ptBuff[0], (float) m_ptBuff[1], null, 0, false);

				if (hits.numRemaining() > 0) {
					final long hit = hits.nextLong();

					if (m_view.m_selectedAnchors.count(hit) == 0)
						m_view.m_selectedAnchors.insert(hit);
				}
			}
		}

		m_button1NodeDrag = true;
		m_view.setContentChanged();
		return chosenEdgeSelected;
	}
	
	private long[] setSelectedNodes() {
		long [] selectedNodes = null;
		
		m_ptBuff[0] = m_selectionRect.x;
		m_ptBuff[1] = m_selectionRect.y;
		m_view.xformComponentToNodeCoords(m_ptBuff);

		final double xMin = m_ptBuff[0];
		final double yMin = m_ptBuff[1];
		m_ptBuff[0] = m_selectionRect.x + m_selectionRect.width;
		m_ptBuff[1] = m_selectionRect.y + m_selectionRect.height;
		m_view.xformComponentToNodeCoords(m_ptBuff);

		final double xMax = m_ptBuff[0];
		final double yMax = m_ptBuff[1];
		m_stack.empty();
		m_view.getNodesIntersectingRectangle((float) xMin, (float) yMin,
		                                     (float) xMax, (float) yMax,
		                                     (m_lastRenderDetail
		                                     & GraphRenderer.LOD_HIGH_DETAIL) == 0,
		                                     m_stack);
		m_stack2.empty();

		final LongEnumerator nodesXSect = m_stack.elements();

		while (nodesXSect.numRemaining() > 0) {
			final long nodeXSect = nodesXSect.nextLong();

			if (m_view.m_selectedNodes.count(nodeXSect) == 0)
				m_stack2.push(nodeXSect);
		}

		selectedNodes = new long[m_stack2.size()];

		final LongEnumerator nodes = m_stack2.elements();

		for (int i = 0; i < selectedNodes.length; i++)
			selectedNodes[i] = nodes.nextLong();

		if (selectedNodes.length > 0)
			m_view.setContentChanged();	
		return selectedNodes;
	}
	
	
	private long [] setSelectedEdges() {
		long [] selectedEdges = null;
		if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
			m_ptBuff[0] = m_selectionRect.x;
			m_ptBuff[1] = m_selectionRect.y;
			m_view.xformComponentToNodeCoords(m_ptBuff);

			final double xMin = m_ptBuff[0];
			final double yMin = m_ptBuff[1];
			m_ptBuff[0] = m_selectionRect.x + m_selectionRect.width;
			m_ptBuff[1] = m_selectionRect.y + m_selectionRect.height;
			m_view.xformComponentToNodeCoords(m_ptBuff);

			final double xMax = m_ptBuff[0];
			final double yMax = m_ptBuff[1];
			final LongEnumerator hits = m_view.m_spacialA.queryOverlap((float) xMin,
			                                                          (float) yMin,
			                                                          (float) xMax,
			                                                          (float) yMax,
			                                                          null,
			                                                          0,
			                                                          false);

			if (hits.numRemaining() > 0)
				m_view.setContentChanged();

			while (hits.numRemaining() > 0) {
				final long hit = hits.nextLong();

				if (m_view.m_selectedAnchors.count(hit) == 0)
					m_view.m_selectedAnchors.insert(hit);
			}
		}

		computeEdgesIntersecting(m_selectionRect.x, m_selectionRect.y,
		                         m_selectionRect.x + m_selectionRect.width,
		                         m_selectionRect.y + m_selectionRect.height,
		                         m_stack2);
		m_stack.empty();

		final LongEnumerator edgesXSect = m_stack2.elements();

		while (edgesXSect.numRemaining() > 0) {
			final long edgeXSect = edgesXSect.nextLong();

			if (m_view.m_selectedEdges.count(edgeXSect) == 0)
				m_stack.push(edgeXSect);
		}

		selectedEdges = new long[m_stack.size()];

		final LongEnumerator edges = m_stack.elements();

		for (int i = 0; i < selectedEdges.length; i++)
			selectedEdges[i] = edges.nextLong();

		if (selectedEdges.length > 0)
			m_view.setContentChanged();
		return selectedEdges;
	}

	/**
	 * Returns the tool tip text for the specified location if any exists first
	 * checking nodes, then edges, and then returns null if it's empty space.
	 */
	private String getToolTipText(final Point p) {
		// display tips for nodes before edges
		final DNodeView nv = (DNodeView) m_view.getPickedNodeView(p);
		if (nv != null)  {
			final String tooltip = nv.getToolTip();
			return tooltip;
		}
		// only display edge tool tips if the LOD is sufficient
		if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) != 0) {
				DEdgeView ev = (DEdgeView) m_view.getPickedEdgeView(p);
				if (ev != null) 
					return m_view.m_edgeDetails.getTooltipText(ev.getCyEdge(), 0);
		}

		return null;
	}

	// Puts [last drawn] edges intersecting onto stack; as RootGraph indices.
	// Depends on the state of several member variables, such as m_hash.
	// Clobbers m_stack and m_ptBuff.
	// The rectangle extents are in component coordinate space.
	// IMPORTANT: Code that calls this method should be holding m_lock.
	final void computeEdgesIntersecting(final int xMini, final int yMini, final int xMaxi,
	                                    final int yMaxi, final LongStack stack) {
		m_ptBuff[0] = xMini;
		m_ptBuff[1] = yMini;
		m_view.xformComponentToNodeCoords(m_ptBuff);

		final double xMin = m_ptBuff[0];
		final double yMin = m_ptBuff[1];
		m_ptBuff[0] = xMaxi;
		m_ptBuff[1] = yMaxi;
		m_view.xformComponentToNodeCoords(m_ptBuff);

		final double xMax = m_ptBuff[0];
		final double yMax = m_ptBuff[1];
		LongEnumerator edgeNodesEnum = m_hash.elements(); // Positive.
		m_stack.empty();

		final int edgeNodesCount = edgeNodesEnum.numRemaining();

		for (int i = 0; i < edgeNodesCount; i++)
			m_stack.push(edgeNodesEnum.nextLong());

		m_hash.empty();
		edgeNodesEnum = m_stack.elements();
		stack.empty();

		final CyNetwork graph = m_view.m_drawPersp;

		if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
			// We won't need to look up arrows and their sizes.
			for (int i = 0; i < edgeNodesCount; i++) {
				final long node = edgeNodesEnum.nextLong(); // Positive.
				final CyNode nodeObj = graph.getNode(node);

				if (!m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0))
					continue; // Will happen if e.g. node was removed. 

				final float nodeX = (m_view.m_extentsBuff[0] + m_view.m_extentsBuff[2]) / 2;
				final float nodeY = (m_view.m_extentsBuff[1] + m_view.m_extentsBuff[3]) / 2;
				final Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(nodeObj, CyEdge.Type.ANY);

				for ( CyEdge e : touchingEdges ) {      
					final long edge = e.getSUID(); 
					final long otherNode = node ^ e.getSource().getSUID().longValue() ^ e.getTarget().getSUID().longValue(); 

					if (m_hash.get(otherNode) < 0) {
						m_view.m_spacial.exists(otherNode, m_view.m_extentsBuff, 0);

						final float otherNodeX = (m_view.m_extentsBuff[0] + m_view.m_extentsBuff[2]) / 2;
						final float otherNodeY = (m_view.m_extentsBuff[1] + m_view.m_extentsBuff[3]) / 2;
						m_line.setLine(nodeX, nodeY, otherNodeX, otherNodeY);

						if (m_line.intersects(xMin, yMin, xMax - xMin, yMax - yMin))
							stack.push(edge);
					}
				}

				m_hash.put(node);
			}
		} else { // Last render high detail.
			for (int i = 0; i < edgeNodesCount; i++) {
				final long node = edgeNodesEnum.nextLong(); // Positive.
				final CyNode nodeObj = graph.getNode(node);

				if (!m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0))
					continue; /* Will happen if e.g. node was removed. */

				final byte nodeShape = m_view.m_nodeDetails.getShape(nodeObj);
				final Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(nodeObj, CyEdge.Type.ANY);
 
				for ( CyEdge edge : touchingEdges ) {      
//					final int edge = e.getIndex(); // Positive.
					final double segThicknessDiv2 = m_view.m_edgeDetails.getWidth(edge) / 2.0d;
					final long otherNode = node ^ edge.getSource().getSUID().longValue() ^ edge.getTarget().getSUID().longValue();
					final CyNode otherNodeObj = graph.getNode(otherNode);

					if (m_hash.get(otherNode) < 0) {
						m_view.m_spacial.exists(otherNode, m_extentsBuff2, 0);

						final byte otherNodeShape = m_view.m_nodeDetails.getShape(otherNodeObj);
						final byte srcShape;
						final byte trgShape;
						final float[] srcExtents;
						final float[] trgExtents;

						if (node == edge.getSource().getSUID().longValue()) {
							srcShape = nodeShape;
							trgShape = otherNodeShape;
							srcExtents = m_view.m_extentsBuff;
							trgExtents = m_extentsBuff2;
						} else { // node == graph.edgeTarget(edge).
							srcShape = otherNodeShape;
							trgShape = nodeShape;
							srcExtents = m_extentsBuff2;
							trgExtents = m_view.m_extentsBuff;
						}

						final byte srcArrow;
						final byte trgArrow;
						final float srcArrowSize;
						final float trgArrowSize;

						if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ARROWS) == 0) {
							srcArrow = trgArrow = GraphGraphics.ARROW_NONE;
							srcArrowSize = trgArrowSize = 0.0f;
						} else {
							srcArrow = m_view.m_edgeDetails.getSourceArrowShape(edge);
							trgArrow = m_view.m_edgeDetails.getTargetArrowShape(edge);
							srcArrowSize = ((srcArrow == GraphGraphics.ARROW_NONE) 
							                ? 0.0f
							                : m_view.m_edgeDetails.getSourceArrowSize(edge));
							trgArrowSize = ((trgArrow == GraphGraphics.ARROW_NONE) 
							                ? 0.0f
							                : m_view.m_edgeDetails.getTargetArrowSize(edge));
						}

						final EdgeAnchors anchors = (((m_lastRenderDetail
						                              & GraphRenderer.LOD_EDGE_ANCHORS) == 0)
						                             ? null : m_view.m_edgeDetails.getAnchors(edge));

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
							stack.push(edge.getSUID().longValue());
					}
				}

				m_hash.put(node);
			}
		}
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

        if( addEdgeMode.addingEdge() )
        {
            Point2D sourcePoint = AddEdgeStateMonitor.getSourcePoint(m_view);
            double newX = sourcePoint.getX() - changeX;
            double newY = sourcePoint.getY() - changeY;
            sourcePoint.setLocation(newX, newY);
            AddEdgeStateMonitor.setSourcePoint(m_view, sourcePoint);
        }

    }

	
	private void adjustZoom(int notches) {
		final double factor;
		
		if (notches < 0)
			factor = 1.1; // scroll up, zoom in
		else
			factor = 0.9; // scroll down, zoom out

		synchronized (m_lock) {
			m_scaleFactor = m_scaleFactor * factor;
		}

		m_view.setViewportChanged();
		
		// Update view model.
		m_view.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, m_scaleFactor);

        //This code updates the source point so that it is better related to the selected node.
        //TODO: Center the source point on the selected node perfectly.
        if (addEdgeMode.addingEdge())
        {
            NodeView nodeView = mousePressedDelegator.getPickedNodeView();
            View<CyNode> view = (DNodeView)mousePressedDelegator.getPickedNodeView();

            AddEdgeStateMonitor.setSourceNode(m_view, view.getModel());
            double[] coords = new double[2];
            coords[0] = nodeView.getXPosition();
            coords[1] = nodeView.getYPosition();
            ensureInitialized();
            ((DGraphView) m_view).xformNodeToComponentCoords(coords);

            Point sourceP = new Point();
            sourceP.setLocation(coords[0], coords[1]);
            AddEdgeStateMonitor.setSourcePoint(m_view, sourceP);
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

	public void updateSubgraph(List<CyNode> nodes, List<CyEdge> edges) {
		renderSubgraph(m_grafx, false, m_lod[0], nodes, edges);
	}

	// Render just a portion of the graph.  This is used for selections when we want to overwrite
	// a limited number of nodes and edges
	private void renderSubgraph(GraphGraphics graphics, final boolean setLastRenderDetail, final GraphLOD lod, 
	                            List<CyNode> nodes, List<CyEdge> edges) {

		// Pass the color even though we won't use it if we actually only render the subgraph.  If we're
		// not in largeModel mode, or we're only painting a small portion of the network, we'll wind up
		// calling renderGraph anyways and we'll need to clear the image
		final Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(),
							m_backgroundColor.getBlue(), 0);

		int lastRenderDetail = m_view.renderSubgraph(graphics, lod, backgroundColor, 
		                                             m_xCenter, m_yCenter, m_scaleFactor, new LongHash(), nodes, edges);
		if (setLastRenderDetail)
			m_lastRenderDetail = lastRenderDetail;

		repaint();
	}
	
	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	private void renderGraph(GraphGraphics graphics, final boolean setLastRenderDetail, final GraphLOD lod) {
		final int alpha = (m_isOpaque) ? 255 : 0;

		final Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(),
							m_backgroundColor.getBlue(), alpha);

		// long timeBegin = System.currentTimeMillis();
		int lastRenderDetail = m_view.renderGraph(graphics, lod, backgroundColor, m_xCenter, m_yCenter, m_scaleFactor, m_hash);
		// System.out.println("Rendered graph in "+(System.currentTimeMillis()-timeBegin)+"ms");

		if (setLastRenderDetail)
			m_lastRenderDetail = lastRenderDetail;
		// repaint();
	}

	private void handleEscapeKey() {
		AddEdgeStateMonitor.reset(m_view);
		repaint();
	}

	/**
	 * Arrow key handler.
	 * They are used to pan and mode nodes/edge bend handles.
	 * @param k key event
	 */
	private void handleArrowKeys(KeyEvent k) {
		final int code = k.getKeyCode();
		double move = 1.0;

		// Adjust increment if Shift key is pressed
		if (k.isShiftDown())
			move = 15.0;
		
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
		
		if (m_view.m_nodeSelection) {
			// move nodes
			final long[] selectedNodes = m_view.getSelectedNodeIndices();
			for (int i = 0; i < selectedNodes.length; i++) {
				DNodeView nv = ((DNodeView) m_view.getDNodeView(selectedNodes[i]));
				double xPos = nv.getXPosition();
				double yPos = nv.getYPosition();

				if (code == KeyEvent.VK_UP) {
					yPos -= move;
				} else if (code == KeyEvent.VK_DOWN) {
					yPos += move;
				} else if (code == KeyEvent.VK_LEFT) {
					xPos -= move;
				} else if (code == KeyEvent.VK_RIGHT) {
					xPos += move;
				}

				nv.setOffset(xPos, yPos);
			}

			// move edge anchors
			LongEnumerator anchorsToMove = m_view.m_selectedAnchors.searchRange(Integer.MIN_VALUE,
			                                                                   Integer.MAX_VALUE,
			                                                                   false);

			while (anchorsToMove.numRemaining() > 0) {
				final long edgeAndAnchor = anchorsToMove.nextLong();
				final long edge = edgeAndAnchor >>> 6;
				final int anchorInx = (int)(edgeAndAnchor & 0x000000000000003f);
				final DEdgeView ev = (DEdgeView) m_view.getDEdgeView(edge);

				if( !ev.isValueLocked(BasicVisualLexicon.EDGE_BEND) )
				{
					Bend defaultBend = ev.getDefaultValue(BasicVisualLexicon.EDGE_BEND);
					if( ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend )
					{
						ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl( (BendImpl)defaultBend) );
					}
					else
					{
						ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl( (BendImpl)ev.getBend()) );
					}
				}
				final Bend bend = ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
				final Handle handle = bend.getAllHandles().get(anchorInx);
				final Point2D newPoint = handle.calculateHandleLocation(m_view.getViewModel(),ev);
				m_floatBuff1[0] = (float) newPoint.getX();
				m_floatBuff1[1] = (float) newPoint.getY();

				if (code == KeyEvent.VK_UP) {
					ev.moveHandleInternal(anchorInx, m_floatBuff1[0], m_floatBuff1[1] - move);
				} else if (code == KeyEvent.VK_DOWN) {
					ev.moveHandleInternal(anchorInx, m_floatBuff1[0], m_floatBuff1[1] + move);
				} else if (code == KeyEvent.VK_LEFT) {
					ev.moveHandleInternal(anchorInx, m_floatBuff1[0] - move, m_floatBuff1[1]);
				} else if (code == KeyEvent.VK_RIGHT) {
					ev.moveHandleInternal(anchorInx, m_floatBuff1[0] + move, m_floatBuff1[1]);
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
		m_view.setViewportChanged();
		setHideEdges();
		repaint();
	}
	
	private void maybeDeselectAll(final MouseEvent e, long chosenNode, long chosenEdge, long chosenAnchor) {
		long[] unselectedNodes = null;
		long[] unselectedEdges = null;
		
		// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
		if ((!e.isShiftDown() && !(e.isControlDown() && !e.isAltDown()) && !e.isMetaDown()) // If shift is down never unselect.
		    && (((chosenNode < 0) && (chosenEdge < 0) && (chosenAnchor < 0)) // Mouse missed all.
		       // Not [we hit something but it was already selected].
		       || !( ((chosenNode >= 0) && m_view.getDNodeView(chosenNode).isSelected())
		             || (chosenAnchor >= 0) 
		             || ((chosenEdge >= 0) && m_view.getDEdgeView(chosenEdge).isSelected()) ))) {
				unselectedNodes = getUnselectedNodes();
				unselectedEdges = getUnselectedEdges();
		}
		
		if ((unselectedNodes != null) && (unselectedNodes.length > 0)) {
			List<CyNode> unselectedNodeList = DGraphView.makeNodeList(unselectedNodes, m_view);
			select(unselectedNodeList, CyNode.class, false);
		}

		if ((unselectedEdges != null) && (unselectedEdges.length > 0)) {
			List<CyEdge> unselectedEdgeList = DGraphView.makeEdgeList(unselectedEdges, m_view);
			select(unselectedEdgeList, CyEdge.class, false);
		}
	}
	
	private static boolean isDragSelectionKeyDown(final InputEvent e) {
		return e.isShiftDown() || isControlOrMetaDown(e);
	}
	
	private static boolean isControlOrMetaDown(final InputEvent e) {
		final boolean isMac = LookAndFeelUtil.isMac();
		
		return (isMac && e.isMetaDown()) || (!isMac && e.isControlDown());
	}
	
	private class AddEdgeMousePressedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			Point rawPt = e.getPoint();
			double[] loc = new double[2];
			loc[0] = rawPt.getX();
			loc[1] = rawPt.getY();
			m_view.xformComponentToNodeCoords(loc);
			Point xformPt = new Point();
			xformPt.setLocation(loc[0],loc[1]); 
			NodeView nodeView = m_view.getPickedNodeView(rawPt);
			
			if ( nodeView != null && !InnerCanvas.this.isPopupMenuDisabled())
				popup.createNodeViewMenu(nodeView, e.getX(), e.getY(), "Edge");
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
			long chosenAnchor = -1;
			long chosenNodeSelected = 0;
			long chosenEdgeSelected = 0;
	
			synchronized (m_lock) {
				if (m_view.m_nodeSelection)
					chosenNode = getChosenNode();
	
				if (m_view.m_edgeSelection && (chosenNode < 0)
						&& ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
					chosenAnchor = getChosenAnchor();
	
				if (m_view.m_edgeSelection && (chosenNode < 0) && (chosenAnchor < 0))
					chosenEdge = getChosenEdge();
	
				if (chosenNode >= 0)
				    chosenNodeSelected = toggleSelectedNode(chosenNode, e);
	
				if (chosenAnchor >= 0)
					toggleChosenAnchor(chosenAnchor, e);
	
				if (chosenEdge >= 0)
					chosenEdgeSelected = toggleSelectedEdge(chosenEdge, e);
	
				if ((chosenNode >= 0 || chosenEdge >= 0) && !(e.isShiftDown() || isControlOrMetaDown(e)))
					m_view.m_selectedAnchors.empty();
				
				if (chosenNode < 0 && chosenEdge < 0 && chosenAnchor < 0) {
					m_button1NodeDrag = false;
					
					if (isDragSelectionKeyDown(e)) {
						m_selectionRect = new Rectangle(m_lastXMousePos, m_lastYMousePos, 0, 0);
						changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					} else {
						changeCursor(getMoveCursor());
					}
				}
			}
	
			if (chosenNode < 0 && chosenEdge < 0 && chosenAnchor < 0) {
				// Save all node positions for panning
				m_undoable_edit = new ViewChangeEdit(m_view, ViewChangeEdit.SavedObjs.NODES, "Move", m_undo);
				m_lastXMousePos = e.getX();
				m_lastYMousePos = e.getY();
				m_lod[0].setDrawEdges(false);
			} else {
				maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
				
				if (chosenNode >= 0) {
					CyNode node = ((DNodeView)m_view.getDNodeView(chosenNode)).getModel();
					
					if (chosenNodeSelected > 0) {
						select(Collections.singletonList(node), CyNode.class, true);
					} else if (chosenNodeSelected < 0) {
						select(Collections.singletonList(node), CyNode.class, false);
					}
				}
		
				if (chosenEdge >= 0) {
					CyEdge edge = m_view.getDEdgeView(chosenEdge).getCyEdge();
					
					if (chosenEdgeSelected > 0) {
						select(Collections.singletonList(edge), CyEdge.class, true);
					} else if (chosenEdgeSelected < 0) {
						select(Collections.singletonList(edge), CyEdge.class, false);
					}
				}
			}

			repaint();
		}

		@Override
		void singleMiddleClick(MouseEvent e) {
			// Nothing to do here...
		}

        private NodeView pickedNodeView = null;
        private double pickedNodeWidth = 0.0;
        private double pickedNodeHeight = 0.0;

		private NodeView getPickedNodeView() {
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
			m_undoable_edit = new ViewChangeEdit(m_view,ViewChangeEdit.SavedObjs.NODES,"Move",m_undo);
			m_currMouseButton = 3;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
		
			NodeView nview = m_view.getPickedNodeView(e.getPoint());
			if (nview != null && !InnerCanvas.this.isPopupMenuDisabled()) {
                pickedNodeView = nview;
                pickedNodeHeight = pickedNodeView.getHeight();
                pickedNodeWidth = pickedNodeView.getWidth();
				popup.createNodeViewMenu(nview,e.getX(),e.getY(),"NEW");
			} else {
				EdgeView edgeView = m_view.getPickedEdgeView(e.getPoint());
				if (edgeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
					popup.createEdgeViewMenu(edgeView, e.getX(), e.getY(), "NEW");
				} else {
					// Clicked on empty space...
					Point rawPt = e.getPoint();
					double[] loc = new double[2];
					loc[0] = rawPt.getX();
					loc[1] = rawPt.getY();
					m_view.xformComponentToNodeCoords(loc);
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
			NodeView nview = m_view.getPickedNodeView(e.getPoint());
			if ( nview != null && !InnerCanvas.this.isPopupMenuDisabled())
				popup.createNodeViewMenu(nview,e.getX(), e.getY(), "OPEN");
			else {
				EdgeView edgeView = m_view.getPickedEdgeView(e.getPoint());
				if (edgeView != null && !InnerCanvas.this.isPopupMenuDisabled()) {
					popup.createEdgeViewMenu(edgeView,e.getX(),e.getY(),"OPEN");
				} else {
					Point rawPt = e.getPoint();
					double[] loc = new double[2];
					loc[0] = rawPt.getX();
					loc[1] = rawPt.getY();
					m_view.xformComponentToNodeCoords(loc);
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
					long[] selectedNodes = null;
					long[] selectedEdges = null;
	
					synchronized (m_lock) {
						if (m_view.m_nodeSelection || m_view.m_edgeSelection) {
							if (m_view.m_nodeSelection)
								selectedNodes = setSelectedNodes();	
							if (m_view.m_edgeSelection)
								selectedEdges = setSelectedEdges();
						}
					}
					
					m_selectionRect = null;

					// Update visual property value (x/y)
					if (selectedNodes != null){
						for (long node : selectedNodes) {
							final DNodeView dNodeView = (DNodeView) m_view.getDNodeView(node);
							dNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, dNodeView.getXPosition());
							dNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, dNodeView.getYPosition());
						}						
					}
					if (!m_lod[0].getDrawEdges()) {
						m_lod[0].setDrawEdges(true);
						m_view.setViewportChanged();
					}
	
					if ((selectedNodes != null) && (selectedNodes.length > 0))
						select(DGraphView.makeNodeList(selectedNodes,m_view), CyNode.class, true);
	
					if ((selectedEdges != null) && (selectedEdges.length > 0))
						select(DGraphView.makeEdgeList(selectedEdges,m_view), CyNode.class, true);
					
					repaint();
				} else if (draggingCanvas) {
					setDraggingCanvas(false);
					
					if (m_undoable_edit != null)
						m_undoable_edit.post();

					m_lod[0].setDrawEdges(true);
					m_view.setViewportChanged();
					repaint();
				} else {
					long chosenNode = -1;
					long chosenEdge = -1;
					long chosenAnchor = -1;
					
					if (m_view.m_nodeSelection)
						chosenNode = getChosenNode();
		
					if (m_view.m_edgeSelection && (chosenNode < 0)
							&& ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0))
						chosenAnchor = getChosenAnchor();
		
					if (m_view.m_edgeSelection && (chosenNode < 0) && (chosenAnchor < 0))
						chosenEdge = getChosenEdge();
					
					maybeDeselectAll(e, chosenNode, chosenEdge, chosenAnchor);
					
					m_view.setContentChanged();
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
		this.draggingCanvas = draggingCanvas;
		
		Cursor cursor;
		if(draggingCanvas)
			cursor = getMoveCursor();
		else
			cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

		changeCursor(cursor);
	}

	private void changeCursor(Cursor cursor) {
		String componentName = "__CyNetworkView_" + m_view.getSUID(); // see ViewUtil.createUniqueKey(CyNetworkView)
		Container parent = this;
		while(parent != null) {
			if(componentName.equals(parent.getName())) {
				parent.setCursor(cursor);
				break;
			}
			parent = parent.getParent();
		}
	}
	
	private Cursor getMoveCursor() {
		if(moveCursor == null) {
			Cursor cursor;
			if(LookAndFeelUtil.isMac()) {
				Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(24, 24);
				Image image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
				Graphics graphics = image.getGraphics();
				
				String icon = IconManager.ICON_ARROWS;
				JLabel label = new JLabel();
				label.setBounds(0 , 0, size.width, size.height);
				label.setText(icon);
				label.setFont(m_iconManager.getIconFont(14));
				label.paint(graphics);
				graphics.dispose();
				
				cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0,0), "custom:" + (int)icon.charAt(0));
			}
			else {
				cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
				if(cursor == null) {
					cursor = new Cursor(Cursor.MOVE_CURSOR);
				}
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
					m_undoable_edit = new ViewChangeEdit(m_view, ViewChangeEdit.SavedObjs.SELECTED, "Move",m_undo);
				
				synchronized (m_lock) {
					m_ptBuff[0] = m_lastXMousePos;
					m_ptBuff[1] = m_lastYMousePos;
					m_view.xformComponentToNodeCoords(m_ptBuff);
	
					final double oldX = m_ptBuff[0];
					final double oldY = m_ptBuff[1];
					m_lastXMousePos = e.getX();
					m_lastYMousePos = e.getY();
					m_ptBuff[0] = m_lastXMousePos;
					m_ptBuff[1] = m_lastYMousePos;
					m_view.xformComponentToNodeCoords(m_ptBuff);
	
					final double newX = m_ptBuff[0];
					final double newY = m_ptBuff[1];
					double deltaX = newX - oldX;
					double deltaY = newY - oldY;
	
					// If the shift key is down, then only move horizontally,
					// vertically, or diagonally, depending on the slope.
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
	
					// TODO: Optimize to not instantiate new array on every call.
					final long[] selectedNodes = m_view.getSelectedNodeIndices();
	
					final LongEnumerator anchorsToMove = m_view.m_selectedAnchors.searchRange(Integer.MIN_VALUE,
                            Integer.MAX_VALUE,
                            false);
					
					if ( anchorsToMove.numRemaining() < 1 ) { // If we are moving anchors of edges, no need to move nodes (bug #2360).
					    for (int i = 0; i < selectedNodes.length; i++) {
						    final NodeView dNodeView = m_view.getDNodeView(selectedNodes[i]);
						    final double oldXPos = dNodeView.getXPosition();
						    final double oldYPos = dNodeView.getYPosition();
						    dNodeView.setOffset(oldXPos + deltaX, oldYPos + deltaY);
					    }
					}
	
					while (anchorsToMove.numRemaining() > 0) {
						final long edgeAndAnchor = anchorsToMove.nextLong();
						final long edge = edgeAndAnchor >>> 6;
						final int anchorInx = (int)(edgeAndAnchor & 0x000000000000003f);
						final DEdgeView ev = (DEdgeView) m_view.getDEdgeView(edge);


						if( !ev.isValueLocked(BasicVisualLexicon.EDGE_BEND) )
						{
							Bend defaultBend = ev.getDefaultValue(BasicVisualLexicon.EDGE_BEND);
							if( ev.getVisualProperty(BasicVisualLexicon.EDGE_BEND) == defaultBend )
							{
								if( defaultBend instanceof BendImpl )
									ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl( (BendImpl)defaultBend) );
								else
									ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl());
							}
							else
							{
								ev.setLockedValue(BasicVisualLexicon.EDGE_BEND, new BendImpl( (BendImpl)ev.getBend()) );
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
						final Handle handle = bend.getAllHandles().get(anchorInx);
						final Point2D newPoint = handle.calculateHandleLocation(m_view.getViewModel(),ev);
						m_floatBuff1[0] = (float) newPoint.getX();
						m_floatBuff1[1] = (float) newPoint.getY();
						
						ev.moveHandleInternal(anchorInx, m_floatBuff1[0] + deltaX, m_floatBuff1[1] + deltaY);
					}
	
					if ((selectedNodes.length > 0) || (m_view.m_selectedAnchors.size() > 0))
						m_view.setContentChanged();
					if ((selectedNodes.length > 0) && (m_view.m_selectedAnchors.size() == 0)) {
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
		
				m_view.setViewportChanged();
				m_view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, m_xCenter);
				m_view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_yCenter);
				
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
		m_view = null;
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

	public void select(final Collection<? extends CyIdentifiable> nodesOrEdges, 
	                   final Class<? extends CyIdentifiable> type, final boolean selected) {
		if (nodesOrEdges.isEmpty())
			return;
		
		// Figure out if we're nodes or edges
		CyTable table = type.equals(CyNode.class) ? m_view.getModel().getDefaultNodeTable()
				: m_view.getModel().getDefaultEdgeTable();

		// Disable events
		m_view.cyEventHelper.silenceEventSource(table);

		// Create RowsSetEvent
		List<RowSetRecord> rowsChanged = new ArrayList<RowSetRecord>();
		
		for (final CyIdentifiable nodeOrEdge : nodesOrEdges) {
			CyRow row = m_view.getModel().getRow(nodeOrEdge);
			
			row.set(CyNetwork.SELECTED, selected);		
			// Add to paylod
			rowsChanged.add(new RowSetRecord(row, CyNetwork.SELECTED, selected, selected));
		}

		m_view.cyEventHelper.unsilenceEventSource(table);

		// Fire event
		RowsSetEvent event = new RowsSetEvent(table, rowsChanged);
		m_view.cyEventHelper.fireEvent(event);
	}
}
