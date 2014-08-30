package org.cytoscape.edge.bundler.internal;

/*
 * #%L
 * Cytoscape Edge Bundler Impl (edge-bundler-impl)
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

import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.cytoscape.application.CyApplicationManager;

import static org.cytoscape.work.ServiceProperties.*;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		HandleFactory hf = getService(bc, HandleFactory.class);
		BendFactory bf = getService(bc, BendFactory.class);
		VisualMappingManager vmm = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory discreteFactory = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		CyApplicationManager cam = getService(bc, CyApplicationManager.class);
		
		EdgeBundlerTaskFactory edgeBundlerTaskFactory = new EdgeBundlerTaskFactory(hf, bf, vmm, discreteFactory, 0, cam);
		Properties edgeBundlerTaskFactoryProps = new Properties();
		edgeBundlerTaskFactoryProps.setProperty("id", "edgeBundlerTaskFactory");
		edgeBundlerTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		edgeBundlerTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout.Bundle Edges");
		edgeBundlerTaskFactoryProps.setProperty(MENU_GRAVITY,"11.0");
		edgeBundlerTaskFactoryProps.setProperty(TITLE,"All Nodes and Edges");
		registerService(bc,edgeBundlerTaskFactory,NetworkTaskFactory.class, edgeBundlerTaskFactoryProps);
		
		
		edgeBundlerTaskFactory = new EdgeBundlerTaskFactory(hf, bf, vmm, discreteFactory, 1, cam);
		edgeBundlerTaskFactoryProps = new Properties();
		edgeBundlerTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		edgeBundlerTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout.Bundle Edges");
		edgeBundlerTaskFactoryProps.setProperty(MENU_GRAVITY,"12.0");
		edgeBundlerTaskFactoryProps.setProperty(TITLE,"Selected Nodes Only");
		registerService(bc,edgeBundlerTaskFactory,NetworkTaskFactory.class, edgeBundlerTaskFactoryProps);
		
		
		edgeBundlerTaskFactory = new EdgeBundlerTaskFactory(hf, bf, vmm, discreteFactory, 2, cam);
		edgeBundlerTaskFactoryProps = new Properties();
		edgeBundlerTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		edgeBundlerTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout.Bundle Edges");
		edgeBundlerTaskFactoryProps.setProperty(MENU_GRAVITY,"13.0");
		edgeBundlerTaskFactoryProps.setProperty(TITLE,"Selected Edges Only");
		registerService(bc,edgeBundlerTaskFactory,NetworkTaskFactory.class, edgeBundlerTaskFactoryProps);
	}
}

