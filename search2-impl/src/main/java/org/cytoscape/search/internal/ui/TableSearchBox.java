package org.cytoscape.search.internal.ui;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.search.TableSearchTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class TableSearchBox extends SearchBox {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final CyServiceRegistrar registrar;
	private final SearchManager searchManager;
	
	public TableSearchBox(CyServiceRegistrar registrar, SearchManager searchManager) {
		super(registrar);
		this.registrar = registrar;
		this.searchManager = searchManager;
	}
	

	@Override
	public TableSearchTask getSearchTask(String queryString) {
		var appManager = registrar.getService(CyApplicationManager.class);
		final var currentTable = appManager.getCurrentTable();
		if(currentTable == null) {
			logger.error("Could not find table for search");
			return null;
		}
		
		return new TableSearchTask(registrar, searchManager, queryString, currentTable);
	}
	
}
