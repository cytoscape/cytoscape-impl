package org.cytoscape.ding.impl.cyannotator.tasks;


import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddArrowTask extends AbstractNetworkViewTask {

	private final Point2D location;
	private static int new_node_index =1;
	private final AnnotationFactory annotationFactory; 

	private static final Logger logger = LoggerFactory.getLogger(AddArrowTask.class);
	
	
	public AddArrowTask(CyNetworkView view, Point2D location, AnnotationFactory annotationFactory) {
		super(view);
		this.location = location;
		this.annotationFactory = annotationFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		
		if ( view instanceof DGraphView ) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
			
		 			JFrame frame = annotationFactory.createAnnotationFrame((DGraphView)view, location);	
					frame.setLocation((int)location.getX(), (int)location.getY());
					frame.setVisible(true);
				}
			});
		}
	}
}
