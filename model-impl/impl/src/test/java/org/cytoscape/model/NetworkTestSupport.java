package org.cytoscape.model;


import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;


public class NetworkTestSupport {

	protected CyNetworkFactory networkFactory;
	protected CyEventHelper eventHelper;
	protected CyTableManagerImpl tableMgr;
	protected CyNetworkTableManagerImpl networkTableMgr;
	protected CyRootNetworkManager rootNetworkManager;

	public NetworkTestSupport() {
		eventHelper = new DummyCyEventHelper();
		tableMgr = mock(CyTableManagerImpl.class); 
		networkTableMgr = mock(CyNetworkTableManagerImpl.class);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper, mock(Interpreter.class), serviceRegistrar);
		networkFactory = new CyNetworkFactoryImpl(eventHelper, tableMgr, networkTableMgr, tableFactory, serviceRegistrar);
		rootNetworkManager = new CyRootNetworkManagerImpl();
	}

	public CyNetwork getNetwork() {
		return networkFactory.getInstance();
	}

	public CyNetworkFactory getNetworkFactory() {
		return networkFactory;	
	}

	public CyRootNetworkManager getRootNetworkFactory() {
		return rootNetworkManager;
	}
}


