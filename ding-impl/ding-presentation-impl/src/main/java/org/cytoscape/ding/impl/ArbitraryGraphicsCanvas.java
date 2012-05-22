/*
  File: ArbitraryGraphicsCanvas.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.impl.events.ViewportChangeListener;
import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


/**
 * This class extends cytoscape.view.CytoscapeCanvas.  Its meant
 * to live within a org.cytoscape.ding.impl.DGraphView class.  It is the canvas
 * used for arbitrary graphics drawing (background & foreground panes).
 */
public class ArbitraryGraphicsCanvas extends DingCanvas implements ViewportChangeListener {
	private final static long serialVersionUID = 1202416510975364L;

	/**
	 * Our reference to the GraphPerspective our view belongs to
	 */
	private CyNetwork m_cyNetwork;

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
	public ArbitraryGraphicsCanvas(CyNetwork cyNetwork, DGraphView dGraphView,
	                               InnerCanvas innerCanvas, Color backgroundColor,
	                               boolean isVisible, boolean isOpaque) {
		// init members
		m_cyNetwork = cyNetwork;
		m_dGraphView = dGraphView;
		m_innerCanvas = innerCanvas;
		m_backgroundColor = backgroundColor;
		m_isVisible = isVisible;
		m_isOpaque = isOpaque;
		m_componentToNodeMap = new HashMap<Component, CyNode>();
		m_componentToPointMap = new HashMap<Component, Point>();
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

	public void modifyComponentLocation(int x,int y, int componentNum){
		final Point nodePos = m_componentToPointMap.get(this.getComponent(componentNum));

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
			m_img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
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
		double xOffset = ((image.getWidth()/2)/scaleFactor - xCenter);
		double yOffset = ((image.getHeight()/2)/scaleFactor - yCenter);

		// get list of child components
		Component[] components = getComponents();

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
			if (c instanceof Annotation) {
				Annotation a = (Annotation)c;
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
		if (m_img != null) {
			// get image graphics
			final Graphics2D image2D = ((BufferedImage) m_img).createGraphics();

			// first clear the image
			clearImage(image2D);

			// now paint children
			if (m_isVisible){
				int num=this.getComponentCount();
				for(int i=0;i<num;i++){
					this.getComponent(i).paint(image2D);
				}
			}
			image2D.dispose();
			// render image
			graphics.drawImage(m_img, 0, 0, null);
		}
                
	}


	@Override
	public Component getComponentAt(int x, int y) {

		int n=getComponentCount();

		for(int i=0;i<n;i++){
				Component c=this.getComponent(i).getComponentAt(x, y);

				if(c!=null)
						return c;
		}
		return null;
	}


	/**
	 * Invoke this method to print the component.
	 *
	 * @param graphics Graphics
	 */
	public void print(Graphics graphics) {
		int num=this.getComponentCount();
		for(int i=0;i<num;i++){
			this.getComponent(i).print(graphics);
		}
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
		// set color alpha based on opacity setting
		int alpha = (m_isOpaque) ? 255 : 0;
		Color backgroundColor = new Color(m_backgroundColor.getRed(), m_backgroundColor.getGreen(),
		                                  m_backgroundColor.getBlue(), alpha);

		// set the alpha composite on the image, and clear its area
		Composite origComposite = image2D.getComposite();
		image2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		image2D.setPaint(backgroundColor);
		image2D.fillRect(0, 0, m_img.getWidth(null), m_img.getHeight(null));
		image2D.setComposite(origComposite);
	}

	private void contentChanged() {
		ContentChangeListener lis = m_dGraphView.m_cLis[0];

		if (lis != null)
			lis.contentChanged();
	}
}
