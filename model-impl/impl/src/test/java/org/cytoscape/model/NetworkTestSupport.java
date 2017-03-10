package org.cytoscape.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;

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

/**
 * Provides utility methods to create actual network instances for testing.
 */
public class NetworkTestSupport {

	protected CyNetworkFactory networkFactory;
	protected CyEventHelper eventHelper = new DummyCyEventHelper();
	protected CyTableManagerImpl tableMgr;
	protected CyNetworkTableManagerImpl networkTableMgr;
	protected CyRootNetworkManager rootNetworkManager;
	protected CyNetworkManager networkMgr;
	protected CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class, withSettings().stubOnly());
	
	private CyNetworkNaming namingUtil = mock(CyNetworkNaming.class, withSettings().stubOnly());
	private EquationCompiler compiler = new EquationCompilerImpl(new EquationParserImpl(serviceRegistrar));
	private Interpreter interpreter = mock(Interpreter.class);
	
	public NetworkTestSupport() {
		// Mock objects.
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
		when(serviceRegistrar.getService(EquationCompiler.class)).thenReturn(compiler);
		when(serviceRegistrar.getService(Interpreter.class)).thenReturn(interpreter);
		
		networkTableMgr = new CyNetworkTableManagerImpl();
		networkMgr = new CyNetworkManagerImpl(serviceRegistrar);
		tableMgr = new CyTableManagerImpl(networkTableMgr, networkMgr, serviceRegistrar); 
		
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper, serviceRegistrar);
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
