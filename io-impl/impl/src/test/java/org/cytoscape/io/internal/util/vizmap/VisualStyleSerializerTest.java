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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.CyEdge.INTERACTION;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VisualStyleSerializerTest {

	private static final String DEFAULT_STYLE_NAME = "default";
	private static final String NODE_SIZE_LOCKED_DEPENDENCY = "nodeSizeLocked";
	
	private CyEventHelper evtHelper;
	private VisualStyleSerializer serializer;
	private NullVisualProperty twoDRoot;
	private BasicVisualLexicon lexicon;

	@Before
	public void setUp() throws Exception {
		evtHelper = mock(CyEventHelper.class);
		twoDRoot = new NullVisualProperty("2D_ROOT", "2D Root Visual Property");
		lexicon = new BasicVisualLexicon(twoDRoot);
		VisualStyle defaultStyle = new DummyVisualStyle(DEFAULT_STYLE_NAME);
		
		final VisualStyleFactory visualStyleFactory = mockVisualStyleFactory(defaultStyle);
		final VisualMappingFunctionFactory discreteMappingFactory = mockMappingFunctionFactory(DiscreteMapping.class);
		final VisualMappingFunctionFactory continuousMappingFactory = mockMappingFunctionFactory(ContinuousMapping.class);
		final VisualMappingFunctionFactory passthroughMappingFactory = mockMappingFunctionFactory(PassthroughMapping.class);

		final RenderingEngineManager renderingEngineManager = mock(RenderingEngineManager.class);
		when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(lexicon);

		final CalculatorConverterFactory calcFactory = new CalculatorConverterFactory();

		serializer = new VisualStyleSerializer(calcFactory, visualStyleFactory,
				renderingEngineManager, discreteMappingFactory, continuousMappingFactory, passthroughMappingFactory);
	}

	// TESTS ===========================================================================================================
	
	@Test
	public void testVisualStyleCollectionNotNullForEmptyProps() throws Exception {
		assertNotNull(serializer.createVisualStyles(new Properties()));
	}
	
	@Test
	public void testVisualStyleCollectionNotNullForEmptyVizmap() throws Exception {
		assertNotNull(serializer.createVisualStyles(new Vizmap()));
	}

	@Test
	public void testVizmapNotNullForNullVS() throws Exception {
		assertNotNull(serializer.createVizmap(null));
	}

	@Test
	public void testVizmapNotNullForEmptyVS() throws Exception {
		assertNotNull(serializer.createVizmap(new ArrayList<VisualStyle>()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testCy240Vizmap() throws Exception {
		Properties props = loadVizmapProps("v240_vizmap.props");
		Set<VisualStyle> styles = serializer.createVisualStyles(props);
		assertEquals(4,  styles.size());
		assertVisualStylesNotNull(styles, new String[] { "default", "Sample1", "Sample2", "SimpleBioMoleculeEditor" });
		
		// Test visual styles (defaults, mappings and dependencies)
		// -----
		VisualStyle def = getVisualStyleByTitle(styles, DEFAULT_STYLE_NAME);
		
		assertEquals(new Color(204,204,254), def.getDefaultValue(NETWORK_BACKGROUND_PAINT));
		assertEquals(new Color(0,0,255), def.getDefaultValue(EDGE_UNSELECTED_PAINT));
		assertEquals(new Color(0,0,255), def.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(new Color(254,0,0), def.getDefaultValue(EDGE_STROKE_SELECTED_PAINT));
		assertEquals(new Color(255,153,153), def.getDefaultValue(NODE_FILL_COLOR));
		assertEquals(new Color(254,254,0), def.getDefaultValue(NODE_SELECTED_PAINT));
		
		PassthroughMapping<String, String> nLabelMp = (PassthroughMapping<String, String>) def.getVisualMappingFunction(NODE_LABEL);
		assertEquals(NAME, nLabelMp.getMappingColumnName());
		assertEquals(String.class, nLabelMp.getMappingColumnType());
		
		ContinuousMapping<Double, Paint> nColorMp = (ContinuousMapping<Double, Paint>) def.getVisualMappingFunction(NODE_FILL_COLOR);
		assertEquals("gal1RGexp", nColorMp.getMappingColumnName());
		assertEquals(Number.class, nColorMp.getMappingColumnType());
		assertEquals(3, nColorMp.getPointCount());
		assertEquals(-2.5, nColorMp.getPoint(0).getValue(), 0.0001);
		assertEquals(new Color(255,0,0), nColorMp.getPoint(0).getRange().equalValue);
		assertEquals(new Color(255,0,0), nColorMp.getPoint(0).getRange().greaterValue);
		assertEquals(new Color(0,0,255), nColorMp.getPoint(0).getRange().lesserValue);
		assertEquals(0.0, nColorMp.getPoint(1).getValue(), 0.0);
		assertEquals(new Color(255,255,255), nColorMp.getPoint(1).getRange().equalValue);
		assertEquals(new Color(255,255,255), nColorMp.getPoint(1).getRange().greaterValue);
		assertEquals(new Color(255,255,255), nColorMp.getPoint(1).getRange().lesserValue);
		assertEquals(2.1, nColorMp.getPoint(2).getValue(), 0.0001);
		assertEquals(new Color(0,255,102), nColorMp.getPoint(2).getRange().equalValue);
		assertEquals(new Color(0,0,0), nColorMp.getPoint(2).getRange().greaterValue);
		assertEquals(new Color(0,255,102), nColorMp.getPoint(2).getRange().lesserValue);
		
		ContinuousMapping<Double, NodeShape> nShapeMp = (ContinuousMapping<Double, NodeShape>) def.getVisualMappingFunction(NODE_SHAPE);
		assertEquals("gal1RGexp", nShapeMp.getMappingColumnName());
		assertEquals(Number.class, nShapeMp.getMappingColumnType());
		assertEquals(3, nShapeMp.getPointCount());
		assertEquals(-1.0, nShapeMp.getPoint(0).getValue(), 0.0);
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nShapeMp.getPoint(0).getRange().equalValue);
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nShapeMp.getPoint(0).getRange().greaterValue);
		assertEquals(NodeShapeVisualProperty.PARALLELOGRAM, nShapeMp.getPoint(0).getRange().lesserValue);
		assertEquals(0.0, nShapeMp.getPoint(1).getValue(), 0.0);
		assertEquals(NodeShapeVisualProperty.DIAMOND, nShapeMp.getPoint(1).getRange().equalValue);
		assertEquals(NodeShapeVisualProperty.RECTANGLE, nShapeMp.getPoint(1).getRange().greaterValue);
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nShapeMp.getPoint(1).getRange().lesserValue);
		assertEquals(1.0, nShapeMp.getPoint(2).getValue(), 0.0);
		assertEquals(NodeShapeVisualProperty.RECTANGLE, nShapeMp.getPoint(2).getRange().equalValue);
		assertEquals(NodeShapeVisualProperty.HEXAGON, nShapeMp.getPoint(2).getRange().greaterValue);
		assertEquals(NodeShapeVisualProperty.RECTANGLE, nShapeMp.getPoint(2).getRange().lesserValue);
		
		ContinuousMapping<Double, Double> nSizeMp = (ContinuousMapping<Double, Double>) def.getVisualMappingFunction(NODE_SIZE);
		assertEquals("gal1RGexp", nSizeMp.getMappingColumnName());
		assertEquals(Number.class, nSizeMp.getMappingColumnType());
		assertEquals(2, nSizeMp.getPointCount());
		assertEquals(0.0, nSizeMp.getPoint(0).getValue(), 0.0);
		assertEquals(50, nSizeMp.getPoint(0).getRange().equalValue.intValue());
		assertEquals(50, nSizeMp.getPoint(0).getRange().greaterValue.intValue());
		assertEquals(20, nSizeMp.getPoint(0).getRange().lesserValue.intValue());
		assertEquals(1.0, nSizeMp.getPoint(1).getValue(), 0.0);
		assertEquals(100, nSizeMp.getPoint(1).getRange().equalValue.intValue());
		assertEquals(100, nSizeMp.getPoint(1).getRange().greaterValue.intValue());
		assertEquals(100, nSizeMp.getPoint(1).getRange().lesserValue.intValue());
		
		// -----
		VisualStyle bio = getVisualStyleByTitle(styles, "SimpleBioMoleculeEditor");
		
		assertEquals(new Color(204,204,255), bio.getDefaultValue(NETWORK_BACKGROUND_PAINT));
		assertEquals(new Color(0,0,0), bio.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(new Color(255,0,0), bio.getDefaultValue(EDGE_STROKE_SELECTED_PAINT));
		assertEquals(Font.decode("Monospaced-PLAIN-12"), bio.getDefaultValue(EDGE_LABEL_FONT_FACE));
		//defaultEdgeLineType=DASHED_2 (split "DASHED" + "2")
		assertEquals(LineTypeVisualProperty.EQUAL_DASH, bio.getDefaultValue(EDGE_LINE_TYPE));
		assertEquals(2, bio.getDefaultValue(EDGE_WIDTH).intValue());
		// defaultEdgeSourceArrow=WHITE_T (can't test EDGE_SOURCE_ARROW_UNSELECTED_PAINT; it's Ding Lexicon's property)
		assertEquals(ArrowShapeVisualProperty.T, bio.getDefaultValue(EDGE_SOURCE_ARROW_SHAPE));
		// defaultEdgeTargetArrow=BLACK_ARROW
		assertEquals(ArrowShapeVisualProperty.ARROW, bio.getDefaultValue(EDGE_TARGET_ARROW_SHAPE));
		assertEquals(new Color(255,255,255), bio.getDefaultValue(NODE_FILL_COLOR));
		assertEquals(new Color(255,255,0), bio.getDefaultValue(NODE_SELECTED_PAINT));
		assertEquals(35, bio.getDefaultValue(NODE_HEIGHT).intValue());
		assertEquals(28, bio.getDefaultValue(NODE_WIDTH).intValue());
		assertEquals(42, bio.getDefaultValue(NODE_SIZE).intValue());
		// defaultNodeLineType=LINE_1
		assertEquals(LineTypeVisualProperty.SOLID, bio.getDefaultValue(NODE_BORDER_LINE_TYPE));
		assertEquals(1, bio.getDefaultValue(NODE_BORDER_WIDTH).intValue());
		assertEquals(NodeShapeVisualProperty.RECTANGLE, bio.getDefaultValue(NODE_SHAPE));
		
		nLabelMp = (PassthroughMapping<String, String>) bio.getVisualMappingFunction(NODE_LABEL);
		assertEquals("canonicalName", nLabelMp.getMappingColumnName());
		assertEquals(String.class, nLabelMp.getMappingColumnType());

		DiscreteMapping<String, NodeShape> nShape = (DiscreteMapping<String, NodeShape>) bio.getVisualMappingFunction(NODE_SHAPE);
		assertEquals("NODE_TYPE", nShape.getMappingColumnName());
		assertEquals(String.class, nShape.getMappingColumnType());
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nShape.getMapValue("biochemicalReaction"));
		assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, nShape.getMapValue("catalyst"));
		assertEquals(NodeShapeVisualProperty.RECTANGLE, nShape.getMapValue("protein"));
		assertEquals(NodeShapeVisualProperty.DIAMOND, nShape.getMapValue("smallMolecule"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCy252Vizmap() throws Exception {
		Properties props = loadVizmapProps("v252_vizmap.props");
		Set<VisualStyle> styles = serializer.createVisualStyles(props);
		assertEquals(5,  styles.size());
		assertVisualStylesNotNull(styles, new String[] { "default", "Sample1", "Sample2", "Sample3", "SimpleBioMoleculeEditor" });
		
		// Test one style
		// -----
		VisualStyle s3 = getVisualStyleByTitle(styles, "Sample3");

		assertEquals(new Color(0,0,0), s3.getDefaultValue(NETWORK_BACKGROUND_PAINT));
		
		assertEquals(new Color(255,255,255), s3.getDefaultValue(NODE_FILL_COLOR));
		assertEquals(new Color(255,255,0), s3.getDefaultValue(NODE_SELECTED_PAINT));
		assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, s3.getDefaultValue(NODE_SHAPE));
		assertEquals(80, s3.getDefaultValue(NODE_WIDTH).intValue());
		assertEquals(30, s3.getDefaultValue(NODE_HEIGHT).intValue());
		assertEquals(180, s3.getDefaultValue(NODE_TRANSPARENCY).intValue());
		assertEquals(new Color(153,153,255), s3.getDefaultValue(NODE_BORDER_PAINT));
		assertEquals(2, s3.getDefaultValue(NODE_BORDER_WIDTH).intValue());
		assertEquals(255, s3.getDefaultValue(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals(new Font("Arial-BoldMT", Font.PLAIN, 12), s3.getDefaultValue(NODE_LABEL_FONT_FACE));
		assertEquals(16, s3.getDefaultValue(NODE_LABEL_FONT_SIZE).intValue());
		assertEquals(new Color(255,255,255), s3.getDefaultValue(NODE_LABEL_COLOR));
		assertEquals(255, s3.getDefaultValue(NODE_LABEL_TRANSPARENCY).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, s3.getDefaultValue(NODE_BORDER_LINE_TYPE));
		assertEquals("", s3.getDefaultValue(NODE_TOOLTIP));
		
		assertEquals(1, s3.getDefaultValue(EDGE_WIDTH).intValue());
		assertEquals(new Color(153,153,255), s3.getDefaultValue(EDGE_UNSELECTED_PAINT));
		assertEquals(new Color(153,153,255), s3.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(new Color(255,0,0), s3.getDefaultValue(EDGE_STROKE_SELECTED_PAINT));
		assertEquals(255, s3.getDefaultValue(EDGE_TRANSPARENCY).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, s3.getDefaultValue(EDGE_LINE_TYPE));
		assertEquals(Font.decode("SanSerif-PLAIN-10"), s3.getDefaultValue(EDGE_LABEL_FONT_FACE));
		assertEquals(14, s3.getDefaultValue(EDGE_LABEL_FONT_SIZE).intValue());
		assertEquals(new Color(255,255,204), s3.getDefaultValue(EDGE_LABEL_COLOR));
		assertEquals(255, s3.getDefaultValue(EDGE_LABEL_TRANSPARENCY).intValue());
		assertEquals(ArrowShapeVisualProperty.NONE, s3.getDefaultValue(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, s3.getDefaultValue(EDGE_TARGET_ARROW_SHAPE));
		assertEquals("", s3.getDefaultValue(EDGE_TOOLTIP));
		
		PassthroughMapping<String, String> nLabelMp = (PassthroughMapping<String, String>) s3.getVisualMappingFunction(NODE_LABEL);
		assertEquals(NAME, nLabelMp.getMappingColumnName());
		assertEquals(String.class, nLabelMp.getMappingColumnType());
		
		PassthroughMapping<String, String> eLabelMp = (PassthroughMapping<String, String>) s3.getVisualMappingFunction(EDGE_LABEL);
		assertEquals(INTERACTION, eLabelMp.getMappingColumnName());
		assertEquals(String.class, eLabelMp.getMappingColumnType());
		
		PassthroughMapping<String, String> nTooltipMp = (PassthroughMapping<String, String>) s3.getVisualMappingFunction(NODE_TOOLTIP);
		assertEquals("gal4RGexp", nTooltipMp.getMappingColumnName());
		assertEquals(String.class, nTooltipMp.getMappingColumnType());
		
		ContinuousMapping<Double, Paint> nColorMp = (ContinuousMapping<Double, Paint>) s3.getVisualMappingFunction(NODE_FILL_COLOR);
		assertEquals("gal4RGexp", nColorMp.getMappingColumnName());
		assertEquals(Number.class, nColorMp.getMappingColumnType());
		assertEquals(3, nColorMp.getPointCount());
		assertEquals(-2.4059998989105242, nColorMp.getPoint(0).getValue(), 0.0001);
		assertEquals(new Color(0,153,0), nColorMp.getPoint(0).getRange().equalValue);
		assertEquals(new Color(0,153,0), nColorMp.getPoint(0).getRange().greaterValue);
		assertEquals(Color.BLACK, nColorMp.getPoint(0).getRange().lesserValue);
		assertEquals(-3.254413627473696E-8, nColorMp.getPoint(1).getValue(), 0.0001);
		assertEquals(Color.WHITE, nColorMp.getPoint(1).getRange().equalValue);
		assertEquals(Color.WHITE, nColorMp.getPoint(1).getRange().greaterValue);
		assertEquals(Color.WHITE, nColorMp.getPoint(1).getRange().lesserValue);
		assertEquals(1.2239999999999998, nColorMp.getPoint(2).getValue(), 0.0001);
		assertEquals(new Color(255,0,0), nColorMp.getPoint(2).getRange().equalValue);
		assertEquals(new Color(255,255,255), nColorMp.getPoint(2).getRange().greaterValue);
		assertEquals(new Color(255,0,0), nColorMp.getPoint(2).getRange().lesserValue);
		
		DiscreteMapping<String, Paint> eColorMp = (DiscreteMapping<String, Paint>) s3.getVisualMappingFunction(EDGE_STROKE_UNSELECTED_PAINT);
		assertEquals(INTERACTION, eColorMp.getMappingColumnName());
		assertEquals(String.class, eColorMp.getMappingColumnType());
		assertEquals(new Color(102,255,255), eColorMp.getMapValue("pd"));
		assertEquals(new Color(255,255,255), eColorMp.getMapValue("pp"));
		
		DiscreteMapping<String, LineType> eTypeMp = (DiscreteMapping<String, LineType>) s3.getVisualMappingFunction(EDGE_LINE_TYPE);
		assertEquals(INTERACTION, eTypeMp.getMappingColumnName());
		assertEquals(LineTypeVisualProperty.LONG_DASH, eTypeMp.getMapValue("pd"));
		assertEquals(LineTypeVisualProperty.SOLID, eTypeMp.getMapValue("pp"));
		
		VisualPropertyDependency<?> dep1 = getDependency(s3, NODE_SIZE_LOCKED_DEPENDENCY);
		assertFalse(dep1.isDependencyEnabled());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCy270Vizmap() throws Exception {
		Properties props = loadVizmapProps("v270_vizmap.props");
		Set<VisualStyle> styles = serializer.createVisualStyles(props);
		assertEquals(2,  styles.size());
		assertVisualStylesNotNull(styles, new String[] { "default", "Binary_SIF_Version_1" });
		
		// Test visual styles (defaults, mappings and dependencies)
		// -----
		VisualStyle s = getVisualStyleByTitle(styles, "Binary_SIF_Version_1");
		
		assertEquals(new Color(255,153,153), s.getDefaultValue(NODE_FILL_COLOR));
		assertEquals(new Color(255,255,0), s.getDefaultValue(NODE_SELECTED_PAINT));
		assertEquals(NodeShapeVisualProperty.ELLIPSE, s.getDefaultValue(NODE_SHAPE));
		assertEquals(70, s.getDefaultValue(NODE_WIDTH).intValue());
		assertEquals(30, s.getDefaultValue(NODE_HEIGHT).intValue());
		assertEquals(35, s.getDefaultValue(NODE_SIZE).intValue());
		assertEquals(125, s.getDefaultValue(NODE_TRANSPARENCY).intValue());
		assertEquals(new Color(0,0,0), s.getDefaultValue(NODE_BORDER_PAINT));
		assertEquals(1, s.getDefaultValue(NODE_BORDER_WIDTH).intValue());
		assertEquals(255, s.getDefaultValue(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals("", s.getDefaultValue(NODE_LABEL));
		assertEquals(Font.decode("Default-PLAIN-12"), s.getDefaultValue(NODE_LABEL_FONT_FACE));
		assertEquals(12, s.getDefaultValue(NODE_LABEL_FONT_SIZE).intValue());
		assertEquals(new Color(0,0,0), s.getDefaultValue(NODE_LABEL_COLOR));
		assertEquals(100, s.getDefaultValue(NODE_LABEL_WIDTH).intValue());
		assertEquals(255, s.getDefaultValue(NODE_LABEL_TRANSPARENCY).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, s.getDefaultValue(NODE_BORDER_LINE_TYPE));
		assertEquals("", s.getDefaultValue(NODE_TOOLTIP));
		assertEquals(Boolean.TRUE, s.getDefaultValue(NODE_NESTED_NETWORK_IMAGE_VISIBLE));
		
		assertEquals(4, s.getDefaultValue(EDGE_WIDTH).intValue());
		assertEquals(new Color(0,0,0), s.getDefaultValue(EDGE_UNSELECTED_PAINT));
		assertEquals(new Color(0,0,0), s.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(new Color(255,0,0), s.getDefaultValue(EDGE_STROKE_SELECTED_PAINT));
		assertEquals(255, s.getDefaultValue(EDGE_TRANSPARENCY).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, s.getDefaultValue(EDGE_LINE_TYPE));
		assertEquals("", s.getDefaultValue(EDGE_LABEL));
		assertEquals(Font.decode("SanSerif-PLAIN-10"), s.getDefaultValue(EDGE_LABEL_FONT_FACE));
		assertEquals(10, s.getDefaultValue(EDGE_LABEL_FONT_SIZE).intValue());
		assertEquals(new Color(0,0,0), s.getDefaultValue(EDGE_LABEL_COLOR));
		assertEquals(255, s.getDefaultValue(EDGE_LABEL_TRANSPARENCY).intValue());
		assertEquals(ArrowShapeVisualProperty.NONE, s.getDefaultValue(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, s.getDefaultValue(EDGE_TARGET_ARROW_SHAPE));
		assertEquals("", s.getDefaultValue(EDGE_TOOLTIP));
		
		PassthroughMapping<String, String> nLabelMp = (PassthroughMapping<String, String>) s.getVisualMappingFunction(NODE_LABEL);
		assertEquals("biopax.node_label", nLabelMp.getMappingColumnName());
		assertEquals(String.class, nLabelMp.getMappingColumnType());
		
		assertNull(s.getVisualMappingFunction(EDGE_LABEL));
		
		DiscreteMapping<String, Paint> nColorMp = (DiscreteMapping<String, Paint>) s.getVisualMappingFunction(NODE_FILL_COLOR);
		assertEquals("biopax.entity_type", nColorMp.getMappingColumnName());
		assertEquals(String.class, nColorMp.getMappingColumnType());
		assertEquals(new Color(153,153,255), nColorMp.getMapValue("Complex"));
		assertNull(nColorMp.getMapValue("Protein"));
		
		DiscreteMapping<String, Paint> eColorMp = (DiscreteMapping<String, Paint>) s.getVisualMappingFunction(EDGE_STROKE_UNSELECTED_PAINT);
		assertEquals(INTERACTION, eColorMp.getMappingColumnName());
		assertEquals(String.class, eColorMp.getMappingColumnType());
		// Test a few entries
		assertEquals(new Color(255,192,0), eColorMp.getMapValue("COMPONENT_OF"));
		assertEquals(new Color(255,0,0), eColorMp.getMapValue("CO_CONTROL_DEPENDENT_ANTI"));
		assertEquals(new Color(0,176,80), eColorMp.getMapValue("CO_CONTROL_DEPENDENT_SIMILAR"));
		assertEquals(new Color(253,149,166), eColorMp.getMapValue("CO_CONTROL_INDEPENDENT_ANTI"));
		assertEquals(new Color(0,176,80), eColorMp.getMapValue("CO_CONTROL_DEPENDENT_SIMILAR"));
		
		DiscreteMapping<String, ArrowShape> eTgtArrowMp = (DiscreteMapping<String, ArrowShape>) s.getVisualMappingFunction(EDGE_TARGET_ARROW_SHAPE);
		assertEquals(INTERACTION, eTgtArrowMp.getMappingColumnName());
		assertEquals(String.class, eTgtArrowMp.getMappingColumnType());
		assertEquals(ArrowShapeVisualProperty.ARROW, eTgtArrowMp.getMapValue("COMPONENT_OF"));
		assertEquals(ArrowShapeVisualProperty.ARROW, eTgtArrowMp.getMapValue("METABOLIC_CATALYSIS"));
		assertEquals(ArrowShapeVisualProperty.ARROW, eTgtArrowMp.getMapValue("SEQUENTIAL_CATALYSIS"));
		assertEquals(ArrowShapeVisualProperty.ARROW, eTgtArrowMp.getMapValue("SEQUENTIAL_CATALYSIS"));
		assertEquals(ArrowShapeVisualProperty.ARROW, eTgtArrowMp.getMapValue("STATE_CHANGE"));
		assertNull(eTgtArrowMp.getMapValue("INTERACTS_WITH"));
		
		VisualPropertyDependency<?> dep1 = getDependency(s, NODE_SIZE_LOCKED_DEPENDENCY);
		assertFalse(dep1.isDependencyEnabled());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCy283Vizmap() throws Exception {
		Properties props = loadVizmapProps("v283_vizmap.props");
		Set<VisualStyle> styles = serializer.createVisualStyles(props);
		assertEquals(6,  styles.size());
		assertVisualStylesNotNull(styles, new String[] {
				"default", "Sample1", "Solid", "Universe", 
				"{Gal_Filt}: -(1:2),&[A*|B?]+%$#@!\\/;" /*Testing special chars*/,
				"Nested Network Style" });
		
		// Test visual styles (defaults, mappings and dependencies)
		// -----
		VisualStyle sample1 = getVisualStyleByTitle(styles, "Sample1");
		
		assertEquals(new Color(255,255,204), sample1.getDefaultValue(NETWORK_BACKGROUND_PAINT));
		assertEquals(Color.BLACK, sample1.getDefaultValue(EDGE_UNSELECTED_PAINT));
		assertEquals(Color.BLACK, sample1.getDefaultValue(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(new Color(255,0,1), sample1.getDefaultValue(EDGE_STROKE_SELECTED_PAINT));
		assertEquals(Font.decode("Default-PLAIN-10"), sample1.getDefaultValue(EDGE_LABEL_FONT_FACE));
		assertEquals(10, sample1.getDefaultValue(EDGE_LABEL_FONT_SIZE).intValue());
		assertEquals(new Color(0,10,0), sample1.getDefaultValue(EDGE_LABEL_COLOR));
		assertEquals(120, sample1.getDefaultValue(EDGE_LABEL_TRANSPARENCY).intValue());
		assertEquals("", sample1.getDefaultValue(EDGE_LABEL));
		assertEquals(LineTypeVisualProperty.SOLID, sample1.getDefaultValue(EDGE_LINE_TYPE));
		assertEquals(1, sample1.getDefaultValue(EDGE_WIDTH).intValue());
		assertEquals(180, sample1.getDefaultValue(EDGE_TRANSPARENCY).intValue());
		assertEquals(ArrowShapeVisualProperty.NONE, sample1.getDefaultValue(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.CIRCLE, sample1.getDefaultValue(EDGE_TARGET_ARROW_SHAPE));
		assertEquals("", sample1.getDefaultValue(EDGE_TOOLTIP));
		assertEquals(new Color(10,20,0), sample1.getDefaultValue(NODE_BORDER_PAINT));
		assertEquals(242, sample1.getDefaultValue(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals(new Color(204,204,255), sample1.getDefaultValue(NODE_FILL_COLOR));
		assertEquals(new Color(255,255,1), sample1.getDefaultValue(NODE_SELECTED_PAINT));
		assertEquals(Font.decode("Dialog-BOLD-12"), sample1.getDefaultValue(NODE_LABEL_FONT_FACE));
		assertEquals(12, sample1.getDefaultValue(NODE_LABEL_FONT_SIZE).intValue());
		assertEquals("node", sample1.getDefaultValue(NODE_LABEL));
		assertEquals(new Color(0,0,255), sample1.getDefaultValue(NODE_LABEL_COLOR));
		assertEquals(220, sample1.getDefaultValue(NODE_LABEL_TRANSPARENCY).intValue());
		assertEquals(30, sample1.getDefaultValue(NODE_HEIGHT).intValue());
		assertEquals(70, sample1.getDefaultValue(NODE_WIDTH).intValue());
		assertEquals(40, sample1.getDefaultValue(NODE_SIZE).intValue());
		assertEquals(LineTypeVisualProperty.LONG_DASH, sample1.getDefaultValue(NODE_BORDER_LINE_TYPE));
		assertEquals(0, sample1.getDefaultValue(NODE_BORDER_WIDTH).intValue());
		assertEquals(250, sample1.getDefaultValue(NODE_TRANSPARENCY).intValue());
		assertEquals(NodeShapeVisualProperty.ELLIPSE, sample1.getDefaultValue(NODE_SHAPE));
		assertEquals(true, sample1.getDefaultValue(NODE_NESTED_NETWORK_IMAGE_VISIBLE));
		assertEquals("My test...", sample1.getDefaultValue(NODE_TOOLTIP));
		
		PassthroughMapping<String, String> nLabelMp = (PassthroughMapping<String, String>) sample1.getVisualMappingFunction(NODE_LABEL);
		assertEquals(NAME, nLabelMp.getMappingColumnName());
		assertEquals(String.class, nLabelMp.getMappingColumnType());
		
		PassthroughMapping<String, String> eLabelMp = (PassthroughMapping<String, String>) sample1.getVisualMappingFunction(EDGE_LABEL);
		assertEquals(INTERACTION, eLabelMp.getMappingColumnName());
		
		DiscreteMapping<String, Paint> eColorMp1 = (DiscreteMapping<String, Paint>) sample1.getVisualMappingFunction(EDGE_UNSELECTED_PAINT);
		assertEquals(INTERACTION, eColorMp1.getMappingColumnName());
		assertEquals(String.class, eColorMp1.getMappingColumnType());
		assertEquals(new Color(255, 0, 51), eColorMp1.getMapValue("pd"));
		assertEquals(new Color(0, 204, 0), eColorMp1.getMapValue("pp"));
		
		DiscreteMapping<String, Paint> eColorMp2 = (DiscreteMapping<String, Paint>) sample1.getVisualMappingFunction(EDGE_STROKE_UNSELECTED_PAINT);
		assertEquals(INTERACTION, eColorMp2.getMappingColumnName());
		assertEquals(String.class, eColorMp2.getMappingColumnType());
		assertEquals(new Color(255, 0, 51), eColorMp2.getMapValue("pd"));
		assertEquals(new Color(0, 204, 0), eColorMp2.getMapValue("pp"));
		
		DiscreteMapping<String, LineType> eTypeMp = (DiscreteMapping<String, LineType>) sample1.getVisualMappingFunction(EDGE_LINE_TYPE);
		assertEquals(INTERACTION, eTypeMp.getMappingColumnName());
		assertEquals(LineTypeVisualProperty.LONG_DASH, eTypeMp.getMapValue("pd"));
		assertEquals(LineTypeVisualProperty.SOLID, eTypeMp.getMapValue("pp"));
		
		VisualPropertyDependency<?> dep1 = getDependency(sample1, NODE_SIZE_LOCKED_DEPENDENCY);
		assertFalse(dep1.isDependencyEnabled());
		
		// -----
		VisualStyle galFiltered = getVisualStyleByTitle(styles, "{Gal_Filt}: -(1:2),&[A*|B?]+%$#@!\\/;");
		
		ContinuousMapping<Double, Paint> nColorMp = (ContinuousMapping<Double, Paint>) galFiltered.getVisualMappingFunction(NODE_FILL_COLOR);
		assertEquals("gal4RGexp", nColorMp.getMappingColumnName());
		assertEquals(Number.class, nColorMp.getMappingColumnType());
		assertEquals(3, nColorMp.getPointCount());
		assertEquals(-2.0249776914715767, nColorMp.getPoint(0).getValue(), 0.0001);
		assertEquals(new Color(255,0,0), nColorMp.getPoint(0).getRange().equalValue);
		assertEquals(new Color(255,0,0), nColorMp.getPoint(0).getRange().greaterValue);
		assertEquals(Color.BLACK, nColorMp.getPoint(0).getRange().lesserValue);
		assertEquals(0.20517408847808838, nColorMp.getPoint(1).getValue(), 0.0001);
		assertEquals(Color.WHITE, nColorMp.getPoint(1).getRange().equalValue);
		assertEquals(Color.WHITE, nColorMp.getPoint(1).getRange().greaterValue);
		assertEquals(Color.WHITE, nColorMp.getPoint(1).getRange().lesserValue);
		assertEquals(2.5, nColorMp.getPoint(2).getValue(), 0.0001);
		assertEquals(new Color(0,153,0), nColorMp.getPoint(2).getRange().equalValue);
		assertEquals(new Color(0,0,204), nColorMp.getPoint(2).getRange().greaterValue);
		assertEquals(new Color(0,153,0), nColorMp.getPoint(2).getRange().lesserValue);
		
		VisualPropertyDependency<?> dep2 = getDependency(galFiltered, NODE_SIZE_LOCKED_DEPENDENCY);
		assertTrue(dep2.isDependencyEnabled());
		
		// -----
		VisualStyle nested = getVisualStyleByTitle(styles, "Nested Network Style");
		
		DiscreteMapping<Boolean, Paint> nLabelColorMp = (DiscreteMapping<Boolean, Paint>) nested.getVisualMappingFunction(NODE_LABEL_COLOR);
		assertEquals("has_nested_network", nLabelColorMp.getMappingColumnName());
		assertEquals(Boolean.class, nLabelColorMp.getMappingColumnType());
		assertEquals(new Color(0,102,204), nLabelColorMp.getMapValue(true));
	}

	// UTIL ============================================================================================================

	private void assertVisualStylesNotNull(Collection<VisualStyle> styles, String[] titles) {
		for (String s : titles)
			assertNotNull(getVisualStyleByTitle(styles, s));
	}
	
	private VisualStyle getVisualStyleByTitle(Collection<VisualStyle> styles, String title) {
		for (VisualStyle vs : styles) {
			if (vs.getTitle().equals(title)) return vs;
		}
		return null;
	}
	
	private Properties loadVizmapProps(String filename) throws IOException {
		final Properties props = new Properties();
		final FileInputStream is = new FileInputStream(new File("./src/test/resources/testData/vizmap/", filename));
		props.load(is);
		is.close();
		
		return props;
	}
	
	private VisualPropertyDependency<?> getDependency(VisualStyle vs, String id) {
		for (VisualPropertyDependency<?> d : vs.getAllVisualPropertyDependencies()) {
			if (d.getIdString().equals(id)) return d;
		}
		return null;
	}
	
	// MOCKUPS =========================================================================================================
	
	private VisualStyleFactory mockVisualStyleFactory(VisualStyle defStyle) {
		VisualStyleFactory factory = mock(VisualStyleFactory.class);
		
		when(factory.createVisualStyle(defStyle)).thenAnswer(new Answer<VisualStyle>() {
			public VisualStyle answer(InvocationOnMock invocation) throws Throwable {
				return new DummyVisualStyle((VisualStyle) invocation.getArguments()[0]);
			}
		});
		
		when(factory.createVisualStyle(anyString())).thenAnswer(new Answer<VisualStyle>() {
			public VisualStyle answer(InvocationOnMock invocation) throws Throwable {
				return new DummyVisualStyle((String) invocation.getArguments()[0]);
			}
		});
		
		return factory;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private VisualMappingFunctionFactory mockMappingFunctionFactory(final Class<? extends VisualMappingFunction> type) {
		VisualMappingFunctionFactory factory = mock(VisualMappingFunctionFactory.class);
		
		when(factory.createVisualMappingFunction(anyString(), any(Class.class), any(VisualProperty.class)))
			.thenAnswer(new Answer<VisualMappingFunction>() {
				@Override
				public VisualMappingFunction answer(InvocationOnMock invocation) throws Throwable {
					Object[] args = invocation.getArguments();
					if (type == DiscreteMapping.class)
						return new DummyDiscreteMapping((String)args[0], (Class)args[1], (VisualProperty)args[2], evtHelper);
					else if (type == ContinuousMapping.class)
						return new DummyContinuousMapping((String)args[0], (Class)args[1], (VisualProperty)args[2], evtHelper);
					else
						return new DummyPassthroughMapping((String)args[0], (Class)args[1], (VisualProperty)args[2], evtHelper);
				}
			});
		
		return factory;
	}
	
	private class DummyVisualStyle implements VisualStyle {

		private String title;
		private final Map<VisualProperty<?>, Object> defaults;
		private final Map<VisualProperty<?>, VisualMappingFunction<?, ?>> mappings;
		private final Set<VisualPropertyDependency<?>> dependencies;

		public DummyVisualStyle(String title) {
			this.title = title;
			this.defaults = new HashMap<VisualProperty<?>, Object>();
			this.mappings = new HashMap<VisualProperty<?>, VisualMappingFunction<?,?>>();
			this.dependencies = new HashSet<VisualPropertyDependency<?>>();
			createDependencies();
		}

		public DummyVisualStyle(VisualStyle style) {
			this(style.getTitle());
			dependencies.addAll(style.getAllVisualPropertyDependencies());
			
			if (style instanceof DummyVisualStyle) // should always be true in this test!
				defaults.putAll( ((DummyVisualStyle)style).defaults );
			
			for (VisualMappingFunction<?, ?> mp : mappings.values()) {
				try {
					if (mp instanceof DummyPassthroughMapping)
						addVisualMappingFunction(((DummyPassthroughMapping<?, ?>)mp).clone());
					if (mp instanceof DummyDiscreteMapping)
						addVisualMappingFunction(((DummyDiscreteMapping<?, ?>)mp).clone());
					if (mp instanceof DummyContinuousMapping)
						addVisualMappingFunction(((DummyContinuousMapping<?, ?>)mp).clone());
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public String getTitle() {
			return this.title;
		}

		@Override
		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public void addVisualMappingFunction(VisualMappingFunction<?, ?> mapping) {
			mappings.put(mapping.getVisualProperty(), mapping);
		}

		@Override
		public void removeVisualMappingFunction(VisualProperty<?> vp) {
			mappings.remove(vp);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V> VisualMappingFunction<?, V> getVisualMappingFunction(VisualProperty<V> vp) {
			return (VisualMappingFunction<?, V>) mappings.get(vp);
		}

		@Override
		public Collection<VisualMappingFunction<?, ?>> getAllVisualMappingFunctions() {
			return Collections.unmodifiableCollection(mappings.values());
		}

		@Override
		@SuppressWarnings("unchecked")
		public <V> V getDefaultValue(VisualProperty<V> vp) {
			return (V) defaults.get(vp);
		}

		@Override
		public <V, S extends V> void setDefaultValue(VisualProperty<V> vp, S value) {
			defaults.put(vp, value);
		}

		@Override
		public void apply(CyNetworkView networkView) {
			// ignore...
		}

		@Override
		public void apply(CyRow row, View<? extends CyIdentifiable> view) {
			// ignore...
		}

		@Override
		public Set<VisualPropertyDependency<?>> getAllVisualPropertyDependencies() {
			return Collections.unmodifiableSet(dependencies);
		}

		@Override
		public void addVisualPropertyDependency(VisualPropertyDependency<?> dependency) {
			dependencies.add(dependency);
		}

		@Override
		public void removeVisualPropertyDependency(VisualPropertyDependency<?> dependency) {
			dependencies.remove(dependency);
		}
		
		private void createDependencies() {
			// This is not good, but is the only way to test dependencies, since they are Ding-specific
			// Let's create Node Size Dependency only, because the other ones require Ding's lexicon
			final Set<VisualProperty<Double>> nodeSizeProperties = new HashSet<VisualProperty<Double>>();
			nodeSizeProperties.add(NODE_WIDTH);
			nodeSizeProperties.add(NODE_HEIGHT);
			addVisualPropertyDependency(new VisualPropertyDependency<Double>(NODE_SIZE_LOCKED_DEPENDENCY, "Lock node W/H", 
					nodeSizeProperties, lexicon));
		}
	}
	
	private class DummyDiscreteMapping<K, V> extends AbstractVisualMappingFunction<K, V> implements
			DiscreteMapping<K, V>, Cloneable {
		
		private Map<K, V> map;

		public DummyDiscreteMapping(String columnName, Class<K> columnType, VisualProperty<V> vp, 
				CyEventHelper eventHelper) {
			super(columnName, columnType, vp, eventHelper);
			map = new HashMap<K, V>();
		}

		@Override
		public V getMappedValue(CyRow row) {
			return map.get(row.get(columnName, columnType));
		}

		@Override
		public V getMapValue(K key) {
			return map.get(key);
		}

		@Override
		public <T extends V> void putMapValue(K key, T value) {
			map.put(key, value);
		}

		@Override
		public <T extends V> void putAll(Map<K, T> map) {
			this.map.putAll(map);
		}

		@Override
		public Map<K, V> getAll() {
			return Collections.unmodifiableMap(map);
		}
		
		@Override
		protected VisualMappingFunction<?, ?> clone() throws CloneNotSupportedException {
			DummyDiscreteMapping<K, V> clone = new DummyDiscreteMapping<K, V>(columnName, columnType, vp, eventHelper);
			clone.map.putAll(map);
			
			return clone;
		}
	}

	private class DummyContinuousMapping<K, V> extends AbstractVisualMappingFunction<K, V> implements
			ContinuousMapping<K, V>, Cloneable {
		
		private List<ContinuousMappingPoint<K, V>> points;

		public DummyContinuousMapping(String columnName, Class<K> columnType, VisualProperty<V> vp,
				CyEventHelper eventHelper) {
			super(columnName, columnType, vp, eventHelper);
			points = new ArrayList<ContinuousMappingPoint<K,V>>();
		}

		@Override
		public V getMappedValue(CyRow row) {
			return null;
		}

		@Override
		public List<ContinuousMappingPoint<K, V>> getAllPoints() {
			return Collections.unmodifiableList(points);
		}

		@Override
		public void addPoint(K value, BoundaryRangeValues<V> brv) {
			points.add(new ContinuousMappingPoint<K, V>(value, brv, this, eventHelper));
		}

		@Override
		public void removePoint(int index) {
			points.remove(index);
		}

		@Override
		public int getPointCount() {
			return points.size();
		}

		@Override
		public ContinuousMappingPoint<K, V> getPoint(int index) {
			return points.get(index);
		}
		
		@Override
		protected VisualMappingFunction<?, ?> clone() throws CloneNotSupportedException {
			DummyContinuousMapping<K, V> clone = new DummyContinuousMapping<K, V>(columnName, columnType, vp, eventHelper);
			clone.points.addAll(points);
			
			return clone;
		}
	}

	private class DummyPassthroughMapping<K, V> extends AbstractVisualMappingFunction<K, V> implements
			PassthroughMapping<K, V>, Cloneable {

		public DummyPassthroughMapping(String columnName, Class<K> columnType, VisualProperty<V> vp,
				CyEventHelper eventHelper) {
			super(columnName, columnType, vp, eventHelper);
		}

		@Override
		public V getMappedValue(CyRow row) {
			return null;
		}

		@Override
		protected VisualMappingFunction<?, ?> clone() throws CloneNotSupportedException {
			return new DummyPassthroughMapping<K, V>(columnName, columnType, vp, eventHelper);
		}
	}
}
