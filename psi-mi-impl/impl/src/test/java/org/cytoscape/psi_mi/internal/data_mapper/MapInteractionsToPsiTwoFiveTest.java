package org.cytoscape.psi_mi.internal.data_mapper;

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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapFromCytoscape;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.schema.mi25.BibrefType;
import org.cytoscape.psi_mi.internal.schema.mi25.CvType;
import org.cytoscape.psi_mi.internal.schema.mi25.DbReferenceType;
import org.cytoscape.psi_mi.internal.schema.mi25.EntrySet;
import org.cytoscape.psi_mi.internal.schema.mi25.EntrySet.Entry.InteractionList;
import org.cytoscape.psi_mi.internal.schema.mi25.EntrySet.Entry.InteractorList;
import org.cytoscape.psi_mi.internal.schema.mi25.ExperimentType;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractionElementType;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractionElementType.ExperimentList;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractionElementType.ParticipantList;
import org.cytoscape.psi_mi.internal.schema.mi25.InteractorElementType;
import org.cytoscape.psi_mi.internal.schema.mi25.NamesType;
import org.cytoscape.psi_mi.internal.schema.mi25.ParticipantType;
import org.cytoscape.psi_mi.internal.schema.mi25.XrefType;
import org.cytoscape.psi_mi.internal.util.ContentReader;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests MapInteractionsToPsiTwoFive.
 *
 * @author Ethan Cerami
 */
public class MapInteractionsToPsiTwoFiveTest {
	
	private NetworkTestSupport networkTestSupport;

	@Before
	public void setUp() {
		networkTestSupport = new NetworkTestSupport();
	}
	
	/**
	 * Tests Mapper with Sample PSI Data File.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testMapper() throws Exception {
		File file = new File("src/test/resources/testData/psi_sample1.xml");
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent(file.toString());
		List<Interaction> interactions = new ArrayList<Interaction>();

		//  First map PSI-MI Level 1 to interaction objects
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();
		assertEquals(6, interactions.size());

		//  Second, map to Cytoscape objects
		CyNetwork network = networkTestSupport.getNetwork();
		MapToCytoscape mapper2 = new MapToCytoscape(network, interactions, MapToCytoscape.SPOKE_VIEW);
		mapper2.doMapping();

		//  Verify Number of Nodes and Number of Edges
		int nodeCount = network.getNodeCount();
		int edgeCount = network.getEdgeCount();
		assertEquals(7, nodeCount);
		assertEquals(6, edgeCount);

		//  Third, map back to interaction Objects
		MapFromCytoscape mapper3 = new MapFromCytoscape(network);
		mapper3.doMapping();
		interactions = mapper3.getInteractions();
		assertEquals(6, interactions.size());

		//  Fourth, map to PSI-MI Level 2.5
		MapInteractionsToPsiTwoFive mapper4 = new MapInteractionsToPsiTwoFive(interactions);
		mapper4.doMapping();

		EntrySet entrySet = mapper4.getModel();
		validateInteractors(entrySet.getEntry().get(0).getInteractorList());
		// TODO: Fix this -- XREFs for experiments don't seem to work
//		validateInteractions(entrySet.getEntry().get(0).getInteractionList());

		StringWriter writer = new StringWriter();
		JAXBContext jc = JAXBContext.newInstance("org.cytoscape.psi_mi.internal.schema.mi25");
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(entrySet, writer);

		//  Verify that XML indentation is turned on.
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
		                  + "<entrySet version=\"5\" level=\"2\" xmlns=\"net:sf:psidev:mi\">\n"
		                  + "    <entry>\n" + "        <interactorList>";
		assertTrue("XML Indentation Test has failed.  ", writer.toString().startsWith(expected));
	}

	/**
	 * Validates Interactor Objects.
	 *
	 * @param interactorList Castor InteractorList Object.
	 */
	private void validateInteractors(InteractorList interactorList) {
		for (InteractorElementType interactor : interactorList.getInteractor()) {
			NamesType name = interactor.getNames();
			if ("YHR119W".equals(name.getShortLabel())) {
				assertTrue(name.getFullName().startsWith("Gene has a SET or TROMO"));
		
				InteractorElementType.Organism organism = interactor.getOrganism();
				assertEquals(4932, organism.getNcbiTaxId());
				assertEquals("baker's yeast", organism.getNames().getShortLabel());
				assertEquals("Saccharomyces cerevisiae", organism.getNames().getFullName());
		
				XrefType xrefType = interactor.getXref();
				DbReferenceType xref = xrefType.getPrimaryRef();
				assertEquals("Entrez GI", xref.getDb());
				assertEquals("529135", xref.getId());
		
				xref = xrefType.getSecondaryRef().get(0);
				assertEquals("RefSeq GI", xref.getDb());
				assertEquals("6321911", xref.getId());
		
				String sequence = interactor.getSequence();
				assertTrue(sequence.startsWith("MNTYAQESKLRLKTKIGAD"));
				return;
			}
		}
		fail();
	}

	/**
	 * Validates Interaction Objects.
	 *
	 * @param interactionList Castor Interaction Object.
	 */
	@SuppressWarnings("unused")
	private void validateInteractions(InteractionList interactionList) {
		InteractionElementType interaction = interactionList.getInteraction().get(3);
		ExperimentList expList = interaction.getExperimentList();
		ExperimentType expType = (ExperimentType) expList.getExperimentRefOrExperimentDescription()
		                                                 .get(0);
		BibrefType bibRef = expType.getBibref();
		XrefType xref = bibRef.getXref();
		DbReferenceType primaryRef = xref.getPrimaryRef();
		assertEquals("pubmed", primaryRef.getDb());
		assertEquals("11283351", primaryRef.getId());

		CvType cvType = expType.getInteractionDetectionMethod();
		NamesType name = cvType.getNames();
		assertEquals("classical two hybrid", name.getShortLabel());
		xref = cvType.getXref();
		primaryRef = xref.getPrimaryRef();
		assertEquals("PSI-MI", primaryRef.getDb());
		assertEquals("MI:0018", primaryRef.getId());

		ParticipantList pList = interaction.getParticipantList();
		ParticipantType participant = pList.getParticipant().get(0);
		Integer ref = participant.getInteractorRef();
		assertEquals(Integer.valueOf(2), ref);

		//  Verify Interaction XRefs.
		xref = interaction.getXref();
		primaryRef = xref.getPrimaryRef();

		String db = primaryRef.getDb();
		String id = primaryRef.getId();
		assertEquals("DIP", db);
		assertEquals("61E", id);
	}
}
