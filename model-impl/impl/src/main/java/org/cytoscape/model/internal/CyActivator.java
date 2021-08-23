package org.cytoscape.model.internal;

import org.cytoscape.equations.event.EquationFunctionAddedListener;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		var tableEventHelper = new TableEventHelperFacade(serviceRegistrar);
		
		var networkManager = new CyNetworkManagerImpl(serviceRegistrar);
		var networkTableManager = new CyNetworkTableManagerImpl();
		var tableManager = new CyTableManagerImpl(networkTableManager, networkManager, serviceRegistrar);
		var tableFactory = new CyTableFactoryImpl(tableEventHelper, serviceRegistrar);
		var networkFactory = new CyNetworkFactoryImpl(tableEventHelper, tableManager, networkTableManager, tableFactory, serviceRegistrar);
		var rootNetworkFactory = new CyRootNetworkManagerImpl();

		registerService(bc, networkFactory, CyNetworkFactory.class);
		registerService(bc, tableFactory, CyTableFactory.class);
		registerService(bc, tableFactory, SessionLoadedListener.class);
		registerService(bc, rootNetworkFactory, CyRootNetworkManager.class);
		registerService(bc, tableManager, CyTableManager.class);
		registerService(bc, tableManager, NetworkAboutToBeDestroyedListener.class);
		registerService(bc, tableManager, EquationFunctionAddedListener.class);
		registerService(bc, networkTableManager, CyNetworkTableManager.class);
		registerService(bc, networkTableManager, TableAboutToBeDeletedListener.class);
		registerService(bc, networkManager, CyNetworkManager.class);
		
		var selectionMediator = new SelectionMediator(serviceRegistrar);
		registerService(bc, selectionMediator, RowsSetListener.class);
		registerServiceListener(bc, selectionMediator::listenerAdded, selectionMediator::listenerRemoved, SelectedNodesAndEdgesListener.class);
	}
}
