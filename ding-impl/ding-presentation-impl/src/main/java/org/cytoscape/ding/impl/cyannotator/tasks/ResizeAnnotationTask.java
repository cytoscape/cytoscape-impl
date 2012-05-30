package org.cytoscape.ding.impl.cyannotator.tasks;


import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResizeAnnotationTask extends AbstractNetworkViewTask {
	private final Annotation annotation; 
	private final Point2D location; 

	private static final Logger logger = LoggerFactory.getLogger(ResizeAnnotationTask.class);
	
	
	public ResizeAnnotationTask(CyNetworkView view, Annotation annotation, Point2D location) {
		super(view);
		this.annotation = annotation;
		this.location = location;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( view instanceof DGraphView && annotation instanceof ShapeAnnotation) {
			annotation.getCyAnnotator().resizeShape((ShapeAnnotation)annotation);
		}
	}
}
