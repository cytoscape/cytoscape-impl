package org.cytoscape.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.AbstractVisualLexiconTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class DVisualLexiconTest extends AbstractVisualLexiconTest {

	private VisualLexicon dLexicon;

	@Before
	public void setUp() throws Exception {
		dLexicon = new DVisualLexicon();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDVisualLexicon() throws Exception {
		assertNotNull(dLexicon);

		VisualProperty<NullDataType> root = dLexicon.getRootVisualProperty();
		assertNotNull(root);
		assertEquals(DVisualLexicon.DING_ROOT, root);

		assertEquals(1, dLexicon.getVisualLexiconNode(root).getChildren().size());
		
		//FIXME
		//assertEquals(87, dLexicon.getAllVisualProperties().size());
	}

	@Test
	public void testDVisualLexiconTree() throws Exception {
		this.testTree(dLexicon);
	}
}
