package org.cytoscape.search.internal.search;

import org.cytoscape.model.CyTable;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;

public class TableSearchTask extends SearchTask {

	private final CyServiceRegistrar registrar;
	private final CyTable table;
	
	public TableSearchTask(CyServiceRegistrar registrar, SearchManager searchManager, String queryString, CyTable table) {
		super(searchManager, queryString, table);
		this.table = table;
		this.registrar = registrar;
	}

	
	@Override
	public void run(TaskMonitor tm) {
		var results = super.runQuery(tm);
		insertTasksAfterCurrentTask(new TableRowSelectorTask(registrar, table, results));
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
			int hitCount = results.getHitCount(table);
			if(hitCount == 1)
				return "Selected 1 row";
			else
				return "Selected " + hitCount + " rows";
		}
		return results.getErrorMessage();
	}
}
