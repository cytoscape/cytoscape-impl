package org.cytoscape.task.internal.hide;

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
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.hide.HideSelectedNodesTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class HideSelectedNodesTaskFactoryImpl extends AbstractNetworkViewTaskFactory implements
		HideSelectedNodesTaskFactory {
	
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmMgr;

	public HideSelectedNodesTaskFactoryImpl(final UndoSupport undoSupport,
											final CyEventHelper eventHelper,
											final VisualMappingManager vmMgr) {
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		this.vmMgr = vmMgr;
	}

	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(new HideSelectedNodesTask(undoSupport, eventHelper, vmMgr, view));
	}
}
