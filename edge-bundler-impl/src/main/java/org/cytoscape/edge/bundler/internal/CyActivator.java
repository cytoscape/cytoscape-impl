package org.cytoscape.edge.bundler.internal;

import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Edge Bundler Impl (edge-bundler-impl)
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
		
		{
			EdgeBundlerTaskFactory factory = new EdgeBundlerTaskFactory(0, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_MENU, "Layout.Bundle Edges");
			props.setProperty(MENU_GRAVITY, "11.0");
			props.setProperty(TITLE, "All Nodes and Edges");
			registerAllServices(bc, factory, props);
		}
		{
			EdgeBundlerTaskFactory factory = new EdgeBundlerTaskFactory(1, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_MENU, "Layout.Bundle Edges");
			props.setProperty(MENU_GRAVITY, "12.0");
			props.setProperty(TITLE, "Selected Nodes Only");
			registerAllServices(bc, factory, props);
		}
		{
			EdgeBundlerTaskFactory factory = new EdgeBundlerTaskFactory(2, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_MENU, "Layout.Bundle Edges");
			props.setProperty(MENU_GRAVITY, "13.0");
			props.setProperty(TITLE, "Selected Edges Only");
			registerAllServices(bc, factory, props);
		}
	}
}
