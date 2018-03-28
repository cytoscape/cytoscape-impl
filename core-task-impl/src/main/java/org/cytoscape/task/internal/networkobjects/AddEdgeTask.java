package org.cytoscape.task.internal.networkobjects;

import java.util.Arrays;
import java.util.List;

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
import org.cytoscape.command.StringToModel;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class AddEdgeTask extends AbstractTask implements ObservableTask {
	
	CyEdge newEdge;
	CyEventHelper cyEventHelper;
	CyNetworkViewManager networkViewManager;
	VisualMappingManager visualMappingManager;
	CyServiceRegistrar serviceRegistrar;
	
	@Tunable(description="Network", context="nogui", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;

	@Tunable(description="Name of edge source node", 
	         longDescription="Enter the name of an existing node in the network "+
					                 "to be the source of the edge.  Note that this is the "+
													 "name as defined in the 'name' column of the network", 
					 exampleStringValue="Node 1", context="nogui", required=true)
	public String sourceName = null;

	@Tunable(description="Name of edge target node", 
	         longDescription="Enter the name of an existing node in the network "+
					                 "to be the target of the edge.  Note that this is the "+
													 "name as defined in the 'name' column of the network", 
					 exampleStringValue="Node 2", context="nogui", required=true)
	public String targetName = null;

	@Tunable(description="Is the edge directed?", 
	         longDescription="Whether the edge should be directed or not.  Even though "+
					                 "all edges in Cytoscape have a source and target, by default "+
													 "they are treated as undirected.  Setting this to 'true' will "+
													 "flag some algorithms to treat them as directed, although many "+
													 "current implementations will ignore this flag.",
					 exampleStringValue="false", context="nogui")
	public boolean isDirected = false;

	@Tunable(description="Name of the edge to add", 
	         longDescription="Set the 'name' and 'shared name' columns for this edge to the "+
					                 "provided value.",
					 exampleStringValue="Sample Edge",
	         context="nogui")
	public String name = null;

	public AddEdgeTask(VisualMappingManager vmm, CyNetworkViewManager viewManager, CyEventHelper eventHelper, CyServiceRegistrar serviceRegistrar) {
		cyEventHelper = eventHelper;
		networkViewManager = viewManager;
		visualMappingManager = vmm;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) {
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if (network == null) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Network must be specified for add command");
				return;
			}
		}	

		if (sourceName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Source node name must be specified for add command");
			return;
		}	

		if (targetName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Target node name must be specified for add command");
			return;
		}	

		// Find the source and target nodes
		CyNode source = null;
		CyNode target = null;
		for (CyNode node: network.getNodeList()) {
			String nodeName = network.getRow(node).get(CyNetwork.NAME, String.class);
			if (sourceName.equals(nodeName))
				source = node;
			else if (targetName.equals(nodeName))
				target = node;

			if (source != null && target != null)
				break;
		}

		if (source == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Can't find source node named '"+sourceName+"'");
			return;
		}

		if (target == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Can't find target node named '"+targetName+"'");
			return;
		}

		newEdge = network.addEdge(source, target, isDirected);

		if (name != null) {
			network.getRow(newEdge).set(CyNetwork.NAME, name);
			network.getRow(newEdge).set(CyRootNetwork.SHARED_NAME, name);
		}
		cyEventHelper.flushPayloadEvents();
		if (networkViewManager.viewExists(network)) {
			for (CyNetworkView view: networkViewManager.getNetworkViews(network)) {
				View<CyEdge> edgeView = view.getEdgeView(newEdge);
				VisualStyle style = visualMappingManager.getVisualStyle(view);
				if (style != null) {
					style.apply(network.getRow(newEdge), edgeView);
				}
				// Not sure why we need to refresh the view for edges and not nodes, but apparently we do
				view.updateView();
			}
		}
		cyEventHelper.flushPayloadEvents();
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Added edge "+newEdge.toString()+" to network");

	}

	public Object getResults(Class type) {
		if (type.equals(CyEdge.class)) {
			return newEdge;
		} else if (type.equals(String.class)){
			if (newEdge == null)
				return "<none>";
			return newEdge.toString();
		}  else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (newEdge == null) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return "{\"edge\":"+cyJSONUtil.toJson(newEdge)+"}";
			}};
			return res;
		}
		return newEdge;
	}
	
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(CyEdge.class, String.class, JSONResult.class);
	}
}
