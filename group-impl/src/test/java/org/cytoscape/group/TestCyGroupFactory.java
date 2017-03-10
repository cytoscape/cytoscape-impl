package org.cytoscape.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.LockedVisualPropertiesManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
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

public class TestCyGroupFactory {
	
	public static CyGroupFactory getFactory() {
		final DummyCyEventHelper eventHelper = new DummyCyEventHelper();
		final VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		final CyNetworkViewManager netViewMgr = mock(CyNetworkViewManager.class);
		
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		
		final CyGroupManagerImpl mgr = new CyGroupManagerImpl(serviceRegistrar);
		final LockedVisualPropertiesManager lvpMgr = new LockedVisualPropertiesManager(serviceRegistrar);
		final CyGroupFactoryImpl groupFactory = new CyGroupFactoryImpl(mgr, lvpMgr);
		
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

		// Test maintenance of meta-edges
		// Create two groups that share edges
		// Collapse both and expand them in the opposite order
    @Test
    public void testMetaEdges() throws Exception
    {
			// Set up our data structures
			final CyGroupFactory factory = TestCyGroupFactory.getFactory();
			final NetworkTestSupport support = new NetworkTestSupport();
			final CyNetwork net = support.getNetwork();
	
			final CyNode nodeA = net.addNode();
			final CyNode nodeB = net.addNode();
			final CyNode nodeC = net.addNode();
			final CyNode nodeD = net.addNode();
			final CyNode nodeE = net.addNode();

			final CyEdge edge1 = net.addEdge(nodeA, nodeE, false);
			final CyEdge edge2 = net.addEdge(nodeE, nodeB, false);
			final CyEdge edge3 = net.addEdge(nodeE, nodeC, false);
			final CyEdge edge4 = net.addEdge(nodeE, nodeD, false);
			final CyEdge edge5 = net.addEdge(nodeC, nodeB, false);
			final CyEdge edge6 = net.addEdge(nodeC, nodeD, false);

			final CyGroup group1 = factory.createGroup(net, null, Arrays.asList(nodeA, nodeB, nodeC), null, true);
			assertNotNull(group1);
			assertEqualWithoutOrder(group1.getNodeList(), Arrays.asList(nodeA, nodeB, nodeC));
			assertEqualWithoutOrder(group1.getInternalEdgeList(), Arrays.asList(edge5));
			assertEqualWithoutOrder(group1.getExternalEdgeList(), Arrays.asList(edge1, edge2, edge3, edge6));

			final CyGroup group2 = factory.createGroup(net, null, Arrays.asList(nodeD, nodeE), null, true);
			assertNotNull(group2);
			assertEqualWithoutOrder(group2.getNodeList(), Arrays.asList(nodeD, nodeE));
			assertEqualWithoutOrder(group2.getInternalEdgeList(), Arrays.asList(edge4));
			assertTrue(group2.getExternalEdgeList().size()==4); // 4 external edges, we create meta-edges when we collapse

			// Collapse both groups
			group1.collapse(net);
			assertTrue(group1.isCollapsed(net));

			group2.collapse(net);
			assertTrue(group2.isCollapsed(net));

			assertEqualWithoutOrder(net.getNodeList(), Arrays.asList(group1.getGroupNode(), group2.getGroupNode()));

			// Expand in opposite order
			group1.expand(net);
			assertEqualWithoutOrder(net.getNodeList(), Arrays.asList(nodeA, nodeB, nodeC, group2.getGroupNode()));
			assertTrue(net.getEdgeList().size()==4); // 1 internal edge + 3 meta-edges

			group2.expand(net);
			assertEqualWithoutOrder(net.getNodeList(), Arrays.asList(nodeA, nodeB, nodeC, nodeD, nodeE));
			assertEqualWithoutOrder(net.getEdgeList(), Arrays.asList(edge1, edge2, edge3, edge4, edge5, edge6));

		}

    private static <T> void assertEqualWithoutOrder(final Collection<T> a, final Collection<T> b)
    {
        final Set<T> aset = new HashSet<T>(a);
        final Set<T> bset = new HashSet<T>(b);
        assertEquals(aset, bset);
    }
}

