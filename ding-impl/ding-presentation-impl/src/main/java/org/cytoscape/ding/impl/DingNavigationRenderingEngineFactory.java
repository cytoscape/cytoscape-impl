package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * RenderingEngineFactory for Navigation.
 */
public class DingNavigationRenderingEngineFactory implements RenderingEngineFactory<CyNetwork> {
	
	private static final Logger logger = LoggerFactory.getLogger(DingNavigationRenderingEngineFactory.class);

	private final DingNetworkViewFactory networkViewFactory;
	private final VisualLexicon dingLexicon;
	private final CyServiceRegistrar registrar;

	
	public DingNavigationRenderingEngineFactory(DingNetworkViewFactory networkViewFactory, CyServiceRegistrar registrar, VisualLexicon dingLexicon) {
		this.networkViewFactory = networkViewFactory;
		this.dingLexicon = dingLexicon;
		this.registrar = registrar;
	}

	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(final Object visualizationContainer, final View<CyNetwork> view) {
		if (visualizationContainer == null)
			throw new IllegalArgumentException("Visualization container is null. This should be an JComponent for this rendering engine.");
		if (view == null)
			throw new IllegalArgumentException("View Model is null.");
		if (!(visualizationContainer instanceof JComponent) || !(view instanceof CyNetworkView))
			throw new IllegalArgumentException("Visualization Container object is not of type Component, which is invalid for this implementation of PresentationFactory");
		
		logger.debug("Start adding BEV.");
		final JComponent container = (JComponent) visualizationContainer;
		
		// The DRenderingEngine instance was created when the CyNetworkView was created
		DRenderingEngine re = networkViewFactory.getRenderingEngine((CyNetworkView)view);
		BirdsEyeView bev = new BirdsEyeView(re, registrar);

		container.setLayout(new BorderLayout());
		container.add(bev.getComponent(), BorderLayout.CENTER);

		logger.debug("Bird's Eye View had been set to the component.  Network Model = " + view.getModel().getSUID());
		return bev;
	}


	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}
}
