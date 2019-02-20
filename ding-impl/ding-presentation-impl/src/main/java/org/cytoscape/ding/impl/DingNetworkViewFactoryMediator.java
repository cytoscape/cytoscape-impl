package org.cytoscape.ding.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;

public class DingNetworkViewFactoryMediator implements CyNetworkViewFactory, NetworkViewAboutToBeDestroyedListener {

	private final CyNetworkViewFactory delegateFactory;
	private final Map<CyNetworkView, DRenderingEngine> mainRenderingEngines = new HashMap<>();

	public DingNetworkViewFactoryMediator(CyNetworkViewFactory delegateFactory) {
		this.delegateFactory = Objects.requireNonNull(delegateFactory);
	}

	@Override
	public CyNetworkView createNetworkView(CyNetwork network) {
		CyNetworkView networkView = delegateFactory.createNetworkView(network);
		
		DRenderingEngine re = createRenderingEngine(networkView);
		networkView.addNetworkViewListener(re);
		mainRenderingEngines.put(networkView, re);
		
		return networkView;
	}
	
	private DRenderingEngine createRenderingEngine(CyNetworkView networkView) {
		DRenderingEngine re = new DRenderingEngine();
		return re;
	}
	
	public DRenderingEngine getRenderingEngine(CyNetworkView networkView) {
		return mainRenderingEngines.get(networkView);
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		mainRenderingEngines.remove(e.getNetworkView());
	}
	
}
