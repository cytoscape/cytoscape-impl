package org.cytoscape.data.writer.graphml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.write.graphml.GraphMLWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.junit.After;
import org.junit.Before;

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

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GraphMLWriterTest {

	private final NetworkViewTestSupport support = new NetworkViewTestSupport();

	private CyNetwork network1;

	@Before
	public void setUp() throws Exception {
		network1 = support.getNetwork();
		network1.getRow(network1).set(CyNetwork.NAME, "SampleNet1");
		
		CyNode n1 = network1.addNode();
		CyNode n2 = network1.addNode();
		CyNode n3 = network1.addNode();

		// Not connected
		CyNode n4 = network1.addNode();

		CyEdge e1 = network1.addEdge(n1, n2, true);
		CyEdge e2 = network1.addEdge(n2, n3, true);
		CyEdge e3 = network1.addEdge(n1, n3, true);
		CyEdge e1self = network1.addEdge(n1, n1, true);

		network1.getRow(n1).set(CyNetwork.NAME, "n1");
		network1.getRow(n2).set(CyNetwork.NAME, "n2 日本語");
		network1.getRow(n3).set(CyNetwork.NAME, "n3");
		network1.getRow(n4).set(CyNetwork.NAME, "Alone");

		network1.getRow(e1).set(CyNetwork.NAME, "e1");
		network1.getRow(e2).set(CyNetwork.NAME, "e2");
		network1.getRow(e3).set(CyNetwork.NAME, "e3");
		network1.getRow(e1self).set(CyNetwork.NAME, "e1self");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGraphMLWriter() throws Exception {
		assertNotNull(network1);

		final File temp = File.createTempFile("network1", ".graphml");
		temp.deleteOnExit();

		OutputStream os = new FileOutputStream(temp);
		GraphMLWriter writer = new GraphMLWriter(os, network1);
		writer.run(null);
		os.close();

		// Read contents
		System.out.println("Temp GraphML file = " + temp.getAbsolutePath());

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(temp);
		doc.getDocumentElement().normalize();

		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

		NodeList graphList = doc.getElementsByTagName("graph");

		assertNotNull(graphList);
		assertEquals(1, graphList.getLength());
		Node graphNode = graphList.item(0);
		Node networkName = graphNode.getAttributes().getNamedItem("id");
		System.out.println("Network Name :" + networkName.getNodeValue());
		assertEquals("SampleNet1", networkName.getNodeValue());
		
		NodeList nList = doc.getElementsByTagName("node");
		assertEquals(4, nList.getLength());

		NodeList eList = doc.getElementsByTagName("edge");
		assertEquals(4, eList.getLength());
		
		NodeList typeList = doc.getElementsByTagName("key");
		assertEquals(14, typeList.getLength());
	}
}
