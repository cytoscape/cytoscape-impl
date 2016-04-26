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


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.model.CyNode;


/**
 * This class extends cytoscape.view.CytoscapeCanvas.  Its meant
 * to live within a org.cytoscape.ding.impl.DGraphView class.  It is the canvas
 * used for arbitrary graphics drawing (background & foreground panes).
 */
public class ArbitraryGraphicsCanvas extends DingCanvas implements ViewportChangeListener {
	private final static long serialVersionUID = 1202416510975364L;

	/**
	 * Our reference to the DGraphView we live within
	 */
	private DGraphView m_dGraphView;

	/**
	 * Our reference to the inner canvas
	 */
	private InnerCanvas m_innerCanvas;

	/*
	 * Map of component(s) to hidden node(s)
	 */        
	private Map<Component, CyNode> m_componentToNodeMap;

	/*
	 * Map of component(s) to hidden Points on the canvas
	 */        
	private Map<Component, Point> m_componentToPointMap;

	/*
 	 * Flag to record that we're printing since we don't use the PrinterGraphics interface
 	 */
	private boolean isPrinting = false;
	
	/**
	 * Rendered image.
	 */
	private Image img;

	/**
	 * Constructor.
	 *
	 * @param cyNetwork GraphPerspective
	 * @param dGraphView DGraphView
	 * @param innerCanvas InnerCanvas
	 * @param backgroundColor Color
	 * @param isVisible boolean
	 * @param isOpaque boolean
	 */
	public ArbitraryGraphicsCanvas(DGraphView dGraphView,
	                               InnerCanvas innerCanvas,
	                               Color backgroundColor,
	                               boolean isOpaque) {
		m_dGraphView = dGraphView;
		m_innerCanvas = innerCanvas;
		m_backgroundColor = backgroundColor;
		m_isOpaque = isOpaque;
		m_componentToNodeMap = new HashMap<>();
		m_componentToPointMap = new HashMap<>();
	}

	/**
	 * Our implementation of add
	 */
        @Override
	public Component add(Component component) {
		// Make sure to position the component
		final double[] nodeCanvasCoordinates = new double[2];
		nodeCanvasCoordinates[0] = component.getX();
		nodeCanvasCoordinates[1] = component.getY();

		m_dGraphView.xformComponentToNodeCoords(nodeCanvasCoordinates);

		Point nodePos=new Point( (int)nodeCanvasCoordinates[0], (int)nodeCanvasCoordinates[1]);

		// add to map
		m_componentToPointMap.put(component, nodePos);

		// do our stuff
		Component c = super.add(component);

		contentChanged();

		return c;
	}

	/**
	 * Our implementation of remove
	 */
	@Override
	public void remove(Component component) {
		m_componentToPointMap.remove(component);

		super.remove(component);

		contentChanged();
	}
        
	/**
	 * Our implementation of ViewportChangeListener.
	 */
	public void viewportChanged(int viewportWidth, int viewportHeight, double newXCenter,
	                            double newYCenter, double newScaleFactor) {
		if (setBoundsChildren())
			repaint();
	}

	public void modifyComponentLocation(int x,int y, Component component){
		final Point nodePos = m_componentToPointMap.get(component);
		if (nodePos == null) return;

		final double[] nodeCanvasCoordinates = new double[2];
		nodeCanvasCoordinates[0] = x;
		nodeCanvasCoordinates[1] = y;

		m_dGraphView.xformComponentToNodeCoords(nodeCanvasCoordinates);

		nodePos.x=(int)nodeCanvasCoordinates[0];
		nodePos.y=(int)nodeCanvasCoordinates[1];

		contentChanged();
	}

	/**
	 * Our implementation of JComponent setBounds.
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
                
		// our bounds have changed, create a new image with new size
		if ((width > 1) && (height > 1)) {
			// create the buffered image
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			// update children's bounds
			setBoundsChildren();
		}
	}

	/**
	 * An implementation of getBounds that adjusts the bounds to
	 * include all of the child elements
	 */
	public boolean adjustBounds(double[] currentBounds) {
		final double[] nodeCanvasCoordinates = new double[2];

		// get list of child components
		Component[] components = getComponents();

		// no components, outta here
		if (components.length == 0)
			return false;

		// The currentBounds represents the current extents.  We're
		// going to walk our children and make sure the extents
		// are large enought to cover them

		// interate through the components
		for (Component c : components) {
			// get position of this component in network coordinates
			Point position = m_componentToPointMap.get(c);

			// Adjust, if necessary
			if (position.getX() < currentBounds[0]) 
				currentBounds[0] = position.getX();
			if (position.getY() < currentBounds[1]) 
				currentBounds[1] = position.getY();
			
			// Now, get the maximum extent of the component in component coordinates
			nodeCanvasCoordinates[0] = c.getX() + c.getWidth();
			nodeCanvasCoordinates[1] = c.getY() + c.getHeight();

			// Transform the maximum extent to get network cooredinates
			m_dGraphView.xformComponentToNodeCoords(nodeCanvasCoordinates);

			// Adjust, if necessary
			if (nodeCanvasCoordinates[0] > currentBounds[2])
				currentBounds[2] = nodeCanvasCoordinates[0];
			if (nodeCanvasCoordinates[1] > currentBounds[3])
				currentBounds[3] = nodeCanvasCoordinates[1];
		}

		return true;
	}


