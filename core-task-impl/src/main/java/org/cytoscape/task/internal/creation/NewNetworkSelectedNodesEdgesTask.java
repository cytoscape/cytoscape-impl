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
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.UndoSupport;


public class NewNetworkSelectedNodesEdgesTask extends AbstractNetworkFromSelectionTask {
	
	private Set<CyNode> nodes;
	private Set<CyEdge> edges;
	
	public NewNetworkSelectedNodesEdgesTask(final UndoSupport undoSupport, final CyNetwork net,
	                                        final CyRootNetworkManager cyroot,
	                                        final CyNetworkViewFactory cnvf,
	                                        final CyNetworkManager netmgr,
	                                        final CyNetworkViewManager networkViewManager,
	                                        final CyNetworkNaming cyNetworkNaming,
	                                        final VisualMappingManager vmm,
	                                        final CyApplicationManager appManager,
	                                        final CyEventHelper eventHelper,
	                                        final CyGroupManager groupMgr,
	                                        final RenderingEngineManager renderingEngineMgr) {
		super(undoSupport, net, cyroot, cnvf, netmgr, networkViewManager, cyNetworkNaming,
		      vmm, appManager, eventHelper, groupMgr, renderingEngineMgr);
	}

	/**
	 * Returns the selected nodes plus all nodes that connect the selected edges
	 */
	@Override
	Set<CyNode> getNodes(final CyNetwork net) {
		if (nodes == null) {
			nodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true));
			final Collection<CyEdge> selectedEdges = getEdges(net);
			
			for (final CyEdge e : selectedEdges) {
				nodes.add(e.getSource());
				nodes.add(e.getTarget());
			}
		}
		
		return nodes;
	}
	
	/**
	 * Returns the selected edges.
	 */
	@Override
	Set<CyEdge> getEdges(final CyNetwork net) {
		if (edges == null) {
			edges = new HashSet<CyEdge>(CyTableUtil.getEdgesInState(net, CyNetwork.SELECTED, true));
		}
		
		return edges;
	}
}
