package org.cytoscape.command.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class StringToModelTest {
	
	private CyServiceRegistrar serviceRegistrar;
	
	@Before
    public void setUp() throws Exception {
		serviceRegistrar = mock(CyServiceRegistrar.class);
    }
	
	// TODO still need to test SUID
	@Test
	public void testNodeListParsing() {
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetwork network = networkTestSupport.getNetwork();
		
		CyNode n1 = network.addNode();
		CyNode n2 = network.addNode();
		CyNode n3 = network.addNode();
		CyNode n4 = network.addNode();
		CyNode n5 = network.addNode();
		CyNode n6 = network.addNode();
		CyNode n7 = network.addNode();
		
		network.getRow(n1).set("name", "node 1");
		network.getRow(n2).set("name", "node 2");
		network.getRow(n3).set("name", "node,3");
		network.getRow(n4).set("name", "node:4");
		network.getRow(n5).set("name", "node:5,5");
		network.getRow(n6).set("name", "node\\:6");
		network.getRow(n7).set("name", "name:7");
		
		StringToModel stringToModel = new StringToModelImpl(serviceRegistrar);
		List<CyNode> nodes;
		
		nodes = stringToModel.getNodeList(network, "node 1,node 2");
		assertEquals(2, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		
		nodes = stringToModel.getNodeList(network, "name:node 1,name:node 2");
		assertEquals(2, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		
		nodes = stringToModel.getNodeList(network, "name:node\\,3,name:node 1,name:node 2");
		assertEquals(3, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		assertTrue(nodes.contains(n3));
		
		nodes = stringToModel.getNodeList(network, "name:node\\:4,name:node 1,name:node 2");
		assertEquals(3, nodes.size());
		assertTrue(nodes.contains(n1));
		assertTrue(nodes.contains(n2));
		assertTrue(nodes.contains(n4));
		
		nodes = stringToModel.getNodeList(network, "name:node\\:5\\,5");
		assertEquals(1, nodes.size());
		assertTrue(nodes.contains(n5));
		
		nodes = stringToModel.getNodeList(network, "node\\\\:6");
		assertEquals(1, nodes.size());
		assertTrue(nodes.contains(n6));
		
		nodes = stringToModel.getNodeList(network, "name:node:4"); // shouldn't need to escape colons after the first colon
		assertEquals(1, nodes.size());
		assertTrue(nodes.contains(n4));
	}

}
