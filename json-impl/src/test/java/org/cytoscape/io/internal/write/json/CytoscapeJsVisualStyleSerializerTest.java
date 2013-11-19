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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.dependency.EdgeColorDependencyFactory;
import org.cytoscape.ding.dependency.NodeSizeDependencyFactory;
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
import org.cytoscape.view.vizmap.VisualPropertyDependency;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

		final CustomGraphicsManager cgManager = mock(CustomGraphicsManager.class);
		lexicon = new DVisualLexicon(cgManager);
		
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
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(10, 10, 200));
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
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font("Helvetica", Font.PLAIN, 12));
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 122);

		// For Selected
		style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, Color.RED);

		// Edge default values
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(12,100,200));
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, new Color(222, 100, 10));

		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DOT);

		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 3d);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, Color.red);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_FACE, new Font("SansSerif", Font.BOLD, 12));
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 11);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 220);

		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.DELTA);
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
	public void testSerializeWithoutLock() throws Exception {
		// Unlock all
		final Set<VisualPropertyDependency<?>> locks = style.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: locks) {
			dep.setDependency(false);
		}
		testUnlocked(writeVS("target/vs.json"));
	}

	@Test
	public void testSerializeWithLock() throws Exception {
		// Set Locks
		final NodeSizeDependencyFactory nodeSizeDefFactory = new NodeSizeDependencyFactory(lexicon);
		style.addVisualPropertyDependency(nodeSizeDefFactory.createVisualPropertyDependency());
		final EdgeColorDependencyFactory edgeColorDepFactory = new EdgeColorDependencyFactory(lexicon);
		style.addVisualPropertyDependency(edgeColorDepFactory.createVisualPropertyDependency());
		
		final Set<VisualPropertyDependency<?>> locks = style.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: locks) {
			dep.setDependency(true);
		}
		testLocked(writeVS("target/vs-locked.json"));
	}

	
	private final File writeVS(final String fileName) throws Exception {
		assertNotNull(serializer);
		assertNotNull(style);

		TaskMonitor tm = mock(TaskMonitor.class);
		final Set<VisualStyle> styles = new HashSet<VisualStyle>();
		styles.add(style);

		final ObjectMapper jsMapper = new ObjectMapper();
		jsMapper.registerModule(new CytoscapeJsVisualStyleModule(lexicon));

		File temp = new File(fileName);
		OutputStream os = new FileOutputStream(temp);
		CytoscapeJsVisualStyleWriter writer = new CytoscapeJsVisualStyleWriter(os, jsMapper, styles, lexicon);
		writer.run(tm);

		return temp;
	}
	
	
	private final void testUnlocked(File temp) throws Exception {
		final JsonNode rootNode = read(temp);
		testDefaultsUnlocked(rootNode);
	}

	private final void testLocked(File temp) throws Exception {
		final JsonNode rootNode = read(temp);
		testDefaultsLocked(rootNode);
	}
	
	private final JsonNode read(File generatedJsonFile) throws IOException {
		final FileInputStream fileInputStream = new FileInputStream(generatedJsonFile);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,
				EncodingUtil.getDecoder()));

		final ObjectMapper mapper = new ObjectMapper();

		final JsonNode root = mapper.readValue(reader, JsonNode.class);
		reader.close();
		return root;
	}
	
	private final void testDefaultsUnlocked(final JsonNode root) throws Exception {
		final Map<String, JsonNode> result = getNodesAndEdges(root);
		final JsonNode nodeCSS = result.get("node").get("css");
		final JsonNode edgeCSS = result.get("edge").get("css");
	
		testNodeDefaultsUnlocked(nodeCSS);
		testEdgeDefaultsUnlocked(edgeCSS);
	}


	private final void testDefaultsLocked(final JsonNode root) throws Exception {
		final Map<String, JsonNode> result = getNodesAndEdges(root);
		final JsonNode nodeCSS = result.get("node").get("css");
		final JsonNode edgeCSS = result.get("edge").get("css");
	
		testNodeDefaultsLocked(nodeCSS);
		testEdgeDefaultsLocked(edgeCSS);
	}


	private final Map<String,JsonNode> getNodesAndEdges(final JsonNode root) {
		final Map<String,JsonNode> graphObjects = new HashMap<String, JsonNode>();

		// VS JSON is a array of Visual Styles
		assertTrue(root.isArray());
		assertEquals(1, root.size());
		final JsonNode style1 = root.get(0);
		assertEquals("vs1", style1.get("title").asText());
		final JsonNode styleObject = style1.get("style");
		assertNotNull(styleObject);
		
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

		graphObjects.put("node", nodes);
		graphObjects.put("edge", edges);

		return graphObjects;
	}
	
	
	private final String calcOpacity(int opacity) {
		return Double.toString(opacity/255d);
		
	}
	
	private final void testNodeDefaultsCommon(final JsonNode nodeCSS) {
		assertEquals("rgb(10,10,200)", nodeCSS.get("background-color").asText());
		assertEquals(calcOpacity(200), nodeCSS.get("background-opacity").asText());

		assertEquals("rgb(0,0,255)", nodeCSS.get("border-color").asText());
		assertEquals(calcOpacity(150), nodeCSS.get("border-opacity").asText());
		assertTrue(2d == nodeCSS.get("border-width").asDouble());
		
		// Font defaults
		assertEquals("Helvetica-Light", nodeCSS.get("font-family").asText());
		assertEquals("18", nodeCSS.get("font-size").asText());
		assertEquals("normal", nodeCSS.get("font-weight").asText());
		assertEquals("rgb(0,0,255)", nodeCSS.get("color").asText());
		assertEquals(calcOpacity(122), nodeCSS.get("text-opacity").asText());

		assertEquals("roundrectangle", nodeCSS.get("shape").asText());
	}
	
	private void testEdgeDefaultsCommon(JsonNode edgeCSS) throws Exception {
		assertTrue(3d == edgeCSS.get("width").asDouble());
		
		assertEquals("SansSerif", edgeCSS.get("font-family").asText());
		assertTrue(11d == edgeCSS.get("font-size").asDouble());
		assertEquals("bold", edgeCSS.get("font-weight").asText());
		assertEquals("rgb(255,0,0)", edgeCSS.get("color").asText());
		assertEquals(calcOpacity(220), edgeCSS.get("text-opacity").asText());

		assertEquals("dotted", edgeCSS.get("line-style").asText());

		assertEquals("triangle", edgeCSS.get("target-arrow-shape").asText());
		assertEquals("tee", edgeCSS.get("source-arrow-shape").asText());
	}
	
	private void testNodeDefaultsUnlocked(JsonNode nodeCSS) throws Exception {
		testNodeDefaultsCommon(nodeCSS);
		assertTrue(40d == nodeCSS.get("width").asDouble());
		assertTrue(30d == nodeCSS.get("height").asDouble());
	}
	
	private void testEdgeDefaultsUnlocked(JsonNode edgeCSS) throws Exception {
		testEdgeDefaultsCommon(edgeCSS);
		assertEquals("rgb(12,100,200)", edgeCSS.get("line-color").asText());
	}
	
	
	// Tests for Locked Style
	private void testNodeDefaultsLocked(JsonNode nodeCSS) throws Exception {
		testNodeDefaultsCommon(nodeCSS);
		assertTrue(60d == nodeCSS.get("width").asDouble());
		assertTrue(60d == nodeCSS.get("height").asDouble());
	}
	
	private void testEdgeDefaultsLocked(JsonNode edgeCSS) throws Exception {
		testEdgeDefaultsCommon(edgeCSS);
		assertEquals("rgb(222,100,10)", edgeCSS.get("line-color").asText());
	}
}