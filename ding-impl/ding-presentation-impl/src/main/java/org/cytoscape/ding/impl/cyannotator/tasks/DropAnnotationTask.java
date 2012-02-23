package org.cytoscape.ding.impl.cyannotator.tasks;


import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory;
import org.cytoscape.dnd.DropUtil;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropAnnotationTask extends AbstractNetworkViewTask {

	private final Transferable t;
	private final Point2D location;
	private static int new_node_index =1;
	private final BasicGraphicalEntity bge; 
	private final AnnotationFactory annotationFactory; 

	private static final Logger logger = LoggerFactory.getLogger(DropAnnotationTask.class);
	
	
	public DropAnnotationTask(CyNetworkView view, Transferable t, Point2D location, BasicGraphicalEntity bge, AnnotationFactory annotationFactory) {
		super(view);
		this.t = t;
		this.location = location;
		this.bge = bge;
		this.annotationFactory = annotationFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if ( !DropUtil.transferableMatches(t,bge.getTitle()) ) 
			return;

		if ( view instanceof DGraphView ) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
			
		 			JFrame frame = annotationFactory.createAnnotationFrame((DGraphView)view);	
					frame.setLocation((int)location.getX(), (int)location.getY());
					frame.setVisible(true);
				}
			});
		}
	}
}
