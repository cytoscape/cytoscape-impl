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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;
import java.awt.print.Printable;
import java.util.Objects;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.ding.impl.DRenderingEngine.Canvas;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing component to display overview of the network.
 * 
 */
@SuppressWarnings("serial")
public final class BirdsEyeView extends Component implements RenderingEngine<CyNetwork> {
	
	private static final Dimension MIN_SIZE = new Dimension(180, 180);
	
	private final Color VIEW_WINDOW_COLOR;
	private final Color VIEW_WINDOW_BORDER_COLOR;
	
	// Ratio of the graph image to panel size 
	private static final double SCALE_FACTOR = 0.97;

	private final double[] m_extents = new double[4];

	// This is the view model of this presentation (rendering engine).  Shared with main view.
	private final DRenderingEngine re;
	
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
	private Timer redrawTimer;
	private UpdateImage redrawTask;
	private	BirdsEyeViewLOD bevLOD;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	/**
	 * Creates a new BirdsEyeView object.
	 */
	public BirdsEyeView(DRenderingEngine re, CyServiceRegistrar registrar) {
		this.re = Objects.requireNonNull(re);
		this.registrar = Objects.requireNonNull(registrar);

		m_cLis = new BirdsEyeViewInnerContentChangeListener();
		m_vLis = new BirdsEyeViewInnerViewportChangeListener();
		
		Color c = UIManager.getColor("Table.focusCellBackground");
		VIEW_WINDOW_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
		c = UIManager.getColor("Table.background");
		VIEW_WINDOW_BORDER_COLOR = new Color(c.getRed(), c.getGreen(), c.getBlue(), 90);

		addMouseListener(new InnerMouseListener());
		addMouseWheelListener(new InnerMouseWheelListener());
		addMouseMotionListener(new InnerMouseMotionListener());
		setPreferredSize(MIN_SIZE);
		setMinimumSize(MIN_SIZE);

		initializeView(re);

		// Create thread pool
		redrawTimer = new Timer("Bird's Eye View Timer");
	}
	
	private void initializeView(final DRenderingEngine re) {
		re.addContentChangeListener(m_cLis);
		re.addViewportChangeListener(m_vLis);
		
		updateBounds();
		final Point2D pt = re.getCenter();
		m_viewXCenter = pt.getX();
		m_viewYCenter = pt.getY();
		
		m_viewScaleFactor = re.getZoom();
		
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
		final Rectangle r = re.getBounds();
		return new Rectangle2D.Double(r.x, r.y, r.width, r.height);
	}

