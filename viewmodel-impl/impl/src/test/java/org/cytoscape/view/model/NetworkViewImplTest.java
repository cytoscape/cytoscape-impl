package org.cytoscape.view.model;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Test;

public class NetworkViewImplTest {

	private NetworkTestSupport networkSupport = new NetworkTestSupport();
	
	private CyNetworkViewImpl createTestNetworkView() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		network.addEdge(n1, n2, false);
		network.addEdge(n2, n3, false);
		network.addEdge(n3, n4, false);
		network.addEdge(n4, n1, false);
		return createNetworkView(network);
	}
	
	private static CyNetworkViewImpl createNetworkView(CyNetwork network) {
		VisualProperty<NullDataType> rootVp = new NullVisualProperty("ROOT", "root");
		BasicVisualLexicon lexicon = new BasicVisualLexicon(rootVp);
		CyNetworkViewImpl networkView = new CyNetworkViewImpl(network, lexicon, "test");
		return networkView;
	}
	
	
	@Test
	public void testSnapshot() {
		CyNetworkViewImpl networkView = createTestNetworkView();
		CyNetwork network = networkView.getModel();
		
		assertEquals(4, networkView.getNodeViews().size());
		assertEquals(4, networkView.getEdgeViews().size());
		for(CyNode n : network.getNodeList())
			assertNotNull(networkView.getNodeView(n));
		for(CyEdge e : network.getEdgeList())
			assertNotNull(networkView.getEdgeView(e));
		
		for(View<CyNode> node : networkView.getNodeViews())
			node.setVisualProperty(NODE_PAINT, Color.RED);
		
		CyNetworkView snapshot = networkView.createSnapshot();
		
		//Modify the real network (and view)
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		CyEdge e5 = network.addEdge(n5, n6, false);
		networkView.addNode(n5);
		networkView.addNode(n6);
		networkView.addEdge(e5);
		
		for(View<CyNode> node : networkView.getNodeViews())
			node.setVisualProperty(NODE_PAINT, Color.BLUE);
		
		// real network view gets updated as expected
		assertEquals(6, networkView.getNodeViews().size());
		assertEquals(5, networkView.getEdgeViews().size());
		assertNotNull(networkView.getNodeView(n5));
		for(View<CyNode> node : networkView.getNodeViews())
			assertEquals(Color.BLUE, node.getVisualProperty(NODE_PAINT));
		
		// snapshot should not be affected
		assertEquals(4, snapshot.getNodeViews().size());
		assertEquals(4, snapshot.getEdgeViews().size());
		assertNull(snapshot.getNodeView(n5));
		for(View<CyNode> node : snapshot.getNodeViews())
			assertEquals(Color.RED, node.getVisualProperty(NODE_PAINT));
	}
	
	
	@Test
	public void testRemoveNode() {
		CyNetworkViewImpl netView = createTestNetworkView();
		CyNetwork network = netView.getModel();
		
		assertEquals(4, netView.getNodeViews().size());
		assertEquals(4, netView.getEdgeViews().size());
		
		List<CyNode> nodes = network.getNodeList();
		CyNode n0 = nodes.get(0);
		
		netView.removeNode(n0);
		assertEquals(3, netView.getNodeViews().size());
		assertEquals(2, netView.getEdgeViews().size());
	}
	
	
	@Test
	public void testVisualProperties() {
		CyNetworkViewImpl netView = createTestNetworkView();
		CyNetwork network = netView.getModel();
		
		List<CyNode> nodes = network.getNodeList();
		View<CyNode> n0 = netView.getNodeView(nodes.get(0));
		View<CyNode> n1 = netView.getNodeView(nodes.get(1));
		View<CyNode> n2 = netView.getNodeView(nodes.get(2));
		View<CyNode> n3 = netView.getNodeView(nodes.get(3));
		
		// Clearing the VPs doesn't do anything at this point, but it should still work
		netView.clearVisualProperties();
		for(View<CyNode> v : netView.getNodeViews())
			v.clearVisualProperties();
		for(View<CyEdge> v : netView.getEdgeViews())
			v.clearVisualProperties();
		
		// 0 -> black, 1,2 -> red, 3 -> yellow
		netView.setViewDefault(NODE_PAINT, Color.BLACK);
		n1.setVisualProperty(NODE_PAINT, Color.RED);
		n2.setVisualProperty(NODE_PAINT, Color.RED);
		n3.setVisualProperty(NODE_PAINT, Color.RED);
		n3.setLockedValue(NODE_PAINT, Color.YELLOW);
		
		assertEquals(n0.getVisualProperty(NODE_PAINT), Color.BLACK);
		assertEquals(n1.getVisualProperty(NODE_PAINT), Color.RED);
		assertEquals(n2.getVisualProperty(NODE_PAINT), Color.RED);
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.YELLOW);
		
		assertFalse(n0.isSet(NODE_PAINT));
		assertTrue(n1.isSet(NODE_PAINT));
		assertTrue(n2.isSet(NODE_PAINT));
		assertTrue(n3.isSet(NODE_PAINT));
		
		assertFalse(n0.isValueLocked(NODE_PAINT));
		assertFalse(n1.isValueLocked(NODE_PAINT));
		assertFalse(n2.isValueLocked(NODE_PAINT));
		assertTrue(n3.isValueLocked(NODE_PAINT));
		
		assertFalse(n0.isDirectlyLocked(NODE_PAINT));
		assertFalse(n1.isDirectlyLocked(NODE_PAINT));
		assertFalse(n2.isDirectlyLocked(NODE_PAINT));
		assertTrue(n3.isDirectlyLocked(NODE_PAINT));
		
		// test clearing the VP
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.YELLOW);
		n3.clearValueLock(NODE_PAINT);
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.RED);
		n3.setVisualProperty(NODE_PAINT, null);
		assertEquals(n3.getVisualProperty(NODE_PAINT), Color.BLACK);
		
		
		// test directly locked properties
		n3.setLockedValue(NODE_SIZE, 10d);
		
		assertEquals(10d, n3.getVisualProperty(NODE_SIZE), 0);
		assertEquals(10d, n3.getVisualProperty(NODE_HEIGHT), 0);
		assertEquals(10d, n3.getVisualProperty(NODE_WIDTH), 0);
		
		assertTrue(n3.isValueLocked(NODE_SIZE));
		assertTrue(n3.isValueLocked(NODE_HEIGHT));
		assertTrue(n3.isValueLocked(NODE_WIDTH));
		
		assertTrue(n3.isValueLocked(NODE_SIZE));
		assertFalse(n3.isDirectlyLocked(NODE_HEIGHT));
		assertFalse(n3.isDirectlyLocked(NODE_WIDTH));
	}
	
	
	@Test
	public void testVisualPropertiesParallel() throws Exception {
		CyNetwork network = networkSupport.getNetwork();
		CyNetworkViewImpl netView = createNetworkView(network);
		
		{
			ExecutorService executor = Executors.newCachedThreadPool();
			for(int i = 0; i < 100; i++) {
				executor.submit(() -> {
					CyNode n1 = network.addNode();
					CyNode n2 = network.addNode();
					CyEdge e1 = network.addEdge(n1, n2, false);
					netView.addNode(n1);
					netView.addNode(n2);
					netView.addEdge(e1);
				});
			}
			
			executor.shutdown();
			executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		}
		
		assertEquals(200, netView.getNodeViews().size());
		assertEquals(100, netView.getEdgeViews().size());
		
		{
			ExecutorService executor = Executors.newCachedThreadPool();
			for(View<CyNode> nv : netView.getNodeViews()) {
				executor.submit(() -> {
					nv.setVisualProperty(NODE_PAINT, Color.RED);
				});
			}
			for(View<CyEdge> ev : netView.getEdgeViews()) {
				executor.submit(() -> {
					ev.setVisualProperty(EDGE_PAINT, Color.BLUE);
				});
			}
			
			executor.shutdown();
			executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		}
		
		for(View<CyNode> nv : netView.getNodeViews()) {
			assertEquals(Color.RED, nv.getVisualProperty(NODE_PAINT));
		}
		for(View<CyEdge> ev : netView.getEdgeViews()) {
			assertEquals(Color.BLUE, ev.getVisualProperty(EDGE_PAINT));
		}
	}
	
}
