package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DRenderingEngine.UpdateType;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation.CanvasID;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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
		
		var cyAnnotator = re.getCyAnnotator();
		cyAnnotator.markUndoEdit("Reorder Annotations");
		
		if (canvasName != null)
			changeCanvas();
		else if (shift != null)
			ViewUtils.reorder(annotations, shift, re);
		
		cyAnnotator.postUndoEdit();
		cyAnnotator.fireAnnotationsReordered();
	}

	private void changeCanvas() {
		var id = CanvasID.fromArgName(canvasName);
		
		for (var da : annotations)
			da.changeCanvas(id);
		
		// need to rebuild the tree AFTER changing the canvas
		var cyAnnotator = re.getCyAnnotator();
		var tree = cyAnnotator.getAnnotationTree();
		tree.resetZOrder();
		
		re.updateView(UpdateType.JUST_ANNOTATIONS);
	}
}
