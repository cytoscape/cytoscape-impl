package org.cytoscape.ding.impl;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

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

public class DingRenderingEngineFactory implements RenderingEngineFactory<CyNetwork> {
	
	private final DingNetworkViewFactory viewFactory;
	private final VisualLexicon dingLexicon;

	public DingRenderingEngineFactory(DingNetworkViewFactory viewFactory, VisualLexicon dingLexicon) {
		this.viewFactory = viewFactory;
		this.dingLexicon = dingLexicon;
	}

	/**
	 * Render given view model by Ding rendering engine.
	 */
	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(Object container, View<CyNetwork> view) {
		if (container == null)
			throw new IllegalArgumentException("Container is null.");
		if (view == null)
			throw new IllegalArgumentException("Cannot create presentation for null view model.");
		if (view instanceof CyNetworkView == false)
			throw new IllegalArgumentException("Ding accepts CyNetworkView only.");

		CyNetworkView networkView = (CyNetworkView) view;
		if(!DingRenderer.ID.equals(networkView.getRendererId())) {
			throw new IllegalArgumentException("The given network view was not created by the ding renderer: '" +  networkView.getRendererId() + "'");
		}
		
		DRenderingEngine re = viewFactory.getRenderingEngine(networkView);
		if(re == null) {
			throw new IllegalArgumentException("The given network view is not registered with the CyNetworkViewManager service. "
					+ "Its possible the network view has not been registered yet, or it was registered and has already been destroyed.");
		}
		
		if (container instanceof RootPaneContainer rootPane) {
			re.install(rootPane);
		} else if (container instanceof JComponent component) {
			re.install(component);
		} else {
			throw new IllegalArgumentException("container object must be of type JComponent or RootPaneContainer");
		}
		
		return re;
	}
	
	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}	
}
