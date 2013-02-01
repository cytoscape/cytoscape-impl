package org.cytoscape.psi_mi.internal.cyto_mapper;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiTwoFiveToInteractions;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.vocab.CommonVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractionVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractorVocab;
import org.cytoscape.psi_mi.internal.util.AttributeUtil;
import org.cytoscape.psi_mi.internal.util.ContentReader;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the MapToCytoscape Class.
 *
 * @author Ethan Cerami.
 */
public class MapToCytoscapeTest {

	private NetworkTestSupport networkTestSupport;

	@Before
	public void setUp() {
		networkTestSupport = new NetworkTestSupport();
	}
	
	/**
	 * Tests the MapPsiInteractionsTo Graph mapper.
	 * This test assumes a new empty GraphPerspective.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testMapper1() throws Exception {
		//  First, get some interactions from sample data file.
		List<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/psi_sample1.xml");

		//  Map from PSI One to DataService Interaction Objects.
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Now Map to Cytocape Network Objects.
		CyNetwork network = networkTestSupport.getNetwork();
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.MATRIX_VIEW);
		mapper2.doMapping();

		//  Verify Number of Nodes and Number of Edges
		int nodeCount = network.getNodeCount();
		int edgeCount = network.getEdgeCount();
		assertEquals(7, nodeCount);
		assertEquals(6, edgeCount);

		//  Verify one of the nodes in the graph
		//  First find correct index value
		CyNode node1 = null;

		for (CyNode node : network.getNodeList()) {
			String name = getName(network, node);
			if (name.equals("YDL065C")) {
				node1 = node;
			}
		}
		assertNotNull(node1);

		//  Verify edge in the graph
		//  First find correct index value
		CyEdge edge1 = null;

		for (CyEdge edge : network.getEdgeList()) {
			if (checkEdge(network, edge, "YCR038C", "YDR532C")) {
				edge1 = edge;
			}
		}
		assertNotNull(edge1);

		//  Verify source / target nodes of edge
		CyNode sourceNode = edge1.getSource();

		//  Verify that Attributes were mapped over too...
		CyRow nodeAttributes = network.getRow(sourceNode);
		String taxonomyId = nodeAttributes.get(InteractorVocab.ORGANISM_NCBI_TAXONOMY_ID, String.class); 
		assertEquals("4932", taxonomyId);

		String fullName = nodeAttributes.get(InteractorVocab.FULL_NAME, String.class); 
		assertTrue(fullName.indexOf("GTP/GDP exchange factor") > -1);

		//  Verify that DB Names were mapped over correctly.
		//  There are multiple DB Names in an array of Strings.
		List<?> dbNameList = nodeAttributes.getList(CommonVocab.XREF_DB_NAME, String.class);
		assertEquals(15, dbNameList.size());
		assertEquals("RefSeq GI", dbNameList.get(0));

		//  Verify that Interaction Xrefs were mapped over correctly.
		CyRow edgeAttributes = network.getRow(edge1);
		dbNameList = edgeAttributes.getList(CommonVocab.XREF_DB_NAME, String.class);

		List<?> dbIdList = edgeAttributes.getList(CommonVocab.XREF_DB_ID, String.class);
		assertEquals(2, dbNameList.size());
		assertEquals(2, dbIdList.size());
		assertEquals("DIP", dbNameList.get(0));
		assertEquals("CPATH", dbNameList.get(1));
		assertEquals("61E", dbIdList.get(0));
		assertEquals("12345", dbIdList.get(1));
	}

	private boolean checkEdge(CyNetwork network, CyEdge edge, String source, String target) {
		return getName(network, edge.getSource()).equals(source) && getName(network, edge.getTarget()).equals(target);
	}

	private boolean checkEdge(CyNetwork network, CyEdge edge, String source, String target, String experimentalSystem, String interactionShortName) {
		CyRow attributes = network.getRow(edge);
		return getName(network, edge.getSource()).equals(source) &&
			   getName(network, edge.getTarget()).equals(target) &&
			   attributes.get(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME, String.class).equals(experimentalSystem) &&
			   attributes.get(InteractionVocab.INTERACTION_SHORT_NAME, String.class).equals(interactionShortName);
	}
	
	private String getName(CyNetwork network, CyNode node) {
		return network.getRow(node).get(AttributeUtil.NODE_NAME_ATTR_LABEL, String.class);
	}

	/**
	 * Tests the MapPsiInteractionsTo Graph mapper.
	 * This test assumes a pre-existing GraphPerspective with existing nodes/edges.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testMapper2() throws Exception {
		//  First, get some interactions from sample data file.
		List<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/psi_sample1.xml");

		//  Map from PSI to DataService Interaction Objects.
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Create CyNetwork, and pre-populate it with some existing data.
		CyNetwork network = networkTestSupport.getNetwork();

		//  Now map interactions to cyNetwork.
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.MATRIX_VIEW);
		mapper2.doMapping();

		//  Verify Number of Nodes;  it should be 7.
		int nodeCount = network.getNodeCount();
		assertEquals(7, nodeCount);

		//  Verify Number of Edges;  it should be 6.
		int edgeCount = network.getEdgeCount();
		assertEquals(6, edgeCount);
	}

	/**
	 * Tests the MapPsiInteractionsTo Graph mapper.
	 * This time, we test that the MATRIX_VIEW works with # interactors > 2.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testMapper3() throws Exception {
		//  First, get some interactions from sample data file.
		List<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/psi_sample2.xml");

		//  Map from PSI to DataService Interaction Objects.
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Create CyNetwork
		CyNetwork network = networkTestSupport.getNetwork();

		//  Now map interactions to cyNetwork.
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.MATRIX_VIEW);
		mapper2.doMapping();

		//  Verify Number of Nodes;  there should be 4.
		int nodeCount = network.getNodeCount();
		assertEquals(4, nodeCount);

		//  Verify Number of Edges; there should be 6
		int edgeCount = network.getEdgeCount();
		assertEquals(6, edgeCount);

		int counter = 0;

		for (CyEdge edge : network.getEdgeList()) {
			if (checkEdge(network, edge, "A", "C") ||
				checkEdge(network, edge, "A", "D") ||
				checkEdge(network, edge, "B", "C") ||
				checkEdge(network, edge, "B", "D") ||
				checkEdge(network, edge, "C", "D") ||
				checkEdge(network, edge, "A", "B")) { 
				counter++;
			}
		}

		assertEquals(6, counter);
	}

	/**
	 * Tests the MapPsiInteractionsTo Graph mapper.
	 * This time, we test that the SPOKE_VIEW works with # interactors > 2.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testMapper4() throws Exception {
		//  First, get some interactions from sample data file.
		List<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/psi_sample2.xml");

		//  Map from PSI to DataService Interaction Objects.
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Create CyNetwork
		CyNetwork network = networkTestSupport.getNetwork();

		//  Now map interactions to cyNetwork.
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		//  Verify Number of Nodes;  there should be 4.
		int nodeCount = network.getNodeCount();
		assertEquals(4, nodeCount);

		//  Verify Number of Edges; there should be 3
		int edgeCount = network.getEdgeCount();
		assertEquals(3, edgeCount);

		int counter = 0;

		for (CyEdge edge : network.getEdgeList()) {
			if (checkEdge(network, edge, "A", "B") ||
				checkEdge(network, edge, "A", "C") ||
				checkEdge(network, edge, "A", "D")) {
				counter++;
			}
		}

		assertEquals(3, counter);
	}

	/**
	 * Test PSI-MI Level 2.5.
	 * @throws Exception All Errors.
	 */
	@Test
	public void testMapper5() throws Exception {
		//  First, get some interactions from sample data file.
		List<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/psi_sample_2_5_2.xml");

		//  Map from PSI to DataService Interaction Objects.
		MapPsiTwoFiveToInteractions mapper1 = new MapPsiTwoFiveToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Create CyNetwork
		CyNetwork network = networkTestSupport.getNetwork();

		//  Now map interactions to cyNetwork.
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		//  Verify Number of Nodes;  there should be 3.
		int nodeCount = network.getNodeCount();
		assertEquals(3, nodeCount);

		//  Verify Number of Edges; there should be 35
		int edgeCount = network.getEdgeCount();
		assertEquals(35, edgeCount);

		int counter = 0;
		
		for (CyEdge edge : network.getEdgeList()) {
			if (checkEdge(network, edge, "kaib_synp7", "kaia_synp7", "pull down", "kaib-kaia-2") ||
				checkEdge(network, edge, "kaib_synp7", "kaic_synp7", "pull down", "kaib-kaic-5") ||
				checkEdge(network, edge, "kaic_synp7", "kaia_synp7", "two hybrid", "kaic-kaia-1")) {
				counter++;
			}
		}

		assertEquals(3, counter);
	}

