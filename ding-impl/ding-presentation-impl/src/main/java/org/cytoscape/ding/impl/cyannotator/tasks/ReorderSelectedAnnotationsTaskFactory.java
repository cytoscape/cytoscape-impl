package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

	private final DingRenderer dingRenderer;
	
	private String canvasName;
	private Shift shift;
	
	/**
	 * Use this constructor to move annotations to another canvas.
	 */
	public ReorderSelectedAnnotationsTaskFactory(DingRenderer dingRenderer, String canvasName) {
		this.dingRenderer = dingRenderer;
		this.canvasName = canvasName;
	}
	
	/**
	 * Use this constructor to reorder annotations on the same canvas.
	 */
	public ReorderSelectedAnnotationsTaskFactory(DingRenderer dingRenderer, Shift shift) {
		this.dingRenderer = dingRenderer;
		this.shift = shift;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		DRenderingEngine re = dingRenderer.getRenderingEngine(view);
		if(re == null)
			return null;
		final CyAnnotator cyAnnotator = re.getCyAnnotator();
		final Collection<DingAnnotation> annotations = cyAnnotator != null ?
				cyAnnotator.getAnnotationSelection().getSelectedAnnotations() : Collections.emptySet();
		
		return new TaskIterator(new ReorderAnnotationsTask(re, annotations, canvasName, shift));
	}
	
	@Override
	public boolean isReady(CyNetworkView view) {
		DRenderingEngine re = dingRenderer.getRenderingEngine(view);
		if(re == null)
			return false;
		
		final CyAnnotator cyAnnotator = re.getCyAnnotator();
		final var annotations = cyAnnotator.getAnnotationSelection().getSelectedAnnotations();
		
		if (annotations == null || annotations.isEmpty())
			return false;
		
		
		if (shift != null) {
			boolean fg = cyAnnotator.getAnnotationTree().shiftAllowed(shift, Annotation.FOREGROUND, annotations);
			boolean bg = cyAnnotator.getAnnotationTree().shiftAllowed(shift, Annotation.BACKGROUND, annotations);
			return fg || bg;
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
