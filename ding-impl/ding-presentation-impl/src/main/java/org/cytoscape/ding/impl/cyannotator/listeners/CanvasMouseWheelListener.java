package org.cytoscape.ding.impl.cyannotator.listeners;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;

public class CanvasMouseWheelListener implements MouseWheelListener{
	private final CyAnnotator cyAnnotator;
	private final InnerCanvas networkCanvas;
	private final DRenderingEngine re;
	private double prevZoom = 1.0;

	public CanvasMouseWheelListener(CyAnnotator c, DRenderingEngine re) {
		this.cyAnnotator = c;
		this.re = re;
		this.networkCanvas = re.getCanvas();
	}

	//To handle zooming in and out
	public void mouseWheelMoved(MouseWheelEvent e) {
		networkCanvas.mouseWheelMoved(e);
		return;

		/*
		int notches = e.getWheelRotation();
		double factor = 1.0;

		// scroll up, zoom in
		if (notches < 0)
				factor = 1.1;
		else if (notches > 0)
				factor = 0.9;
		else
				return;

		Set<DingAnnotation> selectedAnnotations = cyAnnotator.getSelectedAnnotations();
		if(selectedAnnotations != null && selectedAnnotations.size() > 0){
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
		networkCanvas.mouseWheelMoved(e);
		*/
	}
}
