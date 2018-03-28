package org.cytoscape.ding.impl.cyannotator.listeners;

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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import java.util.List;

import org.cytoscape.model.CyNode;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;

import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;

public class CanvasMouseMotionListener implements MouseMotionListener{
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;

	public CanvasMouseMotionListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	public void mouseDragged(MouseEvent e) {
		// TODO: handle dragging corners
		networkCanvas.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {
		AbstractAnnotation resizeAnnotation = cyAnnotator.getResizeShape();
		DingAnnotation moveAnnotation = cyAnnotator.getMovingAnnotation();
		ArrowAnnotationImpl repositionAnnotation = cyAnnotator.getRepositioningArrow();
		if (resizeAnnotation == null && moveAnnotation == null && repositionAnnotation == null) {
			networkCanvas.mouseMoved(e);
			return;
		}

		int mouseX = e.getX();
		int mouseY = e.getY();

		if (moveAnnotation != null) {
    	// Get our current transform
			double[] nextLocn = new double[2];
			nextLocn[0] = (double)mouseX;
			nextLocn[1] = (double)mouseY;
			view.xformComponentToNodeCoords(nextLocn);

			// OK, now update
			moveAnnotation.moveAnnotation(new Point2D.Double(nextLocn[0], nextLocn[1]));
			moveAnnotation.update();
			moveAnnotation.getCanvas().repaint();
		} else if (resizeAnnotation != null) {
			Component resizeComponent = resizeAnnotation.getComponent();

			int cornerX1 = resizeComponent.getX();
			int cornerY1 = resizeComponent.getY();
			// int cornerX2 = cornerX1 + resizeComponent.getWidth();
			// int cornerY2 = cornerY1 + resizeComponent.getHeight();
			int cornerX2 = mouseX;
			int cornerY2 = mouseY;
			double borderWidth = 0;
			if (resizeAnnotation instanceof ShapeAnnotationImpl)
				borderWidth = ((ShapeAnnotationImpl)resizeAnnotation).getBorderWidth();

			/*
			 * TODO: change over to use anchors at some point
			 */ 
			/*
			if (Math.abs(mouseX-cornerX1) < Math.abs(mouseX-cornerX2)) {
				// Left
				cornerX1 = mouseX;
			} else {
				// Right
				cornerX2 = mouseX;
			}
			*/

			// System.out.println("X1 = "+cornerX1+", X2 = "+cornerX2+" width = "+resizeComponent.getWidth());
			double width = (double)cornerX2-(double)cornerX1-(borderWidth*2*resizeAnnotation.getZoom());
			// System.out.println("width = "+width);

			/*
			if (mouseY <= cornerY1) {
				// Upper
				cornerY1 = mouseY;
			} else if (mouseY >= cornerY2-resizeComponent.getHeight()/2) {
				// Lower
				cornerY2 = mouseY;
			}
			*/

			double height = (double)cornerY2-(double)cornerY1-(borderWidth*2*resizeAnnotation.getZoom());
			// System.out.println("height = "+height);

			if (width == 0.0) width = 2;
			if (height == 0.0) height = 2;

			if ((Math.abs(width - resizeComponent.getWidth()) < 5) &&
			    (Math.abs(height - resizeComponent.getHeight()) < 5))
				return;

			Dimension d = new Dimension();
			d.setSize(width, height);

			// If shift is down, adjust to preserve the aspect ratio
			if (e.isShiftDown()) {
				d = resizeAnnotation.adjustAspectRatio(d);
			}

			// resizeComponent.setLocation(cornerX1, cornerY1);
			resizeAnnotation.setSize(d);
			resizeAnnotation.update();
			resizeAnnotation.getCanvas().repaint();
		} else if (repositionAnnotation != null) {
			Point2D mousePoint = new Point2D.Double(mouseX, mouseY);

			// See what's under our mouse
			// Annotation?
			List<DingAnnotation> annotations = cyAnnotator.getAnnotationsAt(mousePoint);
			if (annotations.contains(repositionAnnotation))
				annotations.remove(repositionAnnotation);

			if (annotations.size() > 0) {
				repositionAnnotation.setTarget(annotations.get(0));

			// Node?
			} else if (overNode(mousePoint)) {
				CyNode overNode = getNodeAtLocation(mousePoint);
				repositionAnnotation.setTarget(overNode);

			// Nope, just set the point
			} else {
				repositionAnnotation.setTarget(mousePoint);
			}

			repositionAnnotation.update();
			repositionAnnotation.getCanvas().repaint();
		}
	}

	private boolean overNode(Point2D mousePoint) {
		if (view.getPickedNodeView(mousePoint) != null)
			return true;
		return false;
	}

	private CyNode getNodeAtLocation(Point2D mousePoint) {
		DNodeView nv = (DNodeView)view.getPickedNodeView(mousePoint);
		return nv.getModel();
	}
}
