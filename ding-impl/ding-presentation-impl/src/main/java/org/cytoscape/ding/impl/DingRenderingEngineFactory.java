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
	public RenderingEngine<CyNetwork> createRenderingEngine(final Object presentationContainer, final View<CyNetwork> view) {
		if (presentationContainer == null)
			throw new IllegalArgumentException("Container is null.");
		if (view == null)
			throw new IllegalArgumentException("Cannot create presentation for null view model.");
		if (view instanceof CyNetworkView == false)
			throw new IllegalArgumentException("Ding accepts CyNetworkView only.");

		final CyNetworkView targetView = (CyNetworkView) view;
		DRenderingEngine re = null;
		
		if (presentationContainer instanceof JComponent || presentationContainer instanceof RootPaneContainer) {
			re = viewFactory.getRenderingEngine(targetView);
			
			if (presentationContainer instanceof RootPaneContainer) {
				final RootPaneContainer container = (RootPaneContainer) presentationContainer;
				final InternalFrameComponent ifComp = new InternalFrameComponent(container.getLayeredPane(), re);
				container.setContentPane(ifComp);
			} else {
				final JComponent component = (JComponent) presentationContainer;
				component.setLayout(new BorderLayout());
				component.add(re.getCanvas(), BorderLayout.CENTER);
			}
		} else {
			throw new IllegalArgumentException(
					"frame object is not of type JComponent or RootPaneContainer, which is invalid for this implementation of PresentationFactory");
		}

		return re;
	}

//	/**
//	 * This method simply redraw the canvas, NOT updating the view model. To
//	 * apply and draw the new view model, you need to call this after apply.
//	 * 
//	 */
//	@Override
//	public void handleEvent(UpdateNetworkPresentationEvent nvce) {
//		DGraphView gv = vtfListener.viewMap.get(nvce.getSource());
//		logger.debug("NetworkViewChangedEvent listener got view update request: "
//				+ nvce.getSource().getSUID());
//		if (gv != null)
//			gv.updateView();
//	}

	
	@Override
	public VisualLexicon getVisualLexicon() {
		return dingLexicon;
	}	
}
