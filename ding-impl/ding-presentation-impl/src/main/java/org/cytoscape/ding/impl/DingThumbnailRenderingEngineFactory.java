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
	
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;

	public DingThumbnailRenderingEngineFactory(final VisualLexicon dingLexicon, final CyServiceRegistrar registrar) {
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
		
		if (!(view instanceof DGraphView))
			throw new IllegalArgumentException("This rendering engine needs DING view model as its view model.");
		
		
		// Shared instance of the view.
		final DGraphView dgv = (DGraphView) view;
		
		final JComponent container = (JComponent) visualizationContainer;
		
		// Create instance of an engine.
		final ThumbnailView bev = new ThumbnailView(dgv, registrar);
		
		bev.registerServices();

		container.setLayout(new BorderLayout());
		container.add(bev, BorderLayout.CENTER);

		// Register this rendering engine as service.

		return bev;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}
}
