package org.cytoscape.ding.impl.cyannotator.listeners;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;

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
			cyAnnotator.resizeShape(null);
			return;
		}

		if (cyAnnotator.getMovingAnnotation() != null) {
			cyAnnotator.moveAnnotation(null);
			return;
		}


		Annotation annotation = cyAnnotator.getAnnotation(new Point(e.getX(), e.getY()));
		if (annotation == null) {
			cyAnnotator.clearSelectedAnnotations();
			if (!e.isConsumed()) {
				networkCanvas.processMouseEvent(e);
				e.consume();
			}
			return;
		}

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
	}

	public void mouseEntered(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}

	public void mouseExited(MouseEvent e) {
		networkCanvas.processMouseEvent(e);
	}
}
