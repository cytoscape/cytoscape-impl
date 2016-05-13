package org.cytoscape.ding.impl.cyannotator.tasks;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.create.AbstractDingAnnotationFactory;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.work.TaskMonitor;

public class AddAnnotationTask extends AbstractNetworkViewTask {

	private final Point2D location;
	private final AnnotationFactory<?> annotationFactory; 

	public AddAnnotationTask(final CyNetworkView view, final Point2D location,
			final AnnotationFactory<?> annotationFactory) {
		super(view);
		this.location = location;
		this.annotationFactory = annotationFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (view instanceof DGraphView && annotationFactory instanceof AbstractDingAnnotationFactory) {
			SwingUtilities.invokeLater(() -> {
				final JDialog dialog = ((AbstractDingAnnotationFactory<?>) annotationFactory)
						.createAnnotationDialog((DGraphView) view, location);
				dialog.setLocation((int) location.getX(), (int) location.getY());
				dialog.setVisible(true);
			});
		}
	}
}
