package org.cytoscape.search.internal.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkSearchTask extends AbstractTask implements ObservableTask {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	/**
	 * Maximum length in characters a query can be
	 */
	protected static final int MAX_QUERY_LEN = 65536;
	
	private final CyNetwork network;
	private final String queryString;
	private final SearchManager searchManager;
	
	private SearchResults results;
	
	public NetworkSearchTask(SearchManager searchManager, CyNetwork network, String queryString) {
		this.network = Objects.requireNonNull(network);
		this.queryString = Objects.requireNonNull(queryString);
		this.searchManager = Objects.requireNonNull(searchManager);
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		this.results = runQuery(tm);
		insertTasksAfterCurrentTask(new NodeAndEdgeSelectorTask(network, results));
	}

	
	public SearchResults runQuery(TaskMonitor tm) {
		tm.setTitle("Searching the network for: " + queryString);
		
		if(queryString.length() > MAX_QUERY_LEN) {
			logger.error(results.getMessage());
			return SearchResults.syntaxError("At " + queryString.length() + " characters query string is too large");
		}
		if(queryString.isBlank()) {
			return SearchResults.empty();
		}
		if(cancelled) {
			return SearchResults.cancelled();
		}
		
		tm.setProgress(1.0);
		var nodeTable = network.getDefaultNodeTable();
		var edgeTable = network.getDefaultEdgeTable();
		
		tm.setProgress(0.2);
		boolean first = true;
		while(!searchManager.isReady(nodeTable, edgeTable)) {
			if(first) {
				tm.setStatusMessage("Waiting for network index to be ready.");
				first = false;
			}
			try {
				Thread.sleep(300);
			} catch(InterruptedException e) {}
			
			if(cancelled) {
				return SearchResults.cancelled();
			}
		}
		
		tm.setStatusMessage("Running search query.");
		tm.setProgress(0.3);
		
		Query nodeQuery, edgeQuery;
		try {
			QueryParser nodeParser = searchManager.getQueryParser(nodeTable);
			nodeQuery = new ConstantScoreQuery(nodeParser.parse(queryString));
			
			QueryParser edgeParser = searchManager.getQueryParser(edgeTable);
			edgeQuery = new ConstantScoreQuery(edgeParser.parse(queryString));
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.syntaxError();
		}
		
		tm.setProgress(0.5);
		
		IndexReader nodeReader = null, edgeReader = null;
		try {
			nodeReader = searchManager.getIndexReader(nodeTable);
			edgeReader = searchManager.getIndexReader(edgeTable);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.fatalError();
		}
		
		tm.setProgress(0.6);
		
		List<String> nodeIDs;
		try {
			nodeIDs = runSearch(nodeReader, nodeQuery);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.fatalError();
		}
		
		tm.setProgress(0.8);
		
		List<String> edgeIDs;
		try {
			edgeIDs = runSearch(edgeReader, edgeQuery);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.fatalError();
		}
		
		tm.setProgress(0.9);
		
		try {
			nodeReader.close();
			edgeReader.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		if(cancelled) {
			return SearchResults.cancelled();
		}
		
		tm.setProgress(1.0);
		return SearchResults.results(nodeIDs, edgeIDs);
	}

	
	private static List<String> runSearch(IndexReader reader, Query query) throws IOException {
		var searcher = new IndexSearcher(reader);
		TopDocs docs = searcher.search(query, 10_000_000);
		var ids = new ArrayList<String>(docs.scoreDocs.length);
		for(var sd : docs.scoreDocs) {
			Document doc = reader.document(sd.doc);
			String eleID = doc.get(SearchManager.INDEX_FIELD);
			ids.add(eleID);
		}
		return ids;
	}
	
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(SearchResults.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}

}

