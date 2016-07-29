package org.cytoscape.command.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;

public class StringToModelTest {
	
	
	@Test
	public void testNodeListParsing() {
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		
		network.getRow(n1).set("name", "node 1");
		network.getRow(n2).set("name", "node 2");
		network.getRow(n3).set("name", "node,3");
		network.getRow(n4).set("name", "node:4");
		network.getRow(n5).set("name", "node:5,5");
		network.getRow(n6).set("name", "node\\:6");
		
		StringToModel stringToModel = new StringToModelImpl(null, null, null, null);
		List<CyNode> nodes;
		
		nodes = stringToModel.getNodeList(network, "node 1,node 2");
		assertEquals(2, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		
		nodes = stringToModel.getNodeList(network, "name:node 1,name:node 2");
		assertEquals(2, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		
		nodes = stringToModel.getNodeList(network, "name:node\\,3,name:node 1,name:node 2");
		assertEquals(3, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		assertTrue(nodes.contains(n3));
		
		nodes = stringToModel.getNodeList(network, "name:node\\:4,name:node 1,name:node 2");
		assertEquals(3, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		assertTrue(nodes.contains(n4));
		
		nodes = stringToModel.getNodeList(network, "name:node\\:5\\,5");
		assertEquals(1, nodes.size());
		assertTrue(nodes.contains(n5));
		
		nodes = stringToModel.getNodeList(network, "node\\\\:6");
		assertEquals(1, nodes.size());
		assertTrue(nodes.contains(n6));
	}

}
