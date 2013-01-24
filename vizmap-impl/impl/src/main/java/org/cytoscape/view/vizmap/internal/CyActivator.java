package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		final CyServiceRegistrar serviceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);

		// Mapping Factories
		DiscreteMappingFactory discreteMappingFactory = new DiscreteMappingFactory(cyEventHelperServiceRef);
		ContinuousMappingFactory continuousMappingFactory = new ContinuousMappingFactory(cyEventHelperServiceRef);
		PassthroughMappingFactory passthroughMappingFactory = new PassthroughMappingFactory(cyEventHelperServiceRef);
		
		VisualLexiconManager visualLexiconManager = new VisualLexiconManager();
		VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(visualLexiconManager,
				serviceRegistrarServiceRef, passthroughMappingFactory, cyEventHelperServiceRef);
		VisualMappingManagerImpl visualMappingManager = new VisualMappingManagerImpl(cyEventHelperServiceRef,
				visualStyleFactory, visualLexiconManager);
		
		registerAllServices(bc, visualMappingManager, new Properties());
		registerService(bc, visualStyleFactory, VisualStyleFactory.class, new Properties());

		Properties discreteMappingFactoryProps = new Properties();
		discreteMappingFactoryProps.setProperty("service.type", "factory");
		discreteMappingFactoryProps.setProperty("mapping.type", "discrete");
		registerService(bc, discreteMappingFactory, VisualMappingFunctionFactory.class, discreteMappingFactoryProps);

		Properties continuousMappingFactoryProps = new Properties();
		continuousMappingFactoryProps.setProperty("service.type", "factory");
		continuousMappingFactoryProps.setProperty("mapping.type", "continuous");
		registerService(bc, continuousMappingFactory, VisualMappingFunctionFactory.class, continuousMappingFactoryProps);

		Properties passthroughMappingFactoryProps = new Properties();
		passthroughMappingFactoryProps.setProperty("service.type", "factory");
		passthroughMappingFactoryProps.setProperty("mapping.type", "passthrough");
		registerService(bc, passthroughMappingFactory, VisualMappingFunctionFactory.class,
				passthroughMappingFactoryProps);

		registerServiceListener(bc, visualLexiconManager, "addRenderingEngineFactory", "removeRenderingEngineFactory",
				RenderingEngineFactory.class);
		
		registerServiceListener(bc,passthroughMappingFactory,"addValueTranslator","removeValueTranslator", ValueTranslator.class);
	}
}
