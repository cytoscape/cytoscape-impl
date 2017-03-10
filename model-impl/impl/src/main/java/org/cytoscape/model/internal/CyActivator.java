package org.cytoscape.model.internal;

import java.util.Properties;

import org.cytoscape.equations.event.EquationFunctionAddedListener;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

		TableEventHelperFacade tableEventHelper = new TableEventHelperFacade(serviceRegistrar);
		
		CyNetworkManagerImpl networkManager = new CyNetworkManagerImpl(serviceRegistrar);
		CyNetworkTableManagerImpl networkTableManager = new CyNetworkTableManagerImpl();
		CyTableManagerImpl tableManager = new CyTableManagerImpl(networkTableManager, networkManager, serviceRegistrar);
		CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(tableEventHelper, serviceRegistrar);
		CyNetworkFactoryImpl networkFactory = new CyNetworkFactoryImpl(tableEventHelper, tableManager,
				networkTableManager, tableFactory, serviceRegistrar);
		CyRootNetworkManagerImpl rootNetworkFactory = new CyRootNetworkManagerImpl();

		registerService(bc, networkFactory, CyNetworkFactory.class, new Properties());
		registerService(bc, tableFactory, CyTableFactory.class, new Properties());
		registerService(bc, rootNetworkFactory, CyRootNetworkManager.class, new Properties());
		registerService(bc, tableManager, CyTableManager.class, new Properties());
		registerAllServices(bc, networkTableManager, new Properties());
		registerService(bc, tableManager, NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, tableManager, EquationFunctionAddedListener.class, new Properties());
		registerService(bc, networkManager, CyNetworkManager.class, new Properties());
	}
}
