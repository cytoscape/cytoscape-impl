package org.cytoscape.search.internal.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
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
		
		IndexReader reader;
		try {
			Directory directory = searchManager.getDirectory(network);
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			this.results = SearchResults.fatalError();
			logger.error(e.getMessage(), e);
			return;
		}
		
		IndexSearcher searcher = new IndexSearcher(reader);
		var collector = new IdentifiersCollector(searcher);
		try {
			searcher.search(query, collector);
		} catch (IOException e) {
			this.results = SearchResults.fatalError();
			logger.error(e.getMessage(), e);
			return;
		}
		
		this.results = SearchResults.results(collector.getNodes(), collector.getEdges());
		
		insertTasksAfterCurrentTask(new NodeAndEdgeSelectorTask(network, results));
	}

	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(SearchResults.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}

}


class IdentifiersCollector extends TotalHitCountCollector {

	private IndexSearcher searcher;

	public List<String> nodeHitsIdentifiers = new ArrayList<>();
	public List<String> edgeHitsIdentifiers = new ArrayList<>();

	public IdentifiersCollector(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public int getNodeHits() {
		return nodeHitsIdentifiers.size();
	}
	public int getEdgeHits() {
		return edgeHitsIdentifiers.size();
	}

	public List<String> getNodes() {
		return nodeHitsIdentifiers;
	}
	public List<String> getEdges() {
		return edgeHitsIdentifiers;
	}

	@Override
	public void collect(int id) {
		super.collect(id);
		try {
			Document doc = searcher.doc(id);
			String currID = doc.get(SearchManager.INDEX_FIELD);
			String currType = doc.get(SearchManager.TYPE_FIELD);
			
			if (currType.equalsIgnoreCase(SearchManager.NODE_TYPE)) {
				nodeHitsIdentifiers.add(currID);
			} else if (currType.equalsIgnoreCase(SearchManager.EDGE_TYPE)) {
				edgeHitsIdentifiers.add(currID);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return IdentifiersCollector.class.getSimpleName() + " - nodeHits: " + getNodeHits() + ", edgeHits: " + getEdgeHits();
	}

}
