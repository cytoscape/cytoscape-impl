package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;

import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory; 


public class AddAnnotationTaskFactory implements NetworkViewLocationTaskFactory {
	private final AnnotationFactory annotationFactory;
	
	public AddAnnotationTaskFactory( AnnotationFactory annotationFactory) {
		this.annotationFactory = annotationFactory;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView,
			Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new AddAnnotationTask(networkView, xformPt, annotationFactory));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt,
			Point2D xformPt) {
		return true;
	}
}
