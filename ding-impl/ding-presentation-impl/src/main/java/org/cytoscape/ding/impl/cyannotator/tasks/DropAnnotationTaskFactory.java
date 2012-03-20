package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;

import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory; 


public class DropAnnotationTaskFactory implements DropNetworkViewTaskFactory {
	private final BasicGraphicalEntity bge; 
	private final AnnotationFactory annotationFactory;
	
	public DropAnnotationTaskFactory(BasicGraphicalEntity bge, AnnotationFactory annotationFactory) {
		this.bge = bge;
		this.annotationFactory = annotationFactory;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, Transferable t, Point2D javaPt, Point2D xformPt) {
		return new TaskIterator(new DropAnnotationTask(view, t, xformPt, bge, annotationFactory));
	}
	
	@Override
	public boolean isReady(CyNetworkView networkView, Transferable t, Point2D javaPt, Point2D xformPt) {
		return true;
	}
}
