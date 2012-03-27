/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.Properties;

import javax.swing.Icon;

import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;

/**
 * Swing component to display overview of the network.
 * 
 */
public class BirdsEyeView extends Component implements RenderingEngine<CyNetwork> {
	
	private final static long serialVersionUID = 1202416511863994L;
	
	private static final String ENGINE_ID = "ding-bev";
	
	private static final Dimension MIN_SIZE = new Dimension(180, 180);
	
	private static final Color VIEW_WINDOW_COLOR =  new Color(50, 50, 255, 50);
	private static final Color VIEW_WINDOW_BORDER_COLOR =  new Color(10,10,200,180);
	private static final Stroke VIEW_WINDOW_BORDER_STROKE =  new BasicStroke(2);
	
	// Ratio of the graph image to panel size 
	private static final double SCALE_FACTOR = 0.92;

	private final double[] m_extents = new double[4];

	private final DGraphView dgv;
	
	private final ContentChangeListener m_cLis;
	private final ViewportChangeListener m_vLis;
	
	private Image networkImage = null;
	
	private boolean m_contentChanged = false;
	private double m_myXCenter;
	private double m_myYCenter;
	private double m_myScaleFactor;
	
	private int m_viewWidth;
	private int m_viewHeight;
	
	private double m_viewXCenter;
	private double m_viewYCenter;
	private double m_viewScaleFactor;

	/**
	 * Creates a new BirdsEyeView object.
	 * 
	 * @param dgv
	 *            The view to monitor
	 * @param container
	 *            The desktop area holding the view. This should be
	 *            NetworkViewManager.getDesktopPane().
	 */
	public BirdsEyeView(final DGraphView dgv) {
		super();

		if (dgv == null)
			throw new NullPointerException("DGraphView is null.");

		this.dgv = dgv;

		m_cLis = new InnerContentChangeListener();
		m_vLis = new InnerViewportChangeListener();

		addMouseListener(new InnerMouseListener());
		addMouseMotionListener(new InnerMouseMotionListener());
		setPreferredSize(MIN_SIZE);
		setMinimumSize(MIN_SIZE);

		setView(dgv);
	}

	
	private void setView(final GraphView view) {

		dgv.addContentChangeListener(m_cLis);
		dgv.addViewportChangeListener(m_vLis);
		
		updateBounds();
		final Point2D pt = dgv.getCenter();
		m_viewXCenter = pt.getX();
		m_viewYCenter = pt.getY();
		m_viewScaleFactor = dgv.getZoom();
		m_contentChanged = true;

		repaint();
	}

	private void updateBounds() {
		final Rectangle2D viewable = getViewableRect();
		m_viewWidth = (int) viewable.getWidth();
		m_viewHeight = (int) viewable.getHeight();
		
		final Rectangle2D viewableInView = getViewableRectInView(viewable);
		m_viewXCenter = viewableInView.getX() + viewableInView.getWidth() / 2.0;
		m_viewYCenter = viewableInView.getY() + viewableInView.getHeight()
				/ 2.0;
	}
	
	private Rectangle2D getViewableRect() {
		final Rectangle r = dgv.getComponent().getBounds();
		return new Rectangle2D.Double(r.x, r.y, r.width, r.height);
	}

	private Rectangle2D getViewableRectInView(final Rectangle2D viewable) {

		final double[] origin = new double[2];
		origin[0] = viewable.getX();
		origin[1] = viewable.getY();
		dgv.xformComponentToNodeCoords(origin);

		final double[] destination = new double[2];
		destination[0] = viewable.getX() + viewable.getWidth();
		destination[1] = viewable.getY() + viewable.getHeight();
		dgv.xformComponentToNodeCoords(destination);

		return new Rectangle2D.Double(origin[0], origin[1],
				destination[0] - origin[0], destination[1] - origin[1]);
	}
	
