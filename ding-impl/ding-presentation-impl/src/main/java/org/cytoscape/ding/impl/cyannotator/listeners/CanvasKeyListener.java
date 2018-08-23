package org.cytoscape.ding.impl.cyannotator.listeners;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.AnnotationSelection;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;

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

public class CanvasKeyListener implements KeyListener {
	
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;

	public CanvasKeyListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();

		if (!view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			networkCanvas.keyPressed(e);
			return;
		}

		if ((annotationSelection.count() > 0) &&
		    ((code == KeyEvent.VK_UP) || 
		    (code == KeyEvent.VK_DOWN) || 
		    (code == KeyEvent.VK_LEFT)|| 
		    (code == KeyEvent.VK_RIGHT)))
		{
			//Some annotations have been double clicked and selected
			int move=2;
			for (DingAnnotation annotation: annotationSelection) {
				Component c = annotation.getComponent();
				int x=c.getX(), y=c.getY();
				if (annotation instanceof ShapeAnnotationImpl && e.isShiftDown()) {
					ShapeAnnotationImpl sa = (ShapeAnnotationImpl)annotation;
					int width = c.getWidth(), height = c.getHeight();
					int borderWidth = (int)sa.getBorderWidth(); // We need to take this into account
					if (code == KeyEvent.VK_UP) {
						height -= move*2; width -= borderWidth*2;
					} else if (code == KeyEvent.VK_DOWN) {
						height += move; width -= borderWidth*2;
					} else if (code == KeyEvent.VK_LEFT) {
						width -= move*2; height -= borderWidth*2;
					} else if (code == KeyEvent.VK_RIGHT) {
						width += move; height -= borderWidth*2;
					}
					// Adjust the size of the selected annotations
					sa.setSize((double)width, (double)height);

				} else {
					if (code == KeyEvent.VK_UP)
						y-=move;
					else if (code == KeyEvent.VK_DOWN)
						y+=move;
					else if (code == KeyEvent.VK_LEFT)
						x-=move;
					else if (code == KeyEvent.VK_RIGHT)
						x+=move;

					//Adjust the locations of the selected annotations
					annotation.getComponent().setLocation(x,y);
				}
				annotation.update();
				annotation.getCanvas().repaint();	
			}
			return;
		} else if (code == KeyEvent.VK_ESCAPE) {
			if (cyAnnotator.getResizeShape() != null) {
				cyAnnotator.getResizeShape().contentChanged();
				cyAnnotator.resizeShape(null);
				cyAnnotator.postUndoEdit();
				return;
			}

			if (cyAnnotator.getRepositioningArrow() != null) {
				cyAnnotator.getRepositioningArrow().contentChanged();
				cyAnnotator.positionArrow(null);
				cyAnnotator.postUndoEdit();
				return;
			}
		} 
		/*
		else if (code == KeyEvent.VK_ALT) {
			// Get the bounding box of any selected annotations
			Rectangle2D union = null;
			for (DingAnnotation a: selectedAnnotations) {
				if (union == null)
					union = a.getComponent().getBounds().getBounds2D();
				else
					union = union.createUnion(a.getComponent().getBounds().getBounds2D());
			}
			// Draw the box with anchors
			System.out.println("Draw anchors");
			component = new BBoxWithHandles(union, cyAnnotator);
			cyAnnotator.getForeGroundCanvas().add(component);
			cyAnnotator.getForeGroundCanvas().repaint();
		}
		*/
		networkCanvas.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) { 
		int code = e.getKeyCode();
		AnnotationSelection annotationSelection = cyAnnotator.getAnnotationSelection();

		if (!view.getVisualProperty(DVisualLexicon.NETWORK_ANNOTATION_SELECTION)) {
			networkCanvas.keyPressed(e);
			return;
		}

		if (code == KeyEvent.VK_DELETE) {
			if (annotationSelection.count() > 0) {
				Set<DingAnnotation> selectedAnnotations = annotationSelection.getSelectedAnnotations();
				
				for (DingAnnotation ann: selectedAnnotations)
					ann.removeAnnotation();
			}
		}

		networkCanvas.keyPressed(e);
	}

	@Override
	public void keyTyped(KeyEvent e) { }

}
