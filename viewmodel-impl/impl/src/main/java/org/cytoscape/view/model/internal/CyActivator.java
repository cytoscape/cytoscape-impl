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
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.internal.network.CyNetworkViewFactoryProviderImpl;
import org.cytoscape.view.model.internal.network.CyNetworkViewManagerImpl;
import org.cytoscape.view.model.internal.table.CyTableViewFactoryImpl;
import org.cytoscape.view.model.internal.table.CyTableViewFactoryProviderImpl;
import org.cytoscape.view.model.internal.table.CyTableViewManagerImpl;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewFactoryProvider;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		registerNetworkViewSupport(bc);
		registerTableViewSupport(bc);
	}
	
	private void registerNetworkViewSupport(BundleContext bc) {
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		CyNetworkViewManagerImpl networkViewManager = new CyNetworkViewManagerImpl(registrar);
		registerAllServices(bc, networkViewManager);

		NullCyNetworkViewFactory nullCyNetworkViewFactory = new NullCyNetworkViewFactory();
		Properties nullViewFactoryProperties = new Properties();
		nullViewFactoryProperties.put("id", "NullCyNetworkViewFactory");
		registerService(bc, nullCyNetworkViewFactory, CyNetworkViewFactory.class, nullViewFactoryProperties);
		
		CyNetworkViewFactoryProvider factoryFactory = new CyNetworkViewFactoryProviderImpl(registrar);
		registerService(bc, factoryFactory, CyNetworkViewFactoryProvider.class);
	}
	
	private void registerTableViewSupport(BundleContext bc) {
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		CyTableViewManager tableViewManager = new CyTableViewManagerImpl(registrar);
		registerAllServices(bc, tableViewManager);
		
		// TEMPORARY
		// The table renderer should provide the table view factory
		CyTableViewFactory tableViewFactory = new CyTableViewFactoryImpl(registrar, BasicTableVisualLexicon.getInstance(), "TABLE");
		registerService(bc, tableViewFactory, CyTableViewFactory.class);
		
		CyTableViewFactoryProvider tableViewFactoryProvider = new CyTableViewFactoryProviderImpl(registrar);
		registerService(bc, tableViewFactoryProvider, CyTableViewFactoryProvider.class);
	}
	
}
