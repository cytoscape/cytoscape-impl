package org.cytoscape.ding.impl.cyannotator.listeners;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AbstractAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.property.values.Position;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class CanvasMouseMotionListener implements MouseMotionListener {
	
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;

	public CanvasMouseMotionListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
		DingAnnotation a = cyAnnotator.getAnnotationAt(new Point(e.getX(), e.getY()));
		
		if (annotationSelection.isEmpty() || !view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			networkCanvas.mouseDragged(e);
			return;
		}

		if (annotationSelection.isResizing()) {
			// Resize
			annotationSelection.resizeAnnotationsRelative(e.getX(), e.getY());
			// For resize, we *don't* want to pass things to the network canvas
		} else if (a != null && !annotationSelection.isMoving()) {
			// cyAnnotator.moveAnnotation(a);
			// annotationSelection.moveSelection(e.getX(), e.getY());
			// If we're moving, we might have nodes or edges selected and will want to move them also
			// if (!view.getSelectedNodes().isEmpty() || !view.getSelectedEdges().isEmpty())
				networkCanvas.mouseDragged(e);
		} else if (a != null) {
			annotationSelection.moveSelection(e.getX(), e.getY());
			// If we're moving, we might have nodes or edges selected and will want to move them also
			if (!view.getSelectedNodes().isEmpty() || !view.getSelectedEdges().isEmpty())
				networkCanvas.mouseDragged(e);
		} else if (annotationSelection.isMoving()) {
			annotationSelection.moveSelection(e.getX(), e.getY());
			// If we're moving, we might have nodes or edges selected and will want to move them also
			if (!view.getSelectedNodes().isEmpty() || !view.getSelectedEdges().isEmpty()) {
				networkCanvas.mouseDragged(e);
			}
		} else {
			networkCanvas.mouseDragged(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		AbstractAnnotation resizeAnnotation = cyAnnotator.getResizeShape();
		// DingAnnotation moveAnnotation = cyAnnotator.getMovingAnnotation();
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
		ArrowAnnotationImpl repositionAnnotation = cyAnnotator.getRepositioningArrow();
		
		if (resizeAnnotation == null && annotationSelection.isEmpty() && repositionAnnotation == null) {
			networkCanvas.mouseMoved(e);
			return;
		}

		int mouseX = e.getX();
		int mouseY = e.getY();

		if (resizeAnnotation != null) {
			Rectangle2D initialBounds = cyAnnotator.getResizeBounds();
			Rectangle2D bounds = AnnotationSelection.resize(Position.SOUTH_EAST, initialBounds, mouseX, mouseY);
			
			resizeAnnotation.setBounds((int)bounds.getX(), (int)bounds.getY(), (int)bounds.getWidth(), (int)bounds.getHeight());
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
