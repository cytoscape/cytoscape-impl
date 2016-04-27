package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IndexAndSearchTask extends AbstractNetworkTask {
	
	private static final Logger logger = LoggerFactory.getLogger(IndexAndSearchTask.class);
	
	private final EnhancedSearch enhancedSearch;
	private final String query;
	
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * The constructor. Any necessary data that is <i>not</i> provided by
	 * the user should be provided as arguments to the constructor.
	 */
	public IndexAndSearchTask(final CyNetwork network, final EnhancedSearch enhancedSearch, final String query,
			final CyServiceRegistrar serviceRegistrar) {
		super(network);
		this.enhancedSearch = enhancedSearch;
		this.query = query;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		// Give the task a title.
		taskMonitor.setTitle("Searching the network");

		// Index the given network or use existing index
		RAMDirectory idx;
		final String status = enhancedSearch.getNetworkIndexStatus(network);
		
		if (status != null && status.equalsIgnoreCase(EnhancedSearch.INDEX_SET)
				&& !EnhancedSearchPlugin.attributeChanged) {
			idx = enhancedSearch.getNetworkIndex(network);
		} else {
			taskMonitor.setStatusMessage("Indexing network");
			final EnhancedSearchIndex indexHandler = new EnhancedSearchIndex(network, taskMonitor);
			idx = indexHandler.getIndex();
			enhancedSearch.setNetworkIndex(network, idx);
			EnhancedSearchPlugin.attributeChanged = false;
		}

		if (cancelled)
			return;

		// Execute query
		taskMonitor.setStatusMessage("Executing query");
		EnhancedSearchQuery queryHandler = new EnhancedSearchQuery(network, idx);
		queryHandler.executeQuery(query);

		if (cancelled)
			return;
		
		showResults(queryHandler, taskMonitor);
		updateView();
	}

	/**
	 * If view(s) exists for the current network, update them.
	 */
	private void updateView() {
		final CyNetworkViewManager viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		final Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		CyNetworkView targetView = null;
		
		if (views.size() != 0)
			targetView = views.iterator().next();

		if (targetView != null)
			targetView.updateView();

		final CyNetworkView view = appManager.getCurrentNetworkView();
		
		if (view != null)
			view.updateView();
	}

	// Display results
	private void showResults(final EnhancedSearchQuery queryHandler, final TaskMonitor taskMonitor) {
		if (network == null || network.getNodeList().size() == 0)
			return;

		int nodeHitCount = queryHandler.getNodeHitCount();
		int edgeHitCount = queryHandler.getEdgeHitCount();
		
		if (nodeHitCount == 0 && edgeHitCount == 0) {
			taskMonitor.setStatusMessage("Could not find any match.");
			taskMonitor.setTitle("Search Finished");
			taskMonitor.setProgress(1.0);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane
					.showMessageDialog(null, "Could not find any matches.", "No Match", JOptionPane.WARNING_MESSAGE);
					logger.warn("Could not find any matches.");
				}
			});
			return;
		}

		List<CyNode> nodeList = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		
		for (CyNode n : nodeList)
			network.getRow(n).set(CyNetwork.SELECTED,false);
		
		List<CyEdge> edgeList = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		for (CyEdge e : edgeList)
			network.getRow(e).set(CyNetwork.SELECTED, false);

		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " and " + edgeHitCount + " edges");

		ArrayList<String> nodeHits = queryHandler.getNodeHits();
		ArrayList<String> edgeHits = queryHandler.getEdgeHits();

		final Iterator<String> nodeIt = nodeHits.iterator();
		int numCompleted = 0;

		while (nodeIt.hasNext() && !cancelled) {
			int currESPIndex = Integer.parseInt(nodeIt.next().toString());
			CyNode currNode = network.getNode(currESPIndex);

			if (currNode != null)
				network.getRow(currNode).set(CyNetwork.SELECTED, true);
			else
				logger.warn("Unknown node identifier " + (currESPIndex));

			taskMonitor.setProgress(numCompleted++ / nodeHitCount);
		}

		final Iterator<String> edgeIt = edgeHits.iterator();
		numCompleted = 0;

		while (edgeIt.hasNext() && !cancelled) {
			int currESPIndex = Integer.parseInt(edgeIt.next().toString());
			CyEdge currEdge = network.getEdge(currESPIndex);
			if (currEdge != null)
				network.getRow(currEdge).set(CyNetwork.SELECTED, true);
			else
				logger.warn("Unknown edge identifier " + (currESPIndex));

			taskMonitor.setProgress(++numCompleted / edgeHitCount);
		}
	}
}
