package org.cytoscape.view.model.internal.model;

import java.util.Properties;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;



public class CyNetworkViewFactoryImpl implements CyNetworkViewFactory {

	private final CyServiceRegistrar registrar;
	private final VisualLexicon visualLexicon;
	private final String rendererId;
	
	public CyNetworkViewFactoryImpl(CyServiceRegistrar registrar, VisualLexicon visualLexicon, String rendererId) {
		this.registrar = registrar;
		this.visualLexicon = visualLexicon;
		this.rendererId = rendererId;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		CyNetworkViewImpl networkViewImpl = new CyNetworkViewImpl(network, visualLexicon, rendererId);
		NetworkModelListener modelListener = new NetworkModelListener(networkViewImpl, registrar);
		
		networkViewImpl.onDispose(() -> registrar.unregisterAllServices(modelListener));
		registrar.registerAllServices(modelListener, new Properties());
		
		return networkViewImpl;
	}

}
