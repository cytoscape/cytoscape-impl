package org.cytoscape.ding.impl.cyannotator.listeners;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
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
		if (resizeAnnotation == null && moveAnnotation == null) {
			networkCanvas.mouseMoved(e);
			return;
		}

		int mouseX = e.getX();
		int mouseY = e.getY();

		if (moveAnnotation != null) {
			moveAnnotation.getComponent().setLocation(mouseX, mouseY);
			moveAnnotation.getCanvas().repaint();
			return;
		}

		Component resizeComponent = resizeAnnotation.getComponent();
		int cornerX1 = resizeComponent.getX();
		int cornerY1 = resizeComponent.getY();
		int cornerX2 = cornerX1 + resizeComponent.getWidth();
		int cornerY2 = cornerY1 + resizeComponent.getHeight();

		int width = mouseX-cornerX1;
		int height = mouseY-cornerY1;

		if (width <= 0 || height <= 0) {
			if (width <= 0) {
				cornerX1 = cornerX1+width;
				width = cornerX2-cornerX1;
			}
			if (height <= 0) {
				cornerY1 = cornerY1+height;
				height = cornerY2-cornerY1;
			}
			resizeComponent.setLocation(cornerX1, cornerY1);
		}

		if (width == 0) width = 2;
		if (height == 0) height = 2;
		resizeAnnotation.setSize((double)width, (double)height);
		resizeAnnotation.getCanvas().repaint();
	}
}
