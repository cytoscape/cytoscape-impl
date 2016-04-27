package org.cytoscape.ding.impl.cyannotator.tasks;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */



import java.awt.geom.Point2D;

import javax.swing.JComponent;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
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
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		return new TaskIterator(new LayerAnnotationTask(networkView, annotation, newZorder));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(javaPt);
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
