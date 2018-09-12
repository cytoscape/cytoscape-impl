package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collections;
import java.util.Set;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

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

public class ReorderSelectedAnnotationsTaskFactory implements NetworkViewTaskFactory {

	private String canvasName;
	private Integer offset;
	
	/**
	 * Use this constructor to move annotations to another canvas.
	 */
	public ReorderSelectedAnnotationsTaskFactory(String canvasName) {
		this.canvasName = canvasName;
	}
	
	/**
	 * Use this constructor to reorder annotations on the same canvas.
	 */
	public ReorderSelectedAnnotationsTaskFactory(int offset) {
		this.offset = offset;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		final CyAnnotator cyAnnotator = view instanceof DGraphView ? ((DGraphView) view).getCyAnnotator() : null;
		final Set<DingAnnotation> annotations = cyAnnotator != null ?
				cyAnnotator.getAnnotationSelection().getSelectedAnnotations() : Collections.emptySet();
		
		return new TaskIterator(new ReorderAnnotationsTask(view, annotations, canvasName, offset));
	}
	
	@Override
	public boolean isReady(CyNetworkView view) {
		if (view instanceof DGraphView == false)
			return false;
		
		final CyAnnotator cyAnnotator = ((DGraphView) view).getCyAnnotator();
		final Set<DingAnnotation> annotations = cyAnnotator.getAnnotationSelection().getSelectedAnnotations();
		
		if (annotations == null || annotations.isEmpty())
			return false;
		
		
		if (offset != null) {
			return cyAnnotator.getAnnotationTree().shiftAllowed(offset, annotations);
		}
		
		if (canvasName != null) {
			for (DingAnnotation a : annotations) {
				if (!a.getCanvasName().equals(canvasName)) {
					return true;
				}
			}
		}
		return false;
	}
}
