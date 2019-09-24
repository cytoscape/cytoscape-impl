package org.cytoscape.search.internal;

import java.util.Collections;
import java.util.List;

public class SearchResults {

	public static enum Status {
		SUCCESS, ERROR_SYNTAX, ERROR_FATAL
	}
	
	private final Status status;
	private final String errorMessage;
	private final List<String> nodeHits;
	private final List<String> edgeHits;

	
	private SearchResults(Status status, final String errorMessage, List<String> nodeHits, List<String> edgeHits) {
		this.status = status;
		this.errorMessage = errorMessage;
		this.nodeHits = nodeHits == null ? Collections.<String>emptyList() : nodeHits;
		this.edgeHits = edgeHits == null ? Collections.<String>emptyList() : edgeHits;
	}
	
	
	public static SearchResults syntaxError() {
		return new SearchResults(Status.ERROR_SYNTAX, null, null, null);
	}

	public static SearchResults syntaxError(final String errorMessage) {
		return new SearchResults(Status.ERROR_SYNTAX, errorMessage, null, null);
	}

	public static SearchResults fatalError() {
		return new SearchResults(Status.ERROR_FATAL, null, null, null);
	}

	public static SearchResults fatalError(final String errorMessage) {
		return new SearchResults(Status.ERROR_FATAL, errorMessage, null, null);
	}

	public static SearchResults results(List<String> nodeHits, List<String> edgeHits) {
		return new SearchResults(Status.SUCCESS, null, nodeHits, edgeHits);
	}
	
	public boolean isError() {
		return status == Status.ERROR_FATAL || status == Status.ERROR_SYNTAX;
	}
	
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Returns error message if any
	 * @return Error message or null if no error
	 */
	public String getErrorMessage(){
		return errorMessage;
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
