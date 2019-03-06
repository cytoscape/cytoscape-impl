package org.cytoscape.view.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
import org.cytoscape.view.model.internal.model.spacial.SpacialIndex2DFactoryImpl;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Test;

public class SpacialIndex2DTest {

	private NetworkTestSupport networkSupport = new NetworkTestSupport();
	
	private static CyNetworkViewImpl createNetworkView(CyNetwork network) {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		BasicVisualLexicon lexicon = new BasicVisualLexicon(rootVp);
		CyNetworkViewImpl networkView = new CyNetworkViewImpl(network, lexicon, "test");
		return networkView;
	}
	
	private static void setGeometry(View<CyNode> node, float x, float y, float w, float h) {
		node.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
		node.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
		node.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, h);
		node.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, w);
	}
	
	public static void assertMBR(CyNetworkViewSnapshot snapshot, float xMin, float yMin, float xMax, float yMax) {
		float[] mbr = new float[4];
		snapshot.getSpacialIndex2D().getMBR(mbr);
		assertEquals(xMin, mbr[SpacialIndex2D.X_MIN], 0.0f);
		assertEquals(yMin, mbr[SpacialIndex2D.Y_MIN], 0.0f);
		assertEquals(xMax, mbr[SpacialIndex2D.X_MAX], 0.0f);
		assertEquals(yMax, mbr[SpacialIndex2D.Y_MAX], 0.0f);
	}
	
	private static Map<Long,float[]> toMap(SpacialIndex2DEnumerator<Long> overlap) {
		HashMap<Long,float[]> map = new HashMap<>();
		while(overlap.hasNext()) {
			float[] extents = new float[4];
			Long suid = overlap.nextExtents(extents);
			map.put(suid, extents);
		}
		return map;
	}
	
	private static <T> Set<T> toSet(Iterable<T> iterable) {
		Set<T> set = new HashSet<>();
		iterable.forEach(set::add);
		return set;
	}
	
	@Test
	public void testSpacialIndex2DSnapshot() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		View<CyNode> nv1 = networkView.getNodeView(n1);
		setGeometry(nv1, 4, 2, 4, 2);
		View<CyNode> nv2 = networkView.getNodeView(n2);
		setGeometry(nv2, 6, 3, 4, 2);
		View<CyNode> nv3 = networkView.getNodeView(n3);
		setGeometry(nv3, 9, 9, 4, 2);
		
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
		
		float[] extents1 = map.get(nv1.getSUID());
		assertNotNull(extents1);
		assertArrayEquals(new float[] {2.0f, 1.0f, 6.0f, 3.0f}, extents1, 0.0f);
		
		float[] extents2 = map.get(nv2.getSUID());
		assertNotNull(extents2);
		assertArrayEquals(new float[] {4.0f, 2.0f, 8.0f, 4.0f}, extents2, 0.0f);
		
		try {
			spacialIndex.delete(nv1.getSUID());
			fail();
		} catch (UnsupportedOperationException e) {
		}
		
		try {
			spacialIndex.put(99l, 1, 2, 3, 4);
			fail();
		} catch (UnsupportedOperationException e) {
		}
	}
	
	
	private static final long SUID_1 = 100, SUID_2 = 200;
	
	@Test
	public void testSpacialIndex2DMutable() {
		SpacialIndex2D<Long> spacialIndex = new SpacialIndex2DFactoryImpl().createSpacialIndex2D();
		assertEquals(0, spacialIndex.size());
		
		float[] extents = new float[4];
		
		spacialIndex.put(SUID_1, 1, 2, 3, 4);
		assertTrue(spacialIndex.exists(100l));
		assertEquals(1, spacialIndex.size());
		spacialIndex.get(SUID_1, extents);
		assertArrayEquals(new float[] {1,2,3,4}, extents, 0);
		
		spacialIndex.put(SUID_2, 11, 22, 33, 44);
		assertTrue(spacialIndex.exists(200l));
		assertEquals(2, spacialIndex.size());
		spacialIndex.get(SUID_2, extents);
		assertArrayEquals(new float[] {11,22,33,44}, extents, 0);
		
		spacialIndex.put(SUID_1, 111, 222, 333, 444);
		assertTrue(spacialIndex.exists(100l));
		assertEquals(2, spacialIndex.size());
		spacialIndex.get(SUID_1, extents);
		assertArrayEquals(new float[] {111,222,333,444}, extents, 0);
		
		spacialIndex.delete(SUID_1);
		assertFalse(spacialIndex.exists(SUID_1));
		assertEquals(1, spacialIndex.size());
		assertEquals(1, spacialIndex.queryAll().size());
		assertFalse(spacialIndex.get(SUID_1, extents));
		assertArrayEquals(new float[] {111,222,333,444}, extents, 0);
		spacialIndex.delete(SUID_1); // should be no-op
		
		spacialIndex.delete(SUID_2);
		assertFalse(spacialIndex.exists(SUID_1));
		assertEquals(0, spacialIndex.size());
	}
	
	
	
	private static void assertHidden(CyNetworkViewSnapshot snapshot, CyNode n) {
		assertNull(snapshot.getNodeView(n));
	}
	
	private static void assertVisible(CyNetworkViewSnapshot snapshot, CyNode n, int adj) {
		View<CyNode> nv = snapshot.getNodeView(n);
		assertNotNull(nv);
		assertEquals(adj, toSet(snapshot.getAdjacentEdgeIterable(nv)).size());
	}
	
	
	@Test
	public void testHiddenNodes() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyEdge e1 = network.addEdge(n1, n2, false);
		CyEdge e2 = network.addEdge(n2, n3, false);
		CyEdge e3 = network.addEdge(n3, n1, false);
		
		CyNetworkViewImpl networkView = createNetworkView(network);
		setGeometry(networkView.getNodeView(n1), 4, 3, 4, 2);
		setGeometry(networkView.getNodeView(n2), 5, 8, 4, 2);
		setGeometry(networkView.getNodeView(n3), 11, 10, 4, 2);
		
		CyNetworkViewSnapshot snapshot;
		
		// test things are set up correctly
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 2, 2, 13, 11);
		assertVisible(snapshot, n1, 2);
		assertVisible(snapshot, n2, 2);
		assertVisible(snapshot, n3, 2);
		
		// hide nv1
		networkView.getNodeView(n1).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 3, 7, 13, 11);
		assertHidden(snapshot, n1);
		assertVisible(snapshot, n2, 1);
		assertVisible(snapshot, n3, 1);
		
		// hide nv2
		networkView.getNodeView(n2).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 9, 9, 13, 11);
		assertHidden(snapshot, n1);
		assertHidden(snapshot, n2);
		assertVisible(snapshot, n3, 0);
		
		// show nv2
		networkView.getNodeView(n2).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 3, 7, 13, 11);
		assertHidden(snapshot, n1);
		assertVisible(snapshot, n2, 1);
		assertVisible(snapshot, n3, 1);
		
		// hide edges
		networkView.getEdgeView(e1).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
		networkView.getEdgeView(e2).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 3, 7, 13, 11);
		assertHidden(snapshot, n1);
		assertVisible(snapshot, n2, 0);
		assertVisible(snapshot, n3, 0);
		
		// show all nodes
		networkView.getNodeView(n1).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 2, 2, 13, 11);
		assertVisible(snapshot, n1, 1);
		assertVisible(snapshot, n2, 0);
		assertVisible(snapshot, n3, 1);
		
		// show all edges
		networkView.getEdgeView(e1).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
		networkView.getEdgeView(e2).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
		snapshot = networkView.createSnapshot();
		assertMBR(snapshot, 2, 2, 13, 11);
		assertVisible(snapshot, n1, 2);
		assertVisible(snapshot, n2, 2);
		assertVisible(snapshot, n3, 2);
	}
	
}