package org.cytoscape.model;


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

	public NetworkTestSupport() {
		eventHelper = new DummyCyEventHelper();
		networkTableMgr = new CyNetworkTableManagerImpl();
		
		// Mock objects.
		final CyNetworkManager networkManager = new CyNetworkManagerImpl(eventHelper);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		
		tableMgr = new CyTableManagerImpl(eventHelper, networkTableMgr, networkManager); 
		
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
}
