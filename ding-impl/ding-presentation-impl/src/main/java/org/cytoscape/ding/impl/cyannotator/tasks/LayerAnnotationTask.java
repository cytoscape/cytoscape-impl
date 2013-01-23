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



import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LayerAnnotationTask extends AbstractNetworkViewTask {
	private final Annotation annotation; 
	private final int zorder;

	private static final Logger logger = LoggerFactory.getLogger(ResizeAnnotationTask.class);
	
	
	public LayerAnnotationTask(CyNetworkView view, Annotation annotation, int zorder) {
		super(view);
		this.annotation = annotation;
		this.zorder = zorder;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		JComponent canvas = annotation.getCanvas();
		canvas.setComponentZOrder(annotation.getComponent(), zorder);
		canvas.repaint();
		annotation.contentChanged(); // We need to do this to update the Bird's Eye View
	}
}
