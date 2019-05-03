package org.cytoscape.ding.impl;

import java.awt.BorderLayout;

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
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

		DRenderingEngine re = viewFactory.getRenderingEngine((CyNetworkView) view);
		
		if (container instanceof RootPaneContainer) {
			RootPaneContainer rootPane = (RootPaneContainer) container;
			InputHandlerGlassPane glassPane = re.getInputHandlerGlassPane();
			rootPane.setGlassPane(glassPane);
			rootPane.setContentPane(new InternalFrameComponent(rootPane.getLayeredPane(), re));
			glassPane.setVisible(true);
		} else if (container instanceof JComponent){
			JComponent component = (JComponent) container;
			component.setLayout(new BorderLayout());
			component.add(re.getCanvas(), BorderLayout.CENTER);
		} else {
			throw new IllegalArgumentException("visualizationContainer object must be of type JComponent or RootPaneContainer");
		}
		
		return re;
	}

	
	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}	
}
