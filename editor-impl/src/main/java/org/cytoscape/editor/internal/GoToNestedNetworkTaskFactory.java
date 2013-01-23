package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class GoToNestedNetworkTaskFactory extends AbstractNodeViewTaskFactory {
	
	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager netViewMgr;
	private final CyApplicationManager appMgr;
	private final CreateNetworkViewTaskFactory createViewFactory;

	public GoToNestedNetworkTaskFactory(final CyNetworkManager netMgr,
										final CyNetworkViewManager netViewMgr,
										final CyApplicationManager appMgr,
										final CreateNetworkViewTaskFactory createViewFactory) {
		this.netMgr = netMgr;
		this.netViewMgr = netViewMgr;
		this.appMgr = appMgr;
		this.createViewFactory = createViewFactory;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new GoToNestedNetworkTask(nodeView, networkView, netMgr, netViewMgr, appMgr, createViewFactory));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		if (!super.isReady(nodeView, networkView))
			return false;
		
		// Check if there is a network pointer and if it is registered.
		final CyNode node  = nodeView.getModel();
		final CyNetwork np = node.getNetworkPointer();
		
		return np != null && netMgr.networkExists(np.getSUID());
	}
}
