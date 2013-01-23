package org.cytoscape.model;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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


import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;


/**
 * Provides utility methods to create actual network instances for testing.
 */
public class NetworkTestSupport {

	protected CyNetworkFactory networkFactory;
	protected CyEventHelper eventHelper;
	protected CyTableManagerImpl tableMgr;
	protected CyNetworkTableManagerImpl networkTableMgr;
	protected CyRootNetworkManager rootNetworkManager;
	protected CyNetworkManager networkMgr;
	
	public NetworkTestSupport() {
		eventHelper = new DummyCyEventHelper();
		networkTableMgr = new CyNetworkTableManagerImpl();
		
		// Mock objects.
		networkMgr = new CyNetworkManagerImpl(eventHelper);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		tableMgr = new CyTableManagerImpl(eventHelper, networkTableMgr, networkMgr); 
		
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper, mock(Interpreter.class), serviceRegistrar);
		networkFactory = new CyNetworkFactoryImpl(eventHelper, tableMgr, networkTableMgr, tableFactory, serviceRegistrar);
		rootNetworkManager = new CyRootNetworkManagerImpl();
	}

	public CyNetwork getNetwork() {
		return networkFactory.createNetwork();
	}

	public CyNetworkFactory getNetworkFactory() {
		return networkFactory;	
	}

	public CyRootNetworkManager getRootNetworkFactory() {
		return rootNetworkManager;
	}
	
	public CyNetworkTableManager getNetworkTableManager() {
		return networkTableMgr;
	}
	
	public CyNetworkManager getNetworkManager() {
		return networkMgr;
	}
}
