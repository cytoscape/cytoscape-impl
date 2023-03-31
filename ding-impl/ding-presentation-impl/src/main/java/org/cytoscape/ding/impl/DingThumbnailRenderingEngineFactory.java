package org.cytoscape.ding.impl;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;


/**
 * RenderingEngineFactory for Navigation.
 */
public class DingThumbnailRenderingEngineFactory implements RenderingEngineFactory<CyNetwork> {
	
	private final DingNetworkViewFactory viewFactoryMediator;
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;

	public DingThumbnailRenderingEngineFactory(DingNetworkViewFactory viewFactoryMediator, VisualLexicon dingLexicon, CyServiceRegistrar registrar) {
		this.viewFactoryMediator = viewFactoryMediator;
		this.dingLexicon = dingLexicon;
		this.registrar = registrar;
	}

	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(final Object visualizationContainer, final View<CyNetwork> view) {
		if (visualizationContainer == null)
			throw new IllegalArgumentException("Visualization container is null.  This should be an JComponent for this rendering engine.");
		if (view == null)
			throw new IllegalArgumentException("View Model is null.");

		// Check data type compatibility.
		if (!(visualizationContainer instanceof JComponent) || !(view instanceof CyNetworkView))
			throw new IllegalArgumentException("Visualization Container object is not of type Component, "
					+ "which is invalid for this implementation of PresentationFactory");
		
		DRenderingEngine re = viewFactoryMediator.getRenderingEngine((CyNetworkView)view);
		if(re == null)
			throw new IllegalStateException("Rendering engine not available to create thumbnail");
		
		JComponent container = (JComponent) visualizationContainer;
		ThumbnailView tv = new ThumbnailView(re);
		
		container.setLayout(new BorderLayout());
		container.add(tv, BorderLayout.CENTER);
		return tv;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}
}
