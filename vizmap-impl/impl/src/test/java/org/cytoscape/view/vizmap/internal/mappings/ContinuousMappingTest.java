package org.cytoscape.view.vizmap.internal.mappings;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangeRecord;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
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

public class ContinuousMappingTest {

	private final static String ATTR_NAME = "Sample Attr 1";
	
	private final Double value1 = 0.1;
	private final Double value2 = 0.99;
	private BoundaryRangeValues<Double> brv1 = new BoundaryRangeValues<>(20.0, 20.0, 20.0);
	private BoundaryRangeValues<Double> brv2 = new BoundaryRangeValues<>(100.0, 100.0, 100.0);
	private ContinuousMapping<Double, Double> mapping;
	
	private CyEventHelper eventHelper;
	
	@Before
	public void setUp() {
		eventHelper = mock(CyEventHelper.class);
		mapping = createNodeSizeMapping(ATTR_NAME);
		reset(eventHelper);
	}
	
	@Test
	public void testCreateMapping() {
		assertEquals(ATTR_NAME, mapping.getMappingColumnName());
		assertEquals(Double.class, mapping.getMappingColumnType());
		assertEquals(BasicVisualLexicon.NODE_SIZE, mapping.getVisualProperty());
		assertEquals(2, mapping.getPointCount());
		
		assertEquals(value1, mapping.getPoint(0).getValue());
		assertEquals(brv1, mapping.getPoint(0).getRange());
		assertEquals(value2, mapping.getPoint(1).getValue());
		assertEquals(brv2, mapping.getPoint(1).getRange());
		
		List<ContinuousMappingPoint<Double, Double>> points = mapping.getAllPoints();
		assertEquals(2, points.size());
		assertEquals(value1, points.get(0).getValue());
		assertEquals(value2, points.get(1).getValue());
		assertEquals(brv1, points.get(0).getRange());
		assertEquals(brv2, points.get(1).getRange());
	}
	
	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testColumnTypeMustBeNumber() {
		// This throws exception
		new ContinuousMappingImpl(ATTR_NAME, String.class, BasicVisualLexicon.NODE_SIZE, eventHelper);
	}
	
	@Test
	public void testAddPoint() {
		BoundaryRangeValues<Double> brv3 = new BoundaryRangeValues<>(80.0, 80.0, 80.0);
		mapping.addPoint(0.6, brv3);
		
		assertEquals(3, mapping.getPointCount());
		
		ContinuousMappingPoint<Double, Double> pt = mapping.getPoint(2);
		assertEquals(new Double(0.6), pt.getValue());
		assertEquals(brv3, pt.getRange());
		
		List<ContinuousMappingPoint<Double, Double>> points = mapping.getAllPoints();
		assertEquals(3, points.size());
		assertEquals(new Double(0.6), points.get(2).getValue());
		assertEquals(brv3, points.get(2).getRange());
		
		verify(eventHelper, times(1))
			.addEventPayload(
				eq(mapping),
				any(VisualMappingFunctionChangeRecord.class),
				eq(VisualMappingFunctionChangedEvent.class));
	}
	
	@Test
	public void testAddSamePoint() {
		// The same point can be added again (it's a List, not a Set)
		mapping.addPoint(value1, brv1);
		
		assertEquals(3, mapping.getPointCount());
		assertEquals(value1, mapping.getPoint(0).getValue());
		assertEquals(brv1, mapping.getPoint(0).getRange());
		assertEquals(value2, mapping.getPoint(1).getValue());
		assertEquals(brv2, mapping.getPoint(1).getRange());
		assertEquals(value1, mapping.getPoint(2).getValue());
		assertEquals(brv1, mapping.getPoint(2).getRange());
		
		verify(eventHelper, times(1))
			.addEventPayload(
					eq(mapping),
					any(VisualMappingFunctionChangeRecord.class),
					eq(VisualMappingFunctionChangedEvent.class));
	}
	
	@Test
	public void testRemovePoint() {
		mapping.removePoint(1);
		assertEquals(1, mapping.getPointCount());
		
		verify(eventHelper, times(1))
			.addEventPayload(
				eq(mapping),
				any(VisualMappingFunctionChangeRecord.class),
				eq(VisualMappingFunctionChangedEvent.class));
	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testRemoveInvalidPoint() {
		mapping.removePoint(2);
	}
	
	private ContinuousMapping<Double, Double> createNodeSizeMapping(final String attrName) {
		ContinuousMapping<Double, Double> mapping = new ContinuousMappingImpl<>(attrName, Double.class,
				BasicVisualLexicon.NODE_SIZE, eventHelper);
		mapping.addPoint(value1, brv1);
		mapping.addPoint(value2, brv2);

		return mapping;
	}
}
