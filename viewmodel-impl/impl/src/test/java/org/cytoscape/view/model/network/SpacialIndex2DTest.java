package org.cytoscape.view.model.network;

import static org.cytoscape.view.model.network.NetworkViewTestUtils.assertHidden;
import static org.cytoscape.view.model.network.NetworkViewTestUtils.assertMBR;
import static org.cytoscape.view.model.network.NetworkViewTestUtils.assertVisible;
import static org.cytoscape.view.model.network.NetworkViewTestUtils.createNetworkView;
import static org.cytoscape.view.model.network.NetworkViewTestUtils.enumToList;
import static org.cytoscape.view.model.network.NetworkViewTestUtils.setGeometry;
import static org.cytoscape.view.model.network.NetworkViewTestUtils.toMap;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
	public void testZSort() {
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
		
		SpacialIndex2D<Long> spacialIndex = networkView.createSnapshot().getSpacialIndex2D();
		
		SpacialIndex2DEnumerator<Long> allEnum = spacialIndex.queryAll();
		List<Long> suids = enumToList(allEnum);
		assertEquals(expectedOrder, suids);
		
		SpacialIndex2DEnumerator<Long> overlapEnum = spacialIndex.queryOverlap(0, 0, 4, 4);
		List<Long> suids2 = enumToList(overlapEnum);
		assertEquals(expectedOrder, suids2);
	}
	
}