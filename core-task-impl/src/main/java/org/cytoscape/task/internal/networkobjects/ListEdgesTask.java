package org.cytoscape.task.internal.networkobjects;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class ListEdgesTask extends AbstractTask implements ObservableTask {

	private final CyServiceRegistrar serviceRegistrar;
	List<CyEdge> edges;
	CyNetwork network;

	@ContainsTunables
	public EdgeTunable edgeTunable;

	public ListEdgesTask(CyServiceRegistrar serviceRegistrar) {
		edgeTunable = new EdgeTunable(serviceRegistrar);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor tm) {
		network = edgeTunable.getNetwork();
		edges = edgeTunable.getEdgeList();

		if (edges == null || edges.size() == 0) {
			tm.showMessage(TaskMonitor.Level.WARN, "No edges found");
			return;
		}

		tm.showMessage(TaskMonitor.Level.INFO, "Found "+edges.size()+" edges");
	}

	@Override
	public Object getResults(Class type) {
		if (type.equals(List.class)) {
			return edges;
		} else if (type.equals(String.class)){
			String res = "";
			for (CyEdge edge: edges) {
				res += edge.toString()+" ["+getName(network, edge)+"]\n";
			}
			return res.substring(0, res.length()-1);
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return "{\"edges\": "+cyJSONUtil.cyIdentifiablesToJson(edges)+"}";
			};
			return res;
		}
		return edges;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);
	}

	String getName(CyNetwork network, CyEdge edge) {
		return network.getRow(edge).get(CyNetwork.NAME, String.class);
	}
}
