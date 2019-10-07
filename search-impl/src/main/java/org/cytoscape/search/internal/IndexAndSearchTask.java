package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.EnhancedSearch.Status;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexAndSearchTask extends AbstractNetworkTask implements ObservableTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	/**
	 * Maximum length in characters a query can be
	 */
	protected static final int MAX_QUERY_LEN = 65536;
	
	/**
	 * Time to sleep in milliseconds while waiting for index
	 * building and search query threads to complete
	 */
	private static final long SLEEP_TIME = 1000;
	
	private final EnhancedSearch enhancedSearch;
	private final String query;
	private SearchResults results;

	
	private final ViewUpdator viewUpdator;
	private final NodeAndEdgeSelector selector;
	private final EnhancedSearchQueryFactory queryFactory;
	private final EnhancedSearchIndexFactory indexFactory;

	/**
	 * The constructor. Any necessary data that is <i>not</i> provided by
	 * the user should be provided as arguments to the constructor.
	 */
	public IndexAndSearchTask(CyNetwork network, EnhancedSearch enhancedSearch, final String query,
			ViewUpdator viewUpdator, NodeAndEdgeSelector selector, EnhancedSearchQueryFactory queryFactory,
			EnhancedSearchIndexFactory indexFactory) {
		super(network);
		this.enhancedSearch = enhancedSearch;
		this.query = query;
		this.viewUpdator = viewUpdator;
		this.selector = selector;
		this.queryFactory = queryFactory;
		this.indexFactory = indexFactory;
	}

	/**
	 * Lets caller know if task has been cancelled
	 * @return true if task has been cancelled, false otherwise.
	 */
	protected boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void run(final TaskMonitor taskMonitor) {
		// Give the task a title.
		taskMonitor.setTitle("Searching the network");
		
		if (cancelled)
			return;

		// bail if the length of query string is too long
		if (query.length() > IndexAndSearchTask.MAX_QUERY_LEN) {
			this.results = SearchResults.syntaxError("At " + query.length() + " characters query string is too large");
			logger.error(this.results.getMessage());
			return;
		}
		
		// put long running tasks in another thread so we can continue to monitor if 
		// user called cancel
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			// Index the given network or use existing index
			RAMDirectory idx = null;
			final Status status = enhancedSearch.getNetworkIndexStatus(network);
			
			if (status != null && status.equals(Status.INDEX_SET) && !EnhancedSearchPlugin.attributeChanged) {
				// use existing index
				idx = enhancedSearch.getNetworkIndex(network);
			} else {
				taskMonitor.setStatusMessage("Indexing network");
				long startTime = System.currentTimeMillis();
				// create a new index
				idx = this.getIndex(executor, network, taskMonitor);
				if (idx == null) {
					results = SearchResults.fatalError("Error building index");
					return;
				}
				enhancedSearch.setNetworkIndex(network, idx);
				
				// set attributeChanged to false so in future we know index is up to date
				EnhancedSearchPlugin.attributeChanged = false;
				taskMonitor.setStatusMessage("Indexing completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
			}
	
			// Execute query
			taskMonitor.setStatusMessage("Executing query");
			long startTime = System.currentTimeMillis();
			this.executeQuery(executor, network, idx);
			if (cancelled)
				return;
	
			taskMonitor.setStatusMessage("Executing query completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
			
			if (results == null) {
				results = SearchResults.fatalError("Unknown error");
			}
	
			if(!results.isError()) {
				selector.selectNodesAndEdges(network, results, this, taskMonitor);
				taskMonitor.setStatusMessage("Updating any network views");

				startTime = System.currentTimeMillis();
				viewUpdator.updateView(network);
				taskMonitor.setStatusMessage("Updating any network views completed in " + Long.toString(System.currentTimeMillis() - startTime) + " ms");
			}
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Runs search under another thread so this task can be easily cancelled
	 * @param network
	 * @param idx
	 */
	private void executeQuery(ExecutorService executor, CyNetwork network, RAMDirectory idx) {
		Future<SearchResults> futureTask = executor.submit(queryFactory.getEnhancedSearchQuery(network, idx, query));
		
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
			results = SearchResults.fatalError("Query interrupted");
		} catch(ExecutionException ee) {
			results = SearchResults.fatalError("Error running query: " + ee.getMessage());
		} 
	}
	/**
	 * Creates Lucene index under another thread so this task can be easily cancelled
	 * @param network Network to index
	 * @param taskMonitor Monitor used to report progress
	 * @return
	 */
	private RAMDirectory getIndex(ExecutorService executor, CyNetwork network, TaskMonitor taskMonitor) {
		Future<RAMDirectory> futureTask = executor.submit(indexFactory.getEnhancedSearchIndex(network, taskMonitor));
		
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
			logger.error(ee.getMessage(), ee);
			return null;
		} 
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(SearchResults.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}
}
