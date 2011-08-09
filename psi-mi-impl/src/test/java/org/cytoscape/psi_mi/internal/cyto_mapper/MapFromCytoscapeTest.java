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
package org.cytoscape.psi_mi.internal.cyto_mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapFromCytoscape;
import org.cytoscape.psi_mi.internal.cyto_mapper.MapToCytoscape;
import org.cytoscape.psi_mi.internal.data_mapper.MapPsiOneToInteractions;
import org.cytoscape.psi_mi.internal.model.ExternalReference;
import org.cytoscape.psi_mi.internal.model.Interaction;
import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.util.ContentReader;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the MapFromCytoscape Class.
 *
 * @author Ethan Cerami.
 */
public class MapFromCytoscapeTest {

	private NetworkTestSupport networkTestSupport;

	@Before
	public void setUp() {
		networkTestSupport = new NetworkTestSupport();
	}
	
	/**
	 * Tests the Mapper.
	 *
	 * @throws Exception All Exceptions.
	 */
	@Test
	public void testMapper1() throws Exception {
		//  First, get some interactions from sample data file.
		ArrayList<Interaction> interactions = new ArrayList<Interaction>();
		ContentReader reader = new ContentReader();
		String xml = reader.retrieveContent("src/test/resources/testData/psi_sample1.xml");

		//  Map from PSI 1.5 --> DataService Interaction Objects.
		MapPsiOneToInteractions mapper1 = new MapPsiOneToInteractions(xml, interactions);
		mapper1.doMapping();

		//  Now Map from Data Service Objects --> Cytocape Network Objects.
		CyNetwork cyNetwork = networkTestSupport.getNetwork();
		MapToCytoscape mapper2 = new MapToCytoscape(cyNetwork, interactions, MapToCytoscape.MATRIX_VIEW);
		mapper2.doMapping();

		//  And, now map back from Cytoscape --> DataService Interaction Objects
		MapFromCytoscape mapper3 = new MapFromCytoscape(cyNetwork);
		mapper3.doMapping();

		//  Verify that we have 6 interactions still
		interactions = mapper3.getInteractions();
		assertEquals(6, interactions.size());

		//  Verify that Sample Interaction with XRefs is mapped over.
		Interaction interaction = interactions.get(3);
		assertTrue(interaction.toString().startsWith("Interaction:  [YCR038C] [YDR532C]"));

		ExternalReference[] refs = interaction.getExternalRefs();
		assertEquals(2, refs.length);
		assertEquals("DIP", refs[0].getDatabase());
		assertEquals("61E", refs[0].getId());
		assertEquals("CPATH", refs[1].getDatabase());
		assertEquals("12345", refs[1].getId());

		//  Validate sample interactor with XRefs
		Interactor interactor = interaction.getInteractors().get(0);
		assertEquals("YCR038C", interactor.getName());
		assertEquals(15, interactor.getExternalRefs().length);
	}
}
