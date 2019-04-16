package org.cytoscape.view.model.internal;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewConfig;
import org.cytoscape.view.model.CyNetworkViewFactoryFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.model.CyNetworkViewFactoryImpl;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
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
			config.addTrackedVisualProperty(CyNetworkViewConfig.SELECTED_NODES, BasicVisualLexicon.NODE_SELECTED, Boolean.TRUE::equals);
			config.addTrackedVisualProperty(CyNetworkViewConfig.SELECTED_EDGES, BasicVisualLexicon.EDGE_SELECTED, Boolean.TRUE::equals);
			CyNetworkViewImpl.NODE_GEOMETRIC_PROPS.forEach(config::addNonClearableVisualProperty);
		}
		return config;
	}

	@Override
	public CyNetworkViewFactory createNetworkViewFactory(VisualLexicon lexicon, String rendererId, CyNetworkViewConfig config) {
		return new CyNetworkViewFactoryImpl(registrar, lexicon, rendererId, (CyNetworkViewConfigImpl) config);
	}

}
