package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;

import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ImageAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;

public class ChangeAnnotationCanvasTaskFactory implements NetworkViewLocationTaskFactory {
	private CyAnnotator cyAnnotator;
	private Annotation annotation;
	private String canvas;

	public ChangeAnnotationCanvasTaskFactory(String canvas) {
		this.canvas = canvas;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		this.cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		annotation = cyAnnotator.getAnnotationAt(javaPt);
		return new TaskIterator(new ChangeAnnotationCanvasTask(networkView, annotation, canvas));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		this.cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		annotation = cyAnnotator.getAnnotationAt(javaPt);
		if (annotation == null) return false;

		if (!annotation.getCanvasName().equals(canvas))
			return true;
		return false;
	}
}
