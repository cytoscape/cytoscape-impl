package org.cytoscape.view.model.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.internal.model.CyNetworkViewFactoryImpl;
import org.cytoscape.view.presentation.CyNetworkViewFactoryFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CyNetworkViewFactoryFactoryImpl implements CyNetworkViewFactoryFactory {

	private final CyServiceRegistrar registrar;
	
	public CyNetworkViewFactoryFactoryImpl(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	public CyNetworkViewFactory createNetworkViewFactory(BasicVisualLexicon lexicon, String rendererId) {
		return new CyNetworkViewFactoryImpl(registrar, lexicon, rendererId);
	}

}
