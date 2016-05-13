package org.cytoscape.view.model.internal;

/*
 * #%L
 * Cytoscape View Model Impl (viewmodel-impl)
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

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		CyNetworkViewManagerImpl cyNetworkViewManager = new CyNetworkViewManagerImpl(serviceRegistrar);
		registerAllServices(bc, cyNetworkViewManager, new Properties());

		NullCyNetworkViewFactory nullCyNetworkViewFactory = new NullCyNetworkViewFactory();
		Properties nullViewFactoryProperties = new Properties();
		nullViewFactoryProperties.put("id", "NullCyNetworkViewFactory");
		registerService(bc, nullCyNetworkViewFactory, CyNetworkViewFactory.class, nullViewFactoryProperties);
	}
}
