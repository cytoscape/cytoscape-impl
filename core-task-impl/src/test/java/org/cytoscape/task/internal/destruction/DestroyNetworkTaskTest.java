package org.cytoscape.task.internal.destruction;


import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.test.support.NetworkTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DestroyNetworkTaskTest {
	
	private final NetworkTestSupport support = new NetworkTestSupport();
	
	private CyNetworkManager netmgr;

	@Before
	public void setUp() throws Exception {
		netmgr = mock(CyNetworkManager.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDestroyNetworkTask() throws Exception {
		final CyNetwork network1 = support.getNetwork();
		final CyNetwork network2 = support.getNetwork();
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		networks.add(network1);
		networks.add(network2);
		
		final DestroyNetworkTask task = new DestroyNetworkTask(networks, netmgr);
		task.run(null);
		
		verify(netmgr, times(1)).destroyNetwork(network1);
		verify(netmgr, times(1)).destroyNetwork(network2);
	}
	
}
