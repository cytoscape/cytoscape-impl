package org.cytoscape.view.vizmap.mappings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.awt.Paint;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingImpl;
import org.junit.Test;

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

		final DiscreteMapping<String, Paint> mapping = new DiscreteMappingImpl<String, Paint>(attrName, type,
				BasicVisualLexicon.NODE_FILL_COLOR);

		return mapping;
	}
}
