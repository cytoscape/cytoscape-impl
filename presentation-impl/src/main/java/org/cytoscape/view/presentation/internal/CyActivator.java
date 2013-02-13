package org.cytoscape.view.presentation.internal;

/*
 * #%L
 * Cytoscape Presentation Impl (presentation-impl)
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

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.NetworkViewRenderer;
import org.cytoscape.view.presentation.NetworkViewRendererManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		final CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		RenderingEngineManagerImpl renderingEngineManager = new RenderingEngineManagerImpl(eventHelper);

		Properties renderingEngineManagerProps = new Properties();
		renderingEngineManagerProps.setProperty("service.type", "manager");
		registerAllServices(bc, renderingEngineManager, renderingEngineManagerProps);

		registerServiceListener(bc, renderingEngineManager, "addRenderingEngineFactory",
				"removeRenderingEngineFactory", RenderingEngineFactory.class);
		
		NetworkViewRendererManager rendererManager = new NetworkViewRendererManagerImpl();
		registerService(bc, rendererManager, NetworkViewRendererManager.class, new Properties());
		registerServiceListener(bc, rendererManager, "addNetworkViewRenderer", "removeNetworkViewRenderer", NetworkViewRenderer.class);
	}
}
