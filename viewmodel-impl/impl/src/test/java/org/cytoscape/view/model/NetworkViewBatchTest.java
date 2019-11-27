package org.cytoscape.view.model;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_PAINT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.internal.model.CyNetworkViewImpl;
import org.junit.Test;

public class NetworkViewBatchTest {

	private NetworkTestSupport networkSupport = new NetworkTestSupport();
	
	@Test
	public void testBatchSimple() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		network.addEdge(n1, n2, false);
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network);
		
		netView.setVisualProperty(NODE_PAINT, Color.BLUE);
		assertTrue(netView.isDirty(false));
		
		netView.createSnapshot();
		assertTrue(netView.isDirty(true));
		assertFalse(netView.isDirty(false));
		
		netView.batch(net -> {
			net.setVisualProperty(NETWORK_BACKGROUND_PAINT, Color.RED);
			assertFalse(netView.isDirty(false));
			net.setVisualProperty(NODE_PAINT, Color.BLUE);
			assertFalse(netView.isDirty(false));
		});
		
		assertTrue(netView.isDirty(false));
	}
	
	@Test
	public void testBatchChildren() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyEdge e1 = network.addEdge(n1, n2, false);
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network);
		netView.createSnapshot();
		assertTrue(netView.isDirty(true));
		assertFalse(netView.isDirty(false));
		
		netView.batch(net -> {
			View<CyNode> nv1 = netView.getNodeView(n1);
			View<CyNode> nv2 = netView.getNodeView(n2);
			View<CyEdge> ev1 = netView.getEdgeView(e1);
			
			net.setVisualProperty(NETWORK_BACKGROUND_PAINT, Color.RED);
			assertFalse(netView.isDirty(false));
			
			nv1.setVisualProperty(NODE_PAINT, Color.RED);
			assertFalse(netView.isDirty(false));
			
			nv2.setVisualProperty(NODE_PAINT, Color.BLUE);
			assertFalse(netView.isDirty(false));
			
			ev1.setVisualProperty(EDGE_PAINT, Color.BLACK);
			assertFalse(netView.isDirty(false));
		});
		
		assertTrue(netView.isDirty(false));
	}
	
	@Test
	public void testBatchNested() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		network.addEdge(n1, n2, false);
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network);
		netView.createSnapshot();
		netView.isDirty(true);
		
		netView.batch(net -> {
			
			net.setVisualProperty(NETWORK_BACKGROUND_PAINT, Color.RED);
			assertFalse(netView.isDirty(false));
			
			net.batch(net2 -> {
				net2.setVisualProperty(NETWORK_BACKGROUND_PAINT, Color.BLUE);
				assertFalse(netView.isDirty(false));
			});
			assertFalse(netView.isDirty(false));
			
			net.setVisualProperty(NETWORK_BACKGROUND_PAINT, Color.GREEN);
			assertFalse(netView.isDirty(false));
		});
		
		assertTrue(netView.isDirty(false));
	}
	
	@Test
	public void testBatchNestedChildren() {
		CyNetwork network = networkSupport.getNetwork();
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyEdge e1 = network.addEdge(n1, n2, false);
		
		CyNetworkViewImpl netView = NetworkViewTestUtils.createNetworkView(network);
		netView.createSnapshot();
		netView.isDirty(true);
		
		netView.batch(net -> {
			View<CyNode> nv1 = netView.getNodeView(n1);
			View<CyNode> nv2 = netView.getNodeView(n2);
			View<CyEdge> ev1 = netView.getEdgeView(e1);
			
			net.batch(net2 -> {
				net2.setVisualProperty(NETWORK_BACKGROUND_PAINT, Color.RED);
				assertFalse(netView.isDirty(false));
			});
			assertFalse(netView.isDirty(false));
			
			nv1.batch(nv1_2 -> {
				nv1.setVisualProperty(NODE_PAINT, Color.RED);
				assertFalse(netView.isDirty(false));
				
				nv1_2.batch(nv1_3 -> {
					nv2.setVisualProperty(NODE_PAINT, Color.BLUE);
					assertFalse(netView.isDirty(false));
				});
				assertFalse(netView.isDirty(false));
				
			});
			assertFalse(netView.isDirty(false));
			
			ev1.batch(ev1_2 -> {
				ev1.setVisualProperty(EDGE_PAINT, Color.BLACK);
				assertFalse(netView.isDirty(false));
			});
			assertFalse(netView.isDirty(false));
		});
		
		assertTrue(netView.isDirty(false));
	}
	
}
