package org.cytoscape.search.internal;

import java.util.Collections;
import java.util.List;

public class SearchResults {

	public static enum Status {
		SUCCESS, ERROR_SYNTAX, ERROR_FATAL
	}
	
	private final Status status;
	private final List<String> nodeHits;
	private final List<String> edgeHits;

	
	private SearchResults(Status status, List<String> nodeHits, List<String> edgeHits) {
		this.status = status;
		this.nodeHits = nodeHits == null ? Collections.emptyList() : nodeHits;
		this.edgeHits = edgeHits == null ? Collections.emptyList() : edgeHits;
	}
	
	
	public static SearchResults syntaxError() {
		return new SearchResults(Status.ERROR_SYNTAX, null, null);
	}
	
	public static SearchResults fatalError() {
		return new SearchResults(Status.ERROR_FATAL, null, null);
	}
	
	public static SearchResults results(List<String> nodeHits, List<String> edgeHits) {
		return new SearchResults(Status.SUCCESS, nodeHits, edgeHits);
	}
	
	public boolean isError() {
		return status == Status.ERROR_FATAL || status == Status.ERROR_SYNTAX;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public int getNodeHitCount() {
		return nodeHits.size();
	}
	
	public int getEdgeHitCount() {
		return edgeHits.size();
	}

	public List<String> getNodeHits() {
		return nodeHits;
	}
	
	public List<String> getEdgeHits() {
		return edgeHits;
	}
	
}
