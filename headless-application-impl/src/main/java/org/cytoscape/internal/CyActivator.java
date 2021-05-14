package org.cytoscape.internal;

/*
 * #%L
 * Cytoscape Headless Application Impl (headless-application-impl)
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


import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		
		RenderingEngineManager renderManager = getService(bc, RenderingEngineManager.class);
		
		RenderingEngineFactory renderFactory = getService(bc,
                RenderingEngineFactory.class,
                "(id=ding)");
		
		VisualMappingManager vmanager = getService(bc, VisualMappingManager.class);
		
		NetworkAddedSetCurrent myListener = new NetworkAddedSetCurrent(appManager, renderManager, renderFactory, vmanager);
		
		registerService(bc, myListener, NetworkViewAddedListener.class, new Properties());
	}
	
}
