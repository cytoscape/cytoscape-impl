package org.cytoscape.view.vizmap.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

		// Mapping Factories:
		final DiscreteMappingFactory dmFactory = new DiscreteMappingFactory(serviceRegistrar);
		{
			final Properties props = new Properties();
			props.setProperty("service.type", "factory");
			props.setProperty("mapping.type", "discrete");
			registerService(bc, dmFactory, VisualMappingFunctionFactory.class, props);
		}
		
		final ContinuousMappingFactory cmFactory = new ContinuousMappingFactory(serviceRegistrar);
		{
			final Properties props = new Properties();
			props.setProperty("service.type", "factory");
			props.setProperty("mapping.type", "continuous");
			registerService(bc, cmFactory, VisualMappingFunctionFactory.class, props);
		}
		
		final PassthroughMappingFactory pmFactory = new PassthroughMappingFactory(serviceRegistrar);
		{
			final Properties props = new Properties();
			props.setProperty("service.type", "factory");
			props.setProperty("mapping.type", "passthrough");
			registerService(bc, pmFactory, VisualMappingFunctionFactory.class, props);
			registerServiceListener(bc, pmFactory, "addValueTranslator", "removeValueTranslator", ValueTranslator.class);
		}
		
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(serviceRegistrar, pmFactory);
		registerService(bc, visualStyleFactory, VisualStyleFactory.class, new Properties());
		
		final VisualMappingManagerImpl visualMappingManager = new VisualMappingManagerImpl(visualStyleFactory, serviceRegistrar);
		registerAllServices(bc, visualMappingManager, new Properties());
	}
}
