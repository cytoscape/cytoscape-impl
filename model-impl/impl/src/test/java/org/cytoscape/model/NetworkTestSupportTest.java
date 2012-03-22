package org.cytoscape.model;

import org.cytoscape.model.AbstractCyNetworkTest;
import org.junit.After;
import org.junit.Before;

/**
 * This will verify that the network created by NetworkTestSupport
 * is a good network.
 */
public class NetworkTestSupportTest extends AbstractCyNetworkTest {

	private NetworkTestSupport support;
	
	@Before
	public void setUp() {
		support = new NetworkTestSupport();
		net = support.getNetwork();
	}

	@After
	public void tearDown() {
		net = null;
	}

}
