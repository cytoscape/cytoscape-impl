package org.cytoscape.view.vizmap.internal.mappings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangeRecord;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.junit.Before;
import org.junit.Test;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class DiscreteMappingTest {

	CyEventHelper eventHelper;
	
	@Before
	public void setUp() {
		eventHelper = mock(CyEventHelper.class);
	}
	
	@Test
	public void testCreateMapping() {
		final String attrName = "Sample Attr 1";
		final DiscreteMapping<String, Paint> mapping = createPaintDiscreteMapping(attrName);

		assertEquals(attrName, mapping.getMappingColumnName());
		assertEquals(String.class, mapping.getMappingColumnType());
		assertEquals(BasicVisualLexicon.NODE_FILL_COLOR, mapping.getVisualProperty());
	}
	
	@Test
	public void testPutMapValue() {
		final DiscreteMapping<String, Paint> mapping = createPaintDiscreteMapping("ATTR_1");
		mapping.putMapValue("r", Color.RED);
		mapping.putMapValue("g", Color.GREEN);
		mapping.putMapValue("b", Color.BLUE);
		mapping.putMapValue("a", null);

		assertEquals(Color.RED, mapping.getMapValue("r"));
		assertEquals(Color.GREEN, mapping.getMapValue("g"));
		assertEquals(Color.BLUE, mapping.getMapValue("b"));
		assertNull(mapping.getMapValue("a"));
		assertNull(mapping.getMapValue("p"));
		
		Map<String, Paint> all = mapping.getAll();
		assertEquals(4, all.size());
		assertEquals(Color.RED, all.get("r"));
		assertEquals(Color.GREEN, all.get("g"));
		assertEquals(Color.BLUE, all.get("b"));
		assertTrue(all.containsKey("a"));

		mapping.putMapValue("g", Color.GRAY);
		assertEquals(Color.GRAY, mapping.getMapValue("g"));
		
		mapping.putMapValue("g", null);
		mapping.putMapValue("n", null);
		assertEquals(5, mapping.getAll().size());
		assertNull(mapping.getMapValue("g"));
		assertNull(mapping.getMapValue("n"));
	}
	
	@Test
	public void testPutAll() {
		final DiscreteMapping<String, Paint> mapping = createPaintDiscreteMapping("ATTR_1");
		mapping.putMapValue("r", Color.RED);
		mapping.putMapValue("g", Color.GREEN);
		
		assertEquals(2, mapping.getAll().size());
		
		Map<String, Paint> m = new HashMap<>();
		m.put("g", Color.GRAY);
		m.put("b", Color.BLUE);
		m.put("a", null);
		mapping.putAll(m);

		assertEquals(4, mapping.getAll().size());
		assertEquals(Color.RED, mapping.getMapValue("r"));
		assertEquals(Color.GRAY, mapping.getMapValue("g"));
		assertEquals(Color.BLUE, mapping.getMapValue("b"));
		assertTrue(mapping.getAll().containsKey("a"));
		assertNull(mapping.getMapValue("a"));
	}
	
	@Test
	public void testPutAllWithNullMap() {
		final DiscreteMapping<String, Paint> mapping = createPaintDiscreteMapping("ATTR_1");
		mapping.putMapValue("r", Color.RED);
		mapping.putAll(null);
		
		assertEquals(1, mapping.getAll().size());
		assertEquals(Color.RED, mapping.getMapValue("r"));
	}
	
	@Test
	public void testPutAllWithEqualValues() {
		final Color r = new Color(255, 10, 10);
		final Color g = new Color(10, 255, 10);
		final Color b = new Color(10, 10, 255);
		
		final DiscreteMapping<String, Paint> mapping = createPaintDiscreteMapping("ATTR_1");
		
		// Should fire 2 VisualMappingFunctionChangedEvents
		mapping.putMapValue("r", new Color(255, 10, 10));
		mapping.putMapValue("g", new Color(10, 255, 10));
		mapping.putMapValue("b", new Color(10, 10, 255));
		
		assertEquals(3, mapping.getAll().size());
		verify(eventHelper, times(3))
			.addEventPayload(
				eq(mapping),
				any(VisualMappingFunctionChangeRecord.class),
				eq(VisualMappingFunctionChangedEvent.class));

		// Should *not* fire VisualMappingFunctionChangedEvents
		Map<String, Paint> m = new HashMap<>();
		m.put("r", r);
		m.put("g", g);
		mapping.putAll(m);
		
		assertEquals(3, mapping.getAll().size());
		verify(eventHelper, times(3))
			.addEventPayload(
				eq(mapping),
				any(VisualMappingFunctionChangeRecord.class),
				eq(VisualMappingFunctionChangedEvent.class));
		
		// Should *not* fire VisualMappingFunctionChangedEvents
		mapping.putMapValue("b", b);
		assertEquals(3, mapping.getAll().size());
		verify(eventHelper, times(3))
			.addEventPayload(
				eq(mapping),
				any(VisualMappingFunctionChangeRecord.class),
				eq(VisualMappingFunctionChangedEvent.class));
	}
	
	private DiscreteMapping<String, Paint> createPaintDiscreteMapping(final String attrName) {
		DiscreteMapping<String, Paint> mapping = new DiscreteMappingImpl<>(attrName, String.class,
				BasicVisualLexicon.NODE_FILL_COLOR, eventHelper);

		return mapping;
	}
}
