package org.cytoscape.webservice.ncbi;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.client.AbstractWebServiceClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.webservice.ncbi.task.ImportNetworkFromGeneTask;
import org.cytoscape.work.TaskIterator;

public class NCBIWebServiceClient extends AbstractWebServiceClient implements NetworkImportWebServiceClient, SearchWebServiceClient<Set<String>> {

	private final CyNetworkFactory networkFactory;
	private final CyTableFactory tableFactory;
	private final CyNetworkManager manager;

	private ImportNetworkFromGeneTask networkTask;

	public NCBIWebServiceClient(final String uri, String displayName, String description,
			final CyNetworkFactory networkFactory, final CyTableFactory tableFactory, final CyNetworkManager manager) {
		super(uri, displayName, description);
		this.networkFactory = networkFactory;
		this.manager = manager;
		this.tableFactory = tableFactory;
	}

	@Override
	public TaskIterator getTaskIterator() {
		if (currentQuery == null)
			throw new NullPointerException("Query object is null.");
		else {
			networkTask = new ImportNetworkFromGeneTask(this.currentQuery.toString(), networkFactory, tableFactory, manager);
			return new TaskIterator(networkTask);
		}
	}

	@Override
	public Set<CyNetwork> getNetworks() {
		final Set<CyNetwork> result = new HashSet<CyNetwork>();
		if (networkTask != null) {
			final CyNetwork network = networkTask.getNetwork();
			if (network != null)
				result.add(network);
		}
		return result;
	}

	@Override
	public Set<String> getSearchResult() {
		// TODO Auto-generated method stub
		return null;
	}
}
