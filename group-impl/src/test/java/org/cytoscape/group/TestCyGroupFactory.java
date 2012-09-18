/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.group;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

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

}

