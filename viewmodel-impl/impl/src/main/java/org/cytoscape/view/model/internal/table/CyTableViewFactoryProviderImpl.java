package org.cytoscape.view.model.internal.table;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewFactoryProvider;

public class CyTableViewFactoryProviderImpl implements CyTableViewFactoryProvider {

	private final CyServiceRegistrar registrar;
	
	public CyTableViewFactoryProviderImpl(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	public CyTableViewFactory createTableViewFactory(VisualLexicon lexicon, String rendererID) {
		return new CyTableViewFactoryImpl(registrar, lexicon, rendererID);
	}

}
