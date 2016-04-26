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

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.Set;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;

public class CanvasKeyListener implements KeyListener {
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;

	public CanvasKeyListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		Set<DingAnnotation> selectedAnnotations = cyAnnotator.getSelectedAnnotations();

		if ((selectedAnnotations != null && !selectedAnnotations.isEmpty()) &&
		    ((code == KeyEvent.VK_UP) || 
		    (code == KeyEvent.VK_DOWN) || 
		    (code == KeyEvent.VK_LEFT)|| 
		    (code == KeyEvent.VK_RIGHT)))
		{
			//Some annotations have been double clicked and selected
			int move=2;
			for (DingAnnotation annotation: selectedAnnotations) {
				Component c = annotation.getComponent();
				int x=c.getX(), y=c.getY();
				int shiftMask = e.getModifiers() & KeyEvent.SHIFT_DOWN_MASK;
				if (annotation instanceof ShapeAnnotationImpl && e.isShiftDown()) {
					ShapeAnnotationImpl sa = (ShapeAnnotationImpl)annotation;
					int width = c.getWidth(), height = c.getHeight();
					if (code == KeyEvent.VK_UP) {
						height -= move;
					} else if (code == KeyEvent.VK_DOWN) {
						height += move;
					} else if (code == KeyEvent.VK_LEFT) {
						width -= move;
					} else if (code == KeyEvent.VK_RIGHT) {
						width += move;
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
		}
		networkCanvas.keyPressed(e);
	}

	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) { }
}
