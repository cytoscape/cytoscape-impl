package org.cytoscape.ding.impl.cyannotator.listeners;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.ding.internal.util.ViewUtil.isControlOrMetaDown;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AnchorLocation;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.tasks.AnnotationEdit;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.property.values.Position;
import org.cytoscape.work.swing.DialogTaskManager;

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

public class CanvasMouseListener implements MouseListener {
	
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DRenderingEngine re;
	private Point2D mouseDown;

	private AnnotationEdit resizeUndoEdit;
	private AnnotationEdit movingUndoEdit;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CanvasMouseListener(CyAnnotator c, DRenderingEngine re, CyServiceRegistrar serviceRegistrar) {
		this.cyAnnotator = c;
		this.re = re;
		this.networkCanvas = re.getCanvas();
		this.serviceRegistrar = serviceRegistrar;
	}

	// TODO: create annotation-specific popup?
	@Override
	public void mousePressed(MouseEvent e) {
		mouseDown = null;
		resizeUndoEdit = null;
		movingUndoEdit = null;
		
		if (!re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			networkCanvas.processMouseEvent(e);
			return;
		}

		// Assuming we're not handling a special annotation-specific context menu
		if (e.getButton() != MouseEvent.BUTTON1 || (LookAndFeelUtil.isMac() && e.isControlDown() && !e.isMetaDown())) {
			networkCanvas.processMouseEvent(e);
			return;
		}

		DingAnnotation annotation = getAnnotation(e);
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
		
		if (!annotationSelection.isEmpty() && annotationSelection.overAnchor(e.getX(), e.getY()) != null) {
			AnchorLocation anchor = annotationSelection.overAnchor(e.getX(), e.getY());
			
			// save the distance between the anchor location and the mouse location
			double offsetX = e.getX() - annotationSelection.getX() - anchor.getX();
			double offsetY = e.getY() - annotationSelection.getY() - anchor.getY();
			
			setResizeCursor(anchor.getPosition());
			annotationSelection.setResizing(true);
			annotationSelection.saveAnchor(anchor.getPosition(), offsetX, offsetY);
			annotationSelection.saveBounds();
			
			for (DingAnnotation a: cyAnnotator.getAnnotationSelection()) 
				a.saveBounds();
			
			resizeUndoEdit = new AnnotationEdit("Resize Annotation", cyAnnotator, serviceRegistrar);
			
		} else if (annotation == null) {
			if (e.isShiftDown()) {
				// Remember where we did the mouse down. We may be doing a sweep select
				mouseDown = new Point2D.Double(e.getX(), e.getY());
			}
			// Let the InnerCanvas handle this event
			networkCanvas.processMouseEvent(e);
		} else {
			boolean selected = annotation.isSelected();
			if (selected && e.isShiftDown()) {
				annotation.setSelected(false);
			} else {
				if (!selected && !e.isPopupTrigger() && !e.isShiftDown() 
						&& !((e.isControlDown() || e.isMetaDown()) && !e.isAltDown()))
					cyAnnotator.clearSelectedAnnotations();
				
				annotation.setSelected(true);
			}

			if (!annotationSelection.isEmpty()) {
//				networkCanvas.changeCursor(networkCanvas.getMoveCursor());
				annotationSelection.setMoving(true);
				
				movingUndoEdit = new AnnotationEdit("Move Annotation", cyAnnotator, serviceRegistrar);
				
			} else {
				annotationSelection.setMoving(false);
			}

			//We request focus in this window, so that we can move these selected Annotations around using arrow keys
			// annotation.getCanvas().requestFocusInWindow();

			//Repaint the canvas
			annotation.getCanvas().repaint();	

			// OK, now for all of our selected annotations, remember this mousePressed
			for (DingAnnotation a: cyAnnotator.getAnnotationSelection()) {
				a.setOffset(e.getPoint());
			}

			// Let the network canvas know -- NOTE: this messes up double-click for some reason.
			networkCanvas.processMouseEvent(e);

			//We request focus in this window, so that we can move these selected Annotations around using arrow keys
			annotation.getCanvas().requestFocusInWindow();
		}
	}

