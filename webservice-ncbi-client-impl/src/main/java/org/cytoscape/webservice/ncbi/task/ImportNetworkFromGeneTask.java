package org.cytoscape.webservice.ncbi.task;

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableFactory;
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
	
	private CyNetwork newNetwork;
	
	public ImportNetworkFromGeneTask(final String queryString, final CyNetworkFactory networkFactory, final CyTableFactory tableFactory, final CyNetworkManager manager) {
		this.queryString = queryString;
		this.networkFactory = networkFactory;
		this.manager = manager;
		this.tableFactory = tableFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		logger.debug("Import Start: Query = " + queryString);
		
		final EntrezRestClient restClient = new EntrezRestClient(networkFactory, tableFactory);
		
		taskMonitor.setTitle("Accessing NCBI Entrez Gene");
		taskMonitor.setStatusMessage("Searching matching genes...");
		final Set<String> searchResult = restClient.search(queryString);
		
		if(searchResult.size() == 0)
			return;
		
		taskMonitor.setStatusMessage("Creating network from matching genes...");
		newNetwork = restClient.importNetwork(searchResult);
		
		// Register it
		newNetwork.getCyRow().set(CyTableEntry.NAME, "NCBI");
		manager.addNetwork(newNetwork);
	}
	
	public CyNetwork getNetwork() {
		return newNetwork;
	}
}
