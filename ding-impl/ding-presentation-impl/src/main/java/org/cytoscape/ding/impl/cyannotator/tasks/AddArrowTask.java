package org.cytoscape.ding.impl.cyannotator.tasks;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;

import java.awt.geom.Point2D;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public class AddArrowTask extends AbstractTask {

	private final DRenderingEngine re;
	private final Point2D location;
	private final AnnotationFactory<?> annotationFactory; 

	public AddArrowTask(DRenderingEngine re, Point2D location, AnnotationFactory<?> annotationFactory) {
		this.re = re;
		this.location = location;
		this.annotationFactory = annotationFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Add Arrow Annotation");
		
		if (re != null && annotationFactory instanceof AbstractDingAnnotationFactory) {
			invokeOnEDT(() -> {
				// TODO
//				var dialog = ((AbstractDingAnnotationFactory<?>) annotationFactory).createView(re.getViewModel(), location);
//				
//				if (dialog != null) {
//					dialog.setLocation((int) location.getX(), (int) location.getY());
//					dialog.setVisible(true);
//				}
			});
		}
	}
}
