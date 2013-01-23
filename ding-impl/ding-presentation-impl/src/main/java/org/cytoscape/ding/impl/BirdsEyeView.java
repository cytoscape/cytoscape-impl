package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.awt.print.Printable;
import java.util.Properties;

import javax.swing.Icon;

import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * Swing component to display overview of the network.
 * 
 */
public final class BirdsEyeView extends Component implements RenderingEngine<CyNetwork> {
	
	private final static long serialVersionUID = 1202416511863994L;
	
	private static final Dimension MIN_SIZE = new Dimension(180, 180);
	
	private static final Color VIEW_WINDOW_COLOR =  new Color(10, 10, 255, 80);
	
	
	// Ratio of the graph image to panel size 
	private static final double SCALE_FACTOR = 0.97;

	private final double[] m_extents = new double[4];

	// This is the view model of this presentation (rendering engine).  Shared with main view.
	private final DGraphView viewModel;
	
	private final ContentChangeListener m_cLis;
	private final ViewportChangeListener m_vLis;
	private final CyServiceRegistrar registrar;

	private VolatileImage networkImage;
	
	private boolean imageUpdated;
	private boolean boundChanged;
	
	private double m_myXCenter;
	private double m_myYCenter;
	private double m_myScaleFactor;
	
	private int m_viewWidth;
	private int m_viewHeight;
	
	private double m_viewXCenter;
	private double m_viewYCenter;
	private double m_viewScaleFactor;
	
	private int imageWidth;
	private int imageHeight;

	/**
	 * Creates a new BirdsEyeView object.
	 * 
	 * @param viewModel
	 *            The view to monitor
	 */
	public BirdsEyeView(final DGraphView viewModel, final CyServiceRegistrar registrar) {
		super();

		if (viewModel == null)
			throw new NullPointerException("DGraphView is null.");

		this.viewModel = viewModel;
		this.registrar = registrar;

		m_cLis = new InnerContentChangeListener();
		m_vLis = new InnerViewportChangeListener();

		addMouseListener(new InnerMouseListener());
		addMouseMotionListener(new InnerMouseMotionListener());
		setPreferredSize(MIN_SIZE);
		setMinimumSize(MIN_SIZE);

		initializeView(viewModel);
	}

	
	private void initializeView(final GraphView view) {
		viewModel.addContentChangeListener(m_cLis);
		viewModel.addViewportChangeListener(m_vLis);
		
		updateBounds();
		final Point2D pt = viewModel.getCenter();
		m_viewXCenter = pt.getX();
		m_viewYCenter = pt.getY();
		
		m_viewScaleFactor = viewModel.getZoom();
		
		// Create default empty graphics object
		imageWidth = MIN_SIZE.width;
		imageHeight = MIN_SIZE.height;
		
		boundChanged = true;
		imageUpdated = true;		
	}

	private void updateBounds() {
		final Rectangle2D viewable = getViewableRect();
		m_viewWidth = (int) viewable.getWidth();
		m_viewHeight = (int) viewable.getHeight();

		final Rectangle2D viewableInView = getViewableRectInView(viewable);
		m_viewXCenter = viewableInView.getX() + viewableInView.getWidth() / 2.0;
		m_viewYCenter = viewableInView.getY() + viewableInView.getHeight() / 2.0;
	}
	
	private Rectangle2D getViewableRect() {
		final Rectangle r = viewModel.getComponent().getBounds();
		return new Rectangle2D.Double(r.x, r.y, r.width, r.height);
	}

