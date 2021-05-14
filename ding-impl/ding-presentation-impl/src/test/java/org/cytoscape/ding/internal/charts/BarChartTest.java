package org.cytoscape.ding.internal.charts;

import static java.util.Arrays.asList;
import static org.cytoscape.cg.internal.charts.AbstractChart.AUTO_RANGE;
import static org.cytoscape.cg.internal.charts.AbstractChart.AXIS_COLOR;
import static org.cytoscape.cg.internal.charts.AbstractChart.AXIS_WIDTH;
import static org.cytoscape.cg.internal.charts.AbstractChart.BORDER_COLOR;
import static org.cytoscape.cg.internal.charts.AbstractChart.BORDER_WIDTH;
import static org.cytoscape.cg.internal.charts.AbstractChart.DATA_COLUMNS;
import static org.cytoscape.cg.internal.charts.AbstractChart.DOMAIN_LABELS_COLUMN;
import static org.cytoscape.cg.internal.charts.AbstractChart.DOMAIN_LABEL_POSITION;
import static org.cytoscape.cg.internal.charts.AbstractChart.GLOBAL_RANGE;
import static org.cytoscape.cg.internal.charts.AbstractChart.ITEM_LABELS;
import static org.cytoscape.cg.internal.charts.AbstractChart.ITEM_LABELS_COLUMN;
import static org.cytoscape.cg.internal.charts.AbstractChart.RANGE;
import static org.cytoscape.cg.internal.charts.AbstractChart.RANGE_LABELS_COLUMN;
import static org.cytoscape.cg.internal.charts.AbstractChart.SHOW_DOMAIN_AXIS;
import static org.cytoscape.cg.internal.charts.AbstractChart.SHOW_ITEM_LABELS;
import static org.cytoscape.cg.internal.charts.AbstractChart.SHOW_RANGE_AXIS;
import static org.cytoscape.cg.internal.charts.AbstractChart.SHOW_RANGE_ZERO_BASELINE;
import static org.cytoscape.cg.internal.charts.AbstractChart.VALUES;
import static org.cytoscape.cg.internal.charts.bar.BarChart.SEPARATION;
import static org.cytoscape.cg.internal.charts.bar.BarChart.TYPE;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLORS;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.COLOR_SCHEME;
import static org.cytoscape.cg.model.AbstractCustomGraphics2.ORIENTATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Map;

import org.cytoscape.cg.internal.charts.LabelPosition;
import org.cytoscape.cg.internal.charts.bar.BarChart;
import org.cytoscape.cg.internal.charts.bar.BarChart.BarChartType;
import org.cytoscape.cg.model.ColorScheme;
import org.cytoscape.cg.model.Orientation;
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

