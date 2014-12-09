package org.cytoscape.data.reader.graphml;

/*
 * #%L
 * Cytoscape GraphML Impl (graphml-impl)
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

import static org.cytoscape.model.CyNetwork.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.read.graphml.GraphMLReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GraphMLReaderTest {
	
	private NetworkTestSupport testSupport;
	private NetworkViewTestSupport nvts;
	private CyNetworkFactory netFactory;
	private CyRootNetworkManager rootFactory;
	private CyNetworkViewFactory viewFactory;
	
	@Mock
	private CyLayoutAlgorithmManager layouts;
	
	@Mock
	private TaskMonitor tm;
	
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Before
	public void setUp() throws Exception {
		testSupport = new NetworkTestSupport();
		nvts = new NetworkViewTestSupport();
		
		rootFactory = testSupport.getRootNetworkFactory();
		netFactory = testSupport.getNetworkFactory();
		viewFactory = nvts.getNetworkViewFactory();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadSimpleGraph() throws Exception {
		File file = new File("src/test/resources/testGraph1.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		final CyNetwork network = networks[0];
		assertEquals(11, network.getNodeCount());
		assertEquals(12, network.getEdgeCount());
	}
	
	@Test
	public void testReadAttrGraph() throws Exception {
		File file = new File("src/test/resources/simpleWithAttributes.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		
		final CyNetwork net = networks[0];
		assertEquals(6, net.getNodeCount());
		assertEquals(7, net.getEdgeCount());

		final CyNode n1 = getNodeByName(net, "n0");
		assertNotNull(n1);
		
		final CyEdge e1 = getEdgeByName(net, "n0 (-) n1");
		assertNotNull(e1);
		
		final CyColumn colorCol = net.getDefaultNodeTable().getColumn("color");
		final CyColumn rankCol = net.getDefaultNodeTable().getColumn("rank");
		final CyColumn degreeCol = net.getDefaultNodeTable().getColumn("degree");
		final CyColumn scoreCol = net.getDefaultNodeTable().getColumn("score");
		final CyColumn taggedCol = net.getDefaultNodeTable().getColumn("tagged");
		final CyColumn weightCol = net.getDefaultEdgeTable().getColumn("weight");
		
		assertNotNull(colorCol);
		assertNotNull(rankCol);
		assertNotNull(degreeCol);
		assertNotNull(scoreCol);
		assertNotNull(taggedCol);
		assertNotNull(weightCol);
		
		assertEquals(String.class, colorCol.getType());
		assertEquals(Integer.class, rankCol.getType());
		assertEquals(Long.class, degreeCol.getType());
		assertEquals(Double.class, scoreCol.getType()); // GraphML "float" is converted to Double by Cytoscape
		assertEquals(Boolean.class, taggedCol.getType());
		assertEquals(Double.class, weightCol.getType());
		
		assertEquals("green", net.getRow(n1).get("color", String.class));
		assertEquals(Integer.valueOf(3), net.getRow(n1).get("rank", Integer.class));
		assertEquals(Long.valueOf(2), net.getRow(n1).get("degree", Long.class));
		assertEquals(Double.valueOf(0.95d), net.getRow(n1).get("score", Double.class));
		assertEquals(Boolean.TRUE, net.getRow(n1).get("tagged", Boolean.class));
		assertEquals(Double.valueOf(1.0d), net.getRow(e1).get("weight", Double.class));
	}

	@Test
	public void testReadAttedOutput() throws Exception {
		File file = new File("src/test/resources/atted.graphml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		final CyNetwork network = networks[0];
		
//		nodes = network.getno
//		
//		assertEquals("AtbZIP52", nodeAttr.getAttribute("At1g06850", "symbol"));
//		assertEquals("bZIP", nodeAttr.getAttribute("At1g06850", "TF_family"));
//		
//		assertEquals("correlation", edgeAttr.getAttribute("At5g48880 (pp) At1g65060", "label"));
//		assertEquals(5.20, edgeAttr.getAttribute("At5g48880 (pp) At1g65060", "mr_all"));
	}
	
	
	@Test
	public void testReadNestedSubgraphs() throws Exception {
		File file = new File("src/test/resources/nested.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(4, networks.length);
		
		final CyNetwork rootNetwork = networks[0];
		for(CyNode node: rootNetwork.getNodeList())
			System.out.println("In root network: " + rootNetwork.getRow(node).get(CyNetwork.NAME, String.class));
		
		assertEquals(11, rootNetwork.getNodeCount());
		assertEquals(12, rootNetwork.getEdgeCount());
		
		final CyNetwork child1 = networks[1];
		assertEquals(3, child1.getNodeCount());
		assertEquals(2, child1.getEdgeCount());
		
		final CyNetwork child2 = networks[2];
		assertEquals(3, child2.getNodeCount());
		assertEquals(2, child2.getEdgeCount());
		
		final CyNetwork child3 = networks[3];
		assertEquals(1, child3.getNodeCount());
		assertEquals(0, child3.getEdgeCount());
	}
	
	@Test
	public void testReadNestedSubgraphs2() throws Exception {
		File file = new File("src/test/resources/nested2.xml");
		InputStream stream = file.toURI().toURL().openStream();
		GraphMLReader reader = new GraphMLReader(stream, layouts, netFactory, viewFactory, rootFactory);
		assertNotNull(reader);
		reader.run(tm);
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(4, networks.length);
		
		final CyNetwork rootNetwork = networks[0];
		for(CyNode node: rootNetwork.getNodeList())
			System.out.println("In root network: " + rootNetwork.getRow(node).get(CyNetwork.NAME, String.class));
		
		assertEquals(8, rootNetwork.getNodeCount());
		assertEquals(10, rootNetwork.getEdgeCount());
		
		final CyNetwork child1 = networks[1];
		assertEquals(6, child1.getNodeCount());
		assertEquals(7, child1.getEdgeCount());
		
		final CyNetwork child2 = networks[2];
		assertEquals(3, child2.getNodeCount());
		assertEquals(3, child2.getEdgeCount());
		
		final CyNetwork child3 = networks[3];
		assertEquals(2, child3.getNodeCount());
		assertEquals(1, child3.getEdgeCount());
	}
	
	private CyNode getNodeByName(final CyNetwork net, final String name) {
		for (CyNode n : net.getNodeList()) {
			if (name.equals(net.getRow(n).get(NAME, String.class)))
				return n;
		}
		
		return null;
	}
	
	private CyEdge getEdgeByName(final CyNetwork net, final String name) {
		for (CyEdge e : net.getEdgeList()) {
			if (name.equals(net.getRow(e).get(NAME, String.class)))
				return e;
		}
		
		return null;
	}

}
