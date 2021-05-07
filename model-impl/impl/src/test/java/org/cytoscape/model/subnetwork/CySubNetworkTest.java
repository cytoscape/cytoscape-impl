package org.cytoscape.model.subnetwork;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.TestCyNetworkFactory;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.internal.CySubNetworkImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CySubNetworkTest extends AbstractCySubNetworkTest {

	private DummyCyEventHelper deh;
	private DummyCyEventHelper deh2;

	@Before
	public void setUp() {
		deh = new DummyCyEventHelper(true);
		deh2 = new DummyCyEventHelper(false);
		root = TestCyNetworkFactory.getPublicRootInstance(deh);
		root2 = TestCyNetworkFactory.getPublicRootInstance(deh2);
	}

	@After
	public void tearDown() {
		deh = null;
		deh2 = null;
		root = null;
		root2 = null;
	}

	// 
	// A bunch of tests to validate that the proper events are fired
	// 

	@Test
	public void testRemoveNodeEvents() {

		CySubNetwork sub = root.addSubNetwork();

		CyNode nx1 = sub.addNode();
		CyNode nx2 = sub.addNode();
		CyNode nx3 = sub.addNode();
		CyNode nx4 = sub.addNode();

		List<CyNode> nodes = new ArrayList<CyNode>();
		nodes.add(nx1);
		nodes.add(nx2);
		nodes.add(nx3);
		nodes.add(nx4);
		
		sub.removeNodes(nodes);

		int aboutToRemoveInd = -1;
		int removedInd = -1;
		int ind = 0;
		for ( Object event : deh.getAllLastEvents() ) {
			if ( event instanceof AboutToRemoveNodesEvent )
				aboutToRemoveInd = ind;
			else if ( event instanceof RemovedNodesEvent )
				removedInd = ind;	
			ind++;
		}

		// verify that we found the events in the list 
		assertTrue( aboutToRemoveInd >= 0 );
		assertTrue( removedInd >= 0 );

		// verify that the about to remove event came first
		assertTrue( aboutToRemoveInd < removedInd ); 
	}

	@Test
	public void testRemoveEdgeEvents() {
		CySubNetwork sub = root.addSubNetwork();

		CyNode nx1 = sub.addNode();
		CyNode nx2 = sub.addNode();
		CyNode nx3 = sub.addNode();
		CyNode nx4 = sub.addNode();

		CyEdge ex1 = sub.addEdge(nx1,nx2,true);
		CyEdge ex2 = sub.addEdge(nx2,nx3,false);
		CyEdge ex3 = sub.addEdge(nx3,nx4,true);

		List<CyEdge> edges = new ArrayList<CyEdge>();
		edges.add(ex1);
		edges.add(ex2);
		edges.add(ex3);
		
		sub.removeEdges(edges);

		int aboutToRemoveInd = -1;
		int removedInd = -1;
		int ind = 0;
		for ( Object event : deh.getAllLastEvents() ) {
			if ( event instanceof AboutToRemoveEdgesEvent )
				aboutToRemoveInd = ind;
			else if ( event instanceof RemovedEdgesEvent )
				removedInd = ind;	
			ind++;
		}

		// verify that we found the events in the list 
		assertTrue( aboutToRemoveInd >= 0 );
		assertTrue( removedInd >= 0 );

		// verify that the about to remove event came first
		assertTrue( aboutToRemoveInd < removedInd ); 
	}

	@Test
	public void testAddNewNodeEventBeforeNetworkAdd() {
		CySubNetwork sub = root.addSubNetwork();
		sub.addNode();
		Object payload = deh.getLastPayload();
		assertNull(payload);
	}

	@Test
	public void testAddNewNodeEventAfterNetworkAdd() {
		CySubNetwork sub = root.addSubNetwork();
		((CySubNetworkImpl)sub).handleEvent( new NetworkAddedEvent(mock(CyNetworkManager.class),sub) );
		sub.addNode();
		Object payload = deh.getLastPayload();
		assertNotNull(payload);
	}

	@Test
	public void testAddExistingNodeEventBeforeNetworkAdd() {
		CyNode rn1 = root.addNode();
		CySubNetwork sub = root.addSubNetwork();
		sub.addNode(rn1);
		Object payload = deh.getLastPayload();
		assertNull(payload);
	}

	@Test
	public void testAddExistingNodeEventAfterNetworkAdd() {
		CyNode rn1 = root.addNode();
		CySubNetwork sub = root.addSubNetwork();
		((CySubNetworkImpl)sub).handleEvent( new NetworkAddedEvent(mock(CyNetworkManager.class),sub) );
		sub.addNode(rn1);
		Object payload = deh.getLastPayload();
		assertNotNull(payload);
	}

	@Test
	public void testAddNewEdgeEventBeforeNetworkAdd() {
		CySubNetwork sub = root.addSubNetwork();
		CyNode nx1 = sub.addNode();
		CyNode nx2 = sub.addNode();
		CyEdge ex1 = sub.addEdge(nx1,nx2,false);
		Object payload = deh.getLastPayload();
		assertNull(payload);
	}

	@Test
	public void testAddNewEdgeEventAfterNetworkAdd() {
		CySubNetwork sub = root.addSubNetwork();
		CyNode nx1 = sub.addNode();
		CyNode nx2 = sub.addNode();
		((CySubNetworkImpl)sub).handleEvent( new NetworkAddedEvent(mock(CyNetworkManager.class),sub) );
		CyEdge ex1 = sub.addEdge(nx1,nx2,false);
		Object payload = deh.getLastPayload();
		assertNotNull(payload);
	}

	@Test
	public void testAddExistingEdgeEventBeforeNetworkAdd() {
		CyNode nx1 = root.addNode();
		CyNode nx2 = root.addNode();
		CyEdge ex1 = root.addEdge(nx1,nx2,false);
		CySubNetwork sub = root.addSubNetwork();
		sub.addNode(nx1);
		sub.addNode(nx2);
		sub.addEdge(ex1);
		Object payload = deh.getLastPayload();
		assertNull(payload);
	}

	@Test
	public void testAddExistingEdgeEventAfterNetworkAdd() {
		CyNode nx1 = root.addNode();
		CyNode nx2 = root.addNode();
		CyEdge ex1 = root.addEdge(nx1,nx2,false);
		CySubNetwork sub = root.addSubNetwork();
		sub.addNode(nx1);
		sub.addNode(nx2);
		((CySubNetworkImpl)sub).handleEvent( new NetworkAddedEvent(mock(CyNetworkManager.class),sub) );
		sub.addEdge(ex1);
		Object payload = deh.getLastPayload();
		assertNotNull(payload);
	}
}
