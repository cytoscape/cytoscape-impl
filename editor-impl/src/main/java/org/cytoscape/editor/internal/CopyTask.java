package org.cytoscape.editor.internal;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CopyTask extends AbstractTask {
	
	private final CyNetworkView netView;
	private final Set<CyNode> nodes;
	private final Set<CyEdge> edges;
	
	private final ClipboardManagerImpl clipMgr;
	private final CyServiceRegistrar serviceRegistrar;
	
	public CopyTask(CyNetworkView netView, ClipboardManagerImpl clipMgr, CyServiceRegistrar serviceRegistrar) {
		this.netView = netView;
		this.clipMgr = clipMgr;
		this.serviceRegistrar = serviceRegistrar;
		
		// Get all of the selected nodes and edges
		nodes = new HashSet<>(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		edges = new HashSet<>(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
	}

	@SuppressWarnings("unchecked")
	public CopyTask(
			CyNetworkView netView,
			View<? extends CyIdentifiable> objView,
			ClipboardManagerImpl clipMgr,
			CyServiceRegistrar serviceRegistrar
	) {
		this(netView, clipMgr, serviceRegistrar); // Get all of the selected nodes and edges first

		// Now, make sure we add our
		if (objView.getModel() instanceof CyNode)
			nodes.add(((View<CyNode>) objView).getModel());
		else if (objView.getModel() instanceof CyEdge)
			edges.add(((View<CyEdge>) objView).getModel());
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Copy Task");
		
		var annotations = serviceRegistrar.getService(AnnotationManager.class).getSelectedAnnotations(netView);
		
		clipMgr.copy(netView, nodes, edges, annotations);
		tm.setStatusMessage("Copied " + nodes.size() + " node(s) and " + edges.size() + " edge(s) to the clipboard");
	}
}
