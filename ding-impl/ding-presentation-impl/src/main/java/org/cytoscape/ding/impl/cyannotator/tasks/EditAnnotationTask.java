package org.cytoscape.ding.impl.cyannotator.tasks;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditAnnotationTask extends AbstractNetworkViewTask {
	private final DingAnnotation annotation; 
	private final Point2D location; 

	private static final Logger logger = LoggerFactory.getLogger(EditAnnotationTask.class);
	
	
	public EditAnnotationTask(CyNetworkView view, DingAnnotation annotation, Point2D location) {
		super(view);
		this.annotation = annotation;
		this.location = location;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		
		if ( view instanceof DGraphView ) {
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
		 			JDialog dialog = annotation.getModifyDialog();	
					if (dialog != null) {
						dialog.setLocation((int)location.getX(), (int)location.getY());
						dialog.setVisible(true);
					}
				}
			});
		}
	}
}
