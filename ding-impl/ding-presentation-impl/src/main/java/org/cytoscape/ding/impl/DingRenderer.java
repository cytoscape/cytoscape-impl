package org.cytoscape.ding.impl;

import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;

public class DingRenderer implements NetworkViewRenderer {
	
	public static final String ID = "org.cytoscape.ding";
	public static final String DISPLAY_NAME = "Cytoscape 2D";
	
	private final DingNetworkViewFactoryMediator viewFactory;
	
	private final DingRenderingEngineFactory defaultEngineFactory;
	private final DingNavigationRenderingEngineFactory navigationEngineFactory;
	private final DingRenderingEngineFactory vsRenderingFactory;
	private final DingThumbnailRenderingEngineFactory thumbnailEngineFactory;
	
	
	public DingRenderer(DingNetworkViewFactoryMediator viewFactory, DVisualLexicon dVisualLexicon, CyServiceRegistrar serviceRegistrar) {
		this.viewFactory = viewFactory;
		
		defaultEngineFactory    = new DingRenderingEngineFactory(viewFactory, dVisualLexicon);
		navigationEngineFactory = new DingNavigationRenderingEngineFactory(viewFactory, serviceRegistrar, dVisualLexicon);
		vsRenderingFactory      = new DingVisualStyleRenderingEngineFactory(viewFactory, dVisualLexicon);
		thumbnailEngineFactory  = new DingThumbnailRenderingEngineFactory(viewFactory, dVisualLexicon, serviceRegistrar);
	}

	public DRenderingEngine getRenderingEngine(CyNetworkView view) {
		return viewFactory.getRenderingEngine(view);
	}
	
	@Override
	public RenderingEngineFactory<CyNetwork> getRenderingEngineFactory(String contextId) {
		switch(contextId) {
			case DEFAULT_CONTEXT:              return defaultEngineFactory;
			case BIRDS_EYE_CONTEXT:            return navigationEngineFactory;
			case VISUAL_STYLE_PREVIEW_CONTEXT: return vsRenderingFactory;
			case THUMBNAIL_CONTEXT:            return thumbnailEngineFactory;
			default: return null;
		}
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
