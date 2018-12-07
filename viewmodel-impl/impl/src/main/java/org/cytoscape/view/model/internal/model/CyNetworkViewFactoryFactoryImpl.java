package org.cytoscape.view.model.internal.model;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryFactory;
import org.cytoscape.view.model.VisualLexicon;

public class CyNetworkViewFactoryFactoryImpl implements CyNetworkViewFactoryFactory {

	private final CyServiceRegistrar registrar;
	
	public CyNetworkViewFactoryFactoryImpl(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	public CyNetworkViewFactory createNetworkViewFactory(VisualLexicon lexicon, String rendererId) {
		return new CyNetworkViewFactoryImpl(registrar, lexicon, rendererId);
	}

}
