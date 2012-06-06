package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;

import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation; 
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation; 
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory; 


public class AddArrowTaskFactory implements NetworkViewLocationTaskFactory {
	private final AnnotationFactory annotationFactory;
	
	public AddArrowTaskFactory( AnnotationFactory annotationFactory) {
		this.annotationFactory = annotationFactory;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView,
			Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new AddArrowTask(networkView, javaPt, annotationFactory));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt,
			Point2D xformPt) {

		// We need to be over an annotation
		/*
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotation(javaPt);
		if (annotation == null || annotation instanceof ArrowAnnotation) return false;
		return true;
		*/
		return false;
	}
}
