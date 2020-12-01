package org.cytoscape.view.model.internal.table;

import java.util.Properties;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewFactory;

public class CyTableViewFactoryImpl implements CyTableViewFactory {

	private final CyServiceRegistrar registrar;
	private final VisualLexicon visualLexicon;
	private final String rendererId;
	
	public CyTableViewFactoryImpl(CyServiceRegistrar registrar, VisualLexicon visualLexicon, String rendererId) {
		this.registrar = registrar;
		this.visualLexicon = visualLexicon;
		this.rendererId = rendererId;
	}
	
	@Override
	public CyTableView createTableView(CyTable table) {
		CyTableViewImpl tableView = createTableViewImpl(table);
		listenForModelChanges(tableView);
		return tableView;
	}
	

	private CyTableViewImpl createTableViewImpl(CyTable table) {
		CyTableViewImpl tableView = new CyTableViewImpl(registrar, table, visualLexicon, rendererId);
		for(CyColumn col : table.getColumns()) {
			tableView.addColumn(col);
		}
		for(CyRow row : table.getAllRows()) {
			tableView.addRow(row);
		}
		return tableView;
	}
	
	
	private void listenForModelChanges(CyTableViewImpl tableView) {
		TableModelListener modelListener = new TableModelListener(tableView, registrar);
		
		tableView.addDisposeListener(() -> {
			registrar.unregisterAllServices(modelListener);
		}); 
		
		registrar.registerAllServices(modelListener, new Properties());
	}
	
}
