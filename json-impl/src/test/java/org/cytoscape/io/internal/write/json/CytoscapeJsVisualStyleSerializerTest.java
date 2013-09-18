package org.cytoscape.io.internal.write.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsModule;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleModule;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleSerializer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.internal.VisualLexiconManager;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsVisualStyleSerializerTest {

	private CytoscapeJsVisualStyleSerializer serializer;

	private VisualStyle style;
	private VisualLexicon lexicon;

	@Before
	public void setUp() throws Exception {

		final NullVisualProperty minimalRoot = new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property");
		lexicon = new BasicVisualLexicon(minimalRoot);
		serializer = new CytoscapeJsVisualStyleSerializer(lexicon);
		style = generateVisualStyle(lexicon);
		setDefaults();

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
		style.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 40d);
		style.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 3d);
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLUE);

		// Edge default values
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.GREEN);
		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 5d);
		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.DIAMOND);
		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);
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

		File temp = new File("src/test/resources/site/app/data/cytoscapeJsStyle.json");

		OutputStream os = new FileOutputStream(temp);
		JSONVisualStyleWriter writer = new JSONVisualStyleWriter(os, jsMapper, styles);
		writer.run(tm);
		testGeneratedJSONFile(temp);
	}

	private final void testGeneratedJSONFile(File temp) throws Exception {
		final FileInputStream fileInputStream = new FileInputStream(temp);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,
				EncodingUtil.getDecoder()));

		final JsonFactory factory = new JsonFactory();
		final JsonParser jp = factory.createParser(reader);

		final ObjectMapper mapper = new ObjectMapper();

		final JsonNode rootNode = mapper.readValue(reader, JsonNode.class);

		System.out.println(mapper);
		// Perform actual tests
		assertNotNull(rootNode);

		reader.close();
	}
}