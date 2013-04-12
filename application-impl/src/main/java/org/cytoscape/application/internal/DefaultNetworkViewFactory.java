package org.cytoscape.application.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;

public class DefaultNetworkViewFactory implements CyNetworkViewFactory {
	
	private final CyApplicationManager applicationManager;

	public DefaultNetworkViewFactory(CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}
	
	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		NetworkViewRenderer renderer = applicationManager.getDefaultNetworkViewRenderer();
		CyNetworkViewFactory viewFactory = renderer.getNetworkViewFactory();
		return viewFactory.createNetworkView(network);
	}
}
