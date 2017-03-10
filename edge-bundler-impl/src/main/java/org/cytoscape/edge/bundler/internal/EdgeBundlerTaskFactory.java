package org.cytoscape.edge.bundler.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Edge Bundler Impl (edge-bundler-impl)
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

public class EdgeBundlerTaskFactory extends AbstractNetworkTaskFactory {

	private int selection;
	private final CyServiceRegistrar serviceRegistrar;
	
	public EdgeBundlerTaskFactory(final int selection, final CyServiceRegistrar serviceRegistrar) { 
		this.selection = selection;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(final CyNetwork network) {
		final CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		return new TaskIterator(new EdgeBundlerTask(view, selection, serviceRegistrar));
	}
}
