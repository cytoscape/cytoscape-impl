package org.cytoscape.view.model;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.CyNetworkViewManagerImpl;
import org.junit.After;


public class CyNetworkViewManagerTest extends AbstractCyNetworkViewManagerTest {
	
	protected NetworkTestSupport netTestSupport;
	protected CyNetworkViewManager viewManager;
	
	
	public CyNetworkViewManagerTest() {
		netTestSupport = new NetworkTestSupport();
	}

	@After
	public void tearDown() throws Exception {
		viewManager = null;
	}

	@Override
	protected CyNetwork newNetwork(boolean registered) {
		CyNetwork net = netTestSupport.getNetwork();
		
		if (registered)
			resgisterNetwork(net);
		
		return net;
	}

	@Override
	protected CyNetworkViewManager getViewManager() {
		if (viewManager == null)
			viewManager = new CyNetworkViewManagerImpl(eventHelper, netManager);
		
		return viewManager;
	}
}
