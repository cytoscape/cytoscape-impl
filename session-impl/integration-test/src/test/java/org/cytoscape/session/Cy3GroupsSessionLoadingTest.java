package org.cytoscape.session;

/*
 * #%L
 * Cytoscape Session Impl Integration Test (session-impl-integration-test)
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

import static org.cytoscape.model.CyNetwork.DEFAULT_ATTRS;
import static org.cytoscape.model.CyNetwork.HIDDEN_ATTRS;
import static org.cytoscape.model.CyNetwork.LOCAL_ATTRS;
import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.CyNetwork.SELECTED;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_ATTRS;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_DEFAULT_ATTRS;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy3GroupsSessionLoadingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session3x/", "groups.cys");
		checkBasicConfiguration();
	}

	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
		confirm();
	}

	private void confirm() {
		checkGlobalStatus();
		checkNetwork();
		checkGroups();
	}
	
	private void checkGlobalStatus() {
		assertEquals(1, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());
		assertEquals(1, applicationManager.getSelectedNetworks().size());
		assertEquals(1, applicationManager.getSelectedNetworkViews().size());
		assertEquals(getNetworkByName("Network"), applicationManager.getCurrentNetwork());
		assertNotNull(applicationManager.getCurrentNetworkView());
		assertEquals("default", vmm.getDefaultVisualStyle().getTitle());
		assertEquals(2, vmm.getAllVisualStyles().size());
		assertEquals(3, groupManager.getGroupSet(applicationManager.getCurrentNetwork()).size());
	}
	
	private void checkNetwork() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		checkNodeEdgeCount(applicationManager.getCurrentNetwork(), 6, 4, 1, 0);
		assertEquals("Directed", vmm.getVisualStyle(viewManager.getNetworkViews(net).iterator().next()).getTitle());
	}
	
	private void checkGroups() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		final CyRootNetwork root = ((CySubNetwork) net).getRootNetwork();
		
		// GROUP NODES
		final CyNode gn1 = getNodeByName(net, "Group 1");
		final CyNode gn2 = getNodeByName(root, "Group 2");
		final CyNode gn3 = getNodeByName(root, "Group 3");
		assertTrue(groupManager.isGroup(gn1, net));
		assertTrue(groupManager.isGroup(gn2, net));
		assertTrue(groupManager.isGroup(gn3, net));
		
		final CyGroup g1 = groupManager.getGroup(gn1, net);
		final CyGroup g2 = groupManager.getGroup(gn2, net);
		final CyGroup g3 = groupManager.getGroup(gn3, net);
		// TODO nested groups not working (cannot be created correctly by Cy3)
//		assertFalse(g1.isCollapsed(net)); // TODO: Test G1 is inside G2, but is still expanded
//		assertTrue(g2.isCollapsed(net)); // TODO
		assertFalse(g3.isCollapsed(net));
		
		// GROUP NETWORKS
		assertEquals(gn1.getNetworkPointer(), g1.getGroupNetwork());
		assertEquals(gn2.getNetworkPointer(), g2.getGroupNetwork());
		assertEquals(gn3.getNetworkPointer(), g3.getGroupNetwork());
		// Make sure the group subnetworks have the correct save policy
		assertEquals(SavePolicy.SESSION_FILE, g1.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g2.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g3.getGroupNetwork().getSavePolicy());
		
		// TODO meta-edges and external edges
		// TODO check group network attributes
		// TODO check meta-edge attributes
	}
}
