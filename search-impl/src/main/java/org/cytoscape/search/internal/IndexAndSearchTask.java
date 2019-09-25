package org.cytoscape.search.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.search.internal.EnhancedSearch.Status;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class IndexAndSearchTask extends AbstractNetworkTask implements ObservableTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	/**
	 * Maximum length in characters a query can be
	 */
	private static final int MAX_QUERY_LEN = 65536;
	
	/**
	 * Time to sleep in milliseconds while waiting for index
	 * building and search query threads to complete
	 */
	private static final long SLEEP_TIME = 1000;
	
	private final EnhancedSearch enhancedSearch;
	private final String query;
	private SearchResults results;

	
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * The constructor. Any necessary data that is <i>not</i> provided by
	 * the user should be provided as arguments to the constructor.
	 */
	public IndexAndSearchTask(CyNetwork network, EnhancedSearch enhancedSearch, String query, CyServiceRegistrar serviceRegistrar) {
		super(network);
		this.enhancedSearch = enhancedSearch;
		this.query = query;
		this.serviceRegistrar = serviceRegistrar;
	}

	
	@Override
	public void run(final TaskMonitor taskMonitor) {
		// Give the task a title.
		taskMonitor.setTitle("Searching the network");

		if (cancelled)
			return;

		if (query.length() > IndexAndSearchTask.MAX_QUERY_LEN) {
			this.results = SearchResults.syntaxError("At " + query.length() + " characters query string is too large");
			logger.error(this.results.getMessage());
			return;
		}

		// Index the given network or use existing index
		RAMDirectory idx = null;
		final Status status = enhancedSearch.getNetworkIndexStatus(network);
		
		if (status != null && status.equals(Status.INDEX_SET) && !EnhancedSearchPlugin.attributeChanged) {
			idx = enhancedSearch.getNetworkIndex(network);
		} else {
			taskMonitor.setStatusMessage("Indexing network");
			long startTime = System.currentTimeMillis();
			idx = this.getIndex(network, taskMonitor);
			if (cancelled)
				return;
			if (idx == null) {
				results = SearchResults.fatalError("Error building index");
				return;
			}
			enhancedSearch.setNetworkIndex(network, idx);
			EnhancedSearchPlugin.attributeChanged = false;
			taskMonitor.setStatusMessage("Indexing completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
		}

		if (cancelled)
			return;

		// Execute query
		taskMonitor.setStatusMessage("Executing query");
		long startTime = System.currentTimeMillis();
		this.executeQuery(network, idx);
		if (cancelled)
			return;

		taskMonitor.setStatusMessage("Executing query completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
		
		if(!results.isError()) {
			showResults(results, taskMonitor);
			updateView();
		}
	}
	
	/**
	 * Runs search under another thread so this task can be easily cancelled
	 * @param network
	 * @param idx
	 */
	private void executeQuery(CyNetwork network, RAMDirectory idx) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<SearchResults> futureTask = executor.submit(new EnhancedSearchQuery(network, idx, query));
		
		try {
			while(futureTask.isDone() == false) {
				if (cancelled) {
					futureTask.cancel(true);
					return;
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch(InterruptedException ie) {
					// do nothing
				}
			}
			results = futureTask.get();
		} catch(InterruptedException ie) {
			results = null;
		} catch(ExecutionException ee) {
			results = null;
		} finally {
			executor.shutdownNow();
		}
	}
	/**
	 * Creates Lucene index under another thread so this task can be easily cancelled
	 * @param network Network to index
	 * @param taskMonitor Monitor used to report progress
	 * @return
	 */
	private RAMDirectory getIndex(CyNetwork network, TaskMonitor taskMonitor) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		Future<RAMDirectory> futureTask = executor.submit(new EnhancedSearchIndex(network, taskMonitor));
		
		try {
			while(futureTask.isDone() == false) {
				if (cancelled) {
					futureTask.cancel(true);
					return null;
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch(InterruptedException ie) {
					// do nothing
				}
			}
			return futureTask.get();
		} catch(InterruptedException ie) {
			return null;
		} catch(ExecutionException ee) {
			return null;
		} finally {
			executor.shutdownNow();
		}
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
	private void showResults(SearchResults results, TaskMonitor taskMonitor) {
		if (network == null || network.getNodeList().size() == 0)
			return;

		int nodeHitCount = results.getNodeHitCount();
		int edgeHitCount = results.getEdgeHitCount();
		
		if (nodeHitCount == 0 && edgeHitCount == 0) {
			taskMonitor.setStatusMessage("Could not find any match.");
			taskMonitor.setTitle("Search Finished");
			taskMonitor.setProgress(1.0);
			return;
		}
		taskMonitor.setStatusMessage("Unsetting any existing network selections");
		long startTime = System.currentTimeMillis();
		List<CyNode> nodeList = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		for (CyNode n : nodeList)
			network.getRow(n).set(CyNetwork.SELECTED,false);
		
		List<CyEdge> edgeList = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		for (CyEdge e : edgeList)
			network.getRow(e).set(CyNetwork.SELECTED, false);
		taskMonitor.setStatusMessage("Unsetting any existing network selections completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " nodes and " + edgeHitCount + " edges");

		List<String> nodeHits = results.getNodeHits();
		List<String> edgeHits = results.getEdgeHits();

		final Iterator<String> nodeIt = nodeHits.iterator();
		int numCompleted = 0;

		startTime = System.currentTimeMillis();
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
		taskMonitor.setStatusMessage("Selecting " + nodeHitCount + " nodes and " + edgeHitCount + " edges completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(SearchResults.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}
}
