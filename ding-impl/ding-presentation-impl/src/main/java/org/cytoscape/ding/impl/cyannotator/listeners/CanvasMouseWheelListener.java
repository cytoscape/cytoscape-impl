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

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.Set;

public class CanvasMouseWheelListener implements MouseWheelListener{
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DGraphView view;
	private double prevZoom = 1.0;

	public CanvasMouseWheelListener(CyAnnotator c, DGraphView view) {
		this.cyAnnotator = c;
		this.view = view;
		this.networkCanvas = view.getCanvas();
	}

	//To handle zooming in and out
	public void mouseWheelMoved(MouseWheelEvent e) {

		int notches = e.getWheelRotation();
		double factor = 1.0;

		// scroll up, zoom in
		if (notches < 0)
				factor = 1.1;
		else
				factor = 0.9;

		Set<DingAnnotation> selectedAnnotations = cyAnnotator.getSelectedAnnotations();
		if(selectedAnnotations != null && !selectedAnnotations.isEmpty()){
			//If some annotations are selected
			for (DingAnnotation annotation: selectedAnnotations) {
				annotation.setSpecificZoom( prevZoom * factor  );
			}

			//In that case only increase the size (Change font in some cases) 
			//for those specific annotations
			prevZoom*=factor;
		} else {
			networkCanvas.mouseWheelMoved(e);
		}
	}
}
