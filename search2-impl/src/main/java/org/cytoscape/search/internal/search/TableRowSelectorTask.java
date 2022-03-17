package org.cytoscape.search.internal.search;

import java.util.HashSet;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class TableRowSelectorTask extends AbstractTask {

	private final CyServiceRegistrar registrar;
	
	private final SearchResults searchResults;
	private final CyTable table;
	
	
	public TableRowSelectorTask(CyServiceRegistrar registrar, CyTable table, SearchResults searchResults) {
		this.registrar = registrar;
		this.searchResults = searchResults;
		this.table = table;
	}


	@Override
	public void run(TaskMonitor tm) {
		var tableViewManager = registrar.getService(CyTableViewManager.class);
		var tableView = tableViewManager.getTableView(table);
		if(tableView == null)
			return;
		
		var tableHits = searchResults.getResultsFor(table);
		if(tableHits == null || tableHits.isEmpty())
			return;
		
		var hitsSet = new HashSet<String>(tableHits);
		
		// Select all the columns first so that the entire row in the table browser becomes highlighted.
		for(var col: table.getColumns()) {
			var colView = tableView.getColumnView(col);
			if(colView != null) {
				colView.setLockedValue(BasicTableVisualLexicon.COLUMN_SELECTED, true);
			}
		}
		
		var keyCol = table.getPrimaryKey();
		for(var key : keyCol.getValues(keyCol.getType())) {
			var row = table.getRow(key);
			var rowView = tableView.getRowView(row);
			if(rowView != null) {
				var keyStr = String.valueOf(key);
				boolean selected = hitsSet.contains(keyStr);
				rowView.setLockedValue(BasicTableVisualLexicon.ROW_SELECTED, selected);
			}
		}
	}

}
