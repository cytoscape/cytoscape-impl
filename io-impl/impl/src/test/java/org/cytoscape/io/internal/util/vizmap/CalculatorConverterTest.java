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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.cytoscape.io.internal.util.vizmap.model.Edge;
import org.cytoscape.io.internal.util.vizmap.model.Network;
import org.cytoscape.io.internal.util.vizmap.model.Node;
import org.cytoscape.io.internal.util.vizmap.model.VisualProperty;
import org.cytoscape.io.internal.util.vizmap.model.VisualStyle;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.junit.Before;
import org.junit.Test;

public class CalculatorConverterTest {

	private Properties props;
	private VisualStyle vs;
	
	@Before
	public void setUp() throws Exception {
		props = new Properties();
		props.setProperty("globalAppearanceCalculator.My style.defaultBackgroundColor", "255,255,255");
		
		vs = new VisualStyle();
		vs.setNetwork(new Network());
		vs.setNode(new Node());
		vs.setEdge(new Edge());
	}
	
	@Test
	public void testGetVisualPropertyId() {
		CalculatorConverter c;
		
		c = new CalculatorConverter("globalAppearanceCalculator.My style.defaultBackgroundColor", null);
		assertEquals("backgroundcolor", c.getVisualPropertyId().toLowerCase());
		c = new CalculatorConverter("nodeAppearanceCalculator.default.defaultNodeFont", null);
		assertEquals("nodefont", c.getVisualPropertyId().toLowerCase());
		c = new CalculatorConverter("nodeAppearanceCalculator.galFiltered Style.nodeUniformSizeCalculator", null);
		assertEquals("nodesize", c.getVisualPropertyId().toLowerCase());
		c = new CalculatorConverter("edgeAppearanceCalculator.My style.edgeLabelCalculator", null);
		assertEquals("edgelabel", c.getVisualPropertyId().toLowerCase());
	}
	
	@Test
	public void testGetVisualPropertyIdForDeprecatedKey() {
		CalculatorConverter c;
		
		c = new CalculatorConverter("edgeAppearanceCalculator.default.defaultEdgeLineStyle", "edgeAppearanceCalculator.default.defaultEdgeLineType");
		assertEquals("edgelinestyle", c.getVisualPropertyId().toLowerCase());
		c = new CalculatorConverter("edgeAppearanceCalculator.default.defaultEdgeLineWidth", "edgeAppearanceCalculator.default.defaultEdgeLineType");
		assertEquals("edgelinewidth", c.getVisualPropertyId().toLowerCase());
	}
	
	@Test
	public void testConvertDefaultValue() {
		testConvertDefaltValue("globalAppearanceCalculator.My style.defaultBackgroundColor", null, CyNetwork.class, "255,255,255", "255,255,255");
		testConvertDefaltValue("nodeAppearanceCalculator.My style.defaultNodeShowNestedNetwork", null, CyNode.class, "true", "true");
		testConvertDefaltValue("edgeAppearanceCalculator.My style.defaultEdgeOpacity", null, CyEdge.class, "125.0", "125.0");
		
	}
	
	@Test
	public void testConvertDefaultValueForDeprecatedKey() {
		testConvertDefaltValue("edgeAppearanceCalculator.My style.defaultEdgeLineStyle",
							   "edgeAppearanceCalculator.My style.defaultEdgeLineType",
							   CyEdge.class,
							   "LINE_1",
							   "SOLID");
		testConvertDefaltValue("edgeAppearanceCalculator.My style.defaultEdgeLineWidth",
							   "edgeAppearanceCalculator.My style.defaultEdgeLineType",
							   CyEdge.class,
							   "LINE_1",
							   "1");
		testConvertDefaltValue("edgeAppearanceCalculator.My style.defaultEdgeTargetArrowColor",
							   "edgeAppearanceCalculator.My style.defaultEdgeTargetArrow",
							   CyEdge.class,
							   "WHITE_T",
							   "255,255,255");
		testConvertDefaltValue("edgeAppearanceCalculator.My style.defaultEdgeTargetArrowShape",
							   "edgeAppearanceCalculator.My style.defaultEdgeTargetArrow",
							   CyEdge.class,
							   "WHITE_T",
							   "T");
	}

