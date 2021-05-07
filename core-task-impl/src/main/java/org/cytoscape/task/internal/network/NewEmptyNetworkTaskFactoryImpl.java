package org.cytoscape.task.internal.network;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
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

public class NewEmptyNetworkTaskFactoryImpl extends AbstractTaskFactory implements NewEmptyNetworkViewFactory {
	
	private final Set<NetworkViewRenderer> viewRenderers;
	private final CyServiceRegistrar serviceRegistrar;
	
	public NewEmptyNetworkTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		viewRenderers = new TreeSet<>(new Comparator<NetworkViewRenderer>() {
			@Override
			public int compare(NetworkViewRenderer r1, NetworkViewRenderer r2) {
				return r1.toString().compareToIgnoreCase(r2.toString());
			}
		});
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(createTask());
	} 

	@Override
	public CyNetworkView createNewEmptyNetworkView() {
		// no tunables, so no need to set the execution context
		NewEmptyNetworkTask task = createTask();
		serviceRegistrar.getService(SynchronousTaskManager.class).execute(new TaskIterator(task));	
		
		return task.getView(); 
	}
	
	public void addNetworkViewRenderer(final NetworkViewRenderer renderer, final Map<?, ?> props) {
		viewRenderers.add(renderer);
	}

	public void removeNetworkViewRenderer(final NetworkViewRenderer renderer, final Map<?, ?> props) {
		viewRenderers.remove(renderer);
	}
	
	private NewEmptyNetworkTask createTask() {
		if (viewRenderers.isEmpty())
			throw new RuntimeException("Unable to create Network View: There is no NetworkViewRenderer.");
		
		return new NewEmptyNetworkTask(viewRenderers, serviceRegistrar);
	}
}
