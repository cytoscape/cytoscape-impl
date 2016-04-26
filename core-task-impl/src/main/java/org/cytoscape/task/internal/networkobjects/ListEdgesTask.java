package org.cytoscape.task.internal.networkobjects;

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
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import org.cytoscape.task.internal.utils.EdgeTunable;

public class ListEdgesTask extends AbstractTask implements ObservableTask {
	private final CyApplicationManager appMgr;
	List<CyEdge> edges = null;
	CyNetwork network = null;

	@ContainsTunables
	public EdgeTunable edgeTunable;

	public ListEdgesTask(CyApplicationManager appMgr) {
		this.appMgr = appMgr;
		edgeTunable = new EdgeTunable(appMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		network = edgeTunable.getNetwork();
		edges = edgeTunable.getEdgeList();

		if (edges == null || edges.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.WARN, "No edges found");
			return;
		}

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Found "+edges.size()+" edges");
	}

	public Object getResults(Class type) {
		if (type.equals(List.class)) {
			return edges;
		} else if (type.equals(String.class)){
			String res = "";
			for (CyEdge edge: edges) {
				res += edge.toString()+" ["+getName(network, edge)+"]\n";
			}
			return res.substring(0, res.length()-1);
		}
		return edges;
	}

	String getName(CyNetwork network, CyEdge edge) {
		return network.getRow(edge).get(CyNetwork.NAME, String.class);
	}
}
