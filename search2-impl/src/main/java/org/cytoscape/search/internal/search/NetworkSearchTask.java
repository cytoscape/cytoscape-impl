package org.cytoscape.search.internal.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.index.TableType;
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
		if (cancelled)
			return SearchResults.cancelled();
		
		// bail if the length of query string is too long
		if (queryString.length() > MAX_QUERY_LEN) {
			logger.error(results.getMessage());
			return SearchResults.syntaxError("At " + queryString.length() + " characters query string is too large");
		}
		if(queryString.isBlank()) {
			return SearchResults.empty();
		}
		
		Query query;
		try {
			QueryParser parser = searchManager.getQueryParser(network);
			query = parser.parse(queryString);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			return SearchResults.syntaxError();
		}
		
		IndexReader nodeReader = null;
		IndexReader edgeReader = null;
		try {
			nodeReader = searchManager.getIndexReader(network.getDefaultNodeTable());
			edgeReader = searchManager.getIndexReader(network.getDefaultEdgeTable());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return SearchResults.fatalError();
		}
		
		if(nodeReader == null || edgeReader == null) {
			logger.warn("index not ready");
			return SearchResults.notReady();
		}
		
		// TODO figure out how to use the MultiReader, maybe we don't need to do two separate searches??
		
		var nodeSearcher = new IndexSearcher(nodeReader);
		var nodeCollector = new IdentifiersCollector(nodeSearcher);
		try {
			nodeSearcher.search(query, nodeCollector);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return SearchResults.fatalError();
		}
		
		var edgeSearcher = new IndexSearcher(edgeReader);
		var edgeCollector = new IdentifiersCollector(edgeSearcher);
		try {
			edgeSearcher.search(query, edgeCollector);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return SearchResults.fatalError();
		}
		
		try {
			nodeReader.close();
			edgeReader.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		return SearchResults.results(nodeCollector.getIDs(), edgeCollector.getIDs());
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

	public List<String> identifiers = new ArrayList<>();

	public IdentifiersCollector(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public List<String> getIDs() {
		return identifiers;
	}

	@Override
	public void collect(int id) {
		super.collect(id);
		try {
			Document doc = searcher.doc(id);
			String eleID = doc.get(SearchManager.INDEX_FIELD);
			identifiers.add(eleID);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return IdentifiersCollector.class.getSimpleName() + " - hits: " + identifiers.size();
	}

}



class TypedIdentifiersCollector extends TotalHitCountCollector {

	private IndexSearcher searcher;

	public List<String> nodeHitsIdentifiers = new ArrayList<>();
	public List<String> edgeHitsIdentifiers = new ArrayList<>();

	public TypedIdentifiersCollector(IndexSearcher searcher) {
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
			
			if(TableType.NODE.name().equals(currType)) {
				nodeHitsIdentifiers.add(currID);
			} else if(TableType.EDGE.name().equals(currType)) {
				edgeHitsIdentifiers.add(currID);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return TypedIdentifiersCollector.class.getSimpleName() + " - nodeHits: " + getNodeHits() + ", edgeHits: " + getEdgeHits();
	}

}
