package org.cytoscape.view.layout;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
		Set<View<CyNode>> nodesToLayOut = new HashSet<>();
		nodesToLayOut.add(networkView.getNodeView(source));
		nodesToLayOut.add(networkView.getNodeView(target));

		assertFalse(layout.isReady(null, null, null, null));
		assertFalse(layout.isReady(null, layoutContext, null, ""));
		Set<View<CyNode>> emptySet = new HashSet<>();
		CyNetworkView emptyView = support.getNetworkView();
		assertFalse(layout.isReady(emptyView, layoutContext, emptySet, "asdf"));

		// Valid case
		assertTrue(layout.isReady(networkView, layoutContext, nodesToLayOut, ""));

	}
}
