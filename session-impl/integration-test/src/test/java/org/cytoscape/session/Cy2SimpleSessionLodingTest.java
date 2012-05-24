package org.cytoscape.session;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * Test for sample file in Cytoscape 2.5.2 distribution
 * 
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy2SimpleSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v252Session.cys");
		checkBasicConfiguration();
	}

	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
	}

	@After
	public void confirm() {
		// test overall status of current session.
		checkGlobalStatus();

		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Iterator<CyNetwork> itr = networks.iterator();
		CyNetwork network = itr.next();

		checkNetwork(network);

	}

	private void checkGlobalStatus() {
		assertEquals(1, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		assertEquals(6, tableManager.getAllTables(true).size());
		assertEquals(3, tableManager.getAllTables(false).size());

	}

	private void checkNetwork(final CyNetwork network) {
		assertEquals(331, network.getNodeCount());
		assertEquals(362, network.getEdgeCount());

		// Selection state
		Collection<CyRow> selectedNodes = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
		Collection<CyRow> selectedEdges = network.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true);
		assertEquals(0, selectedNodes.size());
		assertEquals(0, selectedEdges.size());

		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		assertEquals(1, views.size());

		final CyNetworkView view = views.iterator().next();
		assertEquals(331, view.getNodeViews().size());
		assertEquals(362, view.getEdgeViews().size());

		// Visual Style
		assertEquals(5, vmm.getAllVisualStyles().size());
		final VisualStyle style = vmm.getVisualStyle(view);
		checkVisualStyle(style);

		// Apply the given style
		style.apply(view);

		// Check updated view
		checkView(view);

	}

	private void checkVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals("Sample3", style.getTitle());

		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(6, mappings.size());

		// Test defaults
		NodeShape defaultShape = style.getDefaultValue(BasicVisualLexicon.NODE_SHAPE);
		Paint nodeColor = style.getDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR);
		Integer fontSize = style.getDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		Integer transparency = style.getDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY);
		Double w = style.getDefaultValue(BasicVisualLexicon.NODE_WIDTH);
		Double h = style.getDefaultValue(BasicVisualLexicon.NODE_HEIGHT);
		
		Paint edgeLabelColor = style.getDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR);
		assertEquals(new Color(255,255,204), edgeLabelColor);

		assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, defaultShape);
		assertEquals(Color.WHITE, nodeColor);
		assertEquals(Integer.valueOf(16), fontSize);
		assertEquals(Integer.valueOf(180), transparency);
		assertEquals(Double.valueOf(80), w);
		assertEquals(Double.valueOf(30), h);

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);
		assertTrue(edgeLabelMapping instanceof PassthroughMapping);

		assertEquals(CyNetwork.NAME, nodeLabelMapping.getMappingColumnName());
		assertEquals(CyEdge.INTERACTION, edgeLabelMapping.getMappingColumnName());
		assertEquals(String.class, nodeLabelMapping.getMappingColumnType());
		assertEquals(String.class, edgeLabelMapping.getMappingColumnType());

		// Node Color mapping
		VisualMappingFunction<?, Paint> nodeColorMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
		assertTrue(nodeColorMapping instanceof ContinuousMapping);
		assertEquals("gal4RGexp", nodeColorMapping.getMappingColumnName());
		assertEquals(Number.class, nodeColorMapping.getMappingColumnType());

		// Edge Color mapping
		VisualMappingFunction<?, Paint> edgeColorMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		assertTrue(edgeColorMapping instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeColorMapping.getMappingColumnName());
		assertEquals(String.class, edgeColorMapping.getMappingColumnType());
		DiscreteMapping<String, Paint> disc1 = (DiscreteMapping<String, Paint>) edgeColorMapping;
		assertEquals(Color.WHITE, disc1.getMapValue("pp"));
		assertEquals(new Color(102, 255, 255), disc1.getMapValue("pd"));
		assertEquals(null, disc1.getMapValue("this is an invalid value"));
		
		// Numbers as Tooltip 
		VisualMappingFunction<?, String> nodeTooltipMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.NODE_TOOLTIP);
		assertTrue(nodeTooltipMapping instanceof PassthroughMapping);
		assertEquals("gal4RGexp", nodeTooltipMapping.getMappingColumnName());
		
		// FIXME this should be Number.class, but returns String
		//assertEquals(Number.class, nodeTooltipMapping.getMappingColumnType());

		VisualMappingFunction<?, LineType> edgeLineStyleMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.EDGE_LINE_TYPE);
		assertTrue(edgeLineStyleMapping instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeLineStyleMapping.getMappingColumnName());
		assertEquals(String.class, edgeLineStyleMapping.getMappingColumnType());
		DiscreteMapping<String, LineType> disc2 = (DiscreteMapping<String, LineType>) edgeLineStyleMapping;
		assertEquals(LineTypeVisualProperty.SOLID, disc2.getMapValue("pp"));
		assertEquals(LineTypeVisualProperty.LONG_DASH, disc2.getMapValue("pd"));
		assertEquals(null, disc2.getMapValue("this is an invalid value"));
	}

	private void checkView(final CyNetworkView view) {
		final Color backgroungColor = (Color) view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		assertEquals(Color.BLACK, backgroungColor);
		final Double nodeWidth = view.getNodeView(view.getModel().getNodeList().iterator().next()).getVisualProperty(
				BasicVisualLexicon.NODE_WIDTH);
		assertEquals(Double.valueOf(80.0d), nodeWidth);
	}

}
