package org.cytoscape.search.internal.ui;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.search.NetworkTableSearchTask;
import org.cytoscape.search.internal.search.SearchTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class NetworkTableSearchBox extends SearchBox {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final CyServiceRegistrar registrar;
	private final SearchManager searchManager;
	
	public NetworkTableSearchBox(CyServiceRegistrar registrar, SearchManager searchManager) {
		super(registrar);
		this.registrar = registrar;
		this.searchManager = searchManager;
	}
	

	@Override
	public SearchTask getSearchTask(String queryString) {
		var appManager = registrar.getService(CyApplicationManager.class);
		var networkTableManager = registrar.getService(CyNetworkTableManager.class);
		
		var currentTable = appManager.getCurrentTable();
		if(currentTable == null) {
			logger.error("Could not find table for search");
			return null;
		}
		
		var network = networkTableManager.getNetworkForTable(currentTable);
		if(network == null) {
			logger.error("Could not find network for table to search");
			return null;
		}
		
		return new NetworkTableSearchTask(searchManager, queryString, network, currentTable);
	}
	
}
