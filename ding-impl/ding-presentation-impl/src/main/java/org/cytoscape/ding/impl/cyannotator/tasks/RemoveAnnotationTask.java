package org.cytoscape.ding.impl.cyannotator.tasks;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class RemoveAnnotationTask extends AbstractNetworkViewTask {
	
	private final DingAnnotation annotation;

	private static final Logger logger = LoggerFactory.getLogger(RemoveAnnotationTask.class);

	public RemoveAnnotationTask(CyNetworkView view, DingAnnotation annotation) {
		super(view);
		this.annotation = annotation;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Remove Annotation");
		
		if (view instanceof DGraphView) {
			CyAnnotator annotator = annotation.getCyAnnotator();
			annotator.markUndoEdit("Delete Annotation");
			
			annotation.removeAnnotation();
			
			annotator.postUndoEdit();
		}
	}
}
