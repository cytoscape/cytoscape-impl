package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class MoveAnnotationTaskFactory implements NetworkViewLocationTaskFactory {
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		return new TaskIterator(new MoveAnnotationTask(networkView, annotation, javaPt));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		if (annotation != null)
			return true;
		return false;
	}
}
