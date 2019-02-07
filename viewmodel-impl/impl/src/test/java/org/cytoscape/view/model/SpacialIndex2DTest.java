package org.cytoscape.view.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
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
	
	private static Map<Long,float[]> toMap(SpacialIndex2DEnumerator overlap) {
		HashMap<Long,float[]> map = new HashMap<>();
		while(overlap.hasNext()) {
			float[] extents = new float[4];
			long suid = overlap.nextExtents(extents);
			map.put(suid, extents);
		}
		return map;
	}
	
	@Test
	public void testSpacialIndex2D() {
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
		SpacialIndex2D spacialIndex = snapshot.getSpacialIndex2D();
		
		// exists()
		assertTrue(spacialIndex.exists(nv1.getSUID()));
		assertTrue(spacialIndex.exists(nv2.getSUID()));
		assertTrue(spacialIndex.exists(nv3.getSUID()));
		assertFalse(spacialIndex.exists(999));
		
		// minimum bounding rectangle
		float[] mbr = new float[4];
		spacialIndex.getMBR(mbr);
		assertEquals(2.0f,  mbr[SpacialIndex2D.X_MIN], 0.0f);
		assertEquals(11.0f, mbr[SpacialIndex2D.X_MAX], 0.0f);
		assertEquals(1.0f,  mbr[SpacialIndex2D.Y_MIN], 0.0f);
		assertEquals(10.0f, mbr[SpacialIndex2D.Y_MAX], 0.0f);
		
		// query overlap
		SpacialIndex2DEnumerator overlap = spacialIndex.queryOverlap(0, 0, 6, 9);
		Map<Long,float[]> map = toMap(overlap);
		assertEquals(2, map.size());
		
		float[] extents1 = map.get(nv1.getSUID());
		assertNotNull(extents1);
		assertArrayEquals(new float[] {2.0f, 1.0f, 6.0f, 3.0f}, extents1, 0.0f);
		
		float[] extents2 = map.get(nv2.getSUID());
		assertNotNull(extents2);
		assertArrayEquals(new float[] {4.0f, 2.0f, 8.0f, 4.0f}, extents2, 0.0f);
	}
	
	
	
}