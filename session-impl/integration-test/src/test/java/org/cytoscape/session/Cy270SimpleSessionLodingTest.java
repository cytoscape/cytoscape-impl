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
import java.io.File;
import java.util.Collection;

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
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;


/**
 * Test for sample file in Cytoscape 2.7.0 distribution
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

		final CyNetwork network = getNetworkByName("galFiltered--child");
		checkNetwork(network);
		checkNetworkView(network);
		checkAttributes(network);
	}

	private void checkGlobalStatus() {
		final int ROOTNET_COUNT = 1;
		final int SUBNET_COUNT = 2;
		
		assertEquals(SUBNET_COUNT, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());

		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());

		assertEquals(3*SUBNET_COUNT, tableManager.getAllTables(false).size());
		assertEquals((9*SUBNET_COUNT) + (15*ROOTNET_COUNT), tableManager.getAllTables(true).size());
		
		// Current network and view
		final CyNetwork curNet = getNetworkByName("galFiltered--child");
		assertEquals(curNet, applicationManager.getCurrentNetwork());
		assertEquals(curNet, applicationManager.getCurrentNetworkView().getModel());
		
		// Visual Style
		assertEquals(5, vmm.getAllVisualStyles().size());
		checkCurrentVisualStyle(vmm.getCurrentVisualStyle());
	}

	private void checkNetworkView(CyNetwork network){
		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		assertEquals(1, views.size());

		// Check updated view
		final CyNetworkView view = views.iterator().next();
		final VisualStyle style = vmm.getVisualStyle(view);
		style.apply(view);
		checkView(view);
	}
	
	private void checkNetwork(final CyNetwork network) {
		checkNodeEdgeCount(network, 53, 63, 0, 0);
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

	
	private void checkCurrentVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals("Solid", style.getTitle());

		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(2, mappings.size());

		// Test defaults
		assertEquals(NodeShapeVisualProperty.ELLIPSE, style.getDefaultValue(NODE_SHAPE));
		assertEquals(Double.valueOf(70), style.getDefaultValue(NODE_WIDTH));
		assertEquals(Double.valueOf(30), style.getDefaultValue(NODE_HEIGHT));

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style.getVisualMappingFunction(NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style.getVisualMappingFunction(EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);
		assertTrue(edgeLabelMapping instanceof PassthroughMapping);
	}

	private void checkView(final CyNetworkView view) {
		assertEquals(Color.WHITE, view.getVisualProperty(NETWORK_BACKGROUND_PAINT));
		assertEquals(new Double(400.0d), view.getVisualProperty(NETWORK_WIDTH));
		assertEquals(new Double(400.0d), view.getVisualProperty(NETWORK_HEIGHT));
		assertEquals(new Double(1667.0d), view.getVisualProperty(NETWORK_CENTER_X_LOCATION));
		assertEquals(new Double(2553.0d), view.getVisualProperty(NETWORK_CENTER_Y_LOCATION));
		assertEquals(new Double(0.19686075949367088d), view.getVisualProperty(NETWORK_SCALE_FACTOR));
		
		// All nodes have the same default visual properties
		final View<CyNode> nv = view.getNodeView(view.getModel().getNodeList().iterator().next());
		assertEquals(40, nv.getVisualProperty(NODE_SIZE).intValue());
		assertEquals(new Color(102,102,102), nv.getVisualProperty(NODE_FILL_COLOR));
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nv.getVisualProperty(NODE_SHAPE));
		assertEquals(255, nv.getVisualProperty(NODE_TRANSPARENCY).intValue());
		assertEquals(new Double(0.0d), nv.getVisualProperty(NODE_BORDER_WIDTH));
		assertEquals(LineTypeVisualProperty.SOLID, nv.getVisualProperty(NODE_BORDER_LINE_TYPE));
		assertEquals(new Color(0,0,0), nv.getVisualProperty(NODE_BORDER_PAINT));
		assertEquals(255, nv.getVisualProperty(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals(160, nv.getVisualProperty(NODE_LABEL_TRANSPARENCY).intValue());
		assertEquals(Font.decode("Courier 10 Pitch Bold-PLAIN-18"), nv.getVisualProperty(NODE_LABEL_FONT_FACE));
		assertEquals(18, nv.getVisualProperty(NODE_LABEL_FONT_SIZE).intValue());
		// This node's label (given by the passthrough mapping)
		assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(NODE_LABEL));
		
		// All edges have the same visual properties
		final View<CyEdge> ev = view.getEdgeView(view.getModel().getEdgeList().iterator().next());
		assertEquals(12, ev.getVisualProperty(EDGE_WIDTH).intValue());
		assertEquals(new Color(204,204,204), ev.getVisualProperty(EDGE_UNSELECTED_PAINT));
		assertEquals(255, ev.getVisualProperty(EDGE_TRANSPARENCY).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, ev.getVisualProperty(EDGE_LINE_TYPE));
		assertEquals(Font.decode("SanSerif-PLAIN-10"), ev.getVisualProperty(EDGE_LABEL_FONT_FACE));
		assertEquals(10, ev.getVisualProperty(EDGE_LABEL_FONT_SIZE).intValue());
		assertEquals(190, ev.getVisualProperty(EDGE_LABEL_TRANSPARENCY).intValue());
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
}
