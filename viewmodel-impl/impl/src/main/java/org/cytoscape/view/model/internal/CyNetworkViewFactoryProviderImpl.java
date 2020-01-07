package org.cytoscape.view.model.internal;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SELECTED;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactoryConfig;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.model.CyNetworkViewFactoryImpl;
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
		}
		return config;
	}

	@Override
	public CyNetworkViewFactory createNetworkViewFactory(VisualLexicon lexicon, String rendererId, CyNetworkViewFactoryConfig config) {
		return new CyNetworkViewFactoryImpl(registrar, lexicon, rendererId, (CyNetworkViewFactoryConfigImpl) config);
	}

}
