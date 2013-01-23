package org.cytoscape.internal;

/*
 * #%L
 * Cytoscape Headless Application Impl (headless-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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


import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public class NetworkAddedSetCurrent implements NetworkViewAddedListener{

	private CyApplicationManager appManager;
	private RenderingEngineManager rman;
	private RenderingEngineFactory rfactory;
	private VisualMappingManager vman;
	
	public NetworkAddedSetCurrent(CyApplicationManager appmanager, RenderingEngineManager rman, RenderingEngineFactory renderFactory, VisualMappingManager vman) {
		this.appManager = appmanager;
		this.rman = rman;
		this.rfactory = renderFactory;
		this.vman = vman;
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		e.getNetworkView().setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, 300.0);
		e.getNetworkView().setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, 300.0);
		JPanel temp = new JPanel();
		VisualStyle vs = vman.getVisualStyle(e.getNetworkView());
		vs.apply(e.getNetworkView());
		final RenderingEngine<CyNetwork> renderingEngine = rfactory.createRenderingEngine(temp, e.getNetworkView());
		rman.addRenderingEngine(renderingEngine);
		appManager.setCurrentRenderingEngine(renderingEngine);
	}

}
