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

import java.util.Collection;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;


public class UnHideCommandTask extends AbstractTask {
	
	private CyServiceRegistrar serviceRegistrar;

	@ContainsTunables
	public NodeAndEdgeTunable tunable;

	public UnHideCommandTask(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		tunable = new NodeAndEdgeTunable(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor e) {
		e.setProgress(0.0);

		List<CyEdge> edges = tunable.getEdgeList();
		List<CyNode> nodes = tunable.getNodeList();
		CyNetwork net = tunable.getNetwork();

		if ((edges == null||edges.size() == 0) && (nodes == null||nodes.size() == 0)) {
			e.showMessage(TaskMonitor.Level.ERROR, "Must specify nodes or edges to show");
			return;
		}

		Collection<CyNetworkView> views = serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(net);
		
		if (views == null || views.size() == 0) {
			e.showMessage(TaskMonitor.Level.ERROR, "Network "+net.toString()+" doesn't have a view");
			return;
		}

		// We only handle a single view at this point.  At some point, we'll
		// have to come up with a way to name views...
		int nodeCount = 0;
		int edgeCount = 0;
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		
		for (CyNetworkView view: views) {
			VisualStyle style = vmMgr.getVisualStyle(view);
			
			if (nodes != null) {
				HideUtils.setVisibleNodes(nodes, true, view);
				nodeCount = nodes.size();
				for (CyNode node: nodes) {
					View<CyNode> nodeView = view.getNodeView(node);
					style.apply(net.getRow(node), nodeView);
				}
			}
			if (edges != null) {
				HideUtils.setVisibleEdges(edges, true, view);
				edgeCount = edges.size();
				for (CyEdge edge: edges) {
					View<CyEdge> edgeView = view.getEdgeView(edge);
					style.apply(net.getRow(edge), edgeView);
				}
			}
			
			view.updateView();
		}

		e.showMessage(TaskMonitor.Level.INFO, "Showed "+nodeCount+" nodes and "+edgeCount+" edges");

		e.setProgress(1.0);
	}
}
