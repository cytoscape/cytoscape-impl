package org.cytoscape.view.vizmap.mappings;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingImpl;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DiscreteMappingTest {
	
	@Mock
	private CyTable table;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDiscreteMapping() {
		final String attrName = "sample attr 1";
		final DiscreteMapping<String, Paint> mapping = createDiscreteMapping(attrName);

		assertEquals(attrName, mapping.getMappingColumnName());
		assertEquals(String.class, mapping.getMappingColumnType());
		assertEquals(MinimalVisualLexicon.NODE_FILL_COLOR, mapping.getVisualProperty());

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

		final DiscreteMapping<String, Paint> mapping = new DiscreteMappingImpl<String, Paint>(attrName, type,
				table, MinimalVisualLexicon.NODE_FILL_COLOR);
		
		return mapping;
	}
}
