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
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
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
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;


public class NewNetworkCommandTask extends AbstractNetworkFromSelectionTask {
	
	private Set<CyNode> nodes;
	private Set<CyEdge> edges;

	@Tunable(description="Name of new network", gravity=1.0, context="nogui")
	public String networkName = null;

	@Tunable(description="Source network", gravity=2.0, context="nogui")
	public CyNetwork getsource() {
		return parentNetwork;
	}
	public void setsource(CyNetwork network) { parentNetwork = network; }

	public NodeList nodeList = new NodeList(null);

	@Tunable(description="List of nodes for new network", gravity=3.0, context="nogui")
	public NodeList getnodeList() {
		nodeList.setNetwork(parentNetwork);
		return nodeList;
	}
  public void setnodeList(NodeList setValue) {}

	public EdgeList edgeList = new EdgeList(null);
	@Tunable(description="List of edges for new network", gravity=4.0, context="nogui")
	public EdgeList getedgeList() {
		edgeList.setNetwork(parentNetwork);
		return edgeList;
	}
  public void setedgeList(EdgeList setValue) {}

	@Tunable(description="Exclude connecting edges", gravity=5.0, context="nogui")
	public boolean excludeEdges = false;
	
	public NewNetworkCommandTask(final UndoSupport undoSupport, 
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
		super(undoSupport, null, cyroot, cnvf, netmgr, networkViewManager, cyNetworkNaming,
		      vmm, appManager, eventHelper, groupMgr, renderingEngineMgr);
	}

	/**
	 * Returns the selected nodes plus all nodes that connect the selected edges
	 */
	@Override
	Set<CyNode> getNodes(final CyNetwork net) {
		if (nodes == null) {
			nodes = new HashSet<>(nodeList.getValue());

			if (edgeList != null && edgeList.getValue() != null) {
				final Collection<CyEdge> selectedEdges = edgeList.getValue();
			
				for (final CyEdge e : selectedEdges) {
					nodes.add(e.getSource());
					nodes.add(e.getTarget());
				}
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
			if (edgeList != null && edgeList.getValue() != null)
				edges = new HashSet<>(edgeList.getValue());
			else
				edges = new HashSet<>();
		}

		if (!excludeEdges) {
			List<CyNode> nList = nodeList.getValue();
	
			for (final CyNode n1 : nList) {
				for (final CyNode n2 : nList)
					edges.addAll(net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
			}
		}
		
		return edges;
	}

	/**
 	 * Returns the name of the network if the user gave us one
 	 */
	@Override
	String getNetworkName() {
		if (networkName != null)
			return networkName;
		return super.getNetworkName();
	}
}