	private Rectangle2D getViewableRectInView(final Rectangle2D viewable) {

		final double[] origin = new double[2];
		origin[0] = viewable.getX();
		origin[1] = viewable.getY();
		viewModel.xformComponentToNodeCoords(origin);

		final double[] destination = new double[2];
		destination[0] = viewable.getX() + viewable.getWidth();
		destination[1] = viewable.getY() + viewable.getHeight();
		viewModel.xformComponentToNodeCoords(destination);

		return new Rectangle2D.Double(origin[0], origin[1], destination[0] - origin[0], destination[1] - origin[1]);
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
	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		if(imageWidth != width || imageHeight != height) {
			imageWidth = width;
			imageHeight = height;
			imageUpdated = true;
		}
		boundChanged = true;
	}

	
	/**
	 * Render actual image on the panel.
	 */
	@Override public void update(Graphics g) {
		viewModel.m_networkCanvas.ensureInitialized();
		
		ArbitraryGraphicsCanvas foregroundCanvas = 
		    (ArbitraryGraphicsCanvas) viewModel.getCanvas(DGraphView.Canvas.FOREGROUND_CANVAS);
		ArbitraryGraphicsCanvas backgroundCanvas = 
		    (ArbitraryGraphicsCanvas) viewModel.getCanvas(DGraphView.Canvas.BACKGROUND_CANVAS);

		updateBounds();

		if (imageUpdated || boundChanged) {
			if (viewModel.getExtents(m_extents)) {				

				// Adjust for foreground/background components
				foregroundCanvas.adjustBounds(m_extents);
				backgroundCanvas.adjustBounds(m_extents);

				m_myXCenter = (m_extents[0] + m_extents[2]) / 2.0d;
				m_myYCenter = (m_extents[1] + m_extents[3]) / 2.0d;
				m_myScaleFactor = SCALE_FACTOR * 
				                  Math.min(((double) getWidth()) / (m_extents[2] - m_extents[0]), 
				                           ((double) getHeight()) / (m_extents[3] - m_extents[1]));
			} else {				
				m_myXCenter = 0.0d;
				m_myYCenter = 0.0d;
				m_myScaleFactor = 1.0d;
			}

			// System.out.println("BirdsEyeView: extent[0] = "+m_extents[0]+" extent[1] = "+m_extents[1]+" extent[2] = "+m_extents[2]+" extent[3] = "+m_extents[3]);
			// System.out.println("BirdsEyeView: component = "+getWidth()+"x"+getHeight());
			// System.out.println("BirdsEyeView: image = "+imageWidth+"x"+imageHeight);

			// Create "background" network image.
			if (imageUpdated) {
				// Need to create new image.  This is VERY expensive operation.
				final GraphicsConfiguration gc = getGraphicsConfiguration();
				networkImage = gc.createCompatibleVolatileImage(imageWidth, imageHeight, VolatileImage.OPAQUE);

				// Now draw the network
				viewModel.drawSnapshot(networkImage, viewModel.getGraphLOD(), viewModel.getBackgroundPaint(),
						m_extents[0], m_extents[1], m_myXCenter, m_myYCenter, m_myScaleFactor);
			}
		}

		// Render network graphics.
		g.drawImage(networkImage, 0, 0, null);

		// Compute view area
		final int rectWidth = (int) (m_myScaleFactor * (((double) m_viewWidth) / m_viewScaleFactor));
		final int rectHeight = (int) (m_myScaleFactor * (((double) m_viewHeight) / m_viewScaleFactor));
		
		final double rectXCenter = (((double) getWidth()) / 2.0d) + (m_myScaleFactor * (m_viewXCenter - m_myXCenter));
		final double rectYCenter = (((double) getHeight()) / 2.0d) + (m_myScaleFactor * (m_viewYCenter - m_myYCenter));
		
		final int x = (int) (rectXCenter - (rectWidth/2));
		final int y = (int) (rectYCenter - (rectHeight/2));
		
		// Draw the view area window		
		g.setColor(VIEW_WINDOW_COLOR);
		g.fillRect(x, y, rectWidth, rectHeight);
		// System.out.println("Viewable rectangle: "+x+", "+y+" "+rectWidth+"x"+rectHeight);
		
		boundChanged = false; imageUpdated = false;
	}

	
	@Override public void paint(Graphics g) {
		update(g);
	}

	private final class InnerContentChangeListener implements ContentChangeListener {
		/**
		 * Will be called when something is changed in the main view.
		 */
		public void contentChanged() {
			imageUpdated = true;
			repaint();
		}
	}

	private final class InnerViewportChangeListener implements ViewportChangeListener {
		
		/**
		 * Update the view if panned.
		 */
		public void viewportChanged(int w, int h, double newXCenter, double newYCenter, double newScaleFactor) {
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
		@Override
		public void mouseDragged(MouseEvent e) {
			if (m_currMouseButton == 1) {
				final int currX = e.getX();
				final int currY = e.getY();
				double deltaX = 0;
				double deltaY = 0;
				
				if (!viewModel.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)) {
					deltaX = (currX - m_lastXMousePos) / m_myScaleFactor;
					m_lastXMousePos = currX;
				}
				if (!viewModel.isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
					deltaY = (currY - m_lastYMousePos) / m_myScaleFactor;
					m_lastYMousePos = currY;
				}
				
				if (deltaX != 0 || deltaY != 0) {
					final Point2D pt = viewModel.getCenter();
					viewModel.setCenter(pt.getX() + deltaX, pt.getY() + deltaY);
					viewModel.updateView();
				}
			}
		}
	}

	@Override
	public Dimension getMinimumSize() {
		return MIN_SIZE;
	}

	@Override
	public View<CyNetwork> getViewModel() {
		return viewModel.getViewModel();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return viewModel.getVisualLexicon();
	}

	@Override
	public Properties getProperties() {
		return viewModel.getProperties();
	}
	
	@Override
	public Printable createPrintable() {
		return viewModel.createPrintable();
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return viewModel.createIcon(vp, value, width, height);
	}

	@Override
	public void printCanvas(Graphics printCanvas) {
		throw new UnsupportedOperationException("Printing is not supported for Bird's eye view.");
	}

	public void registerServices() {
		registrar.registerAllServices(this, new Properties());
	}
	
	@Override
	public void dispose() {
		registrar.unregisterAllServices(this);
	}
}
