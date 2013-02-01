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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.model.vocab.InteractionVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractorVocab;
import org.cytoscape.psi_mi.internal.util.ContentReader;
import org.cytoscape.psi_mi.internal.util.DataServiceException;
import org.junit.Test;

/**
 * Tests the MapPsiOneToInteractions Mapper.
 *
 * @author Ethan Cerami
 */
public class MapPsiOneToInteractionsTest {
	/**
	 * Test the PSI Mapper, Case 1.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testPsiMapper1() throws Exception {
		String xml = readFile("src/test/resources/testData/psi_sample2.xml");
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();

		//  Validate Interaction at index = 0.
		Interaction interaction = interactions.get(0);
		validateSample1(interaction);
	}

	/**
	 * Test the PSI Mapper, Case 2.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testPsiMapper2() throws Exception {
		String xml = readFile("src/test/resources/testData/yeast_normalised.xml");
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();

		//  Validate Interaction at index = 0.
		Interaction interaction = interactions.get(0);
		validateSample2(interaction);
	}

	/**
	 * Test the PSI Mapper, Case 3.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testPsiMapper3() throws Exception {
		String xml = readFile("src/test/resources/testData/yeast_denormalised.xml");
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();

		//  Validate Interaction at index = 0.
		Interaction interaction = interactions.get(0);
		validateSample3(interaction);
	}

	/**
	 * Test the PSI Mapper with Sample DIP Data.
	 * Tests Specifically for Changes in ShortLabel Data.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testDipData1() throws Exception {
		String xml = readFile("src/test/resources/testData/dip_sample.xml");
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();

		Interaction interaction = interactions.get(0);
		List<Interactor> interactors = interaction.getInteractors();

		//  The first interactor does not have a short label.
		//  In the absence of a short label, the SwissProt.
		//  These tests verifies this fact.
		Interactor interactor = interactors.get(0);
		String name = interactor.getName();
		assertEquals("P06139", name);

		interactor = interactors.get(1);
		name = interactor.getName();
		assertEquals("major prion PrP-Sc protein precursor", name);

		//  Verify Interaction Xrefs
		ExternalReference[] refs = interaction.getExternalRefs();
		assertEquals(1, refs.length);
		assertEquals("DIP", refs[0].getDatabase());
		assertEquals("61E", refs[0].getId());
	}

	/**
	 * Test the PSI Mapper with Sample DIP Data.
	 * Tests Specifically for Multiple Experimental Results
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testDipData2() throws Exception {
		String xml = readFile("src/test/resources/testData/dip_sample.xml");
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();

		// The sample data file has one PSI interaction.
		// with 5 experimentDescriptions.  This will map to
		// 5 data service Interaction objects.
		assertEquals(5, interactions.size());

		Interaction interaction = interactions.get(0);
		this.validateDipInteractions(interaction, "11821039", "Genetic", "PSI", "MI:0045");
		interaction = interactions.get(1);
		this.validateDipInteractions(interaction, "9174345", "x-ray crystallography", "PSI",
		                             "MI:0114");
		interaction = interactions.get(4);
		this.validateDipInteractions(interaction, "10089390", "x-ray crystallography", "PSI",
		                             "MI:0114");
	}

	/**
	 * Tests sample cPath File.
	 * @throws Exception All Errors.
	 */
	@Test
	public void testcPathData() throws Exception {
		String xml = readFile("src/test/resources/testData/cpath_p53.xml");
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiOneToInteractions mapper = new MapPsiOneToInteractions(xml, interactions);
		mapper.doMapping();
		assertEquals(10, interactions.size());
	}

	/**
	 * Validates Specific Interaction.
	 */
	private void validateSample1(Interaction interaction) {
		assertEquals("11283351", interaction.getAttribute(InteractionVocab.PUB_MED_ID));
		assertEquals("classical two hybrid",
		             interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME));
		assertEquals("MI:0018",
		             interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_ID));

		List<Interactor> interactors = interaction.getInteractors();
		assertEquals(4, interactors.size());

		Interactor interactor0 = interactors.get(0);
		Interactor interactor1 = interactors.get(1);
		Interactor interactor2 = interactors.get(2);
		Interactor interactor3 = interactors.get(3);

		assertEquals("A", interactor0.getName());
		assertEquals("B", interactor1.getName());
		assertEquals("C", interactor2.getName());
		assertEquals("D", interactor3.getName());

		String fullName0 = (String) interactor0.getAttribute(InteractorVocab.FULL_NAME);
		String fullName1 = (String) interactor1.getAttribute(InteractorVocab.FULL_NAME);
		String fullName2 = (String) interactor2.getAttribute(InteractorVocab.FULL_NAME);
		String fullName3 = (String) interactor3.getAttribute(InteractorVocab.FULL_NAME);
		assertTrue(fullName0.startsWith("Gene has a SET"));
		assertTrue(fullName1.startsWith("Kinesin-related"));
		assertTrue(fullName2.startsWith("SH3-domain"));
		assertTrue(fullName3.startsWith("SH3-domain"));
	}

	private void validateDipInteractions(Interaction interaction, String expectedPmid,
	                                     String expectedSystemName, String expectedDbName,
	                                     String expectedDbId) {
		String pmid = (String) interaction.getAttribute(InteractionVocab.PUB_MED_ID);
		String expName = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME);
		String dbName = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_DB);
		String dbId = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_ID);
		assertEquals(expectedPmid, pmid);
		assertEquals(expectedSystemName, expName);
		assertEquals(expectedDbName, dbName);
		assertEquals(expectedDbId, dbId);

		List<Interactor> interactors = interaction.getInteractors();
		assertEquals(2, interactors.size());

		Interactor interactor = interactors.get(0);
		assertEquals("P06139", interactor.getName());
		interactor = interactors.get(1);
		assertEquals("major prion PrP-Sc protein precursor", interactor.getName());
	}

	/**
	 * Validates Specific Interaction.
	 */
	private void validateSample2(Interaction interaction) {
		List<Interactor> interactors = interaction.getInteractors();
		Interactor interactor0 = interactors.get(0);
		assertEquals("MAK10", interactor0.getName());

		Interactor interactor1 = interactors.get(1);
		assertEquals("MAK3", interactor1.getName());
	}

	/**
	 * Validates Specific Interaction.
	 */
	private void validateSample3(Interaction interaction) {
		List<Interactor> interactors = interaction.getInteractors();
		Interactor interactor0 = interactors.get(0);
		assertEquals("MAK10", interactor0.getName());

		Interactor interactor1 = interactors.get(1);
		assertEquals("MAK3", interactor1.getName());
	}

	/**
	 * Outputs Interaction (used for debugging purposes).
	 */
	@SuppressWarnings("unused")
	private void outputInteraction(Interaction interaction) {
		System.out.println("Interaction:");

		String pubMedID = (String) interaction.getAttribute(InteractionVocab.PUB_MED_ID);
		String expSystem = (String) interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME);
		System.out.println(".. PubMedID:  " + pubMedID);
		System.out.println(".. Experimental System:  " + expSystem);

		List<Interactor> interactors = interaction.getInteractors();

		for (int i = 0; i < interactors.size(); i++) {
			Interactor interactor = interactors.get(i);
			outputInteractor(interactor);
		}
	}

	/**
	 * Outputs Interactor.
	 */
	private void outputInteractor(Interactor interactor) {
		System.out.println(".. Interactor:  " + interactor.getName());
		System.out.println("..... Description:  " + interactor.getDescription());
	}
	
	private String readFile(String path) throws DataServiceException {
		File file = new File(path);
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent(file.toString());
		return xml;
	}

}
