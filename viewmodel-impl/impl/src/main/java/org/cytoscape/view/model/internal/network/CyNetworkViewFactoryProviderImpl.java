package org.cytoscape.view.model.internal.network;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryConfig;
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public class CyNetworkViewFactoryProviderImpl implements CyNetworkViewFactoryProvider {

	private final CyServiceRegistrar registrar;
	
	public CyNetworkViewFactoryProviderImpl(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	public CyNetworkViewFactoryConfigImpl createConfig(VisualLexicon lexicon) {
		CyNetworkViewFactoryConfigImpl config = new CyNetworkViewFactoryConfigImpl();
		if(lexicon instanceof BasicVisualLexicon) {
			// Tracked VPs... Don't track any VP's by default
			// Non-clearable VPs
			config.addNonClearableVisualProperty(NODE_X_LOCATION);
			config.addNonClearableVisualProperty(NODE_Y_LOCATION);
			config.addNonClearableVisualProperty(NODE_VISIBLE);
			config.addNonClearableVisualProperty(EDGE_VISIBLE);
			config.addNonClearableVisualProperty(NODE_SELECTED);
			config.addNonClearableVisualProperty(EDGE_SELECTED);
			config.addNonClearableVisualProperty(NETWORK_TITLE);
			config.addNonClearableVisualProperty(NETWORK_WIDTH);
			config.addNonClearableVisualProperty(NETWORK_HEIGHT);
			config.addNonClearableVisualProperty(EDGE_BEND); // CYTOSCAPE-12957
		}
		return config;
	}

	@Override
	public CyNetworkViewFactory createNetworkViewFactory(VisualLexicon lexicon, String rendererId, CyNetworkViewFactoryConfig config) {
		return new CyNetworkViewFactoryImpl(registrar, lexicon, rendererId, (CyNetworkViewFactoryConfigImpl) config);
	}

}
