package org.cytoscape.view.vizmap.internal.mappings;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingImpl;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.junit.Test;
import org.mockito.Mock;

public class DiscreteMappingTest {


	@Test
	public void testDiscreteMapping() {
		final String attrName = "sample attr 1";
		final DiscreteMapping<String, Paint> mapping = createDiscreteMapping(attrName);

		assertEquals(attrName, mapping.getMappingColumnName());
		assertEquals(String.class, mapping.getMappingColumnType());
		assertEquals(BasicVisualLexicon.NODE_FILL_COLOR, mapping.getVisualProperty());

		mapping.putMapValue("r", Color.RED);
		mapping.putMapValue("g", Color.GREEN);
		mapping.putMapValue("b", Color.BLUE);

		assertEquals(Color.RED, mapping.getMapValue("r"));
		assertEquals(Color.GREEN, mapping.getMapValue("g"));
		assertEquals(Color.BLUE, mapping.getMapValue("b"));
		assertEquals(null, mapping.getMapValue("p"));

		mapping.putMapValue("g", Color.GRAY);
		assertEquals(Color.GRAY, mapping.getMapValue("g"));
		mapping.putMapValue("g", null);
		assertNull(mapping.getMapValue("g"));
	}

	private DiscreteMapping<String, Paint> createDiscreteMapping(final String attrName) {

		final Class<String> type = String.class;

		CyEventHelper eventHelper = mock(CyEventHelper.class);
		final DiscreteMapping<String, Paint> mapping = new DiscreteMappingImpl<String, Paint>(attrName, type,
				BasicVisualLexicon.NODE_FILL_COLOR, eventHelper);

		return mapping;
	}
}
