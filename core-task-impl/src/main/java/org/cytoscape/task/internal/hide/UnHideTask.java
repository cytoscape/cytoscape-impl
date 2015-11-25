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


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


public class UnHideTask extends AbstractNetworkViewTask {
	
	private final String description;
	private final boolean unhideNodes;
	private final boolean unhideEdges;
	private final CyServiceRegistrar serviceRegistrar;

	public UnHideTask(
			final String description,
			final boolean unhideNodes,
			final boolean unhideEdges,
			final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(view);
		this.description = description;
		this.unhideNodes = unhideNodes;
		this.unhideEdges = unhideEdges;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		
		final CyNetwork network = view.getModel();
		final List<CyIdentifiable> elements = new ArrayList<>();
		List<CyNode> nodes = null;
		List<CyEdge> edges = null;
		e.setProgress(0.1);
		
		if (unhideNodes) {
			nodes = network.getNodeList();
			elements.addAll(nodes);
		}
		
		if (unhideEdges) {
			edges = network.getEdgeList();
			elements.addAll(edges);
		}
		
		e.setProgress(0.2);
		
		final UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		
		undoSupport.postEdit(new HideEdit(description, view, elements, true, eventHelper, vmMgr));
		e.setProgress(0.3);
		
		if (nodes != null)
			HideUtils.setVisibleNodes(nodes, true, view);
		
		e.setProgress(0.5);
		
		if (edges != null)
			HideUtils.setVisibleEdges(edges, true, view);
		
		e.setProgress(0.7);
		
		vmMgr.getVisualStyle(view).apply(view);
		view.updateView();
		e.setProgress(1.0);
	} 
}
