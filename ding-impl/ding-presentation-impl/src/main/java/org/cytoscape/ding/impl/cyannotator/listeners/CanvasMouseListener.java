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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import java.util.Set;

import javax.swing.JComponent;

import org.cytoscape.view.presentation.property.values.Position;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;


public class CanvasMouseListener implements MouseListener {
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;
	private JComponent anchorComponent;

	public CanvasMouseListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	// TODO: create annotation-specific popup?
	public void mousePressed(MouseEvent e) {
		if (!view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			networkCanvas.processMouseEvent(e);
			return;
		}

		// Assuming we're not handling a special annotation-specific context menu
		if (e.getButton() != MouseEvent.BUTTON1 ||
        (LookAndFeelUtil.isMac() && e.isControlDown() && !e.isMetaDown()))
		{
			networkCanvas.processMouseEvent(e);
			return;
		}

		DingAnnotation annotation = getAnnotation(e);
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
		if (annotationSelection.count() > 0 &&
			  annotationSelection.overAnchor(e.getX(), e.getY()) != null) {
			Position anchor = annotationSelection.overAnchor(e.getX(), e.getY());
			setResizeCursor(anchor);
			annotationSelection.setResizing(true);
			annotationSelection.initialPosition(e.getX(), e.getY(), anchor);
			annotationSelection.saveBounds();
			for (DingAnnotation a: cyAnnotator.getAnnotationSelection()) 
				a.saveBounds();
		} else if (annotation == null) {
				// Let the InnerCanvas handle this event
				networkCanvas.processMouseEvent(e);
		} else {
			boolean selected = annotation.isSelected();
			if (selected && e.isShiftDown()) {
				annotation.setSelected(false);
			} else {
				if (!selected && !e.isShiftDown())
					cyAnnotator.clearSelectedAnnotations();
				annotation.setSelected(true);
			}

			if (annotationSelection.count() > 0)
				networkCanvas.changeCursor(networkCanvas.getMoveCursor());

			//We request focus in this window, so that we can move these selected Annotations around using arrow keys
			annotation.getCanvas().requestFocusInWindow();

			//Repaint the canvas
			annotation.getCanvas().repaint();	

			// OK, now for all of our selected annotations, remember this mousePressed
			for (DingAnnotation a: cyAnnotator.getAnnotationSelection()) {
				a.setOffset(e.getPoint());
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();
		annotationSelection.setResizing(false);

		DingAnnotation annotation = getAnnotation(e);
		if (annotationSelection.count() == 0 ||
				!view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			// Let the InnerCanvas handle this event
			networkCanvas.processMouseEvent(e);
		} else if (annotation != null) {
			// OK, now for all of our selected annotations, clear the mousePressed
			for (DingAnnotation a: annotationSelection) {
				a.setOffset(null);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		// Check to see if we're resizing
		if (cyAnnotator.getResizeShape() != null) {
			cyAnnotator.getResizeShape().contentChanged();
			cyAnnotator.resizeShape(null);
			return;
		}

		if (cyAnnotator.getRepositioningArrow() != null) {
			cyAnnotator.getRepositioningArrow().contentChanged();
			cyAnnotator.positionArrow(null);
			return;
		}

		if (cyAnnotator.getMovingAnnotation() != null) {
			cyAnnotator.getMovingAnnotation().contentChanged();
			cyAnnotator.moveAnnotation(null);
			return;
		}

		DingAnnotation annotation = getAnnotation(e);
		if (annotation == null) {
			if (view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
				cyAnnotator.clearSelectedAnnotations();
				e.consume();
			}

			if (!e.isConsumed()) {
				networkCanvas.processMouseEvent(e);
				e.consume();
			}
		} else if (e.getClickCount()==1 && !e.isConsumed()) {
			e.consume();
		} else if (e.getClickCount()==2 && !e.isConsumed()) {
			e.consume();
			invokeOnEDT(() -> {
				EditAnnotationTaskFactory tf = new EditAnnotationTaskFactory();
				DialogTaskManager dtm = cyAnnotator.getRegistrar().getService(DialogTaskManager.class);
				dtm.execute(tf.createTaskIterator(view, annotation, new Point(e.getX(), e.getY())));

			});
		}
	}

	public void mouseEntered(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}

	private DingAnnotation getAnnotation(MouseEvent e) {
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(new Point(e.getX(), e.getY()));
		if (annotation == null)
			return null;

		if (!view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION))
			return null;

		return annotation;

	}

	public void setResizeCursor(Position anchor) {
		switch(anchor) {
			case NORTH_EAST:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
				break;
			case NORTH:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				break;
			case NORTH_WEST:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
				break;
			case WEST:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
				break;
			case SOUTH_WEST:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
				break;
			case SOUTH:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				break;
			case SOUTH_EAST:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
				break;
			case EAST:
				networkCanvas.changeCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				break;
		}
		return;
	}
}
