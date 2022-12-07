package org.cytoscape.task.internal.edit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class ConnectSelectedNodesTask extends AbstractTask implements ObservableTask {

	static final String DEFAULT_INTERACTION = "interacts with";

	private final CyServiceRegistrar serviceRegistrar;
	private List<CyEdge> newEdges;

	private CyNetwork network;
	@Tunable(description="The network containing the nodes to connect",
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         context="nogui", required=true)
	public CyNetwork getnetwork() {
		return network;
	}
	public void setnetwork(CyNetwork net) {
		network = net;
		if (nodes == null)
			nodes = new NodeList(net);
		else
			nodes.setNetwork(net);
	}

	@Tunable(description="The list of nodes to connect",
	         longDescription=StringToModel.CY_NODE_LIST_LONG_DESCRIPTION,
			 exampleStringValue=StringToModel.CY_NODE_LIST_EXAMPLE_STRING,
	         context="nogui", required=true)
	public NodeList nodes = null;

	public ConnectSelectedNodesTask(CyNetwork network, CyServiceRegistrar serviceRegistrar) {
		this.network = network;
		this.serviceRegistrar = serviceRegistrar;
		nodes = new NodeList();
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		tm.setTitle("Connecting Selected Nodes");
		tm.setStatusMessage("Connecting nodes.  Please wait...");
		
		if (network == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
			return;
		}
		
		final List<CyNode> selectedNodes;
		
		if (nodes.getValue() == null || nodes.getValue().isEmpty())
			selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		else
			selectedNodes = nodes.getValue();

		tm.setProgress(0.1);

		int selectedNodesCount = selectedNodes.size();
		int i = 0;

		newEdges = new ArrayList<>();
		
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		var networkViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		var visualMappingManager = serviceRegistrar.getService(VisualMappingManager.class);
		
		eventHelper.silenceEventSource(network);
		
		for (final CyNode source : selectedNodes) {
			for (final CyNode target : selectedNodes) {
				if (source != target) {
					final List<CyNode> sourceNeighborList = network.getNeighborList(source, Type.ANY);

					if (!sourceNeighborList.contains(target)) {
						// connect it
						final CyEdge newEdge = network.addEdge(source, target, false);
						newEdges.add(newEdge);
						String name = network.getRow(source).get(CyNetwork.NAME, String.class) + " (" + DEFAULT_INTERACTION + ") "
										+ network.getRow(target).get(CyNetwork.NAME, String.class);
						network.getRow(newEdge).set(CyNetwork.NAME, name);
						// System.out.println("Added edge "+name);
						network.getRow(newEdge).set(CyEdge.INTERACTION, DEFAULT_INTERACTION);
					}
				}
			}

			i++;
			tm.setProgress(0.1 + i / (double) selectedNodesCount * 0.9);
		}
		
		eventHelper.unsilenceEventSource(network);

		serviceRegistrar.getService(UndoSupport.class).postEdit(new ConnectSelectedNodesEdit(network, newEdges));

		for (CyEdge edge: newEdges)
			eventHelper.addEventPayload(network, edge, AddedEdgesEvent.class); 
		
		eventHelper.flushPayloadEvents(); // To make sure the edge views are created before applying the style

		// Apply visual style
		for (var view : networkViewManager.getNetworkViews(network)) {
			VisualStyle vs = visualMappingManager.getVisualStyle(view);
			vs.apply(view);
			view.updateView();
		}

		tm.setProgress(1.0);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getResults(Class type) {
		if (type.equals(List.class)) {
			return newEdges;
		} else if (type.equals(String.class)) {
			if (newEdges == null || newEdges.isEmpty())
				return "<none>";
			
			String ret = "";
			
			if (newEdges != null && newEdges.size() > 0) {
				ret += "Edges added: \n";
				
				for (CyEdge edge : newEdges)
					ret += "   " + network.getRow(edge).get(CyNetwork.NAME, String.class) + "\n";
			}
			
			return ret;
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (newEdges == null || newEdges.size() == 0)
					return "{}";
				else {
					CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
					return "{\"edges\":" + cyJSONUtil.cyIdentifiablesToJson(newEdges) + "}";
				}
			};
			
			return res;
		}
		
		return newEdges;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}
}
