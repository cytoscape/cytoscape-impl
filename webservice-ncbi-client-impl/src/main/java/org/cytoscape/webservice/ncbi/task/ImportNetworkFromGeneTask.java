package org.cytoscape.webservice.ncbi.task;


import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.webservice.ncbi.rest.EntrezRestClient;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImportNetworkFromGeneTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(ImportNetworkFromGeneTask.class);

	private final String queryString;
	private final CyNetworkFactory networkFactory;
	private final CyTableFactory tableFactory;
	private final CyNetworkManager manager;
	private final CyTableManager tableManager;

	private CyNetwork newNetwork;

	public ImportNetworkFromGeneTask(final String queryString,
	                                 final CyNetworkFactory networkFactory,
	                                 final CyTableFactory tableFactory,
	                                 final CyNetworkManager manager,
	                                 final CyTableManager tableManager)
	{
		this.queryString    = queryString;
		this.networkFactory = networkFactory;
		this.manager        = manager;
		this.tableFactory   = tableFactory;
		this.tableManager   = tableManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		logger.debug("Import Start: Query = " + queryString);

		taskMonitor.setProgress(0.01d);
		
		final EntrezRestClient restClient =
			new EntrezRestClient(networkFactory, tableFactory, tableManager);

		taskMonitor.setTitle("Accessing NCBI Entrez Gene");
		taskMonitor.setStatusMessage("Searching matching genes...");
		final Set<String> searchResult = restClient.search(queryString);

		if(searchResult.size() == 0)
			return;

		taskMonitor.setStatusMessage("Creating network from matching genes...");
		newNetwork = restClient.importNetwork(searchResult, taskMonitor);

		// Register it
		newNetwork.getCyRow().set(CyTableEntry.NAME, "NCBI");
		manager.addNetwork(newNetwork);
	}

	public CyNetwork getNetwork() {
		return newNetwork;
	}
}
