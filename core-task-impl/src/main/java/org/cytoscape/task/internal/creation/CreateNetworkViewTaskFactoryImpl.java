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

import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkCollectionTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class CreateNetworkViewTaskFactoryImpl extends AbstractNetworkCollectionTaskFactory implements
		CreateNetworkViewTaskFactory {

	private final UndoSupport undoSupport;
	private final CyNetworkViewManager netViewMgr;
	private final CyNetworkViewFactory viewFactory;
	private final CyLayoutAlgorithmManager layoutMgr;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final RenderingEngineManager renderingEngineMgr;

	public CreateNetworkViewTaskFactoryImpl(final UndoSupport undoSupport, final CyNetworkViewFactory viewFactory,
			final CyNetworkViewManager netViewMgr, final CyLayoutAlgorithmManager layoutMgr,
			final CyEventHelper eventHelper, final VisualMappingManager vmm,
			final RenderingEngineManager renderingEngineMgr) {
		this.undoSupport = undoSupport;
		this.viewFactory = viewFactory;
		this.netViewMgr = netViewMgr;
		this.layoutMgr = layoutMgr;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.renderingEngineMgr = renderingEngineMgr;
	}

	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetwork> networks) {
		// Create visualization + layout (optional)
		if (layoutMgr == null)
			return new TaskIterator(1, new CreateNetworkViewTask(undoSupport, networks, viewFactory, netViewMgr,
					layoutMgr, eventHelper, vmm, renderingEngineMgr));
		else
			return new TaskIterator(2, new CreateNetworkViewTask(undoSupport, networks, viewFactory, netViewMgr,
					layoutMgr, eventHelper, vmm, renderingEngineMgr));
	}

	@Override
	public boolean isReady(final Collection<CyNetwork> networks) {
		for (CyNetwork n : networks)
			if (!netViewMgr.getNetworkViews(n).isEmpty())
				return false;

		return true;
	}

}
