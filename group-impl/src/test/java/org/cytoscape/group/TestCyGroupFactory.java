package org.cytoscape.group;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.junit.Test;


public class TestCyGroupFactory {
	public TestCyGroupFactory() { }

	public static CyGroupFactory getFactory() {
		DummyCyEventHelper deh = new DummyCyEventHelper();
		CyGroupManagerImpl mgr = new CyGroupManagerImpl(deh);
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		CyGroupFactoryImpl groupFactory = new CyGroupFactoryImpl(deh, mgr, serviceRegistrar);
		return groupFactory; 
	}

	@Test
	public void testGroupCreate () throws Exception {
		CyGroupFactory factory = TestCyGroupFactory.getFactory();
		// Net to create a network to test this..
		NetworkTestSupport support = new NetworkTestSupport();
		
		CyNetwork network = support.getNetwork();

		// Create some nodes and edges
		CyNode node1 = network.addNode();
		CyNode node2 = network.addNode();
		CyNode node3 = network.addNode();
		CyNode node4 = network.addNode();
		CyNode node5 = network.addNode();
		List<CyNode> groupNodes = new ArrayList<CyNode>();
		groupNodes.add(node1);
		groupNodes.add(node2);
		groupNodes.add(node3);

		CyEdge edge1 = network.addEdge(node1, node2, false);
		CyEdge edge2 = network.addEdge(node2, node3, false);
		CyEdge edge3 = network.addEdge(node2, node4, false);
		CyEdge edge4 = network.addEdge(node2, node5, false);
		CyEdge edge5 = network.addEdge(node3, node5, false);
		List<CyEdge> groupEdges = new ArrayList<CyEdge>();
		groupEdges.add(edge1);
		groupEdges.add(edge2);
		groupEdges.add(edge3);
		groupEdges.add(edge4);

		CyGroup group1 = factory.createGroup(network, groupNodes, null, true);
		assertNotNull(group1);
		assertTrue("group1 node count = 3", group1.getNodeList().size() == 3);
		assertTrue("group1 internal edge count = 2", group1.getInternalEdgeList().size() == 2);
		assertTrue("group1 external edge count = 3", group1.getExternalEdgeList().size() == 3);
		CyGroup group2 = factory.createGroup(network, groupNodes, new ArrayList<CyEdge>(), true);
		assertNotNull(group2);
		assertTrue("group2 node count = 3", group2.getNodeList().size() == 3);
		assertTrue("group2 internal edge count = 0", group2.getInternalEdgeList().size() == 0);
		assertTrue("group2 external edge count = 0", group2.getExternalEdgeList().size() == 0);
		CyGroup group3 = factory.createGroup(network, groupNodes, groupEdges, true);
		assertNotNull(group3);
		assertTrue("group3 node count = 3", group3.getNodeList().size() == 3);
		assertTrue("group3 internal edge count = 2", group3.getInternalEdgeList().size() == 2);
		assertTrue("group3 external edge count = 2", group3.getExternalEdgeList().size() == 2);
	}

    /*
     * Tests for this scenario:

                           group
                     +- - - - - - - - +
                     |        2       |
                        +-------> B
                     |  |         ^   |    Collapse
                        |         |5      ===========>
                     |  v         |   |
               1        +     3   v                            1
            A<--------->G<------->C   |                 A+---------->G
                        ^         ^
                     |  |         |6  |      Expand
                        |         |       <===========
                     |  |     4   v   |
                        +-------->D
                     |                |
                     +- - - - - - - - +
    */
    @Test
    public void testMemberEdges() throws Exception
    {
        // Set up our data structures
		final CyGroupFactory factory = TestCyGroupFactory.getFactory();
		final NetworkTestSupport support = new NetworkTestSupport();
		final CyNetwork net = support.getNetwork();

        final CyNode nodeA = net.addNode();
        final CyNode nodeB = net.addNode();
        final CyNode nodeC = net.addNode();
        final CyNode nodeD = net.addNode();
        final CyNode nodeG = net.addNode();

        final CyEdge edge1 = net.addEdge(nodeA, nodeG, false);
        final CyEdge edge2 = net.addEdge(nodeG, nodeB, false);
        final CyEdge edge3 = net.addEdge(nodeG, nodeC, false);
        final CyEdge edge4 = net.addEdge(nodeG, nodeD, false);
        final CyEdge edge5 = net.addEdge(nodeC, nodeB, false);
        final CyEdge edge6 = net.addEdge(nodeC, nodeD, false);

        final CyGroup group = factory.createGroup(net, nodeG, Arrays.asList(nodeB, nodeC, nodeD), Arrays.asList(edge2, edge3, edge4, edge5, edge6), true);

        // Assert our group has the right nodes and edges
        assertFalse(group.isCollapsed(net));
        assertEqualWithoutOrder(group.getNodeList(), Arrays.asList(nodeB, nodeC, nodeD));
        assertEqualWithoutOrder(group.getInternalEdgeList(), Arrays.asList(edge5, edge6));
        assertEquals(group.getExternalEdgeList().size(), 0);

        // Assert that the network was unaffected by the group creation
        assertEqualWithoutOrder(net.getNodeList(), Arrays.asList(nodeA, nodeB, nodeC, nodeD, nodeG));
        assertEqualWithoutOrder(net.getEdgeList(), Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6));

        // Collapse the group
        group.collapse(net);
        assertTrue(group.isCollapsed(net));

        // Test to see if our network is what we expect
        assertEqualWithoutOrder(net.getNodeList(), Arrays.asList(nodeA, nodeG));
        assertEqualWithoutOrder(net.getEdgeList(), Arrays.asList(edge1));

        // Expand the group
        group.expand(net);
        assertFalse(group.isCollapsed(net));

        // Assert that the network has returned to its original state
        assertEqualWithoutOrder(net.getNodeList(), Arrays.asList(nodeA, nodeB, nodeC, nodeD, nodeG));
        assertEqualWithoutOrder(net.getEdgeList(), Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6));
    }

    private static <T> void assertEqualWithoutOrder(final Collection<T> a, final Collection<T> b)
    {
        final Set<T> aset = new HashSet<T>(a);
        final Set<T> bset = new HashSet<T>(b);
        assertEquals(aset, bset);
    }
}