	@Test
	public void testIsDefaultProperty() {
		assertTrue(CalculatorConverter.isDefaultProperty("globalAppearanceCalculator.My style.defaultBackgroundColor"));
		assertTrue(CalculatorConverter.isDefaultProperty("globalAppearanceCalculator.My style.defaultNodeSelectionColor"));
		assertTrue(CalculatorConverter.isDefaultProperty("globalAppearanceCalculator.My style.defaultEdgeReverseSelectionColor"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeShowNestedNetwork"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeBorderColor"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeBorderOpacity"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.default.defaultNodeFont"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeHight"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeLabel"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeLineStyle"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeCustomGraphics1"));
		assertTrue(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.defaultNodeCustomGraphicsPosition8"));
		assertTrue(CalculatorConverter.isDefaultProperty("edgeAppearanceCalculator.My style.defaultEdgeToolTip"));
		assertTrue(CalculatorConverter.isDefaultProperty("edgeAppearanceCalculator.default.defaultEdgeTargetArrowColor"));
		assertTrue(CalculatorConverter.isDefaultProperty("edgeAppearanceCalculator.default.defaultEDGE_UNSELECTED_PAINT"));
		assertTrue(CalculatorConverter.isDefaultProperty("edgeAppearanceCalculator.default.defaultEDGE_STROKE_UNSELECTED_PAINT"));
		
		assertFalse(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.nodeLabelCalculator"));
		assertFalse(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.My style.nodeSizeLocked"));
		assertFalse(CalculatorConverter.isDefaultProperty("edgeAppearanceCalculator.default.defaultNodeSize"));
		assertFalse(CalculatorConverter.isDefaultProperty("nodeAppearanceCalculator.default.defaultEdgeColor"));
	}

	@Test
	public void testIsMappingFunction() {
		assertTrue(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeLabelCalculator"));
		assertTrue(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeBorderColorCalculator"));
		assertTrue(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeCustomGraphics1"));
		assertTrue(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeCustomGraphicsPosition4"));
		assertTrue(CalculatorConverter.isMappingFunction("edgeAppearanceCalculator.default.edgeColorCalculator"));
		assertTrue(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.galFiltered Style.nodeLabelColor"));
		assertTrue(CalculatorConverter.isMappingFunction("edgeAppearanceCalculator.Sample1.EDGE_UNSELECTED_PAINTCalculator"));
		assertTrue(CalculatorConverter.isMappingFunction("edgeAppearanceCalculator.Sample1.EDGE_STROKE_UNSELECTED_PAINTCalculator"));

		assertFalse(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeSizeLocked"));
		assertFalse(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.defaultNodeShowNestedNetwork"));
		assertFalse(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeCustomGraphicsSizeSync"));
		assertFalse(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.nodeLabelColorFromNodeColor"));
		assertFalse(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.defaultNodeCustomGraphics1"));
		assertFalse(CalculatorConverter.isMappingFunction("nodeAppearanceCalculator.My style.defaultNodeCustomGraphicsPosition2"));
		assertFalse(CalculatorConverter.isMappingFunction("globalAppearanceCalculator.My style.defaultBackgroundColor"));
		assertFalse(CalculatorConverter.isMappingFunction("edgeAppearanceCalculator.default.defaultEdgeColor"));
		assertFalse(CalculatorConverter.isMappingFunction("nodeBorderColorCalculator.My Style-Node Border Color-Discrete Mapper.mapping.controller"));
	}

	@Test
	public void testIsDependency() {
		assertTrue(CalculatorConverter.isDependency("nodeAppearanceCalculator.My style.nodeSizeLocked"));
		assertTrue(CalculatorConverter.isDependency("nodeAppearanceCalculator.My style.nodeCustomGraphicsSizeSync"));
		assertTrue(CalculatorConverter.isDependency("edgeAppearanceCalculator.My style.arrowColorMatchesEdge"));

		assertFalse(CalculatorConverter.isDependency("globalAppearanceCalculator.My style.defaultBackgroundColor"));
		assertFalse(CalculatorConverter.isDependency("nodeAppearanceCalculator.My style.nodeCustomGraphics1"));
		assertFalse(CalculatorConverter.isDependency("nodeAppearanceCalculator.My style.nodeCustomGraphicsPosition4"));
		assertFalse(CalculatorConverter.isDependency("nodeAppearanceCalculator.nodeCustomGraphicsSizeSync.defaultNodeCustomGraphicsPosition2"));
		assertTrue(CalculatorConverter.isDependency("nodeAppearanceCalculator.My style.nodeLabelColorFromNodeColor"));
	}
	
	@Test
	public void testParseStyleName() {
		assertEquals("My style", CalculatorConverter.parseStyleName("globalAppearanceCalculator.My style.defaultBackgroundColor"));
		assertEquals("My style 2", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.My style 2.defaultNodeBorderColor"));
		assertEquals("default", CalculatorConverter.parseStyleName("edgeAppearanceCalculator.default.defaultEdgeToolTip"));
		assertEquals("My style", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.My style.nodeLabelCalculator"));
		assertEquals("default", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.default.defaultNodeCustomGraphics1"));
		assertEquals("My style", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.My style.nodeSizeLocked"));
		assertEquals("My style", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.My style.defaultNodeShowNestedNetworks"));
		assertEquals("My style", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.My style.nodeLabelColorFromNodeColor"));
		assertEquals("My style", CalculatorConverter.parseStyleName("nodeAppearanceCalculator.My style.nodeCustomGraphicsSizeSync"));
		assertEquals("My style", CalculatorConverter.parseStyleName("edgeAppearanceCalculator.My style.arrowColorMatchesEdge"));

		assertNull(CalculatorConverter.parseStyleName("nodeBorderColorCalculator.My style-Node Border Color-Discrete Mapper.mapping.map.A03"));
		assertNull(CalculatorConverter.parseStyleName("edgeColorCalculator.default-Edge Color-Continuous Mapper.mapping.boundaryvalues=2"));
	}
	
	@Test
	public void testParseDataTargetType() throws Exception {
		assertEquals(CyNetwork.class, CalculatorConverter.parseTargetDataType("defaultBackgroundColor"));
		
		assertEquals(CyEdge.class, CalculatorConverter.parseTargetDataType("defaultEdgeFontSize"));
		assertEquals(CyEdge.class, CalculatorConverter.parseTargetDataType("defaultEdgeLineType"));
		assertEquals(CyEdge.class, CalculatorConverter.parseTargetDataType("edgeLineTypeCalculator"));
		assertEquals(CyEdge.class, CalculatorConverter.parseTargetDataType("defaultEdgeSourceArrow"));
		assertEquals(CyEdge.class, CalculatorConverter.parseTargetDataType("defaultEdgeSelectionColor"));

		assertEquals(CyNode.class, CalculatorConverter.parseTargetDataType("defaultNodeSize"));
		assertEquals(CyNode.class, CalculatorConverter.parseTargetDataType("nodeLineTypeCalculator"));
		assertEquals(CyNode.class, CalculatorConverter.parseTargetDataType("defaultNodeSelectionColor"));
	}
	
	private void testConvertDefaltValue(String key,
										String oldKey,
										Class<? extends CyIdentifiable> type,
										String value,
										String expected) {
		CalculatorConverter c = new CalculatorConverter(key, oldKey);
		c.convert(vs, value, props);
		VisualProperty vp = getVisualProperty(c.getVisualPropertyId(), vs, type);
		assertEquals(expected, vp.getDefault());
	}
	
	private VisualProperty getVisualProperty(String id, VisualStyle vs, Class<? extends CyIdentifiable> type) {
		List<VisualProperty> list;
		
		if (type == CyNode.class)
			list = vs.getNode().getVisualProperty();
		else if (type == CyEdge.class)
			list = vs.getEdge().getVisualProperty();
		else 
			list = vs.getNetwork().getVisualProperty();
		
		for (VisualProperty vp : list) {
			if (vp.getName().equalsIgnoreCase(id)) return vp;
		}
		
		return null;
	}
}
