package org.cytoscape.editor.internal;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

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

public class DeleteNestedNetworkTaskFactory extends AbstractNodeViewTaskFactory {
	
	private final CyServiceRegistrar serviceRegistrar;

	public DeleteNestedNetworkTaskFactory(final CyServiceRegistrar serviceRegistrar) { 
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new DeleteNestedNetworkTask(nodeView, networkView, serviceRegistrar));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		if (!super.isReady(nodeView, networkView))
			return false;
		
		// Check if there is a network pointer and if it is registered.
		// Nodes with unregistered network pointers should be ignored because they are probably being used as something
		// else other than regular nested networks (e.g. groups).
		final CyNode node  = nodeView.getModel();
		final CyNetwork np = node.getNetworkPointer();
		final CyNetwork network = networkView.getModel();
		
		final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		final CyGroupManager grMgr = serviceRegistrar.getService(CyGroupManager.class);
		
		return np != null && netMgr.networkExists(np.getSUID()) && !grMgr.isGroup(node, network);
	}
}
