package org.cytoscape.ding.impl.cyannotator.tasks;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
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

public class RemoveAnnotationTask extends AbstractTask {
	
	private final DRenderingEngine re;
	private final DingAnnotation annotation;

	public RemoveAnnotationTask(DRenderingEngine re, DingAnnotation annotation) {
		this.re = re;
		this.annotation = annotation;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Remove Annotation");
		
		if (re != null) {
			var annotator = annotation.getCyAnnotator();
			annotator.markUndoEdit("Remove Annotation");
			
			annotation.removeAnnotation();
			
			annotator.postUndoEdit();
		}
	}
}
