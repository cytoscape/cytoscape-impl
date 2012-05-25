package org.cytoscape.view.layout;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class LayoutPartitionTest {

	private LayoutPartition partition;

	final NetworkViewTestSupport support = new NetworkViewTestSupport();
	final CyNetwork network = support.getNetworkFactory().createNetwork();
	CyNode source = network.addNode();
	CyNode target = network.addNode();
	CyEdge edge = network.addEdge(source, target, true);

	final CyNetworkView networkView = support.getNetworkViewFactory().createNetworkView(network);

	@Before
	public void setUp() throws Exception {
		partition = new LayoutPartition(100, 200);
	}

	@Test
	public void testLayoutPartitionConstructorTest1() {
		assertNotNull(partition);
	}

	@Test
	public void testLayoutPartitionConstructorTest2() {

		Set<View<CyNode>> nodesToLayOut = new HashSet<View<CyNode>>();
		nodesToLayOut.add(networkView.getNodeView(source));
		nodesToLayOut.add(networkView.getNodeView(target));
		EdgeWeighter edgeWeighter = new EdgeWeighter();
		LayoutPartition partition2 = new LayoutPartition(networkView, nodesToLayOut, edgeWeighter);

		assertNotNull(partition2);
	}

	@Test
	public void testSetEdgeWeighter() {
		EdgeWeighter ew = new EdgeWeighter();
		partition.setEdgeWeighter(ew);
	}

	@Test
	public void testAddNode() {
		partition.addNode(network, networkView.getNodeView(source), false);
		assertEquals(1, partition.getNodeList().size());
	}

	@Test
	public void testAddEdge1() {
		CyRow row = network.getRow(edge);
		partition.addEdge(edge, row);
		assertEquals(1, partition.getEdgeList().size());
	}

	@Test
	public void testAddEdge2() {
		CyRow row = network.getRow(edge);
		partition.addEdge(edge, new LayoutNode(networkView.getNodeView(source), 0, network.getRow(source)),
				new LayoutNode(networkView.getNodeView(target), 1, network.getRow(target)), row);
		assertEquals(1, partition.getEdgeList().size());
	}

	@Test
	public void testRandomizeLocations() {
		final Double originalS = networkView.getNodeView(source).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		partition.addNode(network, networkView.getNodeView(source), false);
		partition.randomizeLocations();
		partition.moveNodeToLocation(partition.getNodeList().get(0));

		assertFalse(originalS.equals(networkView.getNodeView(partition.getNodeList().get(0).getNode())
				.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)));

	}


	@Test
	public void testFixEdges() {
		partition.fixEdges();
	}

	@Test
	public void testCalculateEdgeWeights() {
		partition.calculateEdgeWeights();
	}

	@Test
	public void testSize() {
		assertEquals(0, partition.size());
		
		partition.addNode(network, networkView.getNodeView(source), false);
		assertEquals(1, partition.size());
		partition.addNode(network, networkView.getNodeView(target), false);
		assertEquals(2, partition.size());
	}

	@Test
	public void testGetNodeList() {
		partition.addNode(network, networkView.getNodeView(source), false);
		assertEquals(source, partition.getNodeList().get(0).getNode());
	}

	@Test
	public void testGetEdgeList() {
		partition.addNode(network, networkView.getNodeView(source), false);
		partition.addNode(network, networkView.getNodeView(target), false);
		CyRow row = network.getRow(edge);
		partition.addEdge(edge, row);
		assertEquals(edge, partition.getEdgeList().get(0).getEdge());
	}

	@Test
	public void testNodeIterator() {
		assertNotNull(partition.nodeIterator());
	}

	@Test
	public void testEdgeIterator() {
		assertNotNull(partition.edgeIterator());
	}

	@Test
	public void testCount() {

		partition.addNode(network, networkView.getNodeView(source), false);
		partition.addNode(network, networkView.getNodeView(target), false);
		CyRow row = network.getRow(edge);
		partition.addEdge(edge, row);
		assertEquals(1, partition.edgeCount());
		assertEquals(2, partition.nodeCount());
	}

	@Test
	public void testGetMaxX() {
		assertTrue(-100000 == partition.getMaxX());
	}

	@Test
	public void testGetMaxY() {
		assertTrue(-100000 == partition.getMaxY());
	}

	@Test
	public void testGetMinX() {
		assertTrue(100000 == partition.getMinX());
	}

	@Test
	public void testGetMinY() {
		assertTrue(100000 == partition.getMinY());
	}

	@Test
	public void testGetWidth() {
		assertTrue(0 == partition.getWidth());
	}

	@Test
	public void testGetHeight() {
		assertTrue(0 == partition.getHeight());
	}

	@Test
	public void testGetPartitionNumber() {
		assertTrue(1 == partition.getPartitionNumber());
	}

	@Test
	public void testSetPartitionNumber() {
		assertTrue(1 == partition.getPartitionNumber());
		partition.setPartitionNumber(2);
		assertTrue(2 == partition.getPartitionNumber());
	}

	@Test
	public void testLockedNodeCount() {
		int locked = partition.lockedNodeCount();
		assertTrue(0 == locked);
	}

	@Test
	public void testGetAverageLocation() {
		partition.getAverageLocation();
	}

	@Test
	public void testOffset() {
		partition.offset(10d, 20d);
	}

	@Test
	public void testResetNodes() {
		partition.resetNodes();
	}

	@Test
	public void testTrimToSize() {
		partition.trimToSize();
	}

}
