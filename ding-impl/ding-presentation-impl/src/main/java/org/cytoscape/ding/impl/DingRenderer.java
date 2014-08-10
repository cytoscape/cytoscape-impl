package org.cytoscape.ding.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;

public class DingRenderer implements NetworkViewRenderer {
	
	public static final String ID = "org.cytoscape.ding";
	public static final String DISPLAY_NAME = "Cytoscape 2D";
	
	private static DingRenderer instance = new DingRenderer();
	
	public static DingRenderer getInstance() {
		return instance;
	}
	
	private CyNetworkViewFactory viewFactory;
	private Map<String, RenderingEngineFactory<CyNetwork>> renderingEngineFactories;

	private DingRenderer() {
		renderingEngineFactories = new ConcurrentHashMap<String, RenderingEngineFactory<CyNetwork>>(16, 0.75f, 2);
	}

	public void registerNetworkViewFactory(CyNetworkViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}
	
	public void registerRenderingEngineFactory(String contextId, RenderingEngineFactory<CyNetwork> engineFactory) {
		renderingEngineFactories.put(contextId, engineFactory);
	}
	
	@Override
	public RenderingEngineFactory<CyNetwork> getRenderingEngineFactory(String contextId) {
		RenderingEngineFactory<CyNetwork> factory = renderingEngineFactories.get(contextId);
		if (factory != null) {
			return factory;
		}
		return renderingEngineFactories.get(DEFAULT_CONTEXT);
	}

	@Override
	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}

	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String toString() {
		return DISPLAY_NAME;
	}
}
