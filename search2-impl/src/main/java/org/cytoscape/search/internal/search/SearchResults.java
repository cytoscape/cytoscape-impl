package org.cytoscape.search.internal.search;

import java.util.Collections;
import java.util.List;

public class SearchResults {

	public static enum Status {
		SUCCESS, ERROR_SYNTAX, ERROR_FATAL, CANCELLED, NOT_READY
	}
	
	private final Status status;
	private final String message;
	private final List<String> nodeHits;
	private final List<String> edgeHits;

	
	private SearchResults(Status status, String message, List<String> nodeHits, List<String> edgeHits) {
		this.status = status;
		this.nodeHits = nodeHits == null ? Collections.emptyList() : nodeHits;
		this.edgeHits = edgeHits == null ? Collections.emptyList() : edgeHits;
		if (this.isError() == false && message == null) {
			int edges = this.getEdgeHitCount();
			int nodes = this.getNodeHitCount();
			String nodeplural = "s";
			if (nodes == 1) {
				nodeplural = "";
			}
			String edgeplural = "s";
			if (edges == 1) {
				edgeplural = "";
			}
			this.message = "Selected " + nodes + " node" + nodeplural + " and " + edges + " edge" + edgeplural;
		}
		else {
			this.message = message;
		}
	}
	
	private SearchResults(Status status, String message) {
		this(status, message, null, null);
	}

	
	public static SearchResults syntaxError() {
		return new SearchResults(Status.ERROR_SYNTAX, "Cannot execute search query");
	}

	public static SearchResults syntaxError(String message) {
		return new SearchResults(Status.ERROR_SYNTAX, message);
	}

	public static SearchResults fatalError() {
		return new SearchResults(Status.ERROR_FATAL, "Query execution error");
	}

	public static SearchResults fatalError(String message) {
		return new SearchResults(Status.ERROR_FATAL, message);
	}

	public static SearchResults results(List<String> nodeHits, List<String> edgeHits) {
		return new SearchResults(Status.SUCCESS, null, nodeHits, edgeHits);
	}
	
	public static SearchResults empty() {
		return new SearchResults(Status.SUCCESS, null);
	}
	
	public static SearchResults notReady() {
		return new SearchResults(Status.NOT_READY, "Search index is not ready, please try again.");
	}
	
	public static SearchResults cancelled() {
		return new SearchResults(Status.CANCELLED, null);
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
	public String getMessage() {
		return message;
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

	@Override
	public String toString() {
		return "SearchResults [status=" + status + ", nodeHits=" + nodeHits + ", edgeHits=" + edgeHits + "]";
	}
	
	
}
