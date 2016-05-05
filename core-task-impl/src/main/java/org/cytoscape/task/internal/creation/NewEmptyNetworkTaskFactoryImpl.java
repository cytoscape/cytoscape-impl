package org.cytoscape.task.internal.creation;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
 

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

public class NewEmptyNetworkTaskFactoryImpl extends AbstractTaskFactory implements NewEmptyNetworkViewFactory {
	
	private final CyNetworkFactory netFactory;
	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager netViewMgr;
	private final CyNetworkNaming namingUtil;
	private final SynchronousTaskManager<?> syncTaskMgr;
	private final VisualMappingManager vmMgr;
	private final CyRootNetworkManager rootNetMgr;
	private final CyApplicationManager appMgr;
	private final Set<NetworkViewRenderer> viewRenderers;
	
	public NewEmptyNetworkTaskFactoryImpl(final CyNetworkFactory netFactory,
										  final CyNetworkManager netMgr,
										  final CyNetworkViewManager netViewMgr,
										  final CyNetworkNaming namingUtil,
										  final SynchronousTaskManager<?> syncTaskMgr,
										  final VisualMappingManager vmMgr,
										  final CyRootNetworkManager rootNetMgr,
										  final CyApplicationManager appMgr) {
		this.netFactory = netFactory;
		this.netMgr = netMgr;
		this.netViewMgr = netViewMgr;
		this.namingUtil = namingUtil;
		this.syncTaskMgr = syncTaskMgr;
		this.vmMgr = vmMgr;
		this.rootNetMgr = rootNetMgr;
		this.appMgr = appMgr;
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

	private NewEmptyNetworkTask createTask() {
		if (viewRenderers.isEmpty())
			throw new RuntimeException("Unnable to create Network View: There is no NetworkViewRenderer.");
		
		return new NewEmptyNetworkTask(netFactory, netMgr, netViewMgr, namingUtil, vmMgr, rootNetMgr,
				appMgr, viewRenderers);
	}
	
	@Override
	public CyNetworkView createNewEmptyNetworkView() {
		// no tunables, so no need to set the execution context
		NewEmptyNetworkTask task = createTask();
		syncTaskMgr.execute(new TaskIterator(task));	
		
		return task.getView(); 
	}
	
	public void addNetworkViewRenderer(final NetworkViewRenderer renderer, final Map<?, ?> props) {
		viewRenderers.add(renderer);
	}

	public void removeNetworkViewRenderer(final NetworkViewRenderer renderer, final Map<?, ?> props) {
		viewRenderers.remove(renderer);
	}
}
