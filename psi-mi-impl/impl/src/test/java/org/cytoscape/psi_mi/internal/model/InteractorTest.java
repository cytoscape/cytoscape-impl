package org.cytoscape.psi_mi.internal.model;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.cytoscape.psi_mi.internal.model.Interactor;
import org.cytoscape.psi_mi.internal.model.vocab.GoVocab;
import org.cytoscape.psi_mi.internal.model.vocab.InteractorVocab;


/**
 * Tests the Interactor Class.
 *
 * @author Ethan Cerami
 */
public class InteractorTest extends TestCase {
	/**
	 * Tests the Interactor Class.
	 *
	 * @throws Exception All Exceptions.
	 */
	public void testInteractor() throws Exception {
		Interactor interactor = new Interactor();
		interactor.setName("YHR119W");
		interactor.setDescription("Gene has a SET or TROMO domain at its "
		                          + "carboxyterminus like the trithorax gene family from human "
		                          + "and Drosophila with postulated function in chromatin-"
		                          + "mediated gene regulation.");

		//  Set Gene Names
		List<String> geneNameList = new ArrayList<String>();
		geneNameList.add("SET1");
		geneNameList.add("YTX1");
		interactor.addAttribute(InteractorVocab.GENE_NAME, geneNameList);

		//  Set GO Process Terms
		List<Map<String, String>> goProcessTerms = new ArrayList<Map<String, String>>();
		Map<String, String> goTerm = new HashMap<String, String>();
		goTerm.put(GoVocab.GO_ID, "GO:0006348");
		goTerm.put(GoVocab.GO_NAME, "chromatin silencing at telomere");
		goProcessTerms.add(goTerm);
		goTerm = new HashMap<String, String>();
		goTerm.put(GoVocab.GO_ID, "GO:0016571");
		goTerm.put(GoVocab.GO_NAME, "histone methylation");
		goProcessTerms.add(goTerm);
		interactor.addAttribute(GoVocab.GO_CATEGORY_PROCESS, goProcessTerms);
		validateAttributes(interactor);
	}

	/**
	 * Validates the First Go Term.
	 */
	private void validateAttributes(Interactor interactor) {
		@SuppressWarnings("unchecked")
		List<Map<String, String>> list = (List<Map<String, String>>) interactor.getAttribute(GoVocab.GO_CATEGORY_PROCESS);
		Map<String, String> map = list.get(0);
		String id = map.get(GoVocab.GO_ID);
		String name = map.get(GoVocab.GO_NAME);
		assertEquals("GO:0006348", id);
		assertEquals("chromatin silencing at telomere", name);
	}
}
