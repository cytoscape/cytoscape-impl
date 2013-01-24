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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.junit.Before;
import org.junit.Test;

public class LayoutEdgeTest {
	
	private LayoutEdge layoutEdge;
	private final NetworkViewTestSupport support = new NetworkViewTestSupport();
	private CyEdge edge;
	private CyRow row;
	private CyNetwork network = support.getNetworkFactory().createNetwork();	
	private CyNode source = network.addNode();
	private CyNode target = network.addNode();
	
	private LayoutNode layoutNodeS;
	private LayoutNode layoutNodeT;
	

	@Before
	public void setUp() throws Exception {
		edge = network.addEdge(source, target, true);
		row = network.getDefaultEdgeTable().getRow(edge.getSUID());
		layoutEdge = new LayoutEdge(edge, row);
		
		createLayoutNodes();
	}

	

	@Test
	public void testLayoutEdgeConstructor1() {
		assertNotNull(layoutEdge);
	}

	@Test
	public void testLayoutEdgeConstructor2() {
		CyNetworkFactory nFactory = support.getNetworkFactory();
		CyNetwork newNetwork = nFactory.createNetwork();
		CyNode s = newNetwork.addNode();
		CyNode t = newNetwork.addNode();
		CyEdge e = newNetwork.addEdge(s, t, true);
		
		final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
		CyNetworkView newView = viewFactory.createNetworkView(newNetwork);
		final View<CyNode> sourceView = newView.getNodeView(s);
		final View<CyNode> targetView = newView.getNodeView(t);
		
		assertNotNull(sourceView);
		assertNotNull(targetView);
		CyRow sRow = newNetwork.getRow(s);
		CyRow tRow = newNetwork.getRow(t);
		final LayoutNode layoutNodeS = new LayoutNode(sourceView, 0, sRow);
		final LayoutNode layoutNodeT = new LayoutNode(targetView, 1, tRow);
		final LayoutEdge layoutEdge2 = new LayoutEdge(e, layoutNodeS, layoutNodeT, newNetwork.getRow(e));
		assertNotNull(layoutEdge2);
	}

	@Test
	public void testAddNodes() {
		layoutEdge.addNodes(layoutNodeS, layoutNodeT);
		assertEquals(layoutNodeS, layoutEdge.getSource());
		assertEquals(layoutNodeT, layoutEdge.getTarget());
	}
	
	private void createLayoutNodes() {
		CyNetworkFactory nFactory = support.getNetworkFactory();
		CyNetwork newNetwork = nFactory.createNetwork();
		CyNode s = newNetwork.addNode();
		CyNode t = newNetwork.addNode();
		
		final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
		CyNetworkView newView = viewFactory.createNetworkView(newNetwork);
		final View<CyNode> sourceView = newView.getNodeView(s);
		final View<CyNode> targetView = newView.getNodeView(t);
		
		CyRow sRow = newNetwork.getRow(s);
		CyRow tRow = newNetwork.getRow(t);
		layoutNodeS = new LayoutNode(sourceView, 0, sRow);
		layoutNodeT = new LayoutNode(targetView, 1, tRow);
	}

	@Test
	public void testSetWeight() {
		final Double weightDouble = 0.2d;
		layoutEdge.setWeight(weightDouble);
		assertEquals(weightDouble, Double.valueOf(layoutEdge.getWeight()));
	}

	@Test
	public void testSetLogWeight() {
		final Double weightDouble = 0.2d;
		layoutEdge.setLogWeight(weightDouble);
		assertEquals(weightDouble, Double.valueOf(layoutEdge.getLogWeight()));
	}

	@Test
	public void testGetEdge() {
		assertEquals(edge, layoutEdge.getEdge());
	}

	@Test
	public void testGetRow() {
		assertEquals(row, layoutEdge.getRow());
	}

	@Test
	public void testToString() {
		assertTrue(layoutEdge.toString().contains("connecting"));
	}
}
