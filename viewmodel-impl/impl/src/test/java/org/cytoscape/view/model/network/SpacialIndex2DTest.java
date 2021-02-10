package org.cytoscape.view.model.network;

import static org.cytoscape.view.model.network.NetworkViewTestUtils.*;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkViewSnapshot;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.internal.network.CyNetworkViewImpl;
import org.cytoscape.view.model.spacial.EdgeSpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.NetworkSpacialIndex2D;
import org.cytoscape.view.model.spacial.NodeSpacialIndex2DEnumerator;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.junit.Ignore;
import org.junit.Test;


public class SpacialIndex2DTest {

	private NetworkTestSupport networkSupport = new NetworkTestSupport();
	
	
	@Test
	public void testSpacialIndex2DSnapshot() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		View<CyNode> nv1 = networkView.getNodeView(n1);
		setGeometry(nv1, 4, 2, 4, 2, 1);
		View<CyNode> nv2 = networkView.getNodeView(n2);
		setGeometry(nv2, 6, 3, 4, 2, 2);
		View<CyNode> nv3 = networkView.getNodeView(n3);
		setGeometry(nv3, 9, 9, 4, 2, 3);
		
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		SpacialIndex2D<Long> spacialIndex = snapshot.getSpacialIndex2D();
		
		// exists()
		assertTrue(spacialIndex.exists(nv1.getSUID()));
		assertTrue(spacialIndex.exists(nv2.getSUID()));
		assertTrue(spacialIndex.exists(nv3.getSUID()));
		assertFalse(spacialIndex.exists(999l));
		
		// minimum bounding rectangle
		assertMBR(snapshot, 2, 1, 11, 10);
		
		// query overlap
		SpacialIndex2DEnumerator<Long> overlap = spacialIndex.queryOverlap(0, 0, 6, 9);
		Map<Long,float[]> map = toMap(overlap);
		assertEquals(2, map.size());
		
