package org.cytoscape.view.model;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.view.model.AbstractCyNetworkViewTest;
import org.cytoscape.view.model.internal.NetworkViewImpl;
import org.junit.After;
import org.junit.Before;


public class CyNetworkViewTest extends AbstractCyNetworkViewTest {
	
	@Before
	public void setUp() throws Exception {
		buildNetwork();
		final CyEventHelper mockHelper = new DummyCyEventHelper();
		view = new NetworkViewImpl(network, mockHelper);
	}
}
