package org.cytoscape.view.model.table;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.internal.table.CyTableViewFactoryProviderImpl;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;

public class TableViewFactoryTestSupport {

	private CyServiceRegistrar serviceRegistrar;
	
	public TableViewFactoryTestSupport() {
		serviceRegistrar = mock(CyServiceRegistrar.class, withSettings().stubOnly());
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
	}
	
	public CyTableViewFactoryProvider getTableViewFactoryProvider() {
		return new CyTableViewFactoryProviderImpl(serviceRegistrar);
	}

	public CyTableViewFactory getTableViewFactory() {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		VisualLexicon lexicon = new BasicTableVisualLexicon(rootVp);
		return getTableViewFactory(lexicon);
	}
	
	public CyTableViewFactory getTableViewFactory(VisualLexicon lexicon) {
		CyTableViewFactoryProvider networkViewFactoryFactory = getTableViewFactoryProvider();
		return networkViewFactoryFactory.createTableViewFactory(lexicon, "test.renderer");
	}
}
