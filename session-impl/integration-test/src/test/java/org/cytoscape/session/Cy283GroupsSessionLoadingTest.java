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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.util.Collection;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy283GroupsSessionLoadingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v283Groups.cys");
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
		checkNetworkView();
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
		assertEquals(9, vmm.getAllVisualStyles().size());
	}
	
	private void checkNetwork() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		checkNodeEdgeCount(applicationManager.getCurrentNetwork(), 4, 2, 0, 0);
		assertEquals("Nested Network Style", vmm.getVisualStyle(viewManager.getNetworkViews(net).iterator().next()).getTitle());
	}
	
	private void checkNetworkView(){
		// View test
		final CyNetwork net = applicationManager.getCurrentNetwork();
		Collection<CyNetworkView> views = viewManager.getNetworkViews(net);
		assertEquals(1, views.size());

		// Check updated view
		final CyNetworkView view = applicationManager.getCurrentNetworkView();
		final VisualStyle style = vmm.getVisualStyle(view);
		style.apply(view);
		
		// Locked Visual Properties (group nodes and meta-edges)
		final CyNode mn = getNodeByName(net, "Metanode 2");
		final View<CyNode> nv = view.getNodeView(mn);
		assertEquals(NodeShapeVisualProperty.DIAMOND, nv.getVisualProperty(NODE_SHAPE));
		assertTrue(nv.isValueLocked(NODE_SHAPE));
		assertEquals(new Integer(100), nv.getVisualProperty(NODE_TRANSPARENCY));
		assertTrue(nv.isValueLocked(NODE_TRANSPARENCY));
		
		final CyEdge me = getEdgeByName(net, "Metanode 2 (meta-meta-DirectedEdge) node2");
		final View<CyEdge> ev = view.getEdgeView(me);
		assertEquals(LineTypeVisualProperty.EQUAL_DASH, ev.getVisualProperty(EDGE_LINE_TYPE));
		assertTrue(ev.isValueLocked(EDGE_LINE_TYPE));
		assertEquals(new Color(51,51,255), ev.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT));
		assertTrue(ev.isValueLocked(EDGE_STROKE_UNSELECTED_PAINT));
	}
	
	private void checkGroups() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		final CyRootNetwork root = ((CySubNetwork) net).getRootNetwork();
		
		// GROUP NODES
		final CyNode gn1 = getNodeByName(root, "Metanode 1");
		final CyNode gn2 = getNodeByName(root, "Metanode 2");
		final CyNode gn3 = getNodeByName(root, "Metanode 3");
		assertNotNull(gn1);
		assertNotNull(gn2);
		assertNotNull(gn3);
		assertTrue(groupManager.isGroup(gn1, gn2.getNetworkPointer())); // nested group
		assertTrue(groupManager.isGroup(gn2, net));
		assertTrue(groupManager.isGroup(gn3, net));
		
		assertEquals(2, groupManager.getGroupSet(net).size());
		assertEquals(1, groupManager.getGroupSet(gn2.getNetworkPointer()).size());
		
		final CyGroup g1 = groupManager.getGroup(gn1, gn2.getNetworkPointer());
		final CyGroup g2 = groupManager.getGroup(gn2, net);
		final CyGroup g3 = groupManager.getGroup(gn3, net);
		assertTrue(g1.isCollapsed(gn2.getNetworkPointer()));
		assertTrue(g2.isCollapsed(net));
		assertFalse(g3.isCollapsed(net)); // Expanded!
		
		// INTERNAL/EXTERNAL EDGES
		// Metanode 1 (nested group)
		CyEdge e1 = getEdgeByName(root, "node0 (DirectedEdge) node1");
		assertTrue(g1.getInternalEdgeList().contains(e1));
		assertEquals(1, g1.getInternalEdgeList().size());
		CyEdge e2 = getEdgeByName(root, "node1 (DirectedEdge) node2");
		assertTrue(g1.getExternalEdgeList().contains(e2));
		assertEquals(1, g1.getExternalEdgeList().size());
		// Metanode 2
		CyEdge e3 = getEdgeByName(root, "Metanode 1 (DirectedEdge) node3");
		assertTrue(g2.getInternalEdgeList().contains(e3));
		assertEquals(1, g2.getInternalEdgeList().size());
		CyEdge e4 = getEdgeByName(root, "Metanode 1 (meta-DirectedEdge) node2");
		assertTrue(g2.getExternalEdgeList().contains(e4));
		assertEquals(1, g2.getExternalEdgeList().size());
		// Metanode 3
		CyEdge e5 = getEdgeByName(root, "node4 (DirectedEdge) node5");
		assertTrue(g3.getInternalEdgeList().contains(e5));
		assertEquals(1, g3.getInternalEdgeList().size());
		assertEquals(0, g3.getExternalEdgeList().size()); // No external edges
		
		// GROUP NETWORKS
		assertEquals(gn1.getNetworkPointer(), g1.getGroupNetwork());
		assertEquals(gn2.getNetworkPointer(), g2.getGroupNetwork());
		assertEquals(gn3.getNetworkPointer(), g3.getGroupNetwork());
		// Make sure the group subnetworks have the correct save policy
		assertEquals(SavePolicy.SESSION_FILE, g1.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g2.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g3.getGroupNetwork().getSavePolicy());
		
		// Check group network attributes
		assertEquals(new Integer(6), root.getRow(gn1).get("score", Integer.class));
		assertEquals(new Integer(2), net.getRow(gn2).get("score", Integer.class));
		assertEquals(new Integer(5), root.getRow(gn3).get("score", Integer.class));
		
		// Check meta-edge attributes
		CyEdge me1 = getEdgeByName(root, "Metanode 1 (meta-DirectedEdge) node2");
		assertEquals(new Integer(9), root.getRow(me1).get("weight", Integer.class));
		CyEdge me2 = getEdgeByName(root, "Metanode 2 (meta-meta-DirectedEdge) node2");
		assertEquals(new Integer(8), net.getRow(me2).get("weight", Integer.class));
	}
}
