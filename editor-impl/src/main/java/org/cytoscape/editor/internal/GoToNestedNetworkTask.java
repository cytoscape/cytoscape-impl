package org.cytoscape.editor.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

public class GoToNestedNetworkTask extends AbstractNodeViewTask {

	private final CyServiceRegistrar serviceRegistrar;
	
	public GoToNestedNetworkTask(final View<CyNode> nv, final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar) {
		super(nv, view);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode node = nodeView.getModel();
		final CyNetwork netPointer = node.getNetworkPointer();
		final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		
		if (netPointer != null && netMgr.networkExists(netPointer.getSUID())) {
			CyNetworkView pointerView = null;
			
			final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			Collection<CyNetworkView> views = netViewMgr.getNetworkViews(netPointer);
			
			for (CyNetworkView nv : views) {
				// Get the first view
				pointerView = nv;
				break;
			}
			
			if (pointerView == null) {
				// Create a network view
				final Set<CyNetwork> networks = new HashSet<CyNetwork>();
				networks.add(netPointer);
				
				final CreateNetworkViewTaskFactory createViewFactory = serviceRegistrar
						.getService(CreateNetworkViewTaskFactory.class);
				TaskIterator iter = createViewFactory.createTaskIterator(networks);
				this.insertTasksAfterCurrentTask(iter);
			} else {
				// Just set the existing one as current
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				appMgr.setCurrentNetworkView(pointerView);
			}
		}
	}
}
