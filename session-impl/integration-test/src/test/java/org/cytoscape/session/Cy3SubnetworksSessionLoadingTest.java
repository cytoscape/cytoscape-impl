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

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.SavePolicy;
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
public class Cy3SubnetworksSessionLoadingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session3x/", "subnetworks.cys");
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
		checkNetworks();
	}
	
	private void checkGlobalStatus() {
		assertEquals(5, networkManager.getNetworkSet().size());
		assertEquals(4, viewManager.getNetworkViewSet().size());
		assertEquals(1, applicationManager.getSelectedNetworks().size());
		assertEquals(getNetworkByName("Na.1.1"), applicationManager.getCurrentNetwork());
		assertNull(applicationManager.getCurrentNetworkView());
		assertEquals("default", vmm.getDefaultVisualStyle().getTitle());
		assertEquals(6, vmm.getAllVisualStyles().size());
	}
	
	private void checkNetworks() {
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		
		for (CyNetwork net : networks)
			assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		
		CyNetwork na = getNetworkByName("Na");
		CyNetwork na1 = getNetworkByName("Na.1");
		CyNetwork na11 = getNetworkByName("Na.1.1");
		CyNetwork nb = getNetworkByName("Nb");
		CyNetwork nb1 = getNetworkByName("Nb.1");
		
		// Correct base networks
		assertEquals(na, ((CySubNetwork) na1).getRootNetwork().getBaseNetwork());
		assertEquals(nb, ((CySubNetwork) nb1).getRootNetwork().getBaseNetwork());
		
		// Subnetworks' node/edge count and selection
		checkNodeEdgeCount(na, 3, 2, 2, 1);
		checkNodeEdgeCount(na1, 3, 1, 1, 0);
		checkNodeEdgeCount(na11, 1, 0, 1, 0);
		checkNodeEdgeCount(nb, 2, 2, 0, 1);
		checkNodeEdgeCount(nb1, 1, 1, 0, 1);
		
		checkSelection(na, getNodeByName(na, "Node 1"), true);
		checkSelection(na, getNodeByName(na, "Node 2"), true);
		checkSelection(na, getNodeByName(na, "Node 3"), false);
		checkSelection(na, getEdgeByName(na, "Node 1 (interaction) Node 2"), true);
		checkSelection(na, getEdgeByName(na, "Node 2 (interaction) Node 3"), false);
		checkSelection(na1, getNodeByName(na1, "Node 2"), false);
		checkSelection(nb, getEdgeByName(nb, "Node 6 (interaction) Node 5"), true);
		
		// Root-networks' node/edge count and selection
		checkNodeEdgeCount(((CySubNetwork) na).getRootNetwork(), 4, 2, 0, 0);
		checkNodeEdgeCount(((CySubNetwork) nb).getRootNetwork(), 2, 3, 0, 0);
		
		// Network pointers
		assertEquals(nb, getNodeByName(na, "Node 1").getNetworkPointer());
		assertEquals(na1, getNodeByName(na, "Node 3").getNetworkPointer());
		assertEquals(na1, getNodeByName(na1, "Node 3").getNetworkPointer());
		assertEquals(nb1, getNodeByName(nb, "Node 5").getNetworkPointer());
		assertEquals(na, getNodeByName(nb, "Node 6").getNetworkPointer());
		assertEquals(na, getNodeByName(nb1, "Node 6").getNetworkPointer());
		
		// Styles
		assertEquals("Sample1", vmm.getVisualStyle(viewManager.getNetworkViews(na).iterator().next()).getTitle());
		assertEquals("Minimal", vmm.getVisualStyle(viewManager.getNetworkViews(na1).iterator().next()).getTitle());
		assertEquals("Directed", vmm.getVisualStyle(viewManager.getNetworkViews(nb).iterator().next()).getTitle());
		assertEquals("Ripple", vmm.getVisualStyle(viewManager.getNetworkViews(nb1).iterator().next()).getTitle());
	}
}
