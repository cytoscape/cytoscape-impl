package org.cytoscape.task.internal.networkobjects;

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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class DeleteSelectedNodesAndEdgesTaskFactoryImpl extends AbstractNetworkTaskFactory implements DeleteSelectedNodesAndEdgesTaskFactory {
	private final UndoSupport undoSupport;
	private final CyNetworkViewManager networkViewManager;
	private final VisualMappingManager visualMappingManager;
	private final CyEventHelper eventHelper;

	public DeleteSelectedNodesAndEdgesTaskFactoryImpl(final UndoSupport undoSupport,
						      final CyNetworkViewManager networkViewManager,
						      final VisualMappingManager visualMappingManager,
						      final CyEventHelper eventHelper)
	{
		this.undoSupport          = undoSupport;
		this.networkViewManager   = networkViewManager;
		this.visualMappingManager = visualMappingManager;
		this.eventHelper          = eventHelper;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(
			new DeleteSelectedNodesAndEdgesTask(network, undoSupport, 
							    networkViewManager,
							    visualMappingManager, eventHelper));
	}
}
