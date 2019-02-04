package org.cytoscape.ding.impl.cyannotator.tasks;

import static org.cytoscape.ding.internal.util.ViewUtil.invokeOnEDT;

import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Point2D;

import javax.swing.JDialog;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
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

public class AddAnnotationTask extends AbstractNetworkViewTask {

	private final Point2D location;
	private final AnnotationFactory<?> annotationFactory; 

	public AddAnnotationTask(CyNetworkView view, Point2D location, AnnotationFactory<?> annotationFactory) {
		super(view);
		this.location = location;
		this.annotationFactory = annotationFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Add Annotation");
		
		if (view instanceof DGraphView && annotationFactory instanceof AbstractDingAnnotationFactory) {
			invokeOnEDT(() -> {
				final JDialog dialog = ((AbstractDingAnnotationFactory<?>) annotationFactory)
						.createAnnotationDialog((DGraphView) view, location);
				
				if (dialog != null) {
					Window owner = dialog.getOwner();
					
					if (location != null && owner != null) {
						Rectangle screen = owner.getGraphicsConfiguration().getBounds();
						dialog.setLocation((int)location.getX() + screen.x, (int) location.getY() + screen.x);
					} else {
						dialog.setLocationRelativeTo(((DGraphView) view).getCanvas());
					}
					
					dialog.setVisible(true);
				}
			});
		}
	}
}
