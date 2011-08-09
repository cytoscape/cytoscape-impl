package org.cytoscape.test.support;

import org.cytoscape.model.AbstractCyNetworkTest;

/**
 * This will verify that the network created by NetworkTestSupport
 * is a good network.
 */
public class NetworkTestSupportTest extends AbstractCyNetworkTest {

	public NetworkTestSupportTest() {
		NetworkTestSupport support = new NetworkTestSupport();
		net = support.getNetwork();
	}
}
