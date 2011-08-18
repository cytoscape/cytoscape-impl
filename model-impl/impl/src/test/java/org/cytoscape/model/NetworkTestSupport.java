package org.cytoscape.model;


import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyTableFactory;

import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.service.util.CyServiceRegistrar;

import static org.mockito.Mockito.*;


public class NetworkTestSupport {

	protected CyNetworkFactory networkFactory;
	protected CyEventHelper eventHelper;
	protected CyTableManagerImpl tableMgr;

	public NetworkTestSupport() {
		eventHelper = new DummyCyEventHelper();
		tableMgr = mock(CyTableManagerImpl.class); 
		CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper,tableMgr, mock(Interpreter.class));
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		networkFactory =
			new CyNetworkFactoryImpl(eventHelper, tableMgr, tableFactory,
			                         serviceRegistrar);
	}

	public CyNetwork getNetwork() {
		return networkFactory.getInstance();
	}

	public CyNetworkFactory getNetworkFactory() {
		return networkFactory;	
	}
}


