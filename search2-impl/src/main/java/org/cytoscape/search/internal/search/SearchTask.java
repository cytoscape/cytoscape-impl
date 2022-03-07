package org.cytoscape.search.internal.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyTable;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchTask extends AbstractTask implements ObservableTask {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	/**
	 * Maximum length in characters a query can be
	 */
	protected static final int MAX_QUERY_LEN = 65536;
	
	private final String queryString;
	private final SearchManager searchManager;
	private final CyTable[] tables;
	
	private SearchResults results;
	
	public SearchTask(SearchManager searchManager, String queryString, CyTable ... tables) {
		this.queryString = Objects.requireNonNull(queryString);
		this.searchManager = Objects.requireNonNull(searchManager);
		this.tables = tables;
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		runQuery(tm);
	}

	
	public SearchResults runQuery(TaskMonitor tm) {
		tm.setTitle("Searching the network for: " + queryString);
		
		if(queryString.length() > MAX_QUERY_LEN) {
			logger.error(results.getErrorMessage());
			return SearchResults.syntaxError("At " + queryString.length() + " characters query string is too large");
		}
		if(queryString.isBlank()) {
			return SearchResults.empty();
		}
		
		tm.setProgress(1.0);
		
		// TODO calculate progress
		SearchResults results = SearchResults.empty();
		for(CyTable table : tables) {
			if(cancelled) {
				return SearchResults.cancelled();
			}
			
			SearchResults tableResults = searchTable(tm, table);
			if(tableResults.isError()) {
				return tableResults;
			}
			
			results = results.compose(tableResults);
		}

		if(cancelled) {
			return SearchResults.cancelled();
		}
		
		tm.setProgress(1.0);
		
		return this.results = results;
	}

	
	private SearchResults searchTable(TaskMonitor tm, CyTable table) { 
		boolean first = true;
		while(!searchManager.isReady(table)) {
			if(first) {
				tm.setStatusMessage("Waiting for index to be ready.");
				first = false;
			}
			if(cancelled) {
				return SearchResults.cancelled();
			}
		}
		
		tm.setStatusMessage("Running search query on table " + table.getTitle());
		
		Query query;
		try {
			query = searchManager.getQueryParser(table).parse(queryString);
			System.out.println(query);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.syntaxError();
		}
		
		IndexReader reader = null;
		try {
			reader = searchManager.getIndexReader(table);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.fatalError();
		}
		
		List<String> ids;
		try {
			ids = runSearch(reader, query);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.fatalError();
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		return SearchResults.results(table, ids);
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

