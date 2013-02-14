package org.cytoscape.io.internal.util.vizmap;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class CalculatorConverterFactoryTest {

	private CalculatorConverterFactory ccf;

	@Before
	public void setUp() throws Exception {
		ccf = new CalculatorConverterFactory();
	}

	@Test
	public void testConvertersNotNullForNullKey() throws Exception {
		assertNotNull(ccf.getConverters(null));
	}

	public void testConvertersNotNullForEmptyKey() throws Exception {
		assertNotNull(ccf.getConverters(""));
	}

	@Test
	public void testConvertersEmptyForInvalidKey() throws Exception {
		// missing style name
		assertTrue(ccf.getConverters("edgeAppearanceCalculator.defaultEdgeColor").isEmpty());
		// it is just a mapping controller--should not create converters
		assertTrue(ccf.getConverters("edgeLabelCalculator.Solid-Edge Label-Passthrough Mapper.mapping.controller")
				.isEmpty());
		// the first token should be "nodeAppearanceCalculator", because it is a node property
		assertTrue(ccf.getConverters("edgeAppearanceCalculator.My Style.defaultNodeLabel").isEmpty());
		// the first token should be "edgeAppearanceCalculator", because it is an edge property
		assertTrue(ccf.getConverters("nodeAppearanceCalculator.My Style.defaultEdgeLabel").isEmpty());
		// "calculator" is invalid
		assertTrue(ccf.getConverters("calculator.InteractionDB.defaultNodeLabel").isEmpty());
		// completely invalid
		assertTrue(ccf.getConverters("asdfg").isEmpty());
	}

	@Test
	public void testGetOneConverterForRegularKeys() throws Exception {
		assertEquals(1, ccf.getConverters("globalAppearanceCalculator.Universe.defaultBackgroundColor").size());
		assertEquals(1, ccf.getConverters("nodeAppearanceCalculator.Universe.defaultNodeLabel").size());
		assertEquals(1, ccf.getConverters("edgeAppearanceCalculator.My Style.edgeTargetArrowShapeCalculator").size());
	}

	@Test
	public void testUpdateLegacyPropsKey() throws Exception {
		assertLegacyKeyUpdated("nodeAppearanceCalculator.NodeLineTypeCalculator.nodeLineTypeCalculator", 
				new String[]{"nodeAppearanceCalculator.NodeLineTypeCalculator.nodeLineStyleCalculator", 
			                 "nodeAppearanceCalculator.NodeLineTypeCalculator.nodeLineWidthCalculator"});
		
		// ----------
		assertLegacyKeyUpdated("edgeAppearanceCalculator.default.defaultEdgeLineType", 
				new String[]{"edgeAppearanceCalculator.default.defaultEdgeLineStyle", 
			                 "edgeAppearanceCalculator.default.defaultEdgeLineWidth"});
		
		assertLegacyKeyUpdated("edgeAppearanceCalculator.LineTypeCalculator.edgeLineTypeCalculator", 
				new String[]{"edgeAppearanceCalculator.LineTypeCalculator.edgeLineStyleCalculator", 
			                 "edgeAppearanceCalculator.LineTypeCalculator.edgeLineWidthCalculator"});
		
		// ----------
		assertLegacyKeyUpdated("edgeAppearanceCalculator.Sample1.defaultEdgeSourceArrow", 
				new String[]{"edgeAppearanceCalculator.Sample1.defaultEdgeSourceArrowShape", 
			                 "edgeAppearanceCalculator.Sample1.defaultEdgeSourceArrowColor"});
		
		assertLegacyKeyUpdated("edgeAppearanceCalculator.My Arrow Calculator.defaultEdgeTargetArrow", 
				new String[]{"edgeAppearanceCalculator.My Arrow Calculator.defaultEdgeTargetArrowShape", 
			                 "edgeAppearanceCalculator.My Arrow Calculator.defaultEdgeTargetArrowColor"});
		
		assertLegacyKeyUpdated("edgeAppearanceCalculator.MyArrowCalculator.edgeSourceArrowCalculator", 
				new String[]{"edgeAppearanceCalculator.MyArrowCalculator.edgeSourceArrowShapeCalculator", 
							 "edgeAppearanceCalculator.MyArrowCalculator.edgeSourceArrowColorCalculator"});
		
		assertLegacyKeyUpdated("edgeAppearanceCalculator.default.edgeTargetArrowCalculator", 
				new String[]{"edgeAppearanceCalculator.default.edgeTargetArrowShapeCalculator", 
						     "edgeAppearanceCalculator.default.edgeTargetArrowColorCalculator"});
		
		// ----------
		assertLegacyKeyUpdated("edgeAppearanceCalculator.MyDefaultEdgeColor.defaultEdgeColor", 
				new String[]{"edgeAppearanceCalculator.MyDefaultEdgeColor.defaultEDGE_UNSELECTED_PAINT", 
			                 "edgeAppearanceCalculator.MyDefaultEdgeColor.defaultEDGE_STROKE_UNSELECTED_PAINT"});
		assertLegacyKeyUpdated("edgeAppearanceCalculator.MyEdgeColorCalculator.edgeColorCalculator",
				new String[]{"edgeAppearanceCalculator.MyEdgeColorCalculator.EDGE_UNSELECTED_PAINTCalculator", 
				             "edgeAppearanceCalculator.MyEdgeColorCalculator.EDGE_STROKE_UNSELECTED_PAINTCalculator"});
	}

	// Private methods -----------
	
	private void assertLegacyKeyUpdated(final String key, final String[] expectedKeys) {
		final Set<String> updatedKeys = CalculatorConverterFactory.updateLegacyPropsKey(key);
		assertEquals(expectedKeys.length, updatedKeys.size());

		for (final String k : expectedKeys)
			assertTrue("Converted key \"" + k + "\" not found in " + updatedKeys, updatedKeys.contains(k));
	}
}
