package org.cytoscape.view.model.table;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.table.CyTableViewFactoryProviderImpl;
import org.cytoscape.view.model.internal.table.CyTableViewImpl;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class TableViewTestUtils {
	
	public static CyTableViewImpl createTableView(CyTable table) {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		VisualLexicon lexicon = new BasicTableVisualLexicon(rootVp);
		
		CyServiceRegistrar registrar = mock(CyServiceRegistrar.class);
		when(registrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
		
		CyTableViewFactoryProviderImpl factoryFactory = new CyTableViewFactoryProviderImpl(registrar);
		CyTableViewFactory factory = factoryFactory.createTableViewFactory(lexicon, "test");
		CyTableViewImpl tableView = (CyTableViewImpl) factory.createTableView(table);
		return tableView;
	}
	
}
