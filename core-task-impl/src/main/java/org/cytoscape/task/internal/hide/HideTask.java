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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import org.cytoscape.task.internal.utils.NodeAndEdgeTunable;


public class HideTask extends AbstractTask {
	private CyApplicationManager appMgr;
	private CyNetworkViewManager viewMgr;
	private VisualMappingManager vmMgr;

	@ContainsTunables
	public NodeAndEdgeTunable tunable;

	public HideTask(final CyApplicationManager appMgr, final CyNetworkViewManager viewManager,
	                final VisualMappingManager vmMgr) {
		super();
		this.vmMgr = vmMgr;
		this.viewMgr = viewManager;
		this.appMgr = appMgr;
		tunable = new NodeAndEdgeTunable(appMgr);
	}

	public void run(TaskMonitor e) {
		e.setProgress(0.0);

		List<CyEdge> edges = tunable.getEdgeList();
		List<CyNode> nodes = tunable.getNodeList();
		CyNetwork net = tunable.getNetwork();

		if ((edges == null||edges.size() == 0) && (nodes == null||nodes.size() == 0)) {
			e.showMessage(TaskMonitor.Level.ERROR, "Must specify nodes or edges to hide");
			return;
		}

		Collection<CyNetworkView> views = viewMgr.getNetworkViews(net);
		if (views == null || views.size() == 0) {
			e.showMessage(TaskMonitor.Level.ERROR, "Network "+net.toString()+" doesn't have a view");
			return;
		}

		// We only handle a single view at this point.  At some point, we'll
		// have to come up with a way to name views...
		int nodeCount = 0;
		int edgeCount = 0;
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
		
/*
		final CyNetwork network = view.getModel();
		undoSupport.postEdit(new HideEdit(eventHelper, "Hide Selected Nodes & Edges", network, view));
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		e.setProgress(0.2);
		
		final List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		e.setProgress(0.4);
		
		HideUtils.setVisibleNodes(selectedNodes, false, view);
		e.setProgress(0.6);
		
		HideUtils.setVisibleEdges(selectedEdges, false, view);
		e.setProgress(0.8);
		
		vmMgr.getVisualStyle(view).apply(view);
		view.updateView();
*/
		e.setProgress(1.0);
	}
}
