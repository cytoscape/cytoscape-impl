package org.cytoscape.view.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
import org.junit.Test;

public class NetworkViewImplTest {

	
	private NetworkTestSupport networkSupport = new NetworkTestSupport();
	
	
	@Test
	public void testSnapshot() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyEdge e1 = network.addEdge(n1, n2, false);
		CyEdge e2 = network.addEdge(n2, n3, false);
		CyEdge e3 = network.addEdge(n3, n4, false);
		CyEdge e4 = network.addEdge(n4, n1, false);
		
		CyNetworkViewImpl networkView = new CyNetworkViewImpl(network, null, "test");
		
		assertEquals(4, networkView.getNodeViews().size());
		assertEquals(4, networkView.getEdgeViews().size());
		assertNotNull(networkView.getNodeView(n1));
		
		CyNetworkViewImpl snapshot = networkView.createSnapshot();
		
		// manually sync the network to the view, normally a listener would do this
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		CyEdge e5 = network.addEdge(n5, n6, false);
		networkView.addNode(n5);
		networkView.addNode(n6);
		networkView.addEdge(e5);
		
		// real network view gets updated as expected
		assertEquals(6, networkView.getNodeViews().size());
		assertEquals(5, networkView.getEdgeViews().size());
		assertNotNull(networkView.getNodeView(n5));
		
		// snapshot should not be affected
		assertEquals(4, snapshot.getNodeViews().size());
		assertEquals(4, snapshot.getEdgeViews().size());
		assertNull(snapshot.getNodeView(n5));
	}
	
}
