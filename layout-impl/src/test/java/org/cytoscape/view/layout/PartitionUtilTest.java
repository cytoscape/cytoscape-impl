package org.cytoscape.view.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.junit.Test;

public class PartitionUtilTest {

	@Test
	public void testPartition() {
		NetworkViewTestSupport networkSupport = new NetworkViewTestSupport();
		CyNetwork network = networkSupport.getNetworkFactory().createNetwork();
		
		{	// Partition 1
			CyNode n1 = network.addNode();
			CyNode n2 = network.addNode();
			CyNode n3 = network.addNode();
			CyNode n4 = network.addNode();
			
			network.addEdge(n1, n2, false);
			network.addEdge(n2, n3, false);
			network.addEdge(n3, n4, false);
		}
		
		{	// Partition 2
			CyNode n1 = network.addNode();
			CyNode n2 = network.addNode();
			CyNode n3 = network.addNode();
			CyNode n4 = network.addNode();
			
			network.addEdge(n1, n2, false);
			network.addEdge(n2, n3, false);
			network.addEdge(n3, n4, false);
			network.addEdge(n4, n1, false); // cycle
		}
		
		{	// Partition 3
			CyNode n1 = network.addNode();
			CyNode n2 = network.addNode();
			CyNode n3 = network.addNode();
			CyNode n4 = network.addNode();
			
			network.addEdge(n1, n2, true);
			network.addEdge(n1, n3, true);
			network.addEdge(n1, n4, true);
			network.addEdge(n2, n3, true);
			network.addEdge(n3, n4, true);
			network.addEdge(n4, n1, true);
		}
		
		CyNetworkView networkView = networkSupport.getNetworkViewFactory().createNetworkView(network);
	
		EdgeWeighter edgeWeighter = new EdgeWeighter();
		List<LayoutPartition> result = PartitionUtil.partition(networkView, false, edgeWeighter);
		
		assertNotNull(result);
		assertEquals(3, result.size());
	}
	
}
