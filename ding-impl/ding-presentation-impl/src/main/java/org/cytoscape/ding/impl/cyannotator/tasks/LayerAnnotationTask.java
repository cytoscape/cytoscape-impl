package org.cytoscape.ding.impl.cyannotator.tasks;


import javax.swing.JComponent;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator.ReorderType;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class LayerAnnotationTask extends AbstractNetworkViewTask {
	
	private final DingAnnotation annotation;
	private final int zorder;

	public LayerAnnotationTask(CyNetworkView view, DingAnnotation annotation, int zorder) {
		super(view);
		this.annotation = annotation;
		this.zorder = zorder;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		JComponent canvas = annotation.getCanvas();
		
		if (annotation instanceof GroupAnnotation) {
			int z = zorder;
			
			for (Annotation ann : ((GroupAnnotation) annotation).getMembers()) {
				DingAnnotation dAnn = (DingAnnotation) ann;
				z += 1;
				dAnn.getCanvas().setComponentZOrder(dAnn.getComponent(), z);
			}
		}
		
		canvas.setComponentZOrder(annotation.getComponent(), zorder);
		canvas.repaint();
		
		if (view instanceof DGraphView) {
			DGraphView dView = (DGraphView) view;
			CyAnnotator cyAnnotator = dView.getCyAnnotator();
			
			// Force an update of all of the argMaps
			for (Annotation ann : cyAnnotator.getAnnotations())
				cyAnnotator.addAnnotation(ann);
		}
		
		annotation.contentChanged(); // We need to do this to update the Bird's Eye View
		
		if (view instanceof DGraphView)
			((DGraphView) view).getCyAnnotator().annotationsReordered(ReorderType.Z_INDEX);
	}
}
