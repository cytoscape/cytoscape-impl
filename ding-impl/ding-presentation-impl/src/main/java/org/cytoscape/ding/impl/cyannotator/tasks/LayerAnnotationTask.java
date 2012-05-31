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

public class LayerAnnotationTask extends AbstractNetworkViewTask {
	private final Annotation annotation; 
	private final boolean pull;

	private static final Logger logger = LoggerFactory.getLogger(ResizeAnnotationTask.class);
	
	
	public LayerAnnotationTask(CyNetworkView view, Annotation annotation, boolean pull) {
		super(view);
		this.annotation = annotation;
		this.pull = pull;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( view instanceof DGraphView) {
			if (pull)
				annotation.changeCanvas(Annotation.FOREGROUND);
			else
				annotation.changeCanvas(Annotation.BACKGROUND);
		}
	}
}
