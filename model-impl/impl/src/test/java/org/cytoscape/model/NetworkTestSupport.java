
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

import static org.mockito.Mockito.*;

public class NetworkTestSupport {

	protected CyNetworkFactory networkFactory;
	protected CyEventHelper eventHelper;
	protected CyTableManagerImpl tableMgr;

	public NetworkTestSupport() {
		eventHelper = new DummyCyEventHelper();
		tableMgr = mock(CyTableManagerImpl.class); 
		CyTableFactoryImpl tableFactory = new CyTableFactoryImpl(eventHelper,tableMgr, mock(Interpreter.class));

		networkFactory = new CyNetworkFactoryImpl( eventHelper, tableMgr, tableFactory );
	}

	public CyNetwork getNetwork() {
		return networkFactory.getInstance();
	}

	public CyNetworkFactory getNetworkFactory() {
		return networkFactory;	
	}
}


