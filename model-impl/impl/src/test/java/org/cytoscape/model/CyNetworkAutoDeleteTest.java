package org.cytoscape.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.junit.Assert;
import org.junit.Test;

public class CyNetworkAutoDeleteTest {
	
	// Test auto-delete
	// test restore

	private static void rowExists(boolean exists, CyNetwork network, CyIdentifiable element) {
		Class<? extends CyIdentifiable> type;
		if(element instanceof CyNode)
			type = CyNode.class;
		else if(element instanceof CyEdge)
			type = CyEdge.class;
		else
			throw new AssertionError();
		
		Consumer<Boolean> assertion = exists ? Assert::assertTrue : Assert::assertFalse;
		assertion.accept(network.getTable(type, CyNetwork.DEFAULT_ATTRS).rowExists(element.getSUID()));
	}
	
	
//	private CyRootNetwork createTestNetwork() {
//		final String TEST_COLUMN = "testColumn";
//		
//		CyNetwork network1 = TestCyNetworkFactory.getInstance();
//		CyRootNetwork rootNetwork = ((CySubNetwork)network1).getRootNetwork();
//		
//		CyNode n1 = network1.addNode();
//		CyNode n2 = network1.addNode();
//		CyNode n3 = network1.addNode();
//		CyEdge e1 = network1.addEdge(n1, n2, false);
//		CyEdge e2 = network1.addEdge(n2, n3, false);
//		
//		CyTable defaultNodeTable = network1.getDefaultNodeTable();
//		defaultNodeTable.createColumn(TEST_COLUMN, String.class, false);
//		CyTable defaultEdgeTable = network1.getDefaultEdgeTable();
//		defaultEdgeTable.createColumn(TEST_COLUMN, String.class, false);
//		
//		network1.getRow(n1).set(TEST_COLUMN, "my node 1");
//		network1.getRow(n2).set(TEST_COLUMN, "my node 2");
//		network1.getRow(n3).set(TEST_COLUMN, "my node 3");
//		network1.getRow(e1).set(TEST_COLUMN, "my edge 1");
//		network1.getRow(e2).set(TEST_COLUMN, "my edge 2");
//		
//		rootNetwork.addSubNetwork(Arrays.asList(n1,n2,n3), Arrays.asList(e1,e2));
//		
//		return rootNetwork;
//	}
	
	
	@Test
	public void testAutoDeleteFromRootNetwork() {
		CyNetwork network1 = TestCyNetworkFactory.getInstance();
		CyRootNetwork rootNetwork = ((CySubNetwork)network1).getRootNetwork();
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();
		CyEdge e1 = network1.addEdge(n1, n2, false);
		CyEdge e2 = network1.addEdge(n2, n3, false);
		
		CyNetwork network2 = rootNetwork.addSubNetwork(Arrays.asList(n1,n2,n3), Arrays.asList(e1,e2));
		
		
		// Things should work as expected when removing a node from a single subnetwork
		network1.removeNodes(Collections.singleton(n1));
		
		assertTrue(rootNetwork.containsNode(n1));
		assertFalse(network1.containsNode(n1));
		assertTrue(network2.containsNode(n1));
		
		// should also automatically remove the edge that's connected to n1, but only from network1
		assertTrue(rootNetwork.containsEdge(e1));
		assertFalse(network1.containsEdge(e1));
		assertTrue(network2.containsEdge(e1));
		
		assertNotNull(rootNetwork.getNode(n1.getSUID()));
		assertNull(network1.getNode(n1.getSUID()));
		
		rowExists(true,  rootNetwork, n1);
		rowExists(false, network1,    n1);
		rowExists(true,  network2,    n1);
		rowExists(true,  rootNetwork, e1);
		rowExists(false, network1,    e1);
		rowExists(true,  network2,    e1);
		
		
		// Now remove n1 and e1 from the other subnetwork, that should auto-delete from the root network
		network2.removeNodes(Collections.singleton(n1));
		
		assertFalse(rootNetwork.containsNode(n1));
		assertFalse(network1.containsNode(n1));
		assertFalse(network2.containsNode(n1));
		
		assertFalse(rootNetwork.containsEdge(e1));
		assertFalse(network1.containsEdge(e1));
		assertFalse(network2.containsEdge(e1));
		
		assertNull(rootNetwork.getNode(n1.getSUID()));
		assertNull(network1.getNode(n1.getSUID()));
		
		rowExists(false, rootNetwork, n1);
		rowExists(false, network1,    n1);
		rowExists(false, network2,    n1);
		rowExists(false, rootNetwork, e1);
		rowExists(false, network1,    e1);
		rowExists(false, network2,    e1);
	}
	
	
	@Test
	public void testAutoDeleteWhenSubnetworkIsDeleted() {
		CyNetwork network1 = TestCyNetworkFactory.getInstance();
		CyRootNetwork rootNetwork = ((CySubNetwork)network1).getRootNetwork();
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();
		CyEdge e1 = network1.addEdge(n1, n2, false);
		CyEdge e2 = network1.addEdge(n2, n3, false);
		
		CyNetwork network2 = rootNetwork.addSubNetwork(Arrays.asList(n1,n2), Arrays.asList(e1));
		
		rootNetwork.removeSubNetwork((CySubNetwork)network1);
		
		assertFalse(rootNetwork.containsNode(n3));
		assertFalse(network2.containsNode(n3));
		
		assertFalse(rootNetwork.containsEdge(e2));
		assertFalse(network2.containsEdge(e2));
		
		assertNull(rootNetwork.getNode(n3.getSUID()));
		assertNull(network2.getNode(n3.getSUID()));
		
		rowExists(false, rootNetwork, n3);
		rowExists(false, rootNetwork, e2);
	}
	
	
	@Test
	public void testUndoDeletedNodeAndEdge() {
		final String TEST_COLUMN = "testColumn";
		
		CyNetwork network1 = TestCyNetworkFactory.getInstance();
		CyRootNetwork rootNetwork = ((CySubNetwork)network1).getRootNetwork();
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();
		CyEdge e1 = network1.addEdge(n1, n2, false);
		CyEdge e2 = network1.addEdge(n2, n3, false);
		
		CyTable defaultNodeTable = network1.getDefaultNodeTable();
		defaultNodeTable.createColumn(TEST_COLUMN, String.class, false);
		CyTable defaultEdgeTable = network1.getDefaultEdgeTable();
		defaultEdgeTable.createColumn(TEST_COLUMN, String.class, false);
		
		network1.getRow(n1).set(TEST_COLUMN, "my node 1");
		network1.getRow(e1).set(TEST_COLUMN, "my edge 1");
		
		CyNetwork network2 = rootNetwork.addSubNetwork(Arrays.asList(n1,n2,n3), Arrays.asList(e1,e2));
		
		network1.removeNodes(Collections.singleton(n1));
		network2.removeNodes(Collections.singleton(n1));
		
		
		assertFalse(rootNetwork.containsNode(n1));
		assertFalse(network1.containsNode(n1));
		assertFalse(network2.containsNode(n1));
		assertFalse(rootNetwork.containsEdge(e1));
		assertFalse(network1.containsEdge(e1));
		assertFalse(network2.containsEdge(e1));
		
		// restore the node to network1
		((CySubNetwork)network1).addNode(n1);
		
		assertTrue(rootNetwork.containsNode(n1));
		assertTrue(network1.containsNode(n1));
		assertFalse(network2.containsNode(n1));
		assertFalse(rootNetwork.containsEdge(e1));
		assertFalse(network1.containsEdge(e1));
		assertFalse(network2.containsEdge(e1));
		
		((CySubNetwork)network1).addEdge(e1);
		
		assertTrue(rootNetwork.containsNode(n1));
		assertTrue(network1.containsNode(n1));
		assertFalse(network2.containsNode(n1));
		assertTrue(rootNetwork.containsEdge(e1));
		assertTrue(network1.containsEdge(e1));
		assertFalse(network2.containsEdge(e1));
		
		// test that the attributes are restored
		assertEquals("my node 1", rootNetwork.getRow(n1).get(TEST_COLUMN, String.class));
		assertEquals("my edge 1", rootNetwork.getRow(e1).get(TEST_COLUMN, String.class));
	}
	
	
	@Test
	public void testUndoDeletedEdge() {
		CyNetwork network1 = TestCyNetworkFactory.getInstance();
		CyRootNetwork rootNetwork = ((CySubNetwork)network1).getRootNetwork();
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();
		CyEdge e1 = network1.addEdge(n1, n2, false);
		CyEdge e2 = network1.addEdge(n2, n3, false);
		
		
		CyNetwork network2 = rootNetwork.addSubNetwork(Arrays.asList(n1,n2,n3), Arrays.asList(e1,e2));
		
		network1.removeNodes(Collections.singleton(n1));
		network2.removeNodes(Collections.singleton(n1));
		
		assertFalse(rootNetwork.containsEdge(e1));
		assertFalse(rootNetwork.containsNode(n1));
		
		// restoring the edge should automatically restore its source and target nodes as well
		((CySubNetwork)network1).addEdge(e1);
		
		assertTrue(rootNetwork.containsNode(n1));
		assertTrue(network1.containsNode(n1));
		assertFalse(network2.containsNode(n1));
		assertTrue(rootNetwork.containsEdge(e1));
		assertTrue(network1.containsEdge(e1));
		assertFalse(network2.containsEdge(e1));
	}
	
	
	@Test
	public void testRestoreAPI() {
		CyNetwork network1 = TestCyNetworkFactory.getInstance();
		CyRootNetwork rootNetwork = ((CySubNetwork)network1).getRootNetwork();
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();
		CyEdge e1 = network1.addEdge(n1, n2, false);
		CyEdge e2 = network1.addEdge(n2, n3, false);
		
		CyNetwork network2 = rootNetwork.addSubNetwork(Arrays.asList(n1,n2,n3), Arrays.asList(e1,e2));
		
		network1.removeEdges(Collections.singleton(e1));
		network2.removeEdges(Collections.singleton(e1));
		
		assertFalse(rootNetwork.containsEdge(e1));
		
		rootNetwork.restoreEdge(e1);
		
		assertTrue(rootNetwork.containsEdge(e1));
	}
	
}
