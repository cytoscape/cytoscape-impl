package org.cytoscape.view.layout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.junit.Test;

public abstract class AbstractLayoutAlgorithmTest {

	protected CyLayoutAlgorithm layout;
	protected String computerName;
	protected String humanName;

	@Test
	public void testAbstractLayoutAlgorithm() {
		assertNotNull(computerName);
		assertNotNull(humanName);
		assertNotNull(layout);
	}

	@Test
	public void testGetName() {
		assertEquals(computerName, layout.getName());
	}

	@Test
	public void testToString() {
		assertEquals(humanName, layout.toString());
	}

	@Test
	public void testGetSupportedNodeAttributeTypes() {
		assertEquals(0, layout.getSupportedNodeAttributeTypes().size());
	}

	@Test
	public void testGetSupportedEdgeAttributeTypes() {
		assertEquals(0, layout.getSupportedEdgeAttributeTypes().size());
	}


	@Test
	public void testIsReady() {
		// Create network with two nodes and one edge.
		final NetworkViewTestSupport support = new NetworkViewTestSupport();
		final CyNetwork network = support.getNetworkFactory().createNetwork();
		CyNode source = network.addNode();
		CyNode target = network.addNode();
		CyEdge edge = network.addEdge(source, target, true);

		final CyNetworkView networkView = support.getNetworkViewFactory().createNetworkView(network);
		Object layoutContext = layout.createLayoutContext();
		Set<View<CyNode>> nodesToLayOut = new HashSet<View<CyNode>>();
		nodesToLayOut.add(networkView.getNodeView(source));
		nodesToLayOut.add(networkView.getNodeView(target));

		assertFalse(layout.isReady(null, null, null, null));
		assertFalse(layout.isReady(null, layoutContext, null, ""));
		Set<View<CyNode>> emptySet = new HashSet<View<CyNode>>();
		CyNetworkView emptyView = support.getNetworkView();
		assertFalse(layout.isReady(emptyView, layoutContext, emptySet, "asdf"));

		// Valid case
		assertTrue(layout.isReady(networkView, layoutContext, nodesToLayOut, ""));

	}
}
