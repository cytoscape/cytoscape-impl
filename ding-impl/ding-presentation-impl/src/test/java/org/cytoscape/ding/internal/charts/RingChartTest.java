package org.cytoscape.ding.internal.charts;

import static java.util.Arrays.asList;
import static org.cytoscape.cg.internal.charts.AbstractChart.BORDER_COLOR;
import static org.cytoscape.cg.internal.charts.AbstractChart.BORDER_WIDTH;
import static org.cytoscape.cg.internal.charts.AbstractChart.DATA_COLUMNS;
import static org.cytoscape.cg.internal.charts.AbstractChart.ITEM_LABELS;
import static org.cytoscape.cg.internal.charts.AbstractChart.ITEM_LABELS_COLUMN;
import static org.cytoscape.cg.internal.charts.AbstractChart.SHOW_ITEM_LABELS;
import static org.cytoscape.cg.internal.charts.AbstractChart.VALUES;
import static org.cytoscape.cg.internal.charts.ring.RingChart.HOLE_SIZE;
import static org.cytoscape.cg.internal.charts.ring.RingChart.START_ANGLE;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLORS;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLOR_SCHEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Map;

import org.cytoscape.cg.internal.charts.ring.RingChart;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2Test;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
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

public class RingChartTest extends AbstractCustomGraphics2Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
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
		final RingChart chart = new RingChart(props1, serviceRegistrar);
		
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
