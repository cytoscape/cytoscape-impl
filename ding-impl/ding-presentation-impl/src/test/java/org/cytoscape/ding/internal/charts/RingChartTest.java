package org.cytoscape.ding.internal.charts;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.cytoscape.ding.internal.charts.ring.RingChart.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2Test;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.internal.charts.ring.RingChart;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.junit.Before;
import org.junit.Test;

public class RingChartTest extends AbstractCustomGraphics2Test {

	private CyColumnIdentifierFactory colIdFactory = new DummyCyColumnIdentifierFactory();
	
	@Before
	public void setUp() throws Exception {
		props1 = new HashMap<>();
		props1.put(DATA_COLUMNS, 
				asList(colIdFactory.createColumnIdentifier("data1"),
					   colIdFactory.createColumnIdentifier("data2")));
		props1.put(COLOR_SCHEME, "CUSTOM");
		props1.put(COLORS, asList(Color.RED, Color.BLUE, Color.GREEN));
		props1.put(BORDER_COLOR, Color.WHITE);
		props1.put(BORDER_WIDTH, 2.5f);
		props1.put(ITEM_LABELS_COLUMN, "labels1");
		props1.put(ITEM_LABELS, asList("A", "B", "C"));
		props1.put(START_ANGLE, -45.0);
		props1.put(HOLE_SIZE, -2.5);
		props1.put(SHOW_ITEM_LABELS, true);
	}

	@Test
	public void testChartProperties() {
		final RingChart chart = new RingChart(props1, colIdFactory);
		
		// The chart properties has to return exactly the same values,
		// except column names, which are converted to CyColumIdentifier when set as String
		Map<String, Object> props2 = chart.getProperties();
		assertEquals(props1.get(DATA_COLUMNS), props2.get(DATA_COLUMNS));
		assertEquals(props1.get(COLOR_SCHEME), props2.get(COLOR_SCHEME));
		assertEquals(props1.get(COLORS), props2.get(COLORS));
		assertEquals(props1.get(BORDER_COLOR), props2.get(BORDER_COLOR));
		assertEquals(props1.get(BORDER_WIDTH), props2.get(BORDER_WIDTH));
		assertEquals("labels1", ((CyColumnIdentifier)props2.get(ITEM_LABELS_COLUMN)).getColumnName());
		assertEquals(props1.get(ITEM_LABELS), props2.get(ITEM_LABELS));
		assertEquals(props1.get(SHOW_ITEM_LABELS), props2.get(SHOW_ITEM_LABELS));
		assertEquals(props1.get(START_ANGLE), props2.get(START_ANGLE));
		assertEquals(props1.get(HOLE_SIZE), props2.get(HOLE_SIZE));
		assertNull(props2.get(VALUES));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertEquals(props2.get(DATA_COLUMNS), chart.getList(DATA_COLUMNS, CyColumnIdentifier.class));
		assertEquals(ColorScheme.CUSTOM, chart.get(COLOR_SCHEME, ColorScheme.class));
		assertEquals(props1.get(COLORS), chart.getList(COLORS, Color.class));
		assertEquals(Color.WHITE, chart.get(BORDER_COLOR, Color.class));
		assertEquals(new Float(2.5f), chart.get(BORDER_WIDTH, Float.class));
		assertEquals("labels1", chart.get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class).getColumnName());
		assertEquals(props1.get(ITEM_LABELS), chart.getList(ITEM_LABELS, String.class));
		assertEquals(Boolean.TRUE, chart.get(SHOW_ITEM_LABELS, Boolean.class));
		assertEquals(new Double(-45), chart.get(START_ANGLE, Double.class));
		assertEquals(new Double(-2.5), chart.get(HOLE_SIZE, Double.class));
		assertTrue(chart.getList(VALUES, Double.class).isEmpty()); // Must never be null!
	}
}
