package org.cytoscape.view.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

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
	
	private static Map<Long,float[]> toMap(SpacialIndex2DEnumerator<Long> overlap) {
		HashMap<Long,float[]> map = new HashMap<>();
		while(overlap.hasNext()) {
			float[] extents = new float[4];
			Long suid = overlap.nextExtents(extents);
			map.put(suid, extents);
		}
		return map;
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
		float[] mbr = new float[4];
		spacialIndex.getMBR(mbr);
		assertEquals(2.0f,  mbr[SpacialIndex2D.X_MIN], 0.0f);
		assertEquals(11.0f, mbr[SpacialIndex2D.X_MAX], 0.0f);
		assertEquals(1.0f,  mbr[SpacialIndex2D.Y_MIN], 0.0f);
		assertEquals(10.0f, mbr[SpacialIndex2D.Y_MAX], 0.0f);
		
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
	
}