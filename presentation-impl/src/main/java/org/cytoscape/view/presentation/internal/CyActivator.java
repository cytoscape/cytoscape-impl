package org.cytoscape.view.presentation.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.internal.property.values.CyColumnIdentifierFactoryImpl;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Presentation Impl (presentation-impl)
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		RenderingEngineManagerImpl renderingEngineManager = new RenderingEngineManagerImpl(serviceRegistrar);

		Properties renderingEngineManagerProps = new Properties();
		renderingEngineManagerProps.setProperty("service.type", "manager");
		registerAllServices(bc, renderingEngineManager, renderingEngineManagerProps);

		registerServiceListener(bc, renderingEngineManager, "addRenderingEngineFactory",
				"removeRenderingEngineFactory", RenderingEngineFactory.class);
		
		CyColumnIdentifierFactory cyColumnIdentifierFactory = new CyColumnIdentifierFactoryImpl();
		registerAllServices(bc, cyColumnIdentifierFactory, new Properties());
	}
}
