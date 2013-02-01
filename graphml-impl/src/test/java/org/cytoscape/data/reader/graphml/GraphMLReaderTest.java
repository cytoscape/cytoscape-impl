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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.read.graphml.GraphMLReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
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
		final CyNetwork network = networks[0];
		assertEquals(6, network.getNodeCount());
		assertEquals(7, network.getEdgeCount());

		List<CyNode> nodeList = network.getNodeList();
		final CyNode node1 = nodeList.get(0);
		assertNotNull(node1);
		
		List<CyEdge> edgeList = network.getEdgeList();
		CyEdge edge1 = edgeList.get(0);
		assertNotNull(edge1);

		edge1 = null;

		// find edge "e0"
		for (CyEdge edge: edgeList) {
			if (network.getRow(edge).get(CyNetwork.NAME, String.class).equals("n0 (-) n1")) {
				edge1 = edge;
				break;
			}
		}
		assertNotNull(edge1);
		
		final CyColumn colorCol = network.getDefaultNodeTable().getColumn("color");
		final CyColumn weightCol = network.getDefaultEdgeTable().getColumn("weight");
		
		assertNotNull(colorCol);
		assertNotNull(weightCol);
		
		assertEquals(String.class, colorCol.getType());
		assertEquals(Double.class, weightCol.getType());
		
		assertEquals(Double.valueOf(1.0d), network.getRow(edge1).get("weight", Double.class));
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

}
