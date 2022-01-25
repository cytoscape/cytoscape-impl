package org.cytoscape.search.internal.search;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.index.CaseInsensitiveWhitespaceAnalyzer;
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
	
	private final CyNetwork network;
	private final String queryString;
	private final SearchManager searchManager;
	
	private SearchResults results;
	
	public SearchTask(SearchManager searchManager, CyNetwork network, String queryString) {
		this.network = Objects.requireNonNull(network);
		this.queryString = Objects.requireNonNull(queryString);
		this.searchManager = Objects.requireNonNull(searchManager);
	}
	
	
	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Searching the network for: " + queryString);
		if (cancelled)
			return;
		
		// bail if the length of query string is too long
		if (queryString.length() > MAX_QUERY_LEN) {
			this.results = SearchResults.syntaxError("At " + queryString.length() + " characters query string is too large");
			logger.error(results.getMessage());
			return;
		}
		
		Analyzer analyser = new CaseInsensitiveWhitespaceAnalyzer();
		AttributeFields fields = new AttributeFields(network);
		System.out.println("Querying fields: " + Arrays.asList(fields.getFields()));
		QueryParser parser = new MultiFieldQueryParser(fields.getFields(), analyser);
		
		Query query;
		try {
			 query = parser.parse(queryString);
		} catch (ParseException e) {
			results = SearchResults.syntaxError();
			logger.error(e.getMessage(), e);
			return;
		}
		
		System.out.println("Query: " + query);
		
		Path path = searchManager.getIndexPath(network);
		IndexReader reader;
		try {
			FSDirectory directory = FSDirectory.open(path);
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			this.results = SearchResults.fatalError();
			logger.error(e.getMessage(), e);
			return;
		}
		
		IndexSearcher searcher = new IndexSearcher(reader);
		var collector = new TotalHitCountCollector();
		try {
//			searcher.search(query, collector);
			var docs = searcher.search(query, 10);
			System.out.println("Search complete: total hits: " + docs.totalHits);
		} catch (IOException e) {
			this.results = SearchResults.fatalError();
			logger.error(e.getMessage(), e);
			return;
		}
		
//		System.out.println("Search complete: total hits: " + collector.getTotalHits());
	}


	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(SearchResults.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}

}
