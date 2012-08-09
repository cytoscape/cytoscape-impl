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
 * Test for sample file in Cytoscape 2.6.3 distribution
 * 
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy263SimpleSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v263SessionLarge.cys");
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

		checkVisualStyles();
		
		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Iterator<CyNetwork> itr = networks.iterator();

		// get the parent network in the session
		CyNetwork gdNetwork = null;
		while (itr.hasNext()){
			CyNetwork network = itr.next();

			// Check title
			String title = network.getRow(network).get("name", String.class);
			if (title.equalsIgnoreCase("gene_disease_network4.txt")){
				gdNetwork = network;
				break;
			}
		}		
		
		checkAttributes(gdNetwork);
		checkNetwork(gdNetwork);
	}

	private void checkGlobalStatus() {
		assertEquals(7, networkManager.getNetworkSet().size());
		assertEquals(7, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		assertEquals(63, tableManager.getAllTables(true).size());
		assertEquals(42, tableManager.getAllTables(false).size());

	}

	private void checkVisualStyles() {

		Set<VisualStyle> vsSet = vmm.getAllVisualStyles();
		
		VisualStyle gda_geneCentric = null;
		VisualStyle gda_diseaseCentric = null;
		VisualStyle gda_wholeNetwork = null;
		
		Iterator<VisualStyle> it = vsSet.iterator();
		
		while (it.hasNext()){
			VisualStyle vs = it.next();
			if (vs.getTitle().equalsIgnoreCase("gda_geneCentric")){
				gda_geneCentric = vs;
			}
			if (vs.getTitle().equalsIgnoreCase("gda_diseaseCentric")){
				gda_diseaseCentric = vs;
			}
			if (vs.getTitle().equalsIgnoreCase("gda_wholeNetwork")){
				gda_wholeNetwork = vs;
			}
		}
		
		assertNotNull(gda_geneCentric);
		assertNotNull(gda_diseaseCentric);
		assertNotNull(gda_wholeNetwork);

		checkVisualStyle(gda_wholeNetwork);		
	}
	
	
	private void checkVisualStyle(final VisualStyle style) {

		//		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		//		assertEquals(4, mappings.size());

		// Test defaults
		NodeShape defaultShape = style.getDefaultValue(BasicVisualLexicon.NODE_SHAPE);
		Paint nodeColor = style.getDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR);
		Integer fontSize = style.getDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		Integer transparency = style.getDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY);
		Double w = style.getDefaultValue(BasicVisualLexicon.NODE_WIDTH);
		Double h = style.getDefaultValue(BasicVisualLexicon.NODE_HEIGHT);

		Paint edgeLabelColor = style.getDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR);

		assertEquals(NodeShapeVisualProperty.RECTANGLE, defaultShape);
		assertEquals(Color.WHITE, nodeColor);
		assertEquals(Integer.valueOf(12), fontSize);
		assertEquals(Integer.valueOf(255), transparency);
		assertEquals(Double.valueOf(70), w);
		assertEquals(Double.valueOf(30), h);

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);

		assertEquals("Name", nodeLabelMapping.getMappingColumnName());
		assertEquals(String.class, nodeLabelMapping.getMappingColumnType());

		// Node Color mapping
		VisualMappingFunction<?, Paint> nodeColorMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
		assertTrue(nodeColorMapping instanceof DiscreteMapping);
		assertEquals("NodeType", nodeColorMapping.getMappingColumnName());
		assertEquals(String.class, nodeColorMapping.getMappingColumnType());

		VisualMappingFunction<?, ?> edgeWidthMapping = style
				.getVisualMappingFunction(BasicVisualLexicon.EDGE_WIDTH);
		assertTrue(edgeWidthMapping instanceof ContinuousMapping);
		assertEquals("AsssociationCount", edgeWidthMapping.getMappingColumnName());
	}

	private void checkAttributes(final CyNetwork network){
		
		CyTable nodeTable = network.getDefaultNodeTable();
		
		// check attribute name
		assertTrue(CyTableUtil.getColumnNames(nodeTable).contains("NodeType"));
		assertTrue(CyTableUtil.getColumnNames(nodeTable).contains("OMIM_ID"));
		
		// check attribute values		
		Object[] rows1 = nodeTable.getMatchingRows("Name", "Obesity").toArray();
		CyRow row1 = (CyRow)rows1[0];		
		assertEquals("Disease",row1.get("NodeType", String.class));	
		assertEquals(null,row1.get("OMIM_ID", String.class));	

		Object[] rows2 = nodeTable.getMatchingRows("Name", "ACE").toArray();
		CyRow row2 = (CyRow)rows2[0];		
		assertEquals("Gene",row2.get("NodeType", String.class));	
		assertEquals(106180,row2.get("OMIM_ID", Integer.class).intValue());			
	}

	
	private void checkNetwork(final CyNetwork network) {
		assertEquals(4133, network.getNodeCount());
		assertEquals(14558, network.getEdgeCount());

		// Selection state
		Collection<CyRow> selectedNodes = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
		Collection<CyRow> selectedEdges = network.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true);
		assertEquals(0, selectedNodes.size());
		assertEquals(0, selectedEdges.size());
	}
}
