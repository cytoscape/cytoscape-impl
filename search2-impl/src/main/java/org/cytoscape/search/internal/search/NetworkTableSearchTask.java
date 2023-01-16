package org.cytoscape.search.internal.search;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.work.TaskMonitor;

/**
 * Searches network tables then selects the corresponding nodes and edges.
 */
public class NetworkTableSearchTask extends SearchTask {

	private final CyNetwork network;
	
	/**
	 * Its assumed that the given table is from the given network.
	 */
	public NetworkTableSearchTask(SearchManager searchManager, String queryString, CyNetwork network, CyTable table) {
		super(searchManager, queryString, table);
		this.network = network;
	}

	/**
	 * Searches the default node and edge tables.
	 */
	public NetworkTableSearchTask(SearchManager searchManager, String queryString, CyNetwork network) {
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
		if(results.isError() || results.getErrorMessage() != null)
			return results.getErrorMessage();
			
		int edges = results.getHitCount(network.getDefaultEdgeTable());
		int nodes = results.getHitCount(network.getDefaultNodeTable());
		
		if(nodes == 0 && edges == 0) {
			return "Selected 0 nodes and 0 edges";
		}
		
		String nodeplural = (nodes == 1) ? "" : "s";
		String edgeplural = (edges == 1) ? "" : "s";
		
		if(nodes == 0) {
			return "Selected " + edges + " edge" + edgeplural;
		} else if(edges == 0) {
			return "Selected " + nodes + " node" + nodeplural;
		} else {
			return "Selected " + nodes + " node" + nodeplural + " and " + edges + " edge" + edgeplural;
		}
	}
		
}