	/**
	 * This used to be called reshape, which is deprecated, so I've changed it
	 * to setBounds. Not sure if this will break anything!
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	@Override public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0))
			networkImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);

		m_contentChanged = true;
	}

	
	/**
	 * Render actual image on the panel.
	 */
	@Override public void update(Graphics g) {
		if (networkImage == null)
			return;

		updateBounds();

		if (m_contentChanged) {
			if (dgv.getExtents(m_extents)) {				
				m_myXCenter = (m_extents[0] + m_extents[2]) / 2.0d;
				m_myYCenter = (m_extents[1] + m_extents[3]) / 2.0d;
				m_myScaleFactor = SCALE_FACTOR * Math.min(((double) getWidth())
						/ (m_extents[2] - m_extents[0]), ((double) getHeight())
						/ (m_extents[3] - m_extents[1]));
			} else {				
				m_myXCenter = 0.0d;
				m_myYCenter = 0.0d;
				m_myScaleFactor = 1.0d;
			}

			dgv.drawSnapshot(networkImage, dgv.getGraphLOD(),
					dgv.getBackgroundPaint(), m_myXCenter, m_myYCenter,
					m_myScaleFactor);
			m_contentChanged = false;
		}

		// Render network graphics.
		g.drawImage(networkImage, 0, 0, null);

		// Compute view area
		final double rectWidth = m_myScaleFactor * (((double) m_viewWidth) / m_viewScaleFactor);
		final double rectHeight = m_myScaleFactor * (((double) m_viewHeight) / m_viewScaleFactor);
		
		final double rectXCenter = (((double) getWidth()) / 2.0d) + (m_myScaleFactor * (m_viewXCenter - m_myXCenter));
		final double rectYCenter = (((double) getHeight()) / 2.0d) + (m_myScaleFactor * (m_viewYCenter - m_myYCenter));
		
		final double x = rectXCenter - (rectWidth/2);
		final double y = rectYCenter - (rectHeight/2);
		
		final Rectangle2D viewArea = new Rectangle2D.Double(x, y, rectWidth, rectHeight);

		// Draw the view area window		
		final Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(VIEW_WINDOW_BORDER_STROKE);
		g2.setColor(VIEW_WINDOW_COLOR);
		g2.fill(viewArea);
		g2.setColor(VIEW_WINDOW_BORDER_COLOR);
		g2.draw(viewArea);
	}

	
	@Override public void paint(Graphics g) {
		update(g);
	}

	private final class InnerContentChangeListener implements
			ContentChangeListener {
		public void contentChanged() {
			m_contentChanged = true;
			repaint();
		}
	}

	private final class InnerViewportChangeListener implements
			ViewportChangeListener {
		public void viewportChanged(int w, int h, double newXCenter,
				double newYCenter, double newScaleFactor) {
			m_viewWidth = w;
			m_viewHeight = h;
			m_viewXCenter = newXCenter;
			m_viewYCenter = newYCenter;
			m_viewScaleFactor = newScaleFactor;
			repaint();
		}
	}

	private int m_currMouseButton = 0;
	private int m_lastXMousePos = 0;
	private int m_lastYMousePos = 0;

	private final class InnerMouseListener extends MouseAdapter {

		@Override public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				m_currMouseButton = 1;
				m_lastXMousePos = e.getX();
				m_lastYMousePos = e.getY();
			}
		}

		@Override public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (m_currMouseButton == 1)
					m_currMouseButton = 0;
			}
		}
	}

	/**
	 * This class is for panning function.
	 *
	 */
	private final class InnerMouseMotionListener extends MouseMotionAdapter {
		@Override public void mouseDragged(MouseEvent e) {
						
			if (m_currMouseButton == 1) {
				final int currX = e.getX();
				final int currY = e.getY();
				final double deltaX = (currX - m_lastXMousePos)
						/ m_myScaleFactor;
				final double deltaY = (currY - m_lastYMousePos)
						/ m_myScaleFactor;
				m_lastXMousePos = currX;
				m_lastYMousePos = currY;

				final Point2D pt = dgv.getCenter();
				dgv.setCenter(pt.getX() + deltaX, pt.getY() + deltaY);
				dgv.updateView();
			}
		}
	}

	@Override public Dimension getMinimumSize() {
		return MIN_SIZE;
	}

	@Override
	public View<CyNetwork> getViewModel() {
		return dgv.getViewModel();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return dgv.getVisualLexicon();
	}

	@Override
	public Properties getProperties() {
		return dgv.getProperties();
	}

//	@Override
//	public void setProperties(String key, String value) {
//		dgv.setProperties(key, value);
//		
//	}
	
	@Override
	public Printable createPrintable() {
		return dgv.createPrintable();
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return dgv.createIcon(vp, value, width, height);
	}


	@Override
	public void printCanvas(Graphics printCanvas) {
		throw new UnsupportedOperationException("Printing is not supported for Bird's eye view.");
	}


	@Override
	public String getRenderingEngineID() {
		return ENGINE_ID;
	}
}