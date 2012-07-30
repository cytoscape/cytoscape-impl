package org.cytoscape.ding.impl.cyannotator.listeners;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import org.cytoscape.model.CyNode;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;

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
		ShapeAnnotation resizeAnnotation = cyAnnotator.getResizeShape();
		Annotation moveAnnotation = cyAnnotator.getMovingAnnotation();
		ArrowAnnotation repositionAnnotation = cyAnnotator.getRepositioningArrow();
		if (resizeAnnotation == null && moveAnnotation == null && repositionAnnotation == null) {
			networkCanvas.mouseMoved(e);
			return;
		}

		int mouseX = e.getX();
		int mouseY = e.getY();

		if (moveAnnotation != null) {
			moveAnnotation.getComponent().setLocation(mouseX, mouseY);
			moveAnnotation.update();
			moveAnnotation.getCanvas().repaint();
		} else if (resizeAnnotation != null) {
			Component resizeComponent = resizeAnnotation.getComponent();
			int cornerX1 = resizeComponent.getX();
			int cornerY1 = resizeComponent.getY();
			int cornerX2 = cornerX1 + resizeComponent.getWidth();
			int cornerY2 = cornerY1 + resizeComponent.getHeight();

			int width, height;

			// Figure out which corner we're tracking
			if (Math.abs(mouseX-cornerX1) < Math.abs(mouseX-cornerX2)) {
				// Left
				cornerX1 = mouseX;
				width = cornerX2-cornerX1;
			} else {
				// Right
				width = mouseX-cornerX1;
			}

			if (Math.abs(mouseY-cornerY1) < Math.abs(mouseY-cornerY2)) {
				// Upper
				cornerY1 = mouseY;
				height = cornerY2-cornerY1;
			} else {
				// Lower
				height = mouseY-cornerY1;
			}

			if (width == 0) width = 2;
			if (height == 0) height = 2;

			resizeComponent.setLocation(cornerX1, cornerY1);
			resizeAnnotation.setSize((double)width, (double)height);
			resizeAnnotation.update();
			resizeAnnotation.getCanvas().repaint();
		} else if (repositionAnnotation != null) {
			Point2D mousePoint = new Point2D.Double(mouseX, mouseY);

			// See what's under our mouse
			// Annotation?
			Annotation a = cyAnnotator.getAnnotationAt(mousePoint);
			if (a != null && !(a instanceof ArrowAnnotation)) {
				repositionAnnotation.setTarget(a);

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