	private Rectangle2D getViewableRectInView(final Rectangle2D viewable) {

		final double[] origin = new double[2];
		origin[0] = viewable.getX();
		origin[1] = viewable.getY();
		re.xformComponentToNodeCoords(origin);

		final double[] destination = new double[2];
		destination[0] = viewable.getX() + viewable.getWidth();
		destination[1] = viewable.getY() + viewable.getHeight();
		re.xformComponentToNodeCoords(destination);

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
	@Override 
	public void update(Graphics g) {
		re.getCanvas().ensureInitialized();

		AnnotationCanvas foregroundCanvas = (AnnotationCanvas) re.getCanvas(Canvas.FOREGROUND_CANVAS);
		AnnotationCanvas backgroundCanvas = (AnnotationCanvas) re.getCanvas(Canvas.BACKGROUND_CANVAS);

		updateBounds();

		if (imageUpdated || boundChanged) {
			
			boolean hasComponents = re.getExtents(m_extents);
			hasComponents |= foregroundCanvas.adjustBounds(m_extents);
			hasComponents |= backgroundCanvas.adjustBounds(m_extents);
			
			if(hasComponents) {
				m_myXCenter = (m_extents[0] + m_extents[2]) / 2.0d;
				m_myYCenter = (m_extents[1] + m_extents[3]) / 2.0d;
				m_myScaleFactor = SCALE_FACTOR * 
				                  Math.min(((double) getWidth())  / (m_extents[2] - m_extents[0]), 
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
				// This should work, but because of all of the m_locks everywhere, it actually is slower
				// TODO: at some point, we need to implement a different model of drawSnapshot that doesn't
				// use the RTree, which isn't needed since we redraw everything anyways
				if (re.isLargeModel()) {
					// Run as a thread
					if (redrawTask != null) {
						redrawTask.cancel();
					}
					redrawTask = new UpdateImage(m_extents, m_myXCenter, m_myYCenter, m_myScaleFactor);
					// System.out.println("Executing redraw");
					redrawTimer.schedule(redrawTask, 10);
					// System.out.println("redrawTask execution done");
				} else {
					// Need to create new image.  This is VERY expensive operation.
					final GraphicsConfiguration gc = getGraphicsConfiguration();
					networkImage = gc.createCompatibleVolatileImage(imageWidth, imageHeight, VolatileImage.OPAQUE);

					// Now draw the network
					re.drawSnapshot(networkImage, re.getGraphLOD(), re.getBackgroundPaint(),
							m_extents[0], m_extents[1], m_myXCenter, m_myYCenter, m_myScaleFactor);
				}
			}
		}

		if (networkImage != null) {
			// Render network graphics.
			// System.out.println("Rendering network image");
			g.drawImage(networkImage, 0, 0, null);
			// System.out.println("Rendering network image - done");
		}

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
		g.setColor(VIEW_WINDOW_BORDER_COLOR);
		g.drawRect(x, y, rectWidth, rectHeight);
		
		boundChanged = false; 
		imageUpdated = false;
	}


	@Override
	public void paint(Graphics g) {
		update(g);
	}

	private final class BirdsEyeViewInnerContentChangeListener implements ContentChangeListener {
		/**
		 * Will be called when something is changed in the main view.
		 */
		@Override
		public void contentChanged() {
			imageUpdated = true;
			repaint();
		}
	}

	private final class BirdsEyeViewInnerViewportChangeListener implements ViewportChangeListener {
		
		/**
		 * Update the view if panned.
		 */
		@Override
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
				m_lastXMousePos = e.getX(); // needed by drag listener
				m_lastYMousePos = e.getY();
				
				double halfWidth  = (double)getWidth() / 2.0d;
				double halfHeight = (double)getHeight() / 2.0d;
				
				double centerX = ((m_lastXMousePos - halfWidth)  / m_myScaleFactor) + m_myXCenter;
				double centerY = ((m_lastYMousePos - halfHeight) / m_myScaleFactor) + m_myYCenter;
				
				re.setCenter(centerX, centerY);
				re.updateView();
			}
		}

		@Override public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (m_currMouseButton == 1)
					m_currMouseButton = 0;
			}
		}
	}
	
	
	private final class InnerMouseWheelListener implements MouseWheelListener {
		@Override 
		public void mouseWheelMoved(MouseWheelEvent e) {
			re.getInputHandlerGlassPane().processMouseWheelEvent(e);
		}
	}
	

	/**
	 * This class is for panning function.
	 *
	 */
	private final class InnerMouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			// MKTODO this results in very jittery dragging
			if (m_currMouseButton == 1) {
				final int currX = e.getX();
				final int currY = e.getY();
				double deltaX = 0;
				double deltaY = 0;
				
				if (!re.getViewModel().isValueLocked(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)) {
					deltaX = (currX - m_lastXMousePos) / m_myScaleFactor;
					m_lastXMousePos = currX;
				}
				if (!re.getViewModel().isValueLocked(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)) {
					deltaY = (currY - m_lastYMousePos) / m_myScaleFactor;
					m_lastYMousePos = currY;
				}
				
				if (deltaX != 0 || deltaY != 0) {
					re.pan(deltaX, deltaY);
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
		return re.getViewModel();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return re.getVisualLexicon();
	}

	@Override
	public Properties getProperties() {
		return re.getProperties();
	}
	
	@Override
	public Printable createPrintable() {
		return re.createPrintable();
	}

	@Override
	public <V> Icon createIcon(VisualProperty<V> vp, V value, int width, int height) {
		return re.createIcon(vp, value, width, height);
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
	
	@Override
	public String getRendererId() {
		return DingRenderer.ID;
	}

	// XXX: Use javax.swing.Timer instead?  This would then be an ActionEvent.
	class UpdateImage extends TimerTask {
		private final double[] extents;
		private final double xCenter;
		private final double yCenter;
		private final double scale;

		public UpdateImage(double[] extents, double xCenter, double yCenter, double scale) {
			this.extents = extents;
			this.xCenter = xCenter;
			this.yCenter = yCenter;
			this.scale = scale;
		}

		@Override
		public void run() {
			try {
				final GraphicsConfiguration gc = getGraphicsConfiguration();
				VolatileImage networkImage2 = gc.createCompatibleVolatileImage(imageWidth, imageHeight, VolatileImage.OPAQUE);

				// Now draw the network
				// System.out.println("Drawing snapshot");
				if (re.getGraphLOD() instanceof DingGraphLOD)
					bevLOD = new BirdsEyeViewLOD(new DingGraphLOD((DingGraphLOD)re.getGraphLOD()));
				else if (bevLOD == null)
					bevLOD = new BirdsEyeViewLOD(re.getGraphLOD());

				bevLOD.setDrawEdges(true);
				
				re.drawSnapshot(networkImage2, bevLOD, re.getBackgroundPaint(), extents[0], extents[1], xCenter, yCenter, scale);
				networkImage = networkImage2;
			} catch (Exception e) {
				logger.error("UpdateImage Error", e);
			}
			
			imageUpdated = false;
			boundChanged = false;

			// We need to do a repaint so that we get the selection area
			SwingUtilities.invokeLater(() -> repaint());
		}
	}
}