	public void drawCanvas(VolatileImage image, double xMin, double yMin, double xCenter, double yCenter, 
	                       double scaleFactor) {
		// get image graphics
		final Graphics2D image2D = image.createGraphics();
		// System.out.println("drawCanvas: new scaleFactor = "+scaleFactor+", xCenter = "+xCenter+", yCenter = "+yCenter);
		if (m_isOpaque)
			clearImage(image2D);

		double xOffset = ((image.getWidth()/2)/scaleFactor - xCenter);
		double yOffset = ((image.getHeight()/2)/scaleFactor - yCenter);

		// get list of child components
		Component[] components = getComponents();
		zSort(components); // Since we're doing this because we're doing the draw on our own

		// no components, outta here
		if (components.length == 0)
			return;

		// interate through the components
		for (Component c : components) {
			// get position of this component in network coordinates
			Point position = m_componentToPointMap.get(c);
			int xOrig = position.x;
			int yOrig = position.y;

			final double[] nodeCanvasCoordinates = new double[2];
			nodeCanvasCoordinates[0] = position.getX()+xOffset;
			nodeCanvasCoordinates[1] = position.getY()+yOffset;
			// System.out.println("   component is at node position: "+position.getX()+","+position.getY());

			// If we're painting an annotation, set the zoom
			if (c instanceof DingAnnotation) {
				DingAnnotation a = (DingAnnotation)c;
				a.drawAnnotation(image2D, position.getX()+xOffset, position.getY()+yOffset, scaleFactor);
			}
		}
		// System.out.println("drawCanvas: done");
	}

	/**
	 * Our implementation of paint.
	 * Invoked by Swing to draw components.
	 *
	 * @param graphics Graphics
	 */
	public void paint(Graphics graphics) {
		// only paint if we have an image to paint on
		if (img != null) {
			// get image graphics
			final Graphics2D image2D = ((BufferedImage) img).createGraphics();

			// first clear the image
			clearImage(image2D);

			// now paint children
			if (isVisible())
				this.paintChildren(image2D);
			
			image2D.dispose();
			// render image
			graphics.drawImage(img, 0, 0, null);
			
			// Make img publicly available *after* it has been rendered
			m_img = img;
		}
                
	}

	/**
	 * Invoke this method to print the component.
	 *
	 * @param graphics Graphics
	 */
	public void print(Graphics graphics) {
		isPrinting = true;
		// Only do this if we're opaque (i.e. the background canvas)
		if (m_isOpaque)
			clearImage((Graphics2D)graphics);
		this.printChildren(graphics);
		isPrinting = false;
	}

	/**
 	 * Return true if this view is curerntly being printed (as opposed to painted on the screen)
 	 * @return true if we're currently being printed, false otherwise
 	 */
	public boolean isPrinting() { 
		return isPrinting; 
	}

	private boolean setBoundsChildren() {
		// get list of child components
		Component[] components = getComponents();

		// no components, outta here
		if (components.length == 0)
			return false;

		// interate through the components
		for (Component c : components) {
			// get node
			Point node = m_componentToPointMap.get(c);

			// new image coordinates
			double[] currentNodeCoordinates = new double[2];
			currentNodeCoordinates[0] = node.getX();
			currentNodeCoordinates[1] = node.getY();
	
			AffineTransform transform = m_innerCanvas.getAffineTransform();
			transform.transform(currentNodeCoordinates, 0, currentNodeCoordinates, 0, 1);

			// set bounds
			c.setBounds((int) currentNodeCoordinates[0], (int) currentNodeCoordinates[1],
			            c.getWidth(), c.getHeight());
		}

		// outta here
		return true;
	}


	/**
	 * Utility function to clean the background of the image,
	 * using m_backgroundColor
	 *
	 * image2D Graphics2D
	 */
	private void clearImage(Graphics2D image2D) {
		if (img != null) {
			// set color alpha based on opacity setting
			int alpha = (m_isOpaque) ? 255 : 0;
			Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(),
			                                  m_backgroundColor.getBlue(), alpha);

			// set the alpha composite on the image, and clear its area
			Composite origComposite = image2D.getComposite();
			image2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			image2D.setPaint(backgroundColor);
			image2D.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
			image2D.setComposite(origComposite);
		}
	}

	private void contentChanged() {
		ContentChangeListener lis = m_dGraphView.m_cLis[0];

		if (lis != null)
			lis.contentChanged();
	}

	// Sort the components by z order
	private void zSort(Component[] components) {
		Arrays.sort(components, new ZComparator());
		return;
	}

	class ZComparator implements Comparator<Component> {
		public int compare(Component o1, Component o2) {
			if (getComponentZOrder(o1) > getComponentZOrder(o2))
				return -1;
			else if (getComponentZOrder(o1) < getComponentZOrder(o2))
				return 1;
			else
				return 0;
		}

		public boolean equals(Component o1, Component o2) {
			return (getComponentZOrder(o1) == getComponentZOrder(o2));
		}
	}

	public void dispose() {
		// Bug #1178: This class is being leaked by Swing's focus subsystem
		// In order to ensure no other instances get strung along, we should
		// release them here.
		m_dGraphView = null;
		m_innerCanvas = null;
		m_componentToNodeMap = null;
		m_componentToPointMap = null;
	}

}
