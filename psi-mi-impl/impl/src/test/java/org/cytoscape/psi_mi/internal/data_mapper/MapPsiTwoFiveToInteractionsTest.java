/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.psi_mi.internal.data_mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.psi_mi.internal.data_mapper.MapPsiTwoFiveToInteractions;
import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.model.vocab.InteractionVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractorVocab;
import org.cytoscape.psi_mi.internal.util.ContentReader;
import org.junit.Test;

/**
 * Tests the MapPsiTwoFiveToInteractions Mapper.
 *
 * @author Ethan Cerami
 */
public class MapPsiTwoFiveToInteractionsTest {
	/**
	 * Test the PSI Mapper, Case 2.
	 * Sample Data:  PSI-MI Level 2.5 File
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testPsiMapper1() throws Exception {
		File file = new File("src/test/resources/testData/psi_sample_2_5_1.xml");
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent(file.toString());
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiTwoFiveToInteractions mapper = new MapPsiTwoFiveToInteractions(xml, interactions);
		mapper.doMapping();

		//  Validate that we got all interactions
		assertEquals(1, interactions.size());

		//  Validate Interaction at index = 0.
		Interaction interaction = interactions.get(0);
		List<Interactor> interactorList = interaction.getInteractors();
		Interactor interactor = interactorList.get(0);
		assertEquals("hs90b_human", interactor.getName());

		//  Verify bait map
		@SuppressWarnings("unchecked")
		Map<String, String> baitMap = (Map<String, String>) interaction.getAttribute(InteractionVocab.BAIT_MAP);
		String role = baitMap.get(interactor.getName());
		assertEquals("bait", role);
	}

	/**
	 * Test the PSI Mapper, Case 2.
	 * Sample Data:  PSI-MI Level 2.5 File
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testPsiMapper2() throws Exception {
		File file = new File("src/test/resources/testData/psi_sample_2_5_2.xml");
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent(file.toString());
		List<Interaction> interactions = new ArrayList<Interaction>();
		MapPsiTwoFiveToInteractions mapper = new MapPsiTwoFiveToInteractions(xml, interactions);
		mapper.doMapping();

		//  Validate that we got all interactions
		assertEquals(35, interactions.size());

		//  Validate Interaction at index = 0.
		Interaction interaction = interactions.get(0);
		validateInteractionCase1(interaction);
	}

	private void validateInteractionCase1(Interaction interaction) {
		@SuppressWarnings("unchecked")
		Map<String, String> baitMap = (Map<String, String>) interaction.getAttribute(InteractionVocab.BAIT_MAP);
		assertEquals(2, baitMap.size());
		assertEquals("bait", baitMap.get("kaic_synp7"));
		assertEquals("prey", baitMap.get("kaia_synp7"));

		assertEquals("10064581", interaction.getAttribute(InteractionVocab.PUB_MED_ID));
		assertEquals("yeast two hybrid kaiC kaiA interaction",
		             interaction.getAttribute(InteractionVocab.INTERACTION_FULL_NAME));
		assertEquals("kaic-kaia-1",
		             interaction.getAttribute(InteractionVocab.INTERACTION_SHORT_NAME));
		assertEquals("MI:0018",
		             interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_ID));
		assertEquals("two hybrid",
		             interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_NAME));
		assertEquals("psi-mi",
		             interaction.getAttribute(InteractionVocab.EXPERIMENTAL_SYSTEM_XREF_DB));

		//  Validate interactors
		List<Interactor> interactors = interaction.getInteractors();
		Interactor interactor0 = interactors.get(0);
		Interactor interactor1 = interactors.get(1);

		//  Validate short name, full name
		assertEquals("kaic_synp7", interactor0.getName());
		assertEquals("kaia_synp7", interactor1.getName());

		assertEquals("Circadian clock protein kinase kaiC",
		             interactor0.getAttribute(InteractorVocab.FULL_NAME));
		assertEquals("Circadian clock protein kaiA",
		             interactor1.getAttribute(InteractorVocab.FULL_NAME));

		//  Validate organism data
		assertEquals("synp7", interactor0.getAttribute(InteractorVocab.ORGANISM_COMMON_NAME));
		assertEquals("synp7", interactor1.getAttribute(InteractorVocab.ORGANISM_COMMON_NAME));

		assertEquals("Synechococcus sp. (strain PCC 7942)",
		             interactor0.getAttribute(InteractorVocab.ORGANISM_SPECIES_NAME));
		assertEquals("Synechococcus sp. (strain PCC 7942)",
		             interactor1.getAttribute(InteractorVocab.ORGANISM_SPECIES_NAME));

		assertEquals("1140", interactor0.getAttribute(InteractorVocab.ORGANISM_NCBI_TAXONOMY_ID));
		assertEquals("1140", interactor1.getAttribute(InteractorVocab.ORGANISM_NCBI_TAXONOMY_ID));

		//  Validate Sequence Data
		String sequence0 = (String) interactor0.getAttribute(InteractorVocab.SEQUENCE_DATA);
		assertTrue(sequence0.startsWith("MTSAEMTS"));

		String sequence1 = (String) interactor1.getAttribute(InteractorVocab.SEQUENCE_DATA);
		assertTrue(sequence1.startsWith("MLSQIAIC"));

		//  Validate all Xrefs for Interactor 0
		ExternalReference[] xrefs = interactor0.getExternalRefs();
		assertEquals(6, xrefs.length);
		assertEquals("uniprotkb", xrefs[0].getDatabase());
		assertEquals("Q79PF4", xrefs[0].getId());
		assertEquals("interpro", xrefs[1].getDatabase());
		assertEquals("IPR010624", xrefs[1].getId());
		assertEquals("uniprotkb", xrefs[2].getDatabase());
		assertEquals("Q9Z3H2", xrefs[2].getId());
		assertEquals("go", xrefs[3].getDatabase());
		assertEquals("GO:0005515", xrefs[3].getId());
		assertEquals("go", xrefs[4].getDatabase());
		assertEquals("GO:0042754", xrefs[4].getId());
		assertEquals("intact", xrefs[5].getDatabase());
		assertEquals("EBI-592287", xrefs[5].getId());

		//  Validate Xrefs for Interaction
		xrefs = interaction.getExternalRefs();
		assertEquals(1, xrefs.length);
		assertEquals("intact", xrefs[0].getDatabase());
		assertEquals("EBI-781017", xrefs[0].getId());
	}
}
