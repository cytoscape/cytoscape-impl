package org.cytoscape.session;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy283ComplexSessionLodingTest extends BasicIntegrationTest {

	// These are network titles used in the original session file.
	private static final String NET1 = "RUAL.subset";
	private static final String NET2 = "RUAL.subset--child";
	private static final String NET3 = "RUAL.subset--child--child";
	private static final String NET4 = "galFiltered";
	private static final String NET5 = "galFiltered--child";

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v283Session1.cys");
		if (!sessionFile.exists())
			fail("Could not find the file. " + sessionFile.toString());

		checkBasicConfiguration();
	}

	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
		confirm();		
	}

	private void confirm() {
		// test overall status of current session.
		checkGlobalStatus();
		checkNetworks();
	}

	private void checkGlobalStatus() {
		final int ROOTNET_COUNT = 1;
		final int SUBNET_COUNT = 5;
		
		assertEquals(SUBNET_COUNT, networkManager.getNetworkSet().size());
		assertEquals(4, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		assertEquals(3*SUBNET_COUNT, tableManager.getAllTables(false).size());
		assertEquals((9*SUBNET_COUNT) + (15*ROOTNET_COUNT), tableManager.getAllTables(true).size());

		// Visual Style
		assertEquals(7, vmm.getAllVisualStyles().size());
	}

	private void checkNetworks() {
		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Set<CyRootNetwork> rootNetworks = new HashSet<CyRootNetwork>();

		for (CyNetwork network : networks) {
			// Non-default columns should not be immutable
			assertCy2CustomColumnsAreMutable(network);
			
			if (network instanceof CySubNetwork)
				rootNetworks.add(((CySubNetwork)network).getRootNetwork());
			
			// Pick specific network.
			String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
			if (networkName.equals(NET1))
				testNetwork1(network);
			else if (networkName.equals(NET2))
				testNetwork2(network);
			else if (networkName.equals(NET3))
				testNetwork3(network);
			else if (networkName.equals(NET4))
				testNetwork4(network);
			else if (networkName.equals(NET5))
				testNetwork5(network);
		}
		
		for (CyRootNetwork rootNet : rootNetworks)
			assertCy2CustomColumnsAreMutable(rootNet);
	}

	private void testNetwork1(CyNetwork network) {

	}

	private void testNetwork2(CyNetwork network) {

	}

	private void testNetwork3(CyNetwork network) {

	}

	private void testNetwork4(CyNetwork network) {
		assertEquals(331, network.getNodeCount());
		assertEquals(362, network.getEdgeCount());

		// Selection state
		Collection<CyRow> selectedNodes = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
		Collection<CyRow> selectedEdges = network.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true);
		assertEquals(82, selectedNodes.size());
		assertEquals(110, selectedEdges.size());

		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		assertEquals(1, views.size());

		final CyNetworkView view = views.iterator().next();
		assertEquals(331, view.getNodeViews().size());
		assertEquals(362, view.getEdgeViews().size());

		final VisualStyle style = vmm.getVisualStyle(view);
		checkVisualStyle1(style, view);
	}

	private void checkVisualStyle1(final VisualStyle style, final CyNetworkView view) {
		assertNotNull(style);
		assertEquals("Sample1", style.getTitle());

		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(4, mappings.size());

		// Test defaults
		NodeShape defaultShape = style.getDefaultValue(NODE_SHAPE);
		Paint nodeColor = style.getDefaultValue(NODE_FILL_COLOR);
		Integer fontSize = style.getDefaultValue(NODE_LABEL_FONT_SIZE);
		Integer transparency = style.getDefaultValue(NODE_TRANSPARENCY);
		Double size = style.getDefaultValue(NODE_SIZE);
		Paint edgeLabelColor = style.getDefaultValue(EDGE_LABEL_COLOR);

		assertEquals(NodeShapeVisualProperty.ELLIPSE, defaultShape);
		assertEquals(new Color(204, 204, 255), nodeColor);
		assertEquals(Integer.valueOf(12), fontSize);
		assertEquals(Integer.valueOf(255), transparency);
		assertEquals(Double.valueOf(40), size);
		assertEquals(Color.BLACK, edgeLabelColor);

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style.getVisualMappingFunction(NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style.getVisualMappingFunction(EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);
		assertTrue(edgeLabelMapping instanceof PassthroughMapping);

		assertEquals(CyNetwork.NAME, nodeLabelMapping.getMappingColumnName());
		assertEquals(CyEdge.INTERACTION, edgeLabelMapping.getMappingColumnName());
		assertEquals(String.class, nodeLabelMapping.getMappingColumnType());
		assertEquals(String.class, edgeLabelMapping.getMappingColumnType());

		// Edge Color mapping
		VisualMappingFunction<?, Paint> edgeColorMapping = style.getVisualMappingFunction(EDGE_STROKE_UNSELECTED_PAINT);
		assertTrue(edgeColorMapping instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeColorMapping.getMappingColumnName());
		assertEquals(String.class, edgeColorMapping.getMappingColumnType());
		DiscreteMapping<String, Paint> disc1 = (DiscreteMapping<String, Paint>) edgeColorMapping;
		assertEquals(new Color(0, 204, 0), disc1.getMapValue("pp"));
		assertEquals(new Color(255, 0, 51), disc1.getMapValue("pd"));
		assertEquals(null, disc1.getMapValue("this is an invalid value"));

		VisualMappingFunction<?, LineType> edgeLineStyleMapping = style.getVisualMappingFunction(EDGE_LINE_TYPE);
		assertTrue(edgeLineStyleMapping instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeLineStyleMapping.getMappingColumnName());
		assertEquals(String.class, edgeLineStyleMapping.getMappingColumnType());
		DiscreteMapping<String, LineType> disc2 = (DiscreteMapping<String, LineType>) edgeLineStyleMapping;
		assertEquals(LineTypeVisualProperty.SOLID, disc2.getMapValue("pp"));
		assertEquals(LineTypeVisualProperty.LONG_DASH, disc2.getMapValue("pd"));
		assertEquals(null, disc2.getMapValue("this is an invalid value"));

		final Set<VisualPropertyDependency<?>> deps = style.getAllVisualPropertyDependencies();
		assertEquals(3, deps.size());
		
		// Check updated view
		style.apply(view);
		checkView1(view);
	}
	
	private void checkView1(final CyNetworkView view) {
		assertEquals(Color.WHITE, view.getVisualProperty(NETWORK_BACKGROUND_PAINT));
		
		// Check lock is restored
		
		final Double nodeSize = view.getNodeView(view.getModel().getNodeList().iterator().next()).getVisualProperty(
				NODE_SIZE);
		
		// FIXME dependency is broken.
		//assertEquals(Double.valueOf(40.0d), nodeSize);
		
		// Check bypass value
		// Node YBR217W
		
	}

	private void testNetwork5(CyNetwork network) {
		// TODO Auto-generated method stub

	}

}
