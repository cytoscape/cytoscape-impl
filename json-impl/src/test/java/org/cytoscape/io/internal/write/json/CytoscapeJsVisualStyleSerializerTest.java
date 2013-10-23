package org.cytoscape.io.internal.write.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleModule;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleSerializer;
import org.cytoscape.io.internal.write.json.serializer.ValueSerializerManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsVisualStyleSerializerTest {

	private CytoscapeJsVisualStyleSerializer serializer;

	private VisualStyle style;
	private VisualLexicon lexicon;

	private PassthroughMappingFactory passthroughFactory;
	private ContinuousMappingFactory continuousFactory;
	private DiscreteMappingFactory discreteFactory;

	@Before
	public void setUp() throws Exception {

		final NullVisualProperty minimalRoot = new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property");
		lexicon = new BasicVisualLexicon(minimalRoot);
		
		final ValueSerializerManager manager = new ValueSerializerManager();
		serializer = new CytoscapeJsVisualStyleSerializer(manager, lexicon);

		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		passthroughFactory = new PassthroughMappingFactory(eventHelper);
		discreteFactory = new DiscreteMappingFactory(eventHelper);
		continuousFactory = new ContinuousMappingFactory(eventHelper);

		style = generateVisualStyle(lexicon);
		setDefaults();
		setMappings();

		// Simple test to check Visual Style contents
		assertEquals("vs1", style.getTitle());
	}

	@After
	public void tearDown() throws Exception {
	}

	private final VisualStyle generateVisualStyle(final VisualLexicon lexicon) {

		final VisualLexiconManager lexManager = mock(VisualLexiconManager.class);
		final Set<VisualLexicon> lexSet = new HashSet<VisualLexicon>();
		lexSet.add(lexicon);
		final Collection<VisualProperty<?>> nodeVP = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
		final Collection<VisualProperty<?>> edgeVP = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		when(lexManager.getNodeVisualProperties()).thenReturn(nodeVP);
		when(lexManager.getEdgeVisualProperties()).thenReturn(edgeVP);

		when(lexManager.getAllVisualLexicon()).thenReturn(lexSet);

		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(lexManager, serviceRegistrar,
				ptFactory, eventHelper);

		return visualStyleFactory.createVisualStyle("vs1");
	}

	private final void setDefaults() {
		// Node default values
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);

		style.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 40d);
		style.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		style.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 60d);

		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);

		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 2d);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 150);

		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 18);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font("HelvaticaNeu", Font.PLAIN, 12));
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 122);

		// For Selected
		style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, Color.RED);


		// Edge default values
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.GREEN);
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.DARK_GRAY);

		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DOT);

		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 3d);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, Color.red);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_FACE, new Font("SansSerif", Font.BOLD, 12));
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 11);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 220);

		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.DIAMOND);
		style.setDefaultValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.T);
		
		// For Selected
		style.setDefaultValue(BasicVisualLexicon.EDGE_SELECTED_PAINT, Color.PINK);
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.ORANGE);
	}

	private final void setMappings() {
		// Passthrough mappings
		final VisualMappingFunction<String, String> nodeLabelMapping = passthroughFactory.createVisualMappingFunction(
				CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		final VisualMappingFunction<String, String> edgeLabelMapping = passthroughFactory.createVisualMappingFunction(
				CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LABEL);
		style.addVisualMappingFunction(nodeLabelMapping);
		style.addVisualMappingFunction(edgeLabelMapping);

		// Continuous mappings
		// Simple two points mapping.
		final ContinuousMapping<Integer, Paint> nodeLabelColorMapping = (ContinuousMapping<Integer, Paint>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_LABEL_COLOR);
		
		final ContinuousMapping<Integer, Double> nodeWidthMapping = (ContinuousMapping<Integer, Double>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_WIDTH);
		final ContinuousMapping<Integer, Double> nodeHeightMapping = (ContinuousMapping<Integer, Double>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_HEIGHT);
		
		// Complex multi-point mapping
		final ContinuousMapping<Integer, Paint> nodeColorMapping = (ContinuousMapping<Integer, Paint>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);

		final BoundaryRangeValues<Paint> lc1 = new BoundaryRangeValues<Paint>(Color.black, Color.yellow, Color.green);
		final BoundaryRangeValues<Paint> lc2 = new BoundaryRangeValues<Paint>(Color.red, Color.pink, Color.blue);
		nodeLabelColorMapping.addPoint(3, lc1);
		nodeLabelColorMapping.addPoint(10, lc2);
		style.addVisualMappingFunction(nodeLabelColorMapping);
		
		final BoundaryRangeValues<Paint> color1 = new BoundaryRangeValues<Paint>(Color.black, Color.red, Color.orange);
		final BoundaryRangeValues<Paint> color2 = new BoundaryRangeValues<Paint>(Color.white, Color.white, Color.white);
		final BoundaryRangeValues<Paint> color3= new BoundaryRangeValues<Paint>(Color.green, Color.pink, Color.blue);
		
		// Shuffle insertion.
		nodeColorMapping.addPoint(2, color1);
		nodeColorMapping.addPoint(5, color2);
		nodeColorMapping.addPoint(10, color3);

		final BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(20d, 20d, 20d);
		final BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(200d, 200d, 400d);
		nodeWidthMapping.addPoint(1, bv0);
		nodeWidthMapping.addPoint(20, bv1);
		nodeHeightMapping.addPoint(1, bv0);
		nodeHeightMapping.addPoint(20, bv1);

		style.addVisualMappingFunction(nodeWidthMapping);
		style.addVisualMappingFunction(nodeColorMapping);

		// Discrete mappings
		final DiscreteMapping<String, NodeShape> nodeShapeMapping = (DiscreteMapping<String, NodeShape>) discreteFactory
				.createVisualMappingFunction("Node Type", String.class, BasicVisualLexicon.NODE_SHAPE);
		nodeShapeMapping.putMapValue("gene", NodeShapeVisualProperty.DIAMOND);
		nodeShapeMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
		nodeShapeMapping.putMapValue("compound", NodeShapeVisualProperty.ROUND_RECTANGLE);
		nodeShapeMapping.putMapValue("pathway", NodeShapeVisualProperty.OCTAGON);

		style.addVisualMappingFunction(nodeShapeMapping);

		final DiscreteMapping<String, Paint> edgeColorMapping = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction("interaction", String.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		edgeColorMapping.putMapValue("pp", Color.green);
		edgeColorMapping.putMapValue("pd", Color.red);

		style.addVisualMappingFunction(edgeColorMapping);
	}

	@Test
	public void testSerialize() throws Exception {
		assertNotNull(serializer);
		assertNotNull(style);

		TaskMonitor tm = mock(TaskMonitor.class);

		final Set<VisualStyle> styles = new HashSet<VisualStyle>();
		styles.add(style);

		final ObjectMapper jsMapper = new ObjectMapper();
		jsMapper.registerModule(new CytoscapeJsVisualStyleModule(lexicon));

		File temp = new File("src/test/resources/site/app/data/vs1.json");

		OutputStream os = new FileOutputStream(temp);
		CytoscapeJsVisualStyleWriter writer = new CytoscapeJsVisualStyleWriter(os, jsMapper, styles, lexicon);
		writer.run(tm);
		testGeneratedJSONFile(temp);
	}

	private final void testGeneratedJSONFile(File temp) throws Exception {
		final FileInputStream fileInputStream = new FileInputStream(temp);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,
				EncodingUtil.getDecoder()));

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

		// Perform actual tests
		assertNotNull(rootNode);

		testDefaults(rootNode);

		reader.close();
	}

	private final void testDefaults(final JsonNode root) throws Exception {
		// VS JSON is a array of Visual Styles
		assertTrue(root.isArray());
		assertEquals(1, root.size());
		final JsonNode style1 = root.get(0);
		assertEquals("vs1", style1.get("title").asText());
		final JsonNode styleObject = style1.get("style");
		assertNotNull(styleObject);

		int elementSize = styleObject.size();
//		final JsonNode nodeSelector = styleObject.get("node");
//		assertNotNull(nodeSelector);
		
		JsonNode nodes = null;
		JsonNode edges = null;
		
		for(JsonNode jNode: styleObject) {
			final JsonNode css = jNode.get("css");
			final JsonNode selectorType = jNode.get("selector");
			System.out.println(selectorType.asText());
			if(selectorType.asText().equals("node")) {
				nodes = jNode;
			} else if(selectorType.asText().equals("edge")) {
				edges = jNode;
			}
		}
	
		assertNotNull(nodes);
		assertNotNull(edges);
		
		final JsonNode nodeCSS = nodes.get("css");
		final JsonNode edgeCSS = edges.get("css");
	
		testNodeDefaults(nodeCSS);
		testEdgeDefaults(edgeCSS);
	}
	
	private void testNodeDefaults(JsonNode nodeCSS) throws Exception {
		assertEquals("rgb(0,0,255)", nodeCSS.get("border-color").asText());
	}
	
	private void testEdgeDefaults(JsonNode edgeCSS) throws Exception {
		
	}
}