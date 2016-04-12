package org.cytoscape.ding.impl;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;


/**
 * RenderingEngineFactory for Navigation.
 */
public class DingThumbnailRenderingEngineFactory implements RenderingEngineFactory<CyNetwork> {
	
	private final DingNavigationRenderingEngineFactory navigationFactory;

	public DingThumbnailRenderingEngineFactory(DingNavigationRenderingEngineFactory navigationFactory) {
		this.navigationFactory = navigationFactory;
	}

	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(final Object visualizationContainer, final View<CyNetwork> view) {
		return navigationFactory.createRenderingEngine(visualizationContainer, view, false);
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return navigationFactory.getVisualLexicon();
	}
}
