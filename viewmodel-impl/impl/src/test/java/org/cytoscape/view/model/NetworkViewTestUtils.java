package org.cytoscape.view.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.internal.CyNetworkViewFactoryConfigImpl;
import org.cytoscape.view.model.internal.CyNetworkViewFactoryProviderImpl;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
import org.cytoscape.view.model.spacial.SpacialIndex2D;
import org.cytoscape.view.model.spacial.SpacialIndex2DEnumerator;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

public class NetworkViewTestUtils {
	
	private NetworkViewTestUtils() {}
	
	public static CyNetworkViewImpl createNetworkView(CyNetwork network) {
		return createNetworkView(network, null);
	}
	
	
	public static CyNetworkViewImpl createNetworkView(CyNetwork network, Consumer<CyNetworkViewFactoryConfig> configExtender) {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		BasicVisualLexicon lexicon = new BasicVisualLexicon(rootVp);
		
		CyServiceRegistrar registrar = mock(CyServiceRegistrar.class);
		when(registrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
		
		CyNetworkViewFactoryProviderImpl factoryFactory = new CyNetworkViewFactoryProviderImpl(registrar);
		CyNetworkViewFactoryConfigImpl config = factoryFactory.createConfig(lexicon);
		if(configExtender != null) {
			configExtender.accept(config);
		}
		
		CyNetworkViewFactory viewFactory = factoryFactory.createNetworkViewFactory(lexicon, "test", config);
		CyNetworkViewImpl networkView = (CyNetworkViewImpl)viewFactory.createNetworkView(network);
		return networkView;
	}
	

	public static Set<Long> asSuidSet(Iterable<? extends CyIdentifiable> iterable) {
		HashSet<Long> set = new HashSet<>();
		iterable.forEach(item -> set.add(item.getSUID()));
		return set;
	}
	
	public static void assertHidden(CyNetworkViewSnapshot snapshot, CyNode n) {
		assertNull(snapshot.getNodeView(n));
		View<CyNode> nv = snapshot.getMutableNetworkView().getNodeView(n);
		assertFalse(snapshot.getSpacialIndex2D().exists(nv.getSUID()));
	}
	
	public static void assertVisible(CyNetworkViewSnapshot snapshot, CyNode n, int adj) {
		View<CyNode> nv = snapshot.getNodeView(n);
		assertNotNull(nv);
		assertTrue(snapshot.getSpacialIndex2D().exists(nv.getSUID()));
		assertEquals(adj, toSet(snapshot.getAdjacentEdgeIterable(nv)).size());
	}
	
	public static void setGeometry(View<CyNode> node, float x, float y, float w, float h, double z) {
		node.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, h);
		node.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, w);
		node.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
		node.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
		node.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, z);
	}
	
	public static void assertMBR(CyNetworkViewSnapshot snapshot, float xMin, float yMin, float xMax, float yMax) {
		float[] mbr = new float[4];
		snapshot.getSpacialIndex2D().getMBR(mbr);
		assertEquals(xMin, mbr[SpacialIndex2D.X_MIN], 0.0f);
		assertEquals(yMin, mbr[SpacialIndex2D.Y_MIN], 0.0f);
		assertEquals(xMax, mbr[SpacialIndex2D.X_MAX], 0.0f);
		assertEquals(yMax, mbr[SpacialIndex2D.Y_MAX], 0.0f);
	}
	
	public static Map<Long,float[]> toMap(SpacialIndex2DEnumerator<Long> overlap) {
		HashMap<Long,float[]> map = new HashMap<>();
		while(overlap.hasNext()) {
			float[] extents = new float[4];
			Long suid = overlap.nextExtents(extents);
			map.put(suid, extents);
		}
		return map;
	}
	
	public static <T> Set<T> toSet(Iterable<T> iterable) {
		Set<T> set = new HashSet<>();
		iterable.forEach(set::add);
		return set;
	}
	
	public static List<Long> enumToList(SpacialIndex2DEnumerator<Long> indexEnum) {
		List<Long> list = new ArrayList<>(indexEnum.size());
		while(indexEnum.hasNext()) {
			list.add(indexEnum.next());
		}
		return list;
	}
	
}
