package org.cytoscape.view.table;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewFactoryProvider;
import org.cytoscape.view.model.table.TableViewFactoryTestSupport;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class TableViewTestSupport extends TableTestSupport {
	
	protected TableViewFactoryTestSupport tableViewFactoryTestSupport;
	
	public TableViewTestSupport() {
		tableViewFactoryTestSupport = new TableViewFactoryTestSupport();
	}
	
	public CyTableViewFactoryProvider getTableViewFactoryProvider() {
		return tableViewFactoryTestSupport.getTableViewFactoryProvider();
	}

	public CyTableViewFactory getTableViewFactory() {
		// TODO replace the visual lexicon below with a custom one if we end up needing it
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		VisualLexicon lexicon = new BasicTableVisualLexicon(rootVp);
		return tableViewFactoryTestSupport.getTableViewFactory(lexicon);
	}
	
	public CyTableView getTableView(CyTable table) {
		return getTableViewFactory().createTableView(table);
	}
	
	public CyTableView getTableView() {
		CyTable table = getTableFactory().createTable("text", "SUID", Long.class, true, true);
		return getTableView(table);
	}
}
