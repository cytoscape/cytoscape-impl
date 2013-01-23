package org.cytoscape.session;

/*
 * #%L
 * Cytoscape Session Impl Integration Test (session-impl-integration-test)
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
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * Test for sample file in Cytoscape 2.6.3 distribution
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

		// get the parent network in the session
		CyNetwork net = getNetworkByName("gene_disease_network4.txt");
		assertNotNull(net);
		
		checkAttributes(net);
		checkNetwork(net);
	}

	private void checkGlobalStatus() {
		final int ROOTNET_COUNT = 1;
		final int SUBNET_COUNT = 7;
		
		assertEquals(SUBNET_COUNT, networkManager.getNetworkSet().size());
		assertEquals(SUBNET_COUNT, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		assertEquals(3*SUBNET_COUNT, tableManager.getAllTables(false).size());
		assertEquals((9*SUBNET_COUNT) + (15*ROOTNET_COUNT), tableManager.getAllTables(true).size());
		
		// Current network and view
		final CyNetwork curNet = getNetworkByName("gene_disease_network4.txt--child--child.4");
		assertEquals(curNet, applicationManager.getCurrentNetwork());
		assertEquals(curNet, applicationManager.getCurrentNetworkView().getModel());
		
		// Visual Styles
		checkVisualStyles();
		checkCurrentVisualStyle(vmm.getCurrentVisualStyle());
	}

	private void checkVisualStyles() {
		Set<VisualStyle> vsSet = vmm.getAllVisualStyles();
		assertEquals(8, vsSet.size());
		
		VisualStyle gda_geneCentric = null;
		VisualStyle gda_diseaseCentric = null;
		VisualStyle gda_wholeNetwork = null;
		
		Iterator<VisualStyle> it = vsSet.iterator();
		
		while (it.hasNext()){
			VisualStyle vs = it.next();
			if (vs.getTitle().equalsIgnoreCase("gda_geneCentric"))
				gda_geneCentric = vs;
			else if (vs.getTitle().equalsIgnoreCase("gda_diseaseCentric"))
				gda_diseaseCentric = vs;
			else if (vs.getTitle().equalsIgnoreCase("gda_wholeNetwork"))
				gda_wholeNetwork = vs;
		}
		
		assertNotNull(gda_geneCentric);
		assertNotNull(gda_diseaseCentric);
		assertNotNull(gda_wholeNetwork);
	}
	
	private void checkCurrentVisualStyle(final VisualStyle style) {
		assertEquals("gda_wholeNetwork", style.getTitle());
		
		//		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		//		assertEquals(4, mappings.size());

		// Test defaults
		NodeShape defaultShape = style.getDefaultValue(NODE_SHAPE);
		Paint nodeColor = style.getDefaultValue(NODE_FILL_COLOR);
		Integer fontSize = style.getDefaultValue(NODE_LABEL_FONT_SIZE);
		Integer transparency = style.getDefaultValue(NODE_TRANSPARENCY);
		Double w = style.getDefaultValue(NODE_WIDTH);
		Double h = style.getDefaultValue(NODE_HEIGHT);

		Paint edgeLabelColor = style.getDefaultValue(EDGE_LABEL_COLOR);

		assertEquals(NodeShapeVisualProperty.RECTANGLE, defaultShape);
		assertEquals(Color.WHITE, nodeColor);
		assertEquals(Integer.valueOf(12), fontSize);
		assertEquals(Integer.valueOf(255), transparency);
		assertEquals(Double.valueOf(70), w);
		assertEquals(Double.valueOf(30), h);

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style.getVisualMappingFunction(NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style.getVisualMappingFunction(EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);

		assertEquals("Name", nodeLabelMapping.getMappingColumnName());
		assertEquals(String.class, nodeLabelMapping.getMappingColumnType());

		// Node Color mapping
		VisualMappingFunction<?, Paint> nodeColorMapping = style.getVisualMappingFunction(NODE_FILL_COLOR);
		assertTrue(nodeColorMapping instanceof DiscreteMapping);
		assertEquals("NodeType", nodeColorMapping.getMappingColumnName());
		assertEquals(String.class, nodeColorMapping.getMappingColumnType());

		VisualMappingFunction<?, ?> edgeWidthMapping = style.getVisualMappingFunction(EDGE_WIDTH);
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
		checkNodeEdgeCount(network, 4133, 14558, 0, 0);
		
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);

		final CyNetworkView view = views.iterator().next();
		assertEquals(4133, view.getNodeViews().size());
		assertEquals(14558, view.getEdgeViews().size());

		// Check updated view
		final VisualStyle style = vmm.getVisualStyle(view);
		style.apply(view);
		checkView(view);
	}
	
	private void checkView(final CyNetworkView view) {
		assertEquals(new Color(204,204,255), view.getVisualProperty(NETWORK_BACKGROUND_PAINT));
		assertEquals(new Double(247.0d), view.getVisualProperty(NETWORK_WIDTH));
		assertEquals(new Double(208.0d), view.getVisualProperty(NETWORK_HEIGHT));
		assertEquals(new Double(2613.0d), view.getVisualProperty(NETWORK_CENTER_X_LOCATION));
		assertEquals(new Double(2579.0d), view.getVisualProperty(NETWORK_CENTER_Y_LOCATION));
		assertEquals(new Double(0.11904328781215048d), view.getVisualProperty(NETWORK_SCALE_FACTOR));
		
		// All nodes have the same default visual properties
		final View<CyNode> nv = view.getNodeView(view.getModel().getNodeList().iterator().next());
		assertEquals(40, nv.getVisualProperty(NODE_SIZE).intValue());
		assertEquals(new Color(255,153,153), nv.getVisualProperty(NODE_FILL_COLOR));
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nv.getVisualProperty(NODE_SHAPE));
		assertEquals(255, nv.getVisualProperty(NODE_TRANSPARENCY).intValue());
		assertEquals(new Double(1.5d), nv.getVisualProperty(NODE_BORDER_WIDTH));
		assertEquals(LineTypeVisualProperty.SOLID, nv.getVisualProperty(NODE_BORDER_LINE_TYPE));
		assertEquals(new Color(102,102,102), nv.getVisualProperty(NODE_BORDER_PAINT));
		assertEquals(255, nv.getVisualProperty(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals(new Color(0,0,0), nv.getVisualProperty(NODE_LABEL_COLOR));
		assertEquals(255, nv.getVisualProperty(NODE_LABEL_TRANSPARENCY).intValue());
		assertEquals(Font.decode("SansSerif-BOLD-12"), nv.getVisualProperty(NODE_LABEL_FONT_FACE));
		assertEquals(12, nv.getVisualProperty(NODE_LABEL_FONT_SIZE).intValue());
		
		// All edges have the same visual properties
		final View<CyEdge> ev = view.getEdgeView(view.getModel().getEdgeList().iterator().next());
		assertEquals(new Color(0,0,255), ev.getVisualProperty(EDGE_UNSELECTED_PAINT));
		assertEquals(255, ev.getVisualProperty(EDGE_TRANSPARENCY).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, ev.getVisualProperty(EDGE_LINE_TYPE));
		assertEquals(new Double(1.5d), ev.getVisualProperty(EDGE_WIDTH));
		assertEquals(new Color(0,0,0), ev.getVisualProperty(EDGE_LABEL_COLOR));
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
}
