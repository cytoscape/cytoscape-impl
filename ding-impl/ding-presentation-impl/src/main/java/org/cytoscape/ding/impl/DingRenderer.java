package org.cytoscape.ding.impl;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.NetworkViewRenderer;
import org.cytoscape.view.presentation.RenderingEngineFactory;

public class DingRenderer implements NetworkViewRenderer {

	private CyNetworkViewFactory viewFactory;
	private Map<String, RenderingEngineFactory<CyNetwork>> renderingEngineFactories;

	public DingRenderer(CyNetworkViewFactory viewFactory,
			RenderingEngineFactory<CyNetwork> defaultEngineFactory,
			RenderingEngineFactory<CyNetwork> birdsEyeViewEngineFactory) {
		this.viewFactory = viewFactory;
		
		renderingEngineFactories = new HashMap<String, RenderingEngineFactory<CyNetwork>>();
		renderingEngineFactories.put(DEFAULT_CONTEXT, defaultEngineFactory);
		renderingEngineFactories.put(BIRDS_EYE_CONTEXT, birdsEyeViewEngineFactory);
		renderingEngineFactories.put(VISUAL_STYLE_PREVIEW_CONTEXT, defaultEngineFactory);
	}

	@Override
	public RenderingEngineFactory<CyNetwork> getRenderingEngineFactory(String contextId) {
		return renderingEngineFactories.get(contextId);
	}

	@Override
	public CyNetworkViewFactory getNetworkViewFactory() {
		return viewFactory;
	}

}