	/**
	 * Tests BioGrid Data defined in Bug:  0001126
	 *
	 * @throws Exception All Errors
	 */
	@Test
	public void testBioGridData() throws Exception {
		//  First, get some interactions from sample data file.
		List<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/bio_grid.xml");

		//  Map from PSI to DataService Interaction Objects.
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Create CyNetwork
		CyNetwork network = networkTestSupport.getNetwork();

		//  Now map interactions to cyNetwork.
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		CyNode node1 = null;
		for (CyNode node : network.getNodeList()) {
			if (getName(network, node).equals("HGNC:7733")) {
				node1 = node;
			}
		}
		assertNotNull(node1);

		assertEquals(129, network.getEdgeCount());
	}

	/**
	 * Profile Loading of HPRD Data.
	 *
	 * @throws Exception All Exceptions.
	 */
	public void profileHprd() throws Exception {
		List<Interaction> allInteractions = new ArrayList<Interaction>();

		//  First, get some interactions from sample data file.
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/hprd.xml");

		//  Map from PSI to DataService Interaction Objects.
		for (int i = 0; i < 25; i++) {
			List<Interaction> interactions = new ArrayList<Interaction>();
			MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
			mapper1.doMapping();
			allInteractions.addAll(interactions);
		}

		//  Now Map to Cytocape Network Objects.
		System.out.println("Mapping to Cytoscape Network");
		System.out.println("Number of Interactions:  " + allInteractions.size());

		CyNetwork network = networkTestSupport.getNetwork();
		MapToCytoscape mapper2 = new MapToCytoscape(network, allInteractions, MapToCytoscape.MATRIX_VIEW);
		mapper2.doMapping();
		System.out.println("DONE");
	}

	/**
	 * Main Method.  Used for JProfiler.
	 *
	 * @param args Command Line Arguments.
	 * @throws Exception All Exceptions.
	 */
	public static void main(String[] args) throws Exception {
		Date start = new Date();
		MapToCytoscapeTest test = new MapToCytoscapeTest();
		test.profileHprd();

		Date stop = new Date();
		long time = stop.getTime() - start.getTime();
		System.out.println("Time:  " + time);
	}
}
