package org.cytoscape.view.model.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.NetworkViewRenderer;
import org.cytoscape.view.presentation.NetworkViewRendererManager;

public class CyNetworkViewFactoryImpl implements CyNetworkViewFactory {
	
	private NetworkViewRendererManager rendererManager;

	public CyNetworkViewFactoryImpl(NetworkViewRendererManager rendererManager) {
		this.rendererManager = rendererManager;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		NetworkViewRenderer renderer = rendererManager.getCurrentNetworkViewRenderer();
		CyNetworkViewFactory viewFactory = renderer.getNetworkViewFactory();
		return viewFactory.createNetworkView(network);
	}
}
