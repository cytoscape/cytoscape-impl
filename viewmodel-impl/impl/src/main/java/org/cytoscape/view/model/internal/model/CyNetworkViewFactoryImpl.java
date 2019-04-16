package org.cytoscape.view.model.internal.model;

import java.util.Properties;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewListener;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.CyNetworkViewConfigImpl;



public class CyNetworkViewFactoryImpl implements CyNetworkViewFactory {

	private final CyServiceRegistrar registrar;
	private final VisualLexicon visualLexicon;
	private final String rendererId;
	private final CyNetworkViewConfigImpl config;
	
	public CyNetworkViewFactoryImpl(CyServiceRegistrar registrar, VisualLexicon visualLexicon, String rendererId, CyNetworkViewConfigImpl config) {
		this.registrar = registrar;
		this.visualLexicon = visualLexicon;
		this.rendererId = rendererId;
		this.config = config;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		CyNetworkViewImpl networkViewImpl = new CyNetworkViewImpl(registrar, network, visualLexicon, rendererId, config);
		NetworkModelListener modelListener = new NetworkModelListener(networkViewImpl, registrar);
		
		networkViewImpl.addNetworkViewListener(new CyNetworkViewListener() {
			@Override public void handleDispose() {
				registrar.unregisterAllServices(modelListener);
			}
		}); 
		
		registrar.registerAllServices(modelListener, new Properties());
		
		return networkViewImpl;
	}

}
