package org.cytoscape.view.layout.internal.task;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class LayoutWrapperTask extends AbstractTask {
	private final CyApplicationManager appMgr;
	private final CyNetworkViewManager viewMgr;
	List<CyNode> nodes = null;
	private final CyLayoutAlgorithm algorithm;
	
	@Tunable(description="Network to layout", context="nogui")
	public CyNetwork network = null;

	public NodeList nodeList = new NodeList(null);
	@Tunable(description="Nodes to layout", context="nogui")
	public NodeList getnodeList() {
		if (network == null)
			network = appMgr.getCurrentNetwork();
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}

	@ContainsTunables
	public Object layoutContext;

	public LayoutWrapperTask(CyApplicationManager appMgr, CyNetworkViewManager viewMgr, CyLayoutAlgorithm alg) {
		this.appMgr = appMgr;
		this.viewMgr = viewMgr;
		this.algorithm = alg;
		layoutContext = alg.getDefaultLayoutContext();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null)
			network = appMgr.getCurrentNetwork();
		Collection<CyNetworkView> views = viewMgr.getNetworkViews(network);

		nodes = nodeList.getValue();

		for (CyNetworkView view: views) {
			Set<View<CyNode>> nodeViews = new HashSet<View<CyNode>>();
			if (nodes == null || nodes.size() == 0) {
				nodeViews = CyLayoutAlgorithm.ALL_NODE_VIEWS;
			}	else {
				for (CyNode node: nodes) 
					nodeViews.add(view.getNodeView(node));
			} 
	
			insertTasksAfterCurrentTask(algorithm.createTaskIterator(view, layoutContext, nodeViews,""));
		}

	}

}
