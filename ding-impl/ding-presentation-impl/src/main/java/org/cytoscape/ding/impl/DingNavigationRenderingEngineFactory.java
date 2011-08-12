package org.cytoscape.ding.impl;


import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEventListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * RenderingEngineFactory for Navigation.
 * 
 */
public class DingNavigationRenderingEngineFactory implements
		RenderingEngineFactory<CyNetwork>, UpdateNetworkPresentationEventListener
{
	private static final Logger logger = LoggerFactory.getLogger(DingNavigationRenderingEngineFactory.class);
	
	private final RenderingEngineManager renderingEngineManager;
	private final VisualLexicon dingLexicon;
	private final Map<CyNetworkView, DGraphView> viewMap;
	private final CyApplicationManager appManager;

	public DingNavigationRenderingEngineFactory(VisualLexicon dingLexicon,
			RenderingEngineManager renderingEngineManager, CyApplicationManager appManager) {

		this.dingLexicon = dingLexicon;
		this.renderingEngineManager = renderingEngineManager;
		this.appManager = appManager;

		viewMap = new HashMap<CyNetworkView, DGraphView>();
	}
	
	
	@Override public RenderingEngine<CyNetwork> getInstance(final Object visualizationContainer, final View<CyNetwork> view) {

		if (visualizationContainer == null)
			throw new IllegalArgumentException(
					"Visualization container is null.  This should be an JComponent for this rendering engine.");
		if (view == null)
			throw new IllegalArgumentException(
					"View Model is null.");

		if (!(visualizationContainer instanceof JComponent)
				|| !(view instanceof CyNetworkView))
			throw new IllegalArgumentException(
					"Visualization Container object is not of type Component, "
							+ "which is invalid for this implementation of PresentationFactory");
		
		logger.debug("Start adding BEV.");
		final JComponent container = (JComponent) visualizationContainer;
		final RenderingEngine<CyNetwork> engine = appManager.getCurrentRenderingEngine();
		final BirdsEyeView bev = new BirdsEyeView((DGraphView) engine);
		
		container.setLayout(new BorderLayout());
		container.add(bev, BorderLayout.CENTER);
		
		this.renderingEngineManager.addRenderingEngine(bev);
		
		logger.debug("Bird's Eye View had been set to the component.  Network Model = " + view.getModel().getSUID());
		return bev;
	}

	
	/**
	 * Catch the events from view model layer.
	 * 
	 */
	@Override
	public void handleEvent(UpdateNetworkPresentationEvent nvce) {
		DGraphView gv = viewMap.get(nvce.getSource());
		if (gv != null)
			gv.updateView();
	}


	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}

}
