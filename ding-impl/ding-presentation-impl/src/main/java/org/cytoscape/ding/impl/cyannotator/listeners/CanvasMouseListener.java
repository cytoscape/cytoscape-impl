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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;

public class CanvasMouseListener implements MouseListener {
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;

	public CanvasMouseListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	public void mousePressed(MouseEvent e) {
		// Let the InnerCanvas handle this event
		networkCanvas.processMouseEvent(e);
	}

	public void mouseReleased(MouseEvent e) {
		// Let the InnerCanvas handle this event
		networkCanvas.processMouseEvent(e);
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

		DingAnnotation annotation = cyAnnotator.getAnnotationAt(new Point(e.getX(), e.getY()));
		if (annotation == null) {
			// cyAnnotator.clearSelectedAnnotations();
			if (!e.isConsumed()) {
				networkCanvas.processMouseEvent(e);
				e.consume();
			}
			return;
		}

/*
 * It seems to be a little confusing to have double-click
 * selection on annotations.
 */
/*
		if(e.getClickCount()==2 && !e.isConsumed()) {
			e.consume();
			//We have doubled clicked on an Annotation
			if (annotation.isSelected()) {
				annotation.setSelected(false);
			} else {
				//This preVZoom value will help in resizing the selected Annotations
				// double prevZoom=networkCanvas.getScaleFactor();

				// annotation.setSpecificZoom(prevZoom);
				annotation.setSelected(true);

				//We request focus in this window, so that we can move these selected Annotations around using arrow keys
				annotation.getCanvas().requestFocusInWindow();
			}

			//Repaint the canvas
			annotation.getCanvas().repaint();	
		}
*/
	}

	public void mouseEntered(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}
}
