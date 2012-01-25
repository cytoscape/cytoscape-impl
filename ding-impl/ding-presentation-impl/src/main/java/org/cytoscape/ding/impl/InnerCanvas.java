/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
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
import java.util.List;

import org.cytoscape.ding.Bend;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphViewChangeListener;
import org.cytoscape.ding.Handle;
import org.cytoscape.ding.NodeView;
import org.cytoscape.ding.ViewChangeEdit;
import org.cytoscape.ding.impl.events.GraphViewEdgesSelectedEvent;
import org.cytoscape.ding.impl.events.GraphViewEdgesUnselectedEvent;
import org.cytoscape.ding.impl.events.GraphViewNodesSelectedEvent;
import org.cytoscape.ding.impl.events.GraphViewNodesUnselectedEvent;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.dnd.DropUtil;
import org.cytoscape.graph.render.export.ImageImposter;
import org.cytoscape.graph.render.immed.EdgeAnchors;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.graph.render.stateful.GraphRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.IntHash;
import org.cytoscape.util.intr.IntStack;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Canvas to be used for drawing actual network visualization
 */
public class InnerCanvas extends DingCanvas implements MouseListener, MouseMotionListener, DropTargetListener,
		KeyListener, MouseWheelListener {

	private final static long serialVersionUID = 1202416511420671L;

	// TODO This is public because BirdsEyeView needs to ensure that it isn't null
	// and that is ridiculous. 
	public GraphGraphics m_grafx;

	final double[] m_ptBuff = new double[2];
	final float[] m_extentsBuff2 = new float[4];
	final float[] m_floatBuff1 = new float[2];
	final float[] m_floatBuff2 = new float[2];
	final Line2D.Float m_line = new Line2D.Float();
	final GeneralPath m_path = new GeneralPath();
	final GeneralPath m_path2 = new GeneralPath();
	final IntStack m_stack = new IntStack();
	final IntStack m_stack2 = new IntStack();
	final Object m_lock;
	DGraphView m_view;
	final GraphLOD[] m_lod = new GraphLOD[1];
	final IntHash m_hash;
	double m_xCenter;
	double m_yCenter;
	double m_scaleFactor;
	private int m_lastRenderDetail = 0;
	private Rectangle m_selectionRect = null;
	private ViewChangeEdit m_undoable_edit;
	private boolean isPrinting = false;
	private PopupMenuHelper popup;

	FontMetrics m_fontMetrics = null;
	
	private boolean NodeMovement = true;

	//  for turning selection rectangle on and off
	private boolean selecting = true;

	private UndoSupport m_undo;

	private int m_currMouseButton = 0;
	private int m_lastXMousePos = 0;
	private int m_lastYMousePos = 0;
	private boolean m_button1NodeDrag = false;

	private final MousePressedDelegator mousePressedDelegator;
	private final MouseReleasedDelegator mouseReleasedDelegator;
	private final MouseDraggedDelegator mouseDraggedDelegator;
	private final AddEdgeMousePressedDelegator addEdgeMousePressedDelegator;

	private final AddEdgeStateMonitor addEdgeMode;

	InnerCanvas(Object lock, DGraphView view, UndoSupport undo) {
		super();
		m_lock = lock;
		m_view = view;
		m_undo = undo;
		m_lod[0] = new GraphLOD(); // Default LOD.
		m_hash = new IntHash();
		m_backgroundColor = Color.white;
		m_isVisible = true;
		m_isOpaque = false;
		m_xCenter = 0.0d;
		m_yCenter = 0.0d;
		m_scaleFactor = 1.0d;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setFocusable(true);
		new DropTarget(this, DnDConstants.ACTION_COPY, this); 
		popup = new PopupMenuHelper(m_view, this);

		mousePressedDelegator = new MousePressedDelegator();
		mouseReleasedDelegator = new MouseReleasedDelegator();
		mouseDraggedDelegator = new MouseDraggedDelegator();
		addEdgeMousePressedDelegator = new AddEdgeMousePressedDelegator();

		addEdgeMode = new AddEdgeStateMonitor(this,m_view);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0)) {
			final Image img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			GraphGraphics grafx = new GraphGraphics(img, false);

			synchronized (m_lock) {
				m_img = img;
				m_grafx = grafx;
				m_view.m_viewportChanged = true;
			}
		}
	}


	@Override
	public void update(Graphics g) {
		if (m_grafx == null)
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
			if (m_view.m_contentChanged || m_view.m_viewportChanged) {
				renderGraph(m_grafx,/* setLastRenderDetail = */ true, m_lod[0]);
				contentChanged = m_view.m_contentChanged;
				m_view.m_contentChanged = false;
				viewportChanged = m_view.m_viewportChanged;
				xCenter = m_xCenter;
				yCenter = m_yCenter;
				scaleFactor = m_scaleFactor;
				m_view.m_viewportChanged = false;
			}
		}

		// if canvas is visible, draw it (could be made invisible via DingCanvas api)
		if (m_isVisible) {
			g.drawImage(m_img, 0, 0, null);
		}

		if ((m_selectionRect != null) && (this.isSelecting())) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.red);
			g2.draw(m_selectionRect);
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
		renderGraph(new GraphGraphics(
				new ImageImposter(g, getWidth(), getHeight()), false), 
				/* setLastRenderDetail = */ false, m_view.m_printLOD);
		isPrinting = false;
	}


	@Override
	public void printNoImposter(Graphics g) {
		isPrinting = true;
		final Image img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		renderGraph(new GraphGraphics(img, false), /* setLastRenderDetail = */ false, m_view.m_printLOD);
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
	 * @param e
	 *            the MouseEvent to process
	 */
	@Override
	public void processMouseEvent(MouseEvent e) {
		super.processMouseEvent(e);
	}


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		adjustZoom(e.getWheelRotation());
	}
	
	
	public void mouseDragged(MouseEvent e) {
		mouseDraggedDelegator.delegateMouseDragEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		if (addEdgeMode.addingEdge())
			addEdgeMode.drawRubberBand(e);
		else {
			final String tooltipText = getToolTipText(e.getPoint());
			setToolTipText(tooltipText);
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


	private int getChosenNode() {
		m_ptBuff[0] = m_lastXMousePos;
		m_ptBuff[1] = m_lastYMousePos;
		m_view.xformComponentToNodeCoords(m_ptBuff);
		m_stack.empty();
		m_view.getNodesIntersectingRectangle((float) m_ptBuff[0], (float) m_ptBuff[1],
		                                     (float) m_ptBuff[0], (float) m_ptBuff[1],
		                                     (m_lastRenderDetail
		                                     & GraphRenderer.LOD_HIGH_DETAIL) == 0,
		                                     m_stack);
		int chosenNode = (m_stack.size() > 0) ? m_stack.peek() : -1;
		return chosenNode;
	}

	private int getChosenAnchor() {
		m_ptBuff[0] = m_lastXMousePos;
		m_ptBuff[1] = m_lastYMousePos;
		m_view.xformComponentToNodeCoords(m_ptBuff);

		final IntEnumerator hits = m_view.m_spacialA.queryOverlap((float) m_ptBuff[0],
		                                                          (float) m_ptBuff[1],
		                                                          (float) m_ptBuff[0],
		                                                          (float) m_ptBuff[1],
		                                                          null, 0, false);
		int chosenAnchor = (hits.numRemaining() > 0) ? hits.nextInt() : (-1);
		return chosenAnchor;
	}
	
	private int getChosenEdge() {
		computeEdgesIntersecting(m_lastXMousePos - 1, m_lastYMousePos - 1,
                m_lastXMousePos + 1, m_lastYMousePos + 1, m_stack2);
        int chosenEdge = (m_stack2.size() > 0) ? m_stack2.peek() : -1;
        return chosenEdge;
	}
	
	/**
	 * @return an array of indices of unselected nodes
	 */
	private int[] getUnselectedNodes() {
		int [] unselectedNodes;
		if (m_view.m_nodeSelection) { // Unselect all selected nodes.
			unselectedNodes = m_view.getSelectedNodeIndices();

			// Adding this line to speed things up from O(n*log(n)) to O(n).
			m_view.m_selectedNodes.empty();

			for (int i = 0; i < unselectedNodes.length; i++)
				((DNodeView) m_view.getDNodeView(unselectedNodes[i])).unselectInternal();
		} else
			unselectedNodes = new int[0];
		return unselectedNodes;

	}
	
	private int[] getUnselectedEdges() {
		int[] unselectedEdges;
		if (m_view.m_edgeSelection) { // Unselect all selected edges.
			unselectedEdges = m_view.getSelectedEdgeIndices();

			// Adding this line to speed things up from O(n*log(n)) to O(n).
			m_view.m_selectedEdges.empty();

			for (int i = 0; i < unselectedEdges.length; i++)
				((DEdgeView) m_view.getDEdgeView(unselectedEdges[i])).unselectInternal();
		} else
			unselectedEdges = new int[0];
		return unselectedEdges;
	}
	
	private int toggleSelectedNode(int chosenNode, MouseEvent e) {
		int chosenNodeSelected = 0;
		final boolean wasSelected = m_view.getDNodeView(chosenNode).isSelected();

		if (wasSelected && e.isShiftDown()) {
			((DNodeView) m_view.getDNodeView(chosenNode)).unselectInternal();
			chosenNodeSelected = -1;
		} else if (!wasSelected) {
			((DNodeView) m_view.getDNodeView(chosenNode)).selectInternal();
			chosenNodeSelected = 1;
		}

		m_button1NodeDrag = true;
		m_view.m_contentChanged = true;	
		return chosenNodeSelected;
	}
	
	
	private void toggleChosenAnchor (int chosenAnchor, MouseEvent e) {
		if (e.isControlDown()) {
			final int edge = chosenAnchor >>> 6;
			final int anchorInx = chosenAnchor & 0x0000003f;
			// Save remove handle
			m_undoable_edit = new ViewChangeEdit(m_view,ViewChangeEdit.SavedObjs.SELECTED_EDGES,"Remove Edge Handle",m_undo);
			m_view.getDEdgeView(edge).removeHandle(anchorInx);
			m_button1NodeDrag = false;
		} else {
			final boolean wasSelected = m_view.m_selectedAnchors.count(chosenAnchor) > 0;

			if (wasSelected && e.isShiftDown())
				m_view.m_selectedAnchors.delete(chosenAnchor);
			else if (!wasSelected) {
				if (!e.isShiftDown())
					m_view.m_selectedAnchors.empty();

				m_view.m_selectedAnchors.insert(chosenAnchor);
			}

			m_button1NodeDrag = true;
		}

		m_view.m_contentChanged = true;	
	}
	
	private int toggleSelectedEdge(int chosenEdge, MouseEvent e) {
		int chosenEdgeSelected = 0;
		
		// Set Edge Bend.
		if (e.isControlDown() && ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
			
			m_view.m_selectedAnchors.empty();
			m_ptBuff[0] = m_lastXMousePos;
			m_ptBuff[1] = m_lastYMousePos;
			m_view.xformComponentToNodeCoords(m_ptBuff);
			// Store current handle list
			m_undoable_edit = new ViewChangeEdit(m_view, ViewChangeEdit.SavedObjs.SELECTED_EDGES, "Add Edge Handle", m_undo);
			final int chosenInx = m_view.getDEdgeView(chosenEdge).addHandlePoint(new Point2D.Float(
					(float) m_ptBuff[0], (float) m_ptBuff[1]));
			
			m_view.m_selectedAnchors.insert(((chosenEdge) << 6) | chosenInx);
		}

		final boolean wasSelected = m_view.getDEdgeView(chosenEdge).isSelected();

		if (wasSelected && e.isShiftDown()) {
			((DEdgeView) m_view.getDEdgeView(chosenEdge)).unselectInternal();
			chosenEdgeSelected = -1;
		} else if (!wasSelected) {
			((DEdgeView) m_view.getDEdgeView(chosenEdge)).selectInternal(false);
			chosenEdgeSelected = 1;

			if ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0) {
				m_ptBuff[0] = m_lastXMousePos;
				m_ptBuff[1] = m_lastYMousePos;
				m_view.xformComponentToNodeCoords(m_ptBuff);

				final IntEnumerator hits = m_view.m_spacialA.queryOverlap((float) m_ptBuff[0], (float) m_ptBuff[1],
						(float) m_ptBuff[0], (float) m_ptBuff[1], null, 0, false);

				if (hits.numRemaining() > 0) {
					final int hit = hits.nextInt();

					if (m_view.m_selectedAnchors.count(hit) == 0)
						m_view.m_selectedAnchors.insert(hit);
				}
			}
		}

		m_button1NodeDrag = true;
		m_view.m_contentChanged = true;
		return chosenEdgeSelected;
	}
	

	private int[] setSelectedNodes() {
		int [] selectedNodes = null;
		
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

		final IntEnumerator nodesXSect = m_stack.elements();

		while (nodesXSect.numRemaining() > 0) {
			final int nodeXSect = nodesXSect.nextInt();

			if (m_view.m_selectedNodes.count(nodeXSect) == 0)
				m_stack2.push(nodeXSect);
		}

		selectedNodes = new int[m_stack2.size()];

		final IntEnumerator nodes = m_stack2.elements();

		for (int i = 0; i < selectedNodes.length; i++)
			selectedNodes[i] = nodes.nextInt();

		for (int i = 0; i < selectedNodes.length; i++)
			((DNodeView) m_view.getDNodeView(selectedNodes[i])) .selectInternal();

		if (selectedNodes.length > 0)
			m_view.m_contentChanged = true;	
		return selectedNodes;
	}
	
	
	private int [] setSelectedEdges() {
		int [] selectedEdges = null;
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
			final IntEnumerator hits = m_view.m_spacialA.queryOverlap((float) xMin,
			                                                          (float) yMin,
			                                                          (float) xMax,
			                                                          (float) yMax,
			                                                          null,
			                                                          0,
			                                                          false);

			if (hits.numRemaining() > 0)
				m_view.m_contentChanged = true;

			while (hits.numRemaining() > 0) {
				final int hit = hits.nextInt();

				if (m_view.m_selectedAnchors.count(hit) == 0)
					m_view.m_selectedAnchors.insert(hit);
			}
		}

		computeEdgesIntersecting(m_selectionRect.x, m_selectionRect.y,
		                         m_selectionRect.x + m_selectionRect.width,
		                         m_selectionRect.y + m_selectionRect.height,
		                         m_stack2);
		m_stack.empty();

		final IntEnumerator edgesXSect = m_stack2.elements();

		while (edgesXSect.numRemaining() > 0) {
			final int edgeXSect = edgesXSect.nextInt();

			if (m_view.m_selectedEdges.count(edgeXSect) == 0)
				m_stack.push(edgeXSect);
		}

		selectedEdges = new int[m_stack.size()];

		final IntEnumerator edges = m_stack.elements();

		for (int i = 0; i < selectedEdges.length; i++)
			selectedEdges[i] = edges.nextInt();

		for (int i = 0; i < selectedEdges.length; i++)
			((DEdgeView) m_view.getDEdgeView(selectedEdges[i])).selectInternal(true);

		if (selectedEdges.length > 0)
			m_view.m_contentChanged = true;
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
				EdgeView ev = m_view.getPickedEdgeView(p);
				if (ev != null) 
					return ((DEdgeView) ev).getToolTip();
		}

		return null;
	}

	// Puts [last drawn] edges intersecting onto stack; as RootGraph indices.
	// Depends on the state of several member variables, such as m_hash.
	// Clobbers m_stack and m_ptBuff.
	// The rectangle extents are in component coordinate space.
	// IMPORTANT: Code that calls this method should be holding m_lock.
	final void computeEdgesIntersecting(final int xMini, final int yMini, final int xMaxi,
	                                    final int yMaxi, final IntStack stack) {
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
		IntEnumerator edgeNodesEnum = m_hash.elements(); // Positive.
		m_stack.empty();

		final int edgeNodesCount = edgeNodesEnum.numRemaining();

		for (int i = 0; i < edgeNodesCount; i++)
			m_stack.push(edgeNodesEnum.nextInt());

		m_hash.empty();
		edgeNodesEnum = m_stack.elements();
		stack.empty();

		final CyNetwork graph = m_view.m_drawPersp;

		if ((m_lastRenderDetail & GraphRenderer.LOD_HIGH_DETAIL) == 0) {
			// We won't need to look up arrows and their sizes.
			for (int i = 0; i < edgeNodesCount; i++) {
				final int node = edgeNodesEnum.nextInt(); // Positive.
				final CyNode nodeObj = graph.getNode(node);

				if (!m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0))
					continue; /* Will happen if e.g. node was removed. */

				final float nodeX = (m_view.m_extentsBuff[0] + m_view.m_extentsBuff[2]) / 2;
				final float nodeY = (m_view.m_extentsBuff[1] + m_view.m_extentsBuff[3]) / 2;
				final Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(nodeObj, CyEdge.Type.ANY);

				for ( CyEdge e : touchingEdges ) {      
					final int edge = e.getIndex(); // Positive.
					final int otherNode =  // Positive.
						node ^ e.getSource().getIndex() ^ e.getTarget().getIndex(); 

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
				final int node = edgeNodesEnum.nextInt(); // Positive.
				final CyNode nodeObj = graph.getNode(node);

				if (!m_view.m_spacial.exists(node, m_view.m_extentsBuff, 0))
					continue; /* Will happen if e.g. node was removed. */

				final byte nodeShape = m_view.m_nodeDetails.shape(node);
				final Iterable<CyEdge> touchingEdges = graph.getAdjacentEdgeIterable(nodeObj, CyEdge.Type.ANY);
 
				for ( CyEdge e : touchingEdges ) {      
					final int edge = e.getIndex(); // Positive.
					final double segThicknessDiv2 = m_view.m_edgeDetails.segmentThickness(edge) / 2.0d;
					final int otherNode = node ^ e.getSource().getIndex() ^ e.getTarget().getIndex();

					if (m_hash.get(otherNode) < 0) {
						m_view.m_spacial.exists(otherNode, m_extentsBuff2, 0);

						final byte otherNodeShape = m_view.m_nodeDetails.shape(otherNode);
						final byte srcShape;
						final byte trgShape;
						final float[] srcExtents;
						final float[] trgExtents;

						if (node == e.getSource().getIndex()) {
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
							srcArrow = m_view.m_edgeDetails.sourceArrow(edge);
							trgArrow = m_view.m_edgeDetails.targetArrow(edge);
							srcArrowSize = ((srcArrow == GraphGraphics.ARROW_NONE) 
							                ? 0.0f
							                : m_view.m_edgeDetails.sourceArrowSize(edge));
							trgArrowSize = ((trgArrow == GraphGraphics.ARROW_NONE) 
							                ? 0.0f
							                : m_view.m_edgeDetails.targetArrowSize(edge));
						}

						final EdgeAnchors anchors = (((m_lastRenderDetail
						                              & GraphRenderer.LOD_EDGE_ANCHORS) == 0)
						                             ? null : m_view.m_edgeDetails.anchors(edge));

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
							stack.push(edge);
					}
				}

				m_hash.put(node);
			}
		}
	}

	/**
	 * default dragEnter handler.  Accepts the drag.
	 * @param dte the DropTargetDragEvent
	 *
	 */
	public void dragEnter(DropTargetDragEvent dte) {
		dte.acceptDrag(DnDConstants.ACTION_COPY);
	}

	/**
	 * default dragExit handler.  Does nothing, can be overridden.
	 * @param dte the DropTargetDragEvent
	 *
	 */
	public void dragExit(DropTargetEvent dte) { }

	/**
	 * default dropActionChanged handler.  Does nothing, can be overridden.
	 * @param dte the DropTargetDragEvent
	 *
	 */
	public void dropActionChanged(DropTargetDragEvent dte) { }

	/**
	 * default dragOver handler.  Does nothing, can be overridden.
	 * @param dte the DropTargetDragEvent
	 *
	 */
	public void dragOver(DropTargetDragEvent dte) { }

	/**
	 * default drop handler.  
	 */
	public void drop(DropTargetDropEvent dte) {
		requestFocusInWindow();
		dte.acceptDrop(DnDConstants.ACTION_COPY);

		Transferable t = dte.getTransferable();
		String action = getPreferredDropAction(t);

		Point rawPt = dte.getLocation();
		double[] loc = new double[2];
		loc[0] = rawPt.getX();
		loc[1] = rawPt.getY();
		m_view.xformComponentToNodeCoords(loc);
		Point xformPt = new Point();
		xformPt.setLocation(loc[0],loc[1]); 

		NodeView nview = m_view.getPickedNodeView(rawPt);
		if ( nview != null ) 
			popup.createDropNodeViewMenu(m_view.m_drawPersp,nview,rawPt,xformPt,t,action);
		else
			popup.createDropEmptySpaceMenu(rawPt,xformPt,t,action); 

		dte.dropComplete(true);
	}

	private String getPreferredDropAction(Transferable t) {
		String[] actions = DropUtil.getTransferableDataStrings(t);
		if (actions.length > 0)
			return actions[0];
		else
			return null;
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

		m_view.m_viewportChanged = true;
		
		// Update view model.
		m_view.setVisualProperty(MinimalVisualLexicon.NETWORK_SCALE_FACTOR, m_scaleFactor);
		repaint();
	}


	public int getLastRenderDetail() {
		return m_lastRenderDetail;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param s DOCUMENT ME!
	 */
	public void setSelecting(boolean s) {
		selecting = s;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
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

	/**
	 *  @param setLastRenderDetail if true, "m_lastRenderDetail" will be updated, otherwise it will not be updated.
	 */
	private void renderGraph(GraphGraphics graphics, final boolean setLastRenderDetail, final GraphLOD lod) {
		// Set color alpha based on opacity setting
		final int alpha = (m_isOpaque) ? 255 : 0;

		final Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(),
							m_backgroundColor.getBlue(), alpha);

		synchronized (m_lock) {
			final int lastRenderDetail = GraphRenderer.renderGraph(m_view.m_drawPersp,
									       m_view.m_spacial, lod,
									       m_view.m_nodeDetails,
									       m_view.m_edgeDetails, m_hash,
									       graphics, backgroundColor, m_xCenter,
									       m_yCenter, m_scaleFactor);
			if (setLastRenderDetail)
				m_lastRenderDetail = lastRenderDetail;
		}
	}

	private void handleEscapeKey() {
		AddEdgeStateMonitor.reset(m_view);
		repaint();
	}

	private void handleArrowKeys(KeyEvent k) {
		final int code = k.getKeyCode();
		double move = 1.0;

		if (k.isShiftDown())
			move = 10.0;

		if (m_view.m_nodeSelection) {
			// move nodes
			int[] selectedNodes = m_view.getSelectedNodeIndices();

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
			IntEnumerator anchorsToMove = m_view.m_selectedAnchors.searchRange(Integer.MIN_VALUE,
			                                                                   Integer.MAX_VALUE,
			                                                                   false);

			while (anchorsToMove.numRemaining() > 0) {
				final int edgeAndAnchor = anchorsToMove.nextInt();
				final int edge = edgeAndAnchor >>> 6;
				final int anchorInx = edgeAndAnchor & 0x0000003f;
				final DEdgeView ev = (DEdgeView) m_view.getDEdgeView(edge);
				//ev.getHandleInternal(anchorInx, m_floatBuff1);
				
				final Bend bend = ev.getBend();
				final Handle handle = bend.getAllHandles().get(anchorInx);
				final Point2D newPoint = handle.calculateHandleLocation(ev);
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
			String action = "Edge";
			NodeView nview = m_view.getPickedNodeView(rawPt);
			if ( nview != null ) 
				popup.createDropNodeViewMenu(m_view.m_drawPersp,nview,rawPt,xformPt,null,action);
		}
	}

	private final class MousePressedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			m_undoable_edit = null;
		
			m_currMouseButton = 1;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
		
			int[] unselectedNodes = null;
			int[] unselectedEdges = null;
			int chosenNode = -1;
			int chosenEdge = -1;
			int chosenAnchor = -1;
			int chosenNodeSelected = 0;
			int chosenEdgeSelected = 0;
	
			synchronized (m_lock) {
				if (m_view.m_nodeSelection) {
					chosenNode = getChosenNode();
				}
	
				if (m_view.m_edgeSelection && (chosenNode < 0)
				    && ((m_lastRenderDetail & GraphRenderer.LOD_EDGE_ANCHORS) != 0)) {
					chosenAnchor = getChosenAnchor();
				}
	
				if (m_view.m_edgeSelection && (chosenNode < 0) && (chosenAnchor < 0)) {
					chosenEdge = getChosenEdge();
				}
	
				if ((!e.isShiftDown()) // If shift is down never unselect.
				    && (((chosenNode < 0) && (chosenEdge < 0) && (chosenAnchor < 0)) // Mouse missed all.
				       // Not [we hit something but it was already selected].
				       || !( ((chosenNode >= 0) && m_view.getDNodeView(chosenNode).isSelected())
				             || (chosenAnchor >= 0) 
				             || ((chosenEdge >= 0) && m_view.getDEdgeView(chosenEdge).isSelected()) ))) {
				
						unselectedNodes = getUnselectedNodes();
						unselectedEdges = getUnselectedEdges();
	
					if ((unselectedNodes.length > 0) || (unselectedEdges.length > 0))
						m_view.m_contentChanged = true;
				}
				
	
				if (chosenNode >= 0) {
				    chosenNodeSelected = toggleSelectedNode(chosenNode, e);
				}
	
				if (chosenAnchor >= 0) {
					toggleChosenAnchor(chosenAnchor, e);
				}
	
				if (chosenEdge >= 0)
					chosenEdgeSelected = toggleSelectedEdge(chosenEdge, e);
	
				if ((chosenNode < 0) && (chosenEdge < 0) && (chosenAnchor < 0)) {
					m_selectionRect = new Rectangle(m_lastXMousePos, m_lastYMousePos, 0, 0);
					m_button1NodeDrag = false;
				}
			}
	
			final GraphViewChangeListener listener = m_view.m_lis[0];
	
			// delegating to listeners
			if (listener != null) {
				if ((unselectedNodes != null) && (unselectedNodes.length > 0))
					listener.graphViewChanged(new GraphViewNodesUnselectedEvent(m_view,
   	                                       DGraphView.makeNodeList(unselectedNodes,m_view)));
	
				if ((unselectedEdges != null) && (unselectedEdges.length > 0))
					listener.graphViewChanged(new GraphViewEdgesUnselectedEvent(m_view,
   	                                       DGraphView.makeEdgeList(unselectedEdges,m_view)));
	
				if (chosenNode >= 0) {
					if (chosenNodeSelected > 0)
						listener.graphViewChanged(new GraphViewNodesSelectedEvent(m_view,
							DGraphView.makeList(((DNodeView)m_view.getDNodeView(chosenNode)).getModel())));
					else if (chosenNodeSelected < 0)
						listener.graphViewChanged(new GraphViewNodesUnselectedEvent(m_view,
							DGraphView.makeList(((DNodeView)m_view.getDNodeView(chosenNode)).getModel())));
				}
	
				if (chosenEdge >= 0) {
					if (chosenEdgeSelected > 0)
						listener.graphViewChanged(new GraphViewEdgesSelectedEvent(m_view,
							DGraphView.makeList(m_view.getDEdgeView(chosenEdge).getEdge())));
					else if (chosenEdgeSelected < 0)
						listener.graphViewChanged(new GraphViewEdgesUnselectedEvent(m_view,
							DGraphView.makeList(m_view.getDEdgeView(chosenEdge).getEdge())));
				}
			}
	
			// Repaint after listener events are fired because listeners may change
			// something in the graph view.
			repaint();
		}
	
		@Override
		void singleLeftControlClick(MouseEvent e) {
			//System.out.println("MousePressed ----> singleLeftControlClick");
			
			/* clicking on empty space
			if ((getChosenNode() < 0) && (getChosenEdge() < 0) && (getChosenAnchor() < 0)) {
				popup.createEmptySpaceMenu(e.getX(), e.getY(),"NEW"); 
			}
			*/
			
			// Cascade
			this.singleLeftClick(e);
		}
	
		@Override
		void singleMiddleClick(MouseEvent e) {
			//System.out.println("MousePressed ----> singleMiddleClick");
			// Save all node positions
			m_undoable_edit = new ViewChangeEdit(m_view,ViewChangeEdit.SavedObjs.NODES,"Move",m_undo);
			m_currMouseButton = 2;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
		}
	
	
		@Override
		void singleRightClick(MouseEvent e) {
			//System.out.println("MousePressed ----> singleRightClick");
			// Save all node positions
			m_undoable_edit = new ViewChangeEdit(m_view,ViewChangeEdit.SavedObjs.NODES,"Move",m_undo);
			m_currMouseButton = 3;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
		
			NodeView nview = m_view.getPickedNodeView(e.getPoint());
			if (nview != null) {
				popup.createNodeViewMenu(m_view.m_drawPersp,nview,e.getX(),e.getY(),null);
			} else {
				EdgeView edgeView = m_view.getPickedEdgeView(e.getPoint());
				if (edgeView != null) {
					popup.createEdgeViewMenu(m_view.m_drawPersp,edgeView,e.getX(),e.getY(),null);
				} else {
					// Clicked on empty space...
					popup.createEmptySpaceMenu(e.getX(), e.getY(),"NEW"); 
				}
			}
		}
	
		@Override
		void doubleLeftClick(MouseEvent e) {
			//System.out.println("MousePressed ----> doubleLeftClick");
			NodeView nview = m_view.getPickedNodeView(e.getPoint());
			if ( nview != null )
				popup.createNodeViewMenu(m_view.m_drawPersp,nview,e.getX(),e.getY(),"OPEN");
			else 
				popup.createEmptySpaceMenu(e.getX(), e.getY(),"OPEN"); 
		}
	}

	private final class MouseReleasedDelegator extends ButtonDelegator {

		@Override
		void singleLeftClick(MouseEvent e) {
			//System.out.println("1. MouseReleased ----> singleLeftClick");
			
			if (m_currMouseButton == 1) {
				m_currMouseButton = 0;
	
				if (m_selectionRect != null) {
					int[] selectedNodes = null;
					int[] selectedEdges = null;
	
					synchronized (m_lock) {
						if (m_view.m_nodeSelection || m_view.m_edgeSelection) {
							if (m_view.m_nodeSelection)
								selectedNodes = setSelectedNodes();	
							if (m_view.m_edgeSelection)
								selectedEdges = setSelectedEdges ();
						}
					}
					
					m_selectionRect = null;
	
					final GraphViewChangeListener listener = m_view.m_lis[0];
	
					if (listener != null) {
						if ((selectedNodes != null) && (selectedNodes.length > 0))
							listener.graphViewChanged(new GraphViewNodesSelectedEvent(m_view,
					                                DGraphView.makeNodeList(selectedNodes,m_view)));
	
						if ((selectedEdges != null) && (selectedEdges.length > 0))
							listener.graphViewChanged(new GraphViewEdgesSelectedEvent(m_view,
					                               DGraphView.makeEdgeList(selectedEdges,m_view)));
					}
					
					// Repaint after listener events are fired because listeners may
					// change something in the graph view.
					repaint();
				}
			}
			
	
			if (m_undoable_edit != null)
				m_undoable_edit.post();

			final List<CyNode> selected = m_view.getSelectedNodes();			
			// Update view model if necessary (node location)
			if(selected.size() != 0) {
				for (CyNode node : selected) {
					final DNodeView dNodeView = (DNodeView) m_view.getDNodeView(node);
					// Update visual property value (x/y)
					dNodeView.setVisualProperty(MinimalVisualLexicon.NODE_X_LOCATION, dNodeView.getXPosition());
					dNodeView.setVisualProperty(MinimalVisualLexicon.NODE_Y_LOCATION, dNodeView.getYPosition());					
				}
			}
		}
	
		@Override
		void singleMiddleClick(MouseEvent e) {
			//System.out.println("MouseReleased ----> singleMiddleClick");
			if (m_currMouseButton == 2)
				m_currMouseButton = 0;
	
			if (m_undoable_edit != null)
				m_undoable_edit.post();
		}
	
		@Override
		void singleRightClick(MouseEvent e) {
			//System.out.println("MouseReleased ----> singleRightClick");
			if (m_currMouseButton == 3)
				m_currMouseButton = 0;
	
			if (m_undoable_edit != null)
				m_undoable_edit.post();
		}
	}

	private final class MouseDraggedDelegator extends ButtonDelegator {

		// emulate right click and middle click with a left clic and a modifier
		// TODO: make sure to be consistent with other emulation inside cytoscape and on Mac OSX
		static final int FAKEMIDDLECLIC = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.META_DOWN_MASK;
		static final int FAKERIGHTCLIC  = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK;
		
		@Override
		void singleLeftClick(MouseEvent e) {
			//System.out.println("MouseDragged ----> singleLeftClick");
			if (m_button1NodeDrag) {
				// save selected node and edge positions
				if (m_undoable_edit == null) {
					m_undoable_edit = new ViewChangeEdit(m_view, ViewChangeEdit.SavedObjs.SELECTED, "Move",m_undo);
				}
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
					final int[] selectedNodes = m_view.getSelectedNodeIndices();
	
					for (int i = 0; i < selectedNodes.length; i++) {
						final NodeView dNodeView = m_view.getDNodeView(selectedNodes[i]);
						final double oldXPos = dNodeView.getXPosition();
						final double oldYPos = dNodeView.getYPosition();
						dNodeView.setOffset(oldXPos + deltaX, oldYPos + deltaY);
					}
	
					final IntEnumerator anchorsToMove = m_view.m_selectedAnchors.searchRange(Integer.MIN_VALUE,
					                                                                         Integer.MAX_VALUE,
					                                                                         false);
	
					while (anchorsToMove.numRemaining() > 0) {
						final int edgeAndAnchor = anchorsToMove.nextInt();
						final int edge = edgeAndAnchor >>> 6;
						final int anchorInx = edgeAndAnchor & 0x0000003f;
						final DEdgeView ev = (DEdgeView) m_view.getDEdgeView(edge);
						
						//ev.getHandleInternal(anchorInx, m_floatBuff1);
						
						final Bend bend = ev.getBend();
						final Handle handle = bend.getAllHandles().get(anchorInx);
						final Point2D newPoint = handle.calculateHandleLocation(ev);
						m_floatBuff1[0] = (float) newPoint.getX();
						m_floatBuff1[1] = (float) newPoint.getY();
						
						ev.moveHandleInternal(anchorInx, m_floatBuff1[0] + deltaX, m_floatBuff1[1] + deltaY);
					}
	
					if ((selectedNodes.length > 0) || (m_view.m_selectedAnchors.size() > 0))
						m_view.m_contentChanged = true;
				}
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

		public void delegateMouseDragEvent(MouseEvent e) {
			// Note: the fake clic are here for OSX but I do not see a reason to disable them on other systems
			switch (e.getModifiersEx()) {
			case MouseEvent.BUTTON1_DOWN_MASK:
				singleLeftClick(e);
				break;
			case MouseEvent.BUTTON2_DOWN_MASK:
			case FAKEMIDDLECLIC:
				singleMiddleClick(e);
				break;
			case MouseEvent.BUTTON3_DOWN_MASK:
			case FAKERIGHTCLIC:
				singleRightClick(e);
				break;
			}
		}

		@Override
		void singleMiddleClick(MouseEvent e) {
			double deltaX = e.getX() - m_lastXMousePos;
			double deltaY = e.getY() - m_lastYMousePos;
			m_lastXMousePos = e.getX();
			m_lastYMousePos = e.getY();
	
			synchronized (m_lock) {
				m_xCenter -= (deltaX / m_scaleFactor);
				m_yCenter -= (deltaY / m_scaleFactor);
			}
	
			m_view.m_viewportChanged = true;
			m_view.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_X_LOCATION, m_xCenter);
			m_view.setVisualProperty(MinimalVisualLexicon.NETWORK_CENTER_Y_LOCATION, m_yCenter);
			
			repaint();
		}

		@Override
		void singleRightClick(MouseEvent e) {
			//System.out.println("MouseDragged ----> singleRightClick");
			double deltaY = e.getY() - m_lastYMousePos;
	
			synchronized (m_lock) {
				m_lastXMousePos = e.getX();
				m_lastYMousePos = e.getY();
				m_scaleFactor *= Math.pow(2, -deltaY / 300.0d);
			}
	
			m_view.m_viewportChanged = true;
			repaint();
		}
	}

	public double getScaleFactor(){
		return m_scaleFactor;
	}
}
