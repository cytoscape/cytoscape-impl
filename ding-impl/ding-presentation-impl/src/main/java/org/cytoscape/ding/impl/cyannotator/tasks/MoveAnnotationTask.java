package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

public class MoveAnnotationTask extends AbstractNetworkViewTask {
	
	private final DingAnnotation annotation; 
	private final Point2D start; 

	
	public MoveAnnotationTask(CyNetworkView view, DingAnnotation annotation, Point2D startingLocation) {
		super(view);
		while (annotation.getGroupParent() != null) {
			annotation = (DingAnnotation)annotation.getGroupParent();
		}
		this.annotation = annotation;
		this.start = startingLocation;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( view instanceof DGraphView ) {
			annotation.moveAnnotation(start);
			annotation.getCyAnnotator().moveAnnotation(annotation);
		}
	}
}
