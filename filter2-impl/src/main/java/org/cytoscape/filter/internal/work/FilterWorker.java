package org.cytoscape.filter.internal.work;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.MemoizableTransformer;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class FilterWorker extends AbstractWorker<FilterPanel, FilterPanelController> {
	
	public FilterWorker(LazyWorkQueue queue, final CyServiceRegistrar serviceRegistrar) {
		super(queue, serviceRegistrar);
	}

	@Override
	public void doWork(ProgressMonitor monitor) {
		if (controller == null) {
			return;
		}
		
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		CyNetwork network;
		
		if (networkView != null) {
			network = networkView.getModel();
		} else {
			network = applicationManager.getCurrentNetwork();
		}
		
		if (network == null) {
			return;
		}
		
		monitor.setProgress(0);
		monitor.setStatusMessage(null);
		int nodeCount = 0;
		int edgeCount = 0;
		int counter = 0;
		long startTime = System.currentTimeMillis();
		try {
			Filter<CyNetwork, CyIdentifiable> filter = controller.getFilter();
			if (filter instanceof CompositeFilter) {
				// If we have an empty CompositeFilter, bail out. 
				CompositeFilter<CyNetwork, CyIdentifiable> composite = (CompositeFilter<CyNetwork, CyIdentifiable>) filter;
				if (composite.getLength() == 0) {
					return;
				}
			}
			
			if (filter instanceof MemoizableTransformer) {
				((MemoizableTransformer) filter).startCaching();
			}
			try {
				List<CyNode> nodeList = network.getNodeList();
				List<CyEdge> edgeList = network.getEdgeList();
				double total = nodeList.size() + edgeList.size();
				
				List<CyNode> selectedNodes = new ArrayList<>();
				List<CyNode> unselectedNodes = new ArrayList<>();
				List<CyEdge> selectedEdges = new ArrayList<>();
				List<CyEdge> unselectedEdges = new ArrayList<>();
				
				// First run the filter, then do the selection after.
				// The filter.accepts() method can be slow, if we update the selection 
				// as we go that can cause excessive RowsSetEvents to be fired.
				
				for (CyNode node : nodeList) {
					if (monitor.isCancelled()) {
						return;
					}
					boolean accepted = filter.accepts(network, node);
					if (accepted) {
						selectedNodes.add(node);
						nodeCount++;
					} else {
						unselectedNodes.add(node);
					}
					monitor.setProgress(++counter / total);
				}
				for (CyEdge edge : edgeList) {
					if (monitor.isCancelled()) {
						return;
					}
					boolean accepted = filter.accepts(network, edge);
					if (accepted) {
						selectedEdges.add(edge);
						edgeCount++;
					} else {
						unselectedEdges.add(edge);
					}
					monitor.setProgress(++counter / total);
				}
				
				// now do the selection
				if(applyAction == ApplyAction.FILTER) {
					filter(networkView, selectedNodes, unselectedNodes, selectedEdges, unselectedEdges);
				} else {
					select(network, selectedNodes, unselectedNodes, selectedEdges, unselectedEdges);
				}
				
			}
			finally {
				if (filter instanceof MemoizableTransformer) {
					((MemoizableTransformer) filter).clearCache();
				}
			}
			
			if (networkView != null) {
				networkView.updateView();
			}
		} finally {
			long duration = System.currentTimeMillis() - startTime;
			monitor.setProgress(1.0);
			monitor.setStatusMessage(String.format("Selected %d %s and %d %s in %dms",
					nodeCount,
					nodeCount == 1 ? "node" : "nodes",
					edgeCount,
					edgeCount == 1 ? "edge" : "edges",
					duration));
		}
	}
	
	
	private void filter(CyNetworkView networkView, List<CyNode> selectedNodes, List<CyNode> unselectedNodes, List<CyEdge> selectedEdges, List<CyEdge> unselectedEdges) {
		HideTaskFactory hideFactory = serviceRegistrar.getService(HideTaskFactory.class);
		TaskIterator hideTasks = hideFactory.createTaskIterator(networkView, unselectedNodes, unselectedEdges);
		
		UnHideTaskFactory unhideFactory = serviceRegistrar.getService(UnHideTaskFactory.class);
		TaskIterator unhideTasks = unhideFactory.createTaskIterator(networkView, selectedNodes, selectedEdges);
		
		TaskIterator taskIterator = new TaskIterator();
		taskIterator.append(hideTasks);
		taskIterator.append(unhideTasks);
		
		SynchronousTaskManager<?> taskManager = serviceRegistrar.getService(SynchronousTaskManager.class);
		taskManager.execute(taskIterator);
	}
	
	
	private void select(CyNetwork network, List<CyNode> selectedNodes, List<CyNode> unselectedNodes, List<CyEdge> selectedEdges, List<CyEdge> unselectedEdges) {
		// now do the selection
		for(CyNode element : unselectedNodes) {
			CyRow row = network.getRow(element);
			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.FALSE);
			}
		}
		for(CyEdge element : unselectedEdges) {
			CyRow row = network.getRow(element);
			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.FALSE);
			}
		}
		for(CyNode element : selectedNodes) {
			CyRow row = network.getRow(element);
			if (!row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			}
		}
		for(CyEdge element : selectedEdges) {
			CyRow row = network.getRow(element);
			if (!row.get(CyNetwork.SELECTED, Boolean.class)) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			}
		}
	}
}
