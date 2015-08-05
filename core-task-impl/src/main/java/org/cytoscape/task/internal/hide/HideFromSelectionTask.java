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
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


public class HideFromSelectionTask extends AbstractNetworkViewTask {
	
	private final String description;
	private final boolean hideNodes;
	private final boolean hideEdges;
	private final boolean selectionValue;
	private final CyServiceRegistrar serviceRegistrar;

	public HideFromSelectionTask(
			final String description,
			final boolean hideNodes,
			final boolean hideEdges,
			final boolean selectionValue,
			final CyNetworkView view,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(view);
		this.description = description;
		this.hideNodes = hideNodes;
		this.hideEdges = hideEdges;
		this.selectionValue = selectionValue;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) {
		tm.setProgress(0.0);
		final CyNetwork network = view.getModel();
		final List<CyIdentifiable> elements = new ArrayList<>();
		List<CyNode> nodes = null;
		List<CyEdge> edges = null;
		
		if (hideNodes) {
			nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, selectionValue);
			elements.addAll(nodes);
		}
		
		tm.setProgress(0.1);
		
		if (hideEdges) {
			edges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, selectionValue);
			elements.addAll(edges);
		}
		
		final UndoSupport undoSupport = serviceRegistrar.getService(UndoSupport.class);
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		
		undoSupport.postEdit(new HideEdit(description, view, elements, false, eventHelper, vmMgr));
		tm.setProgress(0.4);
		
		if (nodes != null)
			HideUtils.setVisibleNodes(nodes, false, view);
		
		tm.setProgress(0.6);
		
		if (edges != null)
			HideUtils.setVisibleEdges(edges, false, view);
		
		tm.setProgress(0.8);
		VisualStyle style = vmMgr.getVisualStyle(view);
		
		for (CyIdentifiable e: elements) {
			View<? extends CyIdentifiable> ev = null;
			
			if (e instanceof CyNode)
				ev = view.getNodeView((CyNode)e);
			else if (e instanceof CyEdge)
				ev = view.getEdgeView((CyEdge)e);
			
			if (ev != null)
				style.apply(network.getRow(e), ev);
		}

		view.updateView();
		tm.setProgress(1.0);
	}
}
