package org.cytoscape.ding.impl.cyannotator.tasks; 


import java.awt.geom.Point2D;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class LayerAnnotationTaskFactory implements NetworkViewLocationTaskFactory {
	private int offset;
	private int newZorder;

	public LayerAnnotationTaskFactory(int offset) {
		this.offset = offset;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		return new TaskIterator(new LayerAnnotationTask(networkView, annotation, newZorder));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		Annotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		if (annotation == null) return false;

		JComponent canvas = annotation.getCanvas();
		int zorder = canvas.getComponentZOrder(annotation.getComponent());

		if ((offset < 0 && zorder > 0) || 
		    (offset > 0 && zorder < canvas.getComponentCount()-1)) {
			this.newZorder = zorder + offset;
			if (this.newZorder < 0) 
				this.newZorder = 0;
			else if (this.newZorder > canvas.getComponentCount()-1)
				this.newZorder = canvas.getComponentCount()-1;

			return true;
		}
		return false;
	}
}
