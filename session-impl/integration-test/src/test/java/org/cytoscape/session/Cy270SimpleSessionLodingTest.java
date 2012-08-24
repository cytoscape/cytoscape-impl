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
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyTable;


/**
 * Test for sample file in Cytoscape 2.7.0 distribution
 * 
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy270SimpleSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v270session.cys");
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

		Iterator<CyNetworkView> viewIt = viewManager.getNetworkViewSet().iterator();

		CyNetworkView view = viewIt.next(); 
		CyNetwork network = view.getModel();
		
		checkNetwork(network);
		checkNetworkView(network);
		checkAttributes(network);
	}

	private void checkGlobalStatus() {
		assertEquals(2, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		assertEquals(18, tableManager.getAllTables(true).size());
		assertEquals(6, tableManager.getAllTables(false).size());
	}


	private void checkNetworkView(CyNetwork network){
		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		assertEquals(1, views.size());

		final CyNetworkView view = views.iterator().next();

		// Visual Style
		assertEquals(5, vmm.getAllVisualStyles().size());
		final VisualStyle style = vmm.getVisualStyle(view);
		checkVisualStyle(style);

		// Apply the given style
		style.apply(view);

		// Check updated view
		checkView(view);
		
	}
	
	private void checkNetwork(final CyNetwork network) {
		assertEquals(53, network.getNodeCount());
		assertEquals(63, network.getEdgeCount());

		// Selection state
		Collection<CyRow> selectedNodes = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
		Collection<CyRow> selectedEdges = network.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true);
		assertEquals(0, selectedNodes.size());
		assertEquals(0, selectedEdges.size());		
	}

	
	private void checkAttributes(final CyNetwork network){
		
		CyTable nodeTable = network.getDefaultNodeTable();
		
		// check attribute name
		assertTrue(CyTableUtil.getColumnNames(nodeTable).contains("attrInt"));
		assertTrue(CyTableUtil.getColumnNames(nodeTable).contains("attrString"));
		assertTrue(CyTableUtil.getColumnNames(nodeTable).contains("attrFloat"));
		assertTrue(CyTableUtil.getColumnNames(nodeTable).contains("attrBoolean"));

		
		// check attribute values		
		Object[] rows1 = nodeTable.getMatchingRows("name", "YBL069W").toArray();
		CyRow row1 = (CyRow)rows1[0];		
		assertEquals(1,row1.get("attrInt", Integer.class).intValue());		
		assertTrue(row1.get("attrBoolean", Boolean.class));		
		assertTrue(row1.get("attrString", String.class).equals("aaa"));
		
		Object[] rows2 = nodeTable.getMatchingRows("name", "YDL023C").toArray();
		CyRow row2 = (CyRow)rows2[0];		
		assertEquals(2,row2.get("attrInt", Integer.class).intValue());		
		assertFalse(row2.get("attrBoolean", Boolean.class));		
		assertTrue(row2.get("attrString", String.class).equals("bbb"));
	}

	
	private void checkVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals("Solid", style.getTitle());

		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(2, mappings.size());

		// Test defaults
		NodeShape defaultShape = style.getDefaultValue(BasicVisualLexicon.NODE_SHAPE);
		Double w = style.getDefaultValue(BasicVisualLexicon.NODE_WIDTH);
		Double h = style.getDefaultValue(BasicVisualLexicon.NODE_HEIGHT);

		assertEquals(NodeShapeVisualProperty.ELLIPSE, defaultShape);
		assertEquals(Double.valueOf(70), w);
		assertEquals(Double.valueOf(30), h);

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);
		assertTrue(edgeLabelMapping instanceof PassthroughMapping);
	}

	private void checkView(final CyNetworkView view) {
		final Color backgroungColor = (Color) view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		assertEquals(Color.white, backgroungColor);
	}

}
