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
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.model.CyNetworkViewFactoryImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public class CyNetworkViewFactoryFactoryImpl implements CyNetworkViewFactoryFactory {

	private final CyServiceRegistrar registrar;
	
	public CyNetworkViewFactoryFactoryImpl(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	@Override
	public CyNetworkViewConfigImpl createConfig(VisualLexicon lexicon) {
		CyNetworkViewConfigImpl config = new CyNetworkViewConfigImpl();
		if(lexicon instanceof BasicVisualLexicon) {
			// Tracked VPs
			config.addTrackedVisualProperty(CyNetworkViewConfig.SELECTED_NODES, NODE_SELECTED, Boolean.TRUE::equals);
//			config.addTrackedVisualProperty(CyNetworkViewConfig.SELECTED_EDGES, EDGE_SELECTED, Boolean.TRUE::equals);
			config.addTrackedVisualProperty(CyNetworkViewConfig.HIDDEN_NODES, NODE_VISIBLE, Boolean.FALSE::equals);
			config.addTrackedVisualProperty(CyNetworkViewConfig.HIDDEN_EDGES, EDGE_VISIBLE, Boolean.FALSE::equals);
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
	public CyNetworkViewFactory createNetworkViewFactory(VisualLexicon lexicon, String rendererId, CyNetworkViewConfig config) {
		return new CyNetworkViewFactoryImpl(registrar, lexicon, rendererId, (CyNetworkViewConfigImpl) config);
	}

}
