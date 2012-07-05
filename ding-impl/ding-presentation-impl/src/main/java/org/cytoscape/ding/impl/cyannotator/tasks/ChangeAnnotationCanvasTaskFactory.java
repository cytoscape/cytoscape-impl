package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class ChangeAnnotationCanvasTaskFactory implements NetworkViewLocationTaskFactory {
	private String canvas;

	public ChangeAnnotationCanvasTaskFactory(String canvas) {
		this.canvas = canvas;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		return new TaskIterator(new ChangeAnnotationCanvasTask(networkView, annotation, canvas));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		if (annotation == null) return false;

		if (!annotation.getCanvasName().equals(canvas))
			return true;
		return false;
	}
}
