package org.cytoscape.search.internal.search;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.work.TaskMonitor;

public class NetworkSearchTask extends SearchTask {

	private final CyNetwork network;
	
	public NetworkSearchTask(SearchManager searchManager, String queryString, CyNetwork network) {
		super(searchManager, queryString, network.getDefaultNodeTable(), network.getDefaultEdgeTable());
		this.network = network;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		var results = super.runQuery(tm);
		insertTasksAfterCurrentTask(new NodeAndEdgeSelectorTask(network, results));
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			var results = super.getResults(SearchResults.class);
			return type.cast(getPopupMessage(results));
		}
		return super.getResults(type);
	}
	
	private String getPopupMessage(SearchResults results) {
		if(results.isError() == false && results.getErrorMessage() == null) {
			int edges = results.getHitCount(network.getDefaultEdgeTable());
			int nodes = results.getHitCount(network.getDefaultNodeTable());
			String nodeplural = "s";
			if (nodes == 1) {
				nodeplural = "";
			}
			String edgeplural = "s";
			if (edges == 1) {
				edgeplural = "";
			}
			return "Selected " + nodes + " node" + nodeplural + " and " + edges + " edge" + edgeplural;
		}
		return results.getErrorMessage();
	}
}

