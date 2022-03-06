package org.cytoscape.search.internal.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;

public class SearchResults {

	public static enum Status {
		SUCCESS, ERROR_SYNTAX, ERROR_FATAL, CANCELLED;
	}
	
	private final Status status;
	private final String errorMessage;

	// The results are List<String> because Lucene will only store the String representation of the Row's primary key.
	private final Map<Long,List<String>> resultsMap = new HashMap<>(); // Table SUID -> List of row primary keys
	
	
	private SearchResults(Status status, String errorMessage) {
		this.status = status;
		this.errorMessage = errorMessage;
	}
	
	
	public void addResults(Long tableSUID, List<String> results) {
		resultsMap.put(tableSUID, results);
	}
	
	public void addResults(CyTable table, List<String> results) {
		resultsMap.put(table.getSUID(), results);
	}
	
	public List<String> getResultsFor(Long tableSUID) {
		return resultsMap.get(tableSUID);
	}
	
	public List<String> getResultsFor(CyTable table) {
		return resultsMap.get(table.getSUID());
	}
	
	public int getHitCount(Long tableSUID) {
		var list = resultsMap.get(tableSUID);
		return list == null ? 0 : list.size();
	}
	
	public int getHitCount(CyTable table) {
		return getHitCount(table.getSUID());
	}
	
	public static SearchResults syntaxError() {
		return new SearchResults(Status.ERROR_SYNTAX, "Cannot execute search query");
	}

	public static SearchResults syntaxError(String message) {
		return new SearchResults(Status.ERROR_SYNTAX, message);
	}

	public static  SearchResults fatalError() {
		return new SearchResults(Status.ERROR_FATAL, "Query execution error");
	}

	public static SearchResults fatalError(String message) {
		return new SearchResults(Status.ERROR_FATAL, message);
	}

	public static SearchResults empty() {
		return new SearchResults(Status.SUCCESS, null);
	}
	
	public static  SearchResults cancelled() {
		return new SearchResults(Status.CANCELLED, null);
	}
	
	public static SearchResults networkResults(CyNetwork network, List<String> nodeHits, List<String> edgeHits) {
		var results = new SearchResults(Status.SUCCESS, null);
		results.addResults(network.getDefaultNodeTable(), nodeHits);
		results.addResults(network.getDefaultEdgeTable(), edgeHits);
		return results;
	}
	
	public static SearchResults results(CyTable table, List<String> keys) {
		var results = new SearchResults(Status.SUCCESS, null);
		results.addResults(table.getSUID(), keys);
		return results;
	}
	
	public SearchResults compose(final SearchResults that) {
		if(this.isError())
			return this;
		if(that.isError())
			return that;
		
		var results = SearchResults.empty();
		
		for(var entry : this.resultsMap.entrySet()) {
			results.addResults(entry.getKey(), entry.getValue());
		}
		for(var entry : that.resultsMap.entrySet()) {
			results.addResults(entry.getKey(), entry.getValue());
		}
		
		return results;
	}
	
	
	public boolean isError() {
		return status == Status.ERROR_FATAL || status == Status.ERROR_SYNTAX;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}


	@Override
	public String toString() {
		return "SearchResults [status=" + status + ", resultsMap=" + resultsMap + "]";
	}
	
	
}
