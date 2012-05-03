package org.cytoscape.model;

public class CyTableUtilTest extends AbstractCyTableUtilTest {
	@Override
	protected CyNetwork createNetwork() {
		NetworkTestSupport support = new NetworkTestSupport();
		return support.getNetwork();
	}
}
