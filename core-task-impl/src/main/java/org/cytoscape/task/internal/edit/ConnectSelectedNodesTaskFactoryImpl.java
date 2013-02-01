package org.cytoscape.task.internal.edit;

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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class ConnectSelectedNodesTaskFactoryImpl extends AbstractNetworkTaskFactory implements
		ConnectSelectedNodesTaskFactory, NodeViewTaskFactory {
	
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final CyNetworkViewManager netViewMgr;

	public ConnectSelectedNodesTaskFactoryImpl(final UndoSupport undoSupport, final CyEventHelper eventHelper,
			final VisualMappingManager vmm, final CyNetworkViewManager netViewMgr) {
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.netViewMgr = netViewMgr;
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetwork network) {
		return new TaskIterator(new ConnectSelectedNodesTask(undoSupport, network, eventHelper, vmm, netViewMgr));
	}

	/**
	 * This is for registering this to node context menu
	 */
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new ConnectSelectedNodesTask(undoSupport, networkView.getModel(), eventHelper, vmm, netViewMgr));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return nodeView != null && networkView != null;
	}
}
