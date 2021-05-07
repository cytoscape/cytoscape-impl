package org.cytoscape.task.internal.view;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkCollectionTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class CreateNetworkViewTaskFactoryImpl extends AbstractNetworkCollectionTaskFactory implements
		CreateNetworkViewTaskFactory, TaskFactory {

	private final CyNetworkManager netMgr;
	private final CyLayoutAlgorithmManager layoutMgr;
	private final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;
	private final Set<NetworkViewRenderer> viewRenderers;

	public CreateNetworkViewTaskFactoryImpl(
			CyNetworkManager netMgr,
			CyLayoutAlgorithmManager layoutMgr,
			CyApplicationManager appMgr,
			CyServiceRegistrar serviceRegistrar
	) {
		this.netMgr = netMgr;
		this.layoutMgr = layoutMgr;
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
		viewRenderers = new HashSet<>();
	}

	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetwork> networks, final CyNetworkViewFactory factory) {
		// Create visualization + layout (optional)
		final int expectedNumTasks = layoutMgr == null ? 1 : 2;

		return new TaskIterator(expectedNumTasks,
				new CreateNetworkViewTask(networks, factory, netMgr, layoutMgr, appMgr, serviceRegistrar));
	}
	
	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetwork> networks) {
		if (viewRenderers.isEmpty())
			throw new RuntimeException("Unnable to create Network View: There is no NetworkViewRenderer.");
		
		// Create visualization + layout (optional)
		final int expectedNumTasks = layoutMgr == null ? 1 : 2;
		
		return new TaskIterator(expectedNumTasks,
				new CreateNetworkViewTask(networks, netMgr, layoutMgr, appMgr, viewRenderers, serviceRegistrar));
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(Collections.singletonList(appMgr.getCurrentNetwork()));
	}

	@Override
	public boolean isReady() {
		return appMgr.getCurrentNetwork() != null;
	}
	
	// TODO delete this method when multiple views per network is completely supported
	@Override
	public boolean isReady(Collection<CyNetwork> networks) {
		var netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (CyNetwork n : networks) {
			if (netViewMgr.getNetworkViews(n).isEmpty())
				return true;
		}
		
		return false;
	}
	
	public void addNetworkViewRenderer(final NetworkViewRenderer renderer, final Map<?, ?> props) {
		viewRenderers.add(renderer);
	}

	public void removeNetworkViewRenderer(final NetworkViewRenderer renderer, final Map<?, ?> props) {
		viewRenderers.remove(renderer);
	}
}
