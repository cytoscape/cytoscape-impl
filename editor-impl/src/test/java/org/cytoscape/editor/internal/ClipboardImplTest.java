package org.cytoscape.editor.internal;

import static org.cytoscape.model.CyNetwork.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class ClipboardImplTest {

	@Mock CyServiceRegistrar serviceRegistrar;
	@Mock CyEventHelper eventHelper;
	
	NetworkTestSupport netTestSupport = new NetworkTestSupport();
	NetworkViewTestSupport viewTestSupport = new NetworkViewTestSupport();
	VisualLexicon lexicon = new BasicVisualLexicon(new NullVisualProperty("MINIMAL_ROOT", "Min Root Visual Property"));
	
	CyNetworkView sourceView;
	CyNode n1;
    CyNode n2;
    CyNode n3;
    CyEdge e1;
    CyEdge e2;
    CyEdge e3;
    CyEdge e4;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
        when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
        
        sourceView = viewTestSupport.getNetworkView();
        CyNetwork sourceNet = sourceView.getModel();
        n1 = addNode(sourceNet, "n1");
        n2 = addNode(sourceNet, "n2");
        n3 = addNode(sourceNet, "n3");
        e1 = addEdge(sourceNet, n1, n2, "e1");
        e2 = addEdge(sourceNet, n2, n3, "e2");
        e3 = addEdge(sourceNet, n3, n1, "e3");
        e4 = addEdge(sourceNet, n1, n3, "e4");
	}

	// ----[ TESTS ]----------------------------------------------------------------------------------------------------
	
	// ================================
	// Copy only nodes...
	// ================================
	
	@Test
	public void testCopyNodes_AnotherRoot() throws Exception {
		CyNetworkView targetView = viewTestSupport.getNetworkView();
		Set<CyNode> nodes = new HashSet<>(Arrays.asList(new CyNode[] { n1, n3 }));
		Set<CyEdge> edges = Collections.emptySet();
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(2, targetView.getModel().getNodeCount());
		assertEquals(0, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3"));
		
		// TODO How can we test pasted node and edge views, since the target view is updated when events are flushed?
	}
	
	@Test
	public void testCopyNodes_SameRoot() throws Exception {
		CyNetworkView targetView = createNetworkAndView(sourceView.getModel());
		Set<CyNode> nodes = new HashSet<>(Arrays.asList(new CyNode[] { n1, n3 }));
		Set<CyEdge> edges = Collections.emptySet();
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(2, targetView.getModel().getNodeCount());
		assertEquals(0, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3"));
	}
	
	@Test
	public void testCopyNodes_SameNetwork() throws Exception {
		CyNetworkView targetView = sourceView;
		Set<CyNode> nodes = new HashSet<>(Arrays.asList(new CyNode[] { n1, n3 }));
		Set<CyEdge> edges = Collections.emptySet();
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(5, targetView.getModel().getNodeCount());
		assertEquals(4, targetView.getModel().getEdgeCount());
		// Copied nodes have the same name!
		assertEquals(2, count(targetView.getModel(), "n1", CyNode.class));
		assertEquals(1, count(targetView.getModel(), "n2", CyNode.class));
		assertEquals(2, count(targetView.getModel(), "n3", CyNode.class));
	}
	
	// ================================
	// Copy only edges...
	// ================================
	
	@Test
	public void testCopyEdges_AnotherRoot() throws Exception {
		CyNetworkView targetView = viewTestSupport.getNetworkView();
		Set<CyNode> nodes = Collections.emptySet();
		Set<CyEdge> edges = new HashSet<>(Arrays.asList(new CyEdge[] { e1, e2, e3 }));
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(3, targetView.getModel().getNodeCount()); // Nodes of copied edges also copied!
		assertEquals(3, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n2"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e1"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e2"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e3"));
	}
	
	@Test
	public void testCopyEdges_SameRoot() throws Exception {
		CyNetworkView targetView = createNetworkAndView(sourceView.getModel());
		Set<CyNode> nodes = Collections.emptySet();
		Set<CyEdge> edges = new HashSet<>(Arrays.asList(new CyEdge[] { e1, e2, e3 }));
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(3, targetView.getModel().getNodeCount()); // Nodes of copied edges also copied!
		assertEquals(3, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n2"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e1"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e2"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e3"));
	}
	
	@Test
	public void testCopyEdges_SameNetwork() throws Exception {
		CyNetworkView targetView = sourceView;
		Set<CyNode> nodes = Collections.emptySet();
		Set<CyEdge> edges = new HashSet<>(Arrays.asList(new CyEdge[] { e1, e2, e3 }));
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(3, targetView.getModel().getNodeCount()); // Nodes of copied edges also copied!
		assertEquals(7, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n2"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3"));
		// Copied edges have the same name!
		assertEquals(2, count(targetView.getModel(), "e1", CyEdge.class));
		assertEquals(2, count(targetView.getModel(), "e2", CyEdge.class));
		assertEquals(2, count(targetView.getModel(), "e3", CyEdge.class));
		assertEquals(1, count(targetView.getModel(), "e4", CyEdge.class));
	}
	
	// ================================
	// Copy nodes and edges...
	// ================================
	
	@Test
	public void testCopyBoth_AnotherRoot() throws Exception {
		CyNetworkView targetView = viewTestSupport.getNetworkView();
		Set<CyNode> nodes = new HashSet<>(Arrays.asList(new CyNode[] { n1, n2 }));
		Set<CyEdge> edges = new HashSet<>(Arrays.asList(new CyEdge[] { e1, e3 }));
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(3, targetView.getModel().getNodeCount()); // Nodes of copied edges also copied!
		assertEquals(2, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n2"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3")); // Copied with "e3"
		assertNotNull(getEdgeByName(targetView.getModel(), "e1"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e3"));
	}
	
	@Test
	public void testCopyBoth_SameRoot() throws Exception {
		CyNetworkView targetView = createNetworkAndView(sourceView.getModel());
		Set<CyNode> nodes = new HashSet<>(Arrays.asList(new CyNode[] { n1, n2 }));
		Set<CyEdge> edges = new HashSet<>(Arrays.asList(new CyEdge[] { e1, e3 }));
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(3, targetView.getModel().getNodeCount()); // Nodes of copied edges also copied!
		assertEquals(2, targetView.getModel().getEdgeCount());
		
		assertNotNull(getNodeByName(targetView.getModel(), "n1"));
		assertNotNull(getNodeByName(targetView.getModel(), "n2"));
		assertNotNull(getNodeByName(targetView.getModel(), "n3")); // Copied with "e3"
		assertNotNull(getEdgeByName(targetView.getModel(), "e1"));
		assertNotNull(getEdgeByName(targetView.getModel(), "e3"));
	}
	
	@Test
	public void testCopyBoth_SameNetwork() throws Exception {
		CyNetworkView targetView = sourceView;
		Set<CyNode> nodes = new HashSet<>(Arrays.asList(new CyNode[] { n1, n2 }));
		Set<CyEdge> edges = new HashSet<>(Arrays.asList(new CyEdge[] { e1, e3 }));
		
		ClipboardImpl clipboard = new ClipboardImpl(sourceView, nodes, edges, false, lexicon, serviceRegistrar);
		clipboard.paste(targetView, 0, 0);
		
		assertEquals(5, targetView.getModel().getNodeCount()); // Nodes of copied edges also copied!
		assertEquals(6, targetView.getModel().getEdgeCount());
		
		assertEquals(2, count(targetView.getModel(), "n1", CyNode.class));
		assertEquals(2, count(targetView.getModel(), "n2", CyNode.class));
		assertEquals(1, count(targetView.getModel(), "n3", CyNode.class));
		assertEquals(2, count(targetView.getModel(), "e1", CyEdge.class));
		assertEquals(1, count(targetView.getModel(), "e2", CyEdge.class));
		assertEquals(2, count(targetView.getModel(), "e3", CyEdge.class));
		assertEquals(1, count(targetView.getModel(), "e4", CyEdge.class));
	}
	
	// ----[ PRIVATE METHODS ]------------------------------------------------------------------------------------------
	
	/**
	 * Create a network from the same root-network as the passed subnetwork and then create a view for it.
	 */
	private CyNetworkView createNetworkAndView(CyNetwork net) {
		CySubNetwork newNet = ((CySubNetwork) net).getRootNetwork().addSubNetwork();
		
		return viewTestSupport.getNetworkViewFactory().createNetworkView(newNet);
	}
	
	private CyNode addNode(CyNetwork net, String name) {
		CyNode node = net.addNode();
		net.getRow(node).set(CyNetwork.NAME, name);
		
		return node;
	}
	
	private CyEdge addEdge(CyNetwork net, CyNode n1, CyNode n2, String name) {
		CyEdge edge = net.addEdge(n1, n2, true);
		net.getRow(edge).set(CyNetwork.NAME, name);
		
		return edge;
	}
	
	private CyNode getNodeByName(CyNetwork net, String name) {
		for (CyNode n : net.getNodeList()) {
			if (name.equals(net.getRow(n).get(NAME, String.class)))
				return n;
		}
		
		return null;
	}
	
	private CyEdge getEdgeByName(CyNetwork net, String name) {
		for (CyEdge e : net.getEdgeList()) {
			if (name.equals(net.getRow(e).get(NAME, String.class)))
				return e;
		}
		
		return null;
	}
	
	private int count(CyNetwork net, String name, Class<? extends CyIdentifiable> targetType) {
		int count = 0;
		List<? extends CyIdentifiable> list = targetType == CyNode.class ? net.getNodeList() : net.getEdgeList();
		
		for (CyIdentifiable entry : list) {
			if (name.equals(net.getRow(entry).get(NAME, String.class)))
				count++;
		}
		
		return count;
	}
}
