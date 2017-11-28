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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

public class HideCommandTask extends AbstractTask implements ObservableTask {

	private CyServiceRegistrar serviceRegistrar;
	private List<CyEdge> edges;
	private List<CyNode> nodes;
	private CyNetwork network;

	@ContainsTunables
	public NodeAndEdgeTunable tunable;

	public HideCommandTask(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		tunable = new NodeAndEdgeTunable(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor e) {
		e.setProgress(0.0);

		edges = tunable.getEdgeList();
		nodes = tunable.getNodeList();
		network = tunable.getNetwork();

		if ((edges == null||edges.size() == 0) && (nodes == null||nodes.size() == 0)) {
			e.showMessage(TaskMonitor.Level.ERROR, "Must specify nodes or edges to hide");
			return;
		}

		Collection<CyNetworkView> views = 
						serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(network);

		if (views == null || views.size() == 0) {
			e.showMessage(TaskMonitor.Level.ERROR, "Network "+network.toString()+" doesn't have a view");
			return;
		}

		// We only handle a single view at this point.  At some point, we'll
		// have to come up with a way to name views...
		int nodeCount = 0;
		int edgeCount = 0;
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);

		for (CyNetworkView view: views) {
			if (nodes != null) {
				HideUtils.setVisibleNodes(nodes, false, view);
				nodeCount = nodes.size();
			}
			if (edges != null) {
				HideUtils.setVisibleEdges(edges, false, view);
				edgeCount = edges.size();
			}
			vmMgr.getVisualStyle(view).apply(view);
			view.updateView();
		}

		e.showMessage(TaskMonitor.Level.INFO, "Hid "+nodeCount+" nodes and "+edgeCount+" edges");

		e.setProgress(1.0);
	}

	public Object getResults(Class type) {
		List<CyIdentifiable> identifiables = new ArrayList<>();
		if (nodes != null)
			identifiables.addAll(nodes);
		if (edges != null)
			identifiables.addAll(edges);
		if (type.equals(List.class)) {
			return identifiables;
		} else if (type.equals(String.class)){
			if (identifiables.size() == 0)
				return "<none>";
			String ret = "";
			if (nodes != null && nodes.size() > 0) {
				ret += "Nodes hidden: \n";
				for (CyNode node: nodes) {
					ret += "   "+network.getRow(node).get(CyNetwork.NAME, String.class)+"\n";
				}
			}
			if (edges != null && edges.size() > 0) {
				ret += "Edges hidden: \n";
				for (CyEdge edge: edges) {
					ret += "   "+network.getRow(edge).get(CyNetwork.NAME, String.class)+"\n";
				}
			}
			return ret;
		}  else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {if (identifiables == null || identifiables.size() == 0) 
				return "{}";
			else {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				String result = "{\"nodes\":";
				if (nodes == null || nodes.size() == 0)
					result += "[]";
				else
					result += cyJSONUtil.cyIdentifiablesToJson(nodes);

				result += ", \"edges\":";
				if (edges == null || edges.size() == 0)
					result += "[]";
				else
					result += cyJSONUtil.cyIdentifiablesToJson(edges);

				result += "}";
				return result;
			}};
			return res;
		}
		return identifiables;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, JSONResult.class);
	}

}
