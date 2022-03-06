package org.cytoscape.search.internal.ui;

import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.TableToolBarComponent;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.search.TableSearchTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class TableSearchBox extends SearchBox implements TableToolBarComponent {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final Class<? extends CyIdentifiable> tableType;
	private final CyServiceRegistrar registrar;
	private final SearchManager searchManager;
	
	public TableSearchBox(CyServiceRegistrar registrar, SearchManager searchManager, Class<? extends CyIdentifiable> tableType) {
		super(registrar);
		this.registrar = registrar;
		this.searchManager = searchManager;
		this.tableType = tableType;
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
	
	@Override
	public Component getComponent() {
		return this;
	}
	
	@Override
	public float getToolBarGravity() {
		return Integer.MAX_VALUE-1;
	}

	@Override
	public Class<? extends CyIdentifiable> getTableType() {
		return tableType;
	}

	@Override
	public boolean isApplicable(CyTable table) {
		return table != null;
	}
}
