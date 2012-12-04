package org.cytoscape.ding.impl.cyannotator.listeners;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;

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

		Set<Annotation> selectedAnnotations = cyAnnotator.getSelectedAnnotations();
		if(selectedAnnotations != null && selectedAnnotations.size() > 0){
			//If some annotations are selected
			for (Annotation annotation: selectedAnnotations) {
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
