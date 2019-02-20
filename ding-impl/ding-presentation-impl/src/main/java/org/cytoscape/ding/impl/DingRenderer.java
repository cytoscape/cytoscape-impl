package org.cytoscape.ding.impl;

import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;

public class DingRenderer implements NetworkViewRenderer {
	
	public static final String ID = "org.cytoscape.ding";
	public static final String DISPLAY_NAME = "Cytoscape 2D";
	
	private final DingNetworkViewFactoryMediator viewFactory;
	
	private final DingRenderingEngineFactory defaultEngineFactory;
	private final DingNavigationRenderingEngineFactory navigationEngineFactory;
	private final DingRenderingEngineFactory visualStyleRenderingFactory;
	private final DingThumbnailRenderingEngineFactory thumbnailEngineFactory;
	
	
	public DingRenderer(
			DingNetworkViewFactoryMediator viewFactory, 
			DVisualLexicon dVisualLexicon, 
			ViewTaskFactoryListener vtfListener, 
			AnnotationFactoryManager annotationFactoryManager, 
			DingGraphLOD dingGraphLOD, 
			HandleFactory handleFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		this.viewFactory = viewFactory;
		
		defaultEngineFactory =
				new DingRenderingEngineFactory(viewFactory, dVisualLexicon, vtfListener, annotationFactoryManager, dingGraphLOD, handleFactory, serviceRegistrar);
		navigationEngineFactory =
				new DingNavigationRenderingEngineFactory(viewFactory, serviceRegistrar, dVisualLexicon);
		visualStyleRenderingFactory =
				new DingVisualStyleRenderingEngineFactory(dVisualLexicon, vtfListener, annotationFactoryManager, dingGraphLOD, handleFactory, serviceRegistrar);
		thumbnailEngineFactory =
				new DingThumbnailRenderingEngineFactory(viewFactory, dVisualLexicon, serviceRegistrar);
	}

	public DRenderingEngine getRenderingEngine(CyNetworkView view) {
		return viewFactory.getRenderingEngine(view);
	}
	
	@Override
	public RenderingEngineFactory<CyNetwork> getRenderingEngineFactory(String contextId) {
		switch(contextId) {
			case DEFAULT_CONTEXT:              return defaultEngineFactory;
			case BIRDS_EYE_CONTEXT:            return navigationEngineFactory;
			case VISUAL_STYLE_PREVIEW_CONTEXT: return visualStyleRenderingFactory;
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
