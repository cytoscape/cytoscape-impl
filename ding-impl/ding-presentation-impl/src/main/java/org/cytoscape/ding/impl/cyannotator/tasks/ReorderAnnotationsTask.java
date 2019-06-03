package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.work.AbstractTask;
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

public class ReorderAnnotationsTask extends AbstractTask {

	private final DRenderingEngine re;
	private final List<DingAnnotation> annotations;
	private final String canvasName;
	
	// negative means bring forward (up), positive means send backward (down) we only move one step at a time.
	private final Shift shift;


	public ReorderAnnotationsTask(
			DRenderingEngine re,
			Collection<DingAnnotation> annotations,
			String canvasName,
			Shift shift
	) {
		this.re = re;
		this.annotations = annotations != null ? new ArrayList<>(annotations) : Collections.emptyList();
		this.canvasName = canvasName;
		this.shift = shift;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Reorder Annotations");
		
		if (re == null)
			return;
		if (annotations.isEmpty())
			return;
		if (canvasName == null && shift == null)
			return;
		
		if (canvasName != null) {
			changeCanvas();
		} else if (shift != null) {
			reorder(shift);
		}
		
		CyAnnotator cyAnnotator = re.getCyAnnotator();
		cyAnnotator.fireAnnotationsReordered();
	}

	private void changeCanvas() {
		for (int i = annotations.size() - 1; i >= 0; i--) {
			DingAnnotation da = annotations.get(i);
			// group annotations must stay on the foreground canvas
			if(!(da instanceof GroupAnnotation && Annotation.BACKGROUND.equals(canvasName))) {
				da.changeCanvas(canvasName);
			}
		}
		
		// need to rebuild the tree AFTER changing the canvas
		CyAnnotator cyAnnotator = re.getCyAnnotator();
		AnnotationTree tree = cyAnnotator.getAnnotationTree();
		tree.resetZOrder();
	}
	

	private void reorder(Shift shift) {
		CyAnnotator cyAnnotator = re.getCyAnnotator();
		AnnotationTree tree = cyAnnotator.getAnnotationTree();
		
		Map<String,List<DingAnnotation>> byCanvas = 
				annotations.stream().collect(Collectors.groupingBy(DingAnnotation::getCanvasName));
		
		List<DingAnnotation> fga = byCanvas.get(Annotation.FOREGROUND);
		if(fga != null && !fga.isEmpty())
			tree.shift(shift, Annotation.FOREGROUND, fga);
		
		List<DingAnnotation> bga = byCanvas.get(Annotation.BACKGROUND);
		if(bga != null && !bga.isEmpty())
			tree.shift(shift, Annotation.BACKGROUND, bga);
		
		tree.resetZOrder();
	}
	
}
