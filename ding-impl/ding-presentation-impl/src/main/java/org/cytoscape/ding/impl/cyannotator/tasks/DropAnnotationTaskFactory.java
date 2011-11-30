package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;
import java.awt.datatransfer.Transferable;

import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory; 


public class DropAnnotationTaskFactory implements DropNetworkViewTaskFactory {
	private CyNetworkView view;
	private Transferable t;
	private Point2D javaPt;
	private Point2D xformPt;
	private final BasicGraphicalEntity bge; 
	private final AnnotationFactory annotationFactory;
	
	public DropAnnotationTaskFactory(BasicGraphicalEntity bge, AnnotationFactory annotationFactory) {
		this.bge = bge;
		this.annotationFactory = annotationFactory;
	}

	public void setNetworkView(CyNetworkView view) {
		this.view = view;
	}

	public void setDropInformation(Transferable t, Point2D javaPt, Point2D xformPt) {
		this.t = t;
		this.javaPt = javaPt;
		this.xformPt = xformPt;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new DropAnnotationTask(view, t, xformPt, bge, annotationFactory));
	}
}