	public void mouseReleased(MouseEvent e) {
//		networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
		boolean resizing = annotationSelection.isResizing();
		annotationSelection.setResizing(false);
		annotationSelection.setMoving(false);

		if(resizeUndoEdit != null) 
			resizeUndoEdit.post();
		if(movingUndoEdit != null)
			movingUndoEdit.post();
		
		if (mouseDown != null) {
			double startX = Math.min(mouseDown.getX(), e.getX());
			double startY = Math.min(mouseDown.getY(), e.getY());
			double endX = Math.max(mouseDown.getX(), e.getX());
			double endY = Math.max(mouseDown.getY(), e.getY());
			// Assume we did a sweep select
			Rectangle2D sweepArea = new Rectangle2D.Double(startX, startY, endX-startX, endY-startY);
			List<DingAnnotation> annotations = cyAnnotator.getAnnotationsIn(sweepArea);
			for (DingAnnotation a: annotations) {
				a.setSelected(true);
			}

			mouseDown = null;

			networkCanvas.processMouseEvent(e);
			return;
		}

		DingAnnotation annotation = getAnnotation(e);
		if (annotationSelection.isEmpty() || !re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			// Let the InnerCanvas handle this event
			networkCanvas.processMouseEvent(e);
		} else if (annotation != null) {
			// OK, now for all of our selected annotations, clear the mousePressed
			for (DingAnnotation a: annotationSelection) {
				a.setOffset(null);
			}
		} else if (!annotationSelection.isEmpty()) {
			// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
			if (!e.isPopupTrigger() && !e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()) && !resizing)
				cyAnnotator.clearSelectedAnnotations();
			
			networkCanvas.processMouseEvent(e);
		} else {
			networkCanvas.processMouseEvent(e);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// Check to see if we're resizing
		if (cyAnnotator.getResizeShape() != null) {
			cyAnnotator.getResizeShape().contentChanged();
			cyAnnotator.resizeShape(null);
			cyAnnotator.postUndoEdit(); // markUndoEdit() is in the dialogs like ShapeAnnotationDialog
			return;
		}

		if (cyAnnotator.getRepositioningArrow() != null) {
			cyAnnotator.getRepositioningArrow().contentChanged();
			cyAnnotator.positionArrow(null);
			cyAnnotator.postUndoEdit(); // markUndoEdit() is in ArrowAnnotationDialog
			return;
		}

		DingAnnotation annotation = getAnnotation(e);
		if (annotation == null) {
			if (re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
				// Ignore Ctrl if Alt is down so that Ctrl-Alt can be used for edge bends without side effects
				if (!e.isPopupTrigger() && !e.isShiftDown() && !(isControlOrMetaDown(e) && !e.isAltDown()))
					cyAnnotator.clearSelectedAnnotations();
			}

			// if (!e.isConsumed()){
				networkCanvas.processMouseEvent(e);
			//	e.consume();
			//}
		} else if (e.getClickCount()==1 && !e.isConsumed()) {
			// Do we want to pass this down?
			e.consume();
		} else if (e.getClickCount()==2 && !e.isConsumed()) {
			e.consume();
			invokeOnEDT(() -> {
				EditAnnotationTaskFactory tf = new EditAnnotationTaskFactory(serviceRegistrar.getService(DingRenderer.class));
				DialogTaskManager dtm = cyAnnotator.getRegistrar().getService(DialogTaskManager.class);
				dtm.execute(tf.createTaskIterator(re.getViewModel(), annotation, new Point(e.getX(), e.getY())));

			});
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}

	private DingAnnotation getAnnotation(MouseEvent e) {
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(new Point(e.getX(), e.getY()));
		if (annotation == null)
			return null;

		if (!re.getViewModelSnapshot().getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION))
			return null;

		return annotation;
	}

	public void setResizeCursor(Position anchor) {
//		switch(anchor) {
//			case NORTH_EAST:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
//				break;
//			case NORTH:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
//				break;
//			case NORTH_WEST:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
//				break;
//			case WEST:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
//				break;
//			case SOUTH_WEST:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
//				break;
//			case SOUTH:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
//				break;
//			case SOUTH_EAST:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
//				break;
//			case EAST:
//				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
//				break;
//		}
//		return;
	}
}
