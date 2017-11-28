package org.cytoscape.task.internal.zoom;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

abstract class AbstractZoomTask extends AbstractNetworkViewTask {
	
	private final double factor;
	private final CyServiceRegistrar serviceRegistrar;

	AbstractZoomTask(CyNetworkView view, double factor, CyServiceRegistrar serviceRegistrar) {
		super(view);
		this.factor = factor;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Zoom View");
		tm.setProgress(0.0);
		
		final double oldFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR).doubleValue();
		view.setVisualProperty(NETWORK_SCALE_FACTOR, oldFactor * factor);
		tm.setProgress(0.2);
		
		view.updateView();
		tm.setProgress(0.4);
		
		serviceRegistrar.getService(UndoSupport.class).postEdit(new ZoomEdit(view, factor));
		tm.setProgress(1.0);
	}
}