		assertArrayEquals(new float[] {2.0f, 1.0f, 6.0f, 3.0f}, map.get(nv1.getSUID()), 0);
		assertArrayEquals(new float[] {4.0f, 2.0f, 8.0f, 4.0f}, map.get(nv2.getSUID()), 0);
	}
	
	@Test
	public void testSpacialIndex2DDefaultVPs() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		View<CyNode> nv1 = networkView.getNodeView(n1);
		View<CyNode> nv2 = networkView.getNodeView(n2);
		
		networkView.setViewDefault(NODE_HEIGHT, 100);
		networkView.setViewDefault(NODE_WIDTH, 200);
		
		nv1.setVisualProperty(NODE_X_LOCATION, 0);
		nv1.setVisualProperty(NODE_Y_LOCATION, 0);
		
		CyNetworkViewSnapshot snapshot = networkView.createSnapshot();
		SpacialIndex2D<Long> spacialIndex = snapshot.getSpacialIndex2D();
		
		Map<Long,float[]> map = toMap(spacialIndex.queryAll());
		assertArrayEquals(new float[] {-100, -50, 100, 50}, map.get(nv1.getSUID()), 0);
		
	}
	
	private static final long SUID_1 = 100, SUID_2 = 200;
	
	
	@Test
	public void testHiddenNodes() {
		final String HIDDEN_NODES = "HIDDEN_NODES";
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyEdge e1 = network.addEdge(n1, n2, false);
		CyEdge e2 = network.addEdge(n2, n3, false);
		CyEdge e3 = network.addEdge(n3, n1, false);
		
		CyNetworkViewImpl networkView = createNetworkView(network, config -> {
			config.addTrackedVisualProperty(HIDDEN_NODES, BasicVisualLexicon.NODE_VISIBLE, Boolean.FALSE::equals);
		});
		setGeometry(networkView.getNodeView(n1), 4, 3, 4, 2, 1);
		setGeometry(networkView.getNodeView(n2), 5, 8, 4, 2, 2);
		setGeometry(networkView.getNodeView(n3), 11, 10, 4, 2, 3);
		
		CyNetworkViewSnapshot snapshot;
		
		// test things are set up correctly
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 2, 2, 13, 11);
		assertVisible(snapshot, n1, 2);
		assertVisible(snapshot, n2, 2);
		assertVisible(snapshot, n3, 2);
		
		// hide nv1
		networkView.getNodeView(n1).setVisualProperty(NODE_VISIBLE, false);
		snapshot = networkView.createSnapshot();
		assertEquals(1, snapshot.getTrackedNodeCount(HIDDEN_NODES));
		assertMBR(snapshot, 3, 7, 13, 11);
		assertHidden(snapshot, n1);
		assertVisible(snapshot, n2, 1);
		assertVisible(snapshot, n3, 1);
		
		// hide nv2
		networkView.getNodeView(n2).setVisualProperty(NODE_VISIBLE, false);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 9, 9, 13, 11);
		assertHidden(snapshot, n1);
		assertHidden(snapshot, n2);
		assertVisible(snapshot, n3, 0);
		
		// show nv2
		networkView.getNodeView(n2).setVisualProperty(NODE_VISIBLE, true);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 3, 7, 13, 11);
		assertHidden(snapshot, n1);
		assertVisible(snapshot, n2, 1);
		assertVisible(snapshot, n3, 1);
		
		// hide edges
		networkView.getEdgeView(e1).setVisualProperty(EDGE_VISIBLE, false);
		networkView.getEdgeView(e2).setVisualProperty(EDGE_VISIBLE, false);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 3, 7, 13, 11);
		assertHidden(snapshot, n1);
		assertVisible(snapshot, n2, 0);
		assertVisible(snapshot, n3, 0);
		
		// show all nodes
		networkView.getNodeView(n1).setVisualProperty(NODE_VISIBLE, true);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 2, 2, 13, 11);
		assertVisible(snapshot, n1, 1);
		assertVisible(snapshot, n2, 0);
		assertVisible(snapshot, n3, 1);
		
		// show all edges
		networkView.getEdgeView(e1).setVisualProperty(EDGE_VISIBLE, true);
		networkView.getEdgeView(e2).setVisualProperty(EDGE_VISIBLE, true);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 2, 2, 13, 11);
		assertVisible(snapshot, n1, 2);
		assertVisible(snapshot, n2, 2);
		assertVisible(snapshot, n3, 2);
	}
	

	@Test
	public void testVisibilityBypass() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		setGeometry(networkView.getNodeView(n1), 4, 3, 4, 2, 0);
		
		View<CyNode> nv1 = networkView.getNodeView(n1);
		
		nv1.setLockedValue(NODE_VISIBLE, false);
		nv1.setVisualProperty(NODE_VISIBLE, true);
		
		assertHidden(networkView.createSnapshot(), n1);
		
		nv1.setLockedValue(NODE_VISIBLE, true);
		
		assertVisible(networkView.createSnapshot(), n1, 0);
	}
	
	
	@Ignore
	public void testNullSpacialIndex() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		
		CyNetworkViewImpl networkView = createNetworkView(network); // turn spacial index off
		setGeometry(networkView.getNodeView(n1), 4, 3, 4, 2, 1);
		setGeometry(networkView.getNodeView(n2), 5, 8, 4, 2, 2);
		setGeometry(networkView.getNodeView(n3), 11, 10, 4, 2, 3);
		
		assertNull(networkView.createSnapshot().getSpacialIndex2D());
	}
	
	
	@Test
	public void testNodeZSort() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		CyNode n7 = network.addNode();
		CyNode n8 = network.addNode();
		CyNode n9 = network.addNode();
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		View<CyNode> nv1 = networkView.getNodeView(n1);
		View<CyNode> nv2 = networkView.getNodeView(n2);
		View<CyNode> nv3 = networkView.getNodeView(n3);
		View<CyNode> nv4 = networkView.getNodeView(n4);
		View<CyNode> nv5 = networkView.getNodeView(n5);
		View<CyNode> nv6 = networkView.getNodeView(n6);
		View<CyNode> nv7 = networkView.getNodeView(n7);
		View<CyNode> nv8 = networkView.getNodeView(n8);
		View<CyNode> nv9 = networkView.getNodeView(n9);
		
		setGeometry(nv1, 1, 1, 2, 2, 1);
		setGeometry(nv2, 1, 1, 2, 2, 6);
		setGeometry(nv3, 1, 1, 2, 2, 2);
		setGeometry(nv4, 1, 1, 2, 2, 8);
		setGeometry(nv5, 1, 1, 2, 2, 4);
		setGeometry(nv6, 1, 1, 2, 2, 3);
		setGeometry(nv7, 1, 1, 2, 2, 5);
		setGeometry(nv8, 1, 1, 2, 2, 9);
		setGeometry(nv9, 1, 1, 2, 2, 7);
		
		List<Long> expectedOrder = Arrays.asList(
			nv1.getSUID(), 
			nv3.getSUID(),
			nv6.getSUID(),
			nv5.getSUID(),
			nv7.getSUID(),
			nv2.getSUID(),
			nv9.getSUID(),
			nv4.getSUID(),
			nv8.getSUID()
		);
		
		NetworkSpacialIndex2D spacialIndex = networkView.createSnapshot().getSpacialIndex2D();
		
		SpacialIndex2DEnumerator<Long> allEnum = spacialIndex.queryAll();
		List<Long> suids = enumToList(allEnum);
		assertEquals(expectedOrder, suids);
		
		SpacialIndex2DEnumerator<Long> overlapEnum = spacialIndex.queryOverlap(0, 0, 4, 4);
		List<Long> suids2 = enumToList(overlapEnum);
		assertEquals(expectedOrder, suids2);
		
		NodeSpacialIndex2DEnumerator allNodesEnum = spacialIndex.queryAllNodes();
		List<Long> suids3 = enumToList(allNodesEnum);
		assertEquals(expectedOrder, suids3);
		
		NodeSpacialIndex2DEnumerator overlapNodesEnum = spacialIndex.queryOverlapNodes(0, 0, 4, 4);
		List<Long> suids4 = enumToList(overlapNodesEnum);
		assertEquals(expectedOrder, suids4);
	}
	
	
	@Test
	public void testEdgeZSort() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		
		CyEdge e1 = network.addEdge(n1, n2, false);
		CyEdge e2 = network.addEdge(n1, n3, false);
		CyEdge e3 = network.addEdge(n1, n4, false);
		CyEdge e4 = network.addEdge(n1, n5, false);
		CyEdge e5 = network.addEdge(n1, n6, false);
		CyEdge e6 = network.addEdge(n2, n3, false);
		CyEdge e7 = network.addEdge(n2, n4, false);
		CyEdge e8 = network.addEdge(n2, n5, false);
		CyEdge e9 = network.addEdge(n3, n6, false);
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		
		View<CyNode> nv1 = networkView.getNodeView(n1);
		View<CyNode> nv2 = networkView.getNodeView(n2);
		View<CyNode> nv3 = networkView.getNodeView(n3);
		View<CyNode> nv4 = networkView.getNodeView(n4);
		View<CyNode> nv5 = networkView.getNodeView(n5);
		View<CyNode> nv6 = networkView.getNodeView(n6);
		
		setGeometry(nv1, 1, 1, 2, 2, 1);
		setGeometry(nv2, 1, 1, 2, 2, 6);
		setGeometry(nv3, 1, 1, 2, 2, 2);
		setGeometry(nv4, 1, 1, 2, 2, 8);
		setGeometry(nv5, 1, 1, 2, 2, 4);
		setGeometry(nv6, 1, 1, 2, 2, 3);
		
		View<CyEdge> ev1 = networkView.getEdgeView(e1);
		View<CyEdge> ev2 = networkView.getEdgeView(e2);
		View<CyEdge> ev3 = networkView.getEdgeView(e3);
		View<CyEdge> ev4 = networkView.getEdgeView(e4);
		View<CyEdge> ev5 = networkView.getEdgeView(e5);
		View<CyEdge> ev6 = networkView.getEdgeView(e6);
		View<CyEdge> ev7 = networkView.getEdgeView(e7);
		View<CyEdge> ev8 = networkView.getEdgeView(e8);
		View<CyEdge> ev9 = networkView.getEdgeView(e9);
		
		ev1.setVisualProperty(EDGE_Z_ORDER, 5.0);
		ev2.setVisualProperty(EDGE_Z_ORDER, 2.0);
		ev3.setVisualProperty(EDGE_Z_ORDER, 9.0);
		ev4.setVisualProperty(EDGE_Z_ORDER, 4.0);
		ev5.setVisualProperty(EDGE_Z_ORDER, 7.0);
		ev6.setVisualProperty(EDGE_Z_ORDER, 1.0);
		ev7.setVisualProperty(EDGE_Z_ORDER, 6.0);
		ev8.setVisualProperty(EDGE_Z_ORDER, 8.0);
		ev9.setVisualProperty(EDGE_Z_ORDER, 3.0);
		
		List<Long> expectedOrder = Arrays.asList(
			ev6.getSUID(), 
			ev2.getSUID(), 
			ev9.getSUID(), 
			ev4.getSUID(), 
			ev1.getSUID(), 
			ev7.getSUID(), 
			ev5.getSUID(), 
			ev8.getSUID(), 
			ev3.getSUID() 
		);
		
		NetworkSpacialIndex2D spacialIndex = networkView.createSnapshot().getSpacialIndex2D();
		
		EdgeSpacialIndex2DEnumerator allEnum = spacialIndex.queryAllEdges();
		List<Long> suids = enumToList(allEnum);
		assertEquals(expectedOrder, suids);
		
		EdgeSpacialIndex2DEnumerator overlapEnum = spacialIndex.queryOverlapEdges(0, 0, 4, 4);
		List<Long> suids2 = enumToList(overlapEnum);
		assertEquals(expectedOrder, suids2);
	}
}