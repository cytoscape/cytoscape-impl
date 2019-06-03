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

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.GroupAnnotationImpl;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class UngroupAnnotationsTaskFactory implements NetworkViewLocationTaskFactory {
	
	private final DingRenderer dingRenderer;
	
	public UngroupAnnotationsTaskFactory(DingRenderer dingRenderer) {
		this.dingRenderer = dingRenderer;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		DRenderingEngine re = dingRenderer.getRenderingEngine(networkView);
		if(re == null)
			return null;
		CyAnnotator cyAnnotator = re.getCyAnnotator();
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		return new TaskIterator(new UngroupAnnotationsTask(re, annotation));

	}

	@Override
	public boolean isReady(CyNetworkView networkView, Point2D javaPt, Point2D xformPt) {
		DRenderingEngine re = dingRenderer.getRenderingEngine(networkView);
		if(re == null)
			return false;
		CyAnnotator cyAnnotator = re.getCyAnnotator();
		DingAnnotation annotation = cyAnnotator.getAnnotationAt(javaPt);
		if (annotation != null && annotation instanceof GroupAnnotationImpl)
			return true;
		return false;
	}
}