public class BarChartTest extends AbstractCustomGraphics2Test {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		props1.put(DATA_COLUMNS, 
				asList(colIdFactory.createColumnIdentifier("data1"),
					   colIdFactory.createColumnIdentifier("data2")));
		props1.put(COLOR_SCHEME, "RAINBOW");
		props1.put(TYPE, "HEAT_STRIPS");
		props1.put(SEPARATION, 0.2);
		props1.put(AUTO_RANGE, false);
		props1.put(GLOBAL_RANGE, true);
		props1.put(RANGE, asList(-10.5, 15.0));
		props1.put(COLORS, asList(Color.RED, Color.BLUE, Color.GREEN));
		props1.put(ORIENTATION, "VERTICAL");
		props1.put(BORDER_COLOR, Color.WHITE);
		props1.put(BORDER_WIDTH, 2.5f);
		props1.put(ITEM_LABELS_COLUMN, "labels1");
		props1.put(DOMAIN_LABELS_COLUMN, "labels2");
		props1.put(RANGE_LABELS_COLUMN, colIdFactory.createColumnIdentifier("labels3"));
		props1.put(DOMAIN_LABEL_POSITION, "UP_45");
		props1.put(ITEM_LABELS, asList("A", "B", "C"));
		props1.put(SHOW_ITEM_LABELS, true);
		props1.put(SHOW_DOMAIN_AXIS, true);
		props1.put(SHOW_RANGE_AXIS, false);
		props1.put(SHOW_RANGE_ZERO_BASELINE, true);
		props1.put(AXIS_WIDTH, 1.5f);
		props1.put(AXIS_COLOR, Color.YELLOW);
	}

	@Test
	public void testChartProperties() {
		final BarChart chart = new BarChart(props1, serviceRegistrar);
		
		// The chart properties has to return exactly the same values,
		// except column names, which are converted to CyColumIdentifier when set as String
		Map<String, Object> props2 = chart.getProperties();
		assertEquals(props1.get(DATA_COLUMNS), props2.get(DATA_COLUMNS));
		assertEquals(props1.get(COLOR_SCHEME), props2.get(COLOR_SCHEME));
		assertEquals(props1.get(TYPE), props2.get(TYPE));
		assertEquals(props1.get(SEPARATION), props2.get(SEPARATION));
		assertEquals(props1.get(AUTO_RANGE), props2.get(AUTO_RANGE));
		assertEquals(props1.get(GLOBAL_RANGE), props2.get(GLOBAL_RANGE));
		assertEquals(props1.get(RANGE), props2.get(RANGE));
		assertEquals(props1.get(COLORS), props2.get(COLORS));
		assertEquals(props1.get(ORIENTATION), props2.get(ORIENTATION));
		assertEquals(props1.get(BORDER_COLOR), props2.get(BORDER_COLOR));
		assertEquals(props1.get(BORDER_WIDTH), props2.get(BORDER_WIDTH));
		assertEquals("labels1", ((CyColumnIdentifier)props2.get(ITEM_LABELS_COLUMN)).getColumnName());
		assertEquals("labels2", ((CyColumnIdentifier)props2.get(DOMAIN_LABELS_COLUMN)).getColumnName());
		assertEquals("labels3", ((CyColumnIdentifier)props2.get(RANGE_LABELS_COLUMN)).getColumnName());
		assertEquals(props1.get(DOMAIN_LABEL_POSITION), props2.get(DOMAIN_LABEL_POSITION));
		assertEquals(props1.get(ITEM_LABELS), props2.get(ITEM_LABELS));
		assertEquals(props1.get(SHOW_ITEM_LABELS), props2.get(SHOW_ITEM_LABELS));
		assertEquals(props1.get(SHOW_DOMAIN_AXIS), props2.get(SHOW_DOMAIN_AXIS));
		assertEquals(props1.get(SHOW_RANGE_AXIS), props2.get(SHOW_RANGE_AXIS));
		assertEquals(props1.get(SHOW_RANGE_ZERO_BASELINE), props2.get(SHOW_RANGE_ZERO_BASELINE));
		assertEquals(props1.get(AXIS_WIDTH), props2.get(AXIS_WIDTH));
		assertEquals(props1.get(AXIS_COLOR), props2.get(AXIS_COLOR));
		assertNull(props2.get(VALUES));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertEquals(props2.get(DATA_COLUMNS), chart.getList(DATA_COLUMNS, CyColumnIdentifier.class));
		assertEquals(ColorScheme.RAINBOW, chart.get(COLOR_SCHEME, ColorScheme.class));
		assertEquals(BarChartType.HEAT_STRIPS, chart.get(TYPE, BarChartType.class));
		assertEquals(new Double(0.2), chart.get(SEPARATION, Double.class));
		assertEquals(Boolean.FALSE, chart.get(AUTO_RANGE, Boolean.class));
		assertEquals(Boolean.TRUE, chart.get(GLOBAL_RANGE, Boolean.class));
		assertEquals(props1.get(RANGE), chart.getList(RANGE, Double.class));
		assertEquals(props1.get(COLORS), chart.getList(COLORS, Color.class));
		assertEquals(Orientation.VERTICAL, chart.get(ORIENTATION, Orientation.class));
		assertEquals(Color.WHITE, chart.get(BORDER_COLOR, Color.class));
		assertEquals(new Float(2.5f), chart.get(BORDER_WIDTH, Float.class));
		assertEquals("labels1", chart.get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class).getColumnName());
		assertEquals("labels2", chart.get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class).getColumnName());
		assertEquals("labels3", chart.get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class).getColumnName());
		assertEquals(LabelPosition.UP_45, chart.get(DOMAIN_LABEL_POSITION, LabelPosition.class));
		assertEquals(props1.get(ITEM_LABELS), chart.getList(ITEM_LABELS, String.class));
		assertEquals(Boolean.TRUE, chart.get(SHOW_ITEM_LABELS, Boolean.class));
		assertEquals(Boolean.TRUE, chart.get(SHOW_DOMAIN_AXIS, Boolean.class));
		assertEquals(Boolean.FALSE, chart.get(SHOW_RANGE_AXIS, Boolean.class));
		assertEquals(Boolean.TRUE, chart.get(SHOW_RANGE_ZERO_BASELINE, Boolean.class));
		assertEquals(new Float(1.5f), chart.get(AXIS_WIDTH, Float.class));
		assertEquals(Color.YELLOW, chart.get(AXIS_COLOR, Color.class));
		assertTrue(chart.getList(VALUES, Double.class).isEmpty()); // Must never be null!
	}
}
