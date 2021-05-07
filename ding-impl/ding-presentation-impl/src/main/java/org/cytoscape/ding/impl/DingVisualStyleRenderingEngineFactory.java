package org.cytoscape.ding.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;

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

public class DingVisualStyleRenderingEngineFactory extends DingRenderingEngineFactory {
	
	public DingVisualStyleRenderingEngineFactory(DingNetworkViewFactory viewFactory, VisualLexicon dingLexicon) {
		super(viewFactory, dingLexicon);
	}

	@Override
	public RenderingEngine<CyNetwork> createRenderingEngine(Object presentationContainer, View<CyNetwork> view) {
		RenderingEngine<CyNetwork> engine = super.createRenderingEngine(presentationContainer, view);
		Container component = (Container) presentationContainer;
		// Remove unnecessary mouse listeners.
		final int compCount = component.getComponentCount();
		
		for (int i = 0; i < compCount; i++) {
			final Component comp = component.getComponent(i);
			final MouseListener[] listeners = comp.getMouseListeners();
			
			for (MouseListener ml : listeners)
				comp.removeMouseListener(ml);
		}
		
		return engine;
	}
}
