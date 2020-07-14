package org.cytoscape.ding.impl.cyannotator.tasks;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;

import java.awt.geom.Point2D;

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

public class EditAnnotationTask extends AbstractTask {
	
	private final DRenderingEngine re;
	private final DingAnnotation annotation; 
	private final Point2D location; 

	public EditAnnotationTask(DRenderingEngine re, DingAnnotation annotation, Point2D location) {
		this.re = re;
		this.annotation = annotation;
		this.location = location;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Edit Annotation");
		
		if (re != null) {
			invokeOnEDT(() -> {
				var dialog = annotation.getModifyDialog();
				
				if (dialog != null) {
					var owner = dialog.getOwner();
					
					if (location != null && owner != null) {
						var screen = owner.getGraphicsConfiguration().getBounds();
						dialog.setLocation((int)location.getX() + screen.x, (int) location.getY() + screen.x);
					} else {
						dialog.setLocationRelativeTo(re.getComponent());
					}
					
					dialog.setVisible(true);
				}
			});
		}
	}
}
