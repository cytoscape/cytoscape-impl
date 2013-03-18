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

import static org.cytoscape.model.CyNetwork.DEFAULT_ATTRS;
import static org.cytoscape.model.CyNetwork.HIDDEN_ATTRS;
import static org.cytoscape.model.CyNetwork.LOCAL_ATTRS;
import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.CyNetwork.SELECTED;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_ATTRS;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_DEFAULT_ATTRS;
import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
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

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy3SimpleSessionLodingTest extends BasicIntegrationTest {

	private static final int NODE_COUNT = 3;
	private static final int EDGE_COUNT = 2;

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session3x/", "simpleSession.cys");
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
		
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Iterator<CyNetwork> itr = networks.iterator();
		CyNetwork net = itr.next();
		
		checkNetwork(net);
		checkRootNetwork(((CySubNetwork) net).getRootNetwork());
	}
	
	private void checkGlobalStatus() {
		assertEquals(1, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());
		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());
		// 3 public tables per subnetwork
		assertEquals(3, tableManager.getAllTables(false).size());
		// At least root+base-network; there can be other (private) networks
		final int totalNet = networkTableManager.getNetworkSet().size();
		assertTrue(totalNet >= 2);
		
		for (CyNetwork net : networkTableManager.getNetworkSet())
			checkNetworkTables(net);
	}
	
	private void checkNetwork(final CyNetwork net) {
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		
		assertEquals(NODE_COUNT, net.getNodeCount());
		assertEquals(EDGE_COUNT, net.getEdgeCount());
		
		// Network attributes
		assertEquals("Na", net.getDefaultNetworkTable().getRow(net.getSUID()).get(NAME, String.class));
		assertEquals("Na", net.getTable(CyNetwork.class, LOCAL_ATTRS).getRow(net.getSUID()).get(NAME, String.class));
		
		// Selection state
		Collection<CyRow> selectedNodes = net.getDefaultNodeTable().getMatchingRows(SELECTED, true);
		Collection<CyRow> selectedEdges = net.getDefaultEdgeTable().getMatchingRows(SELECTED, true);
		assertEquals(2, selectedNodes.size());
		assertEquals(1, selectedEdges.size());
		
		for (CyRow row : selectedNodes) {
			String name = row.get(NAME, String.class);
			Boolean selected = row.get(SELECTED, Boolean.class);
			assertEquals(name.equals("Node 1") || name.equals("Node 2"), selected);
		}
		for (CyRow row : selectedEdges) {
			String name = row.get(NAME, String.class);
			Boolean selected = row.get(SELECTED, Boolean.class);
			assertEquals(name.equals("Node 1 (interaction) Node 2"), selected);
		}
		
		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(net);
		assertEquals(1, views.size());
		
		final CyNetworkView view = views.iterator().next();
		assertEquals(3, view.getNodeViews().size());
		assertEquals(2, view.getEdgeViews().size());
		
		// Visual Style
		assertEquals(9, vmm.getAllVisualStyles().size());
		final VisualStyle style = vmm.getVisualStyle(view);
		checkVisualStyle(style);
		
		checkView(view);
		
		// TODO test custom assigned table (e.g. created by a plugin)
		assertTrue(net.getTable(CyNetwork.class, DEFAULT_ATTRS).isPublic());
		assertTrue(net.getTable(CyNode.class, DEFAULT_ATTRS).isPublic());
		assertTrue(net.getTable(CyEdge.class, DEFAULT_ATTRS).isPublic());
	}
	
	private void checkView(final CyNetworkView view) {
		checkNetworkVisualProperties(view, "Na", Color.WHITE, 745d, 244d, 0d, 0d, 1.6067932272185372d);
		
		// Default values
		View<CyNode> nv = view.getNodeView(getNodeByName(view.getModel(), "Node 1"));
		assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, nv.getVisualProperty(BasicVisualLexicon.NODE_SHAPE));
		assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY));
		assertEquals(Double.valueOf(3.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY));
		assertEquals(new Color(0x333333), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT));
		assertEquals(Double.valueOf(70.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
		assertEquals(Double.valueOf(40.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
		assertEquals(new Color(0x00acad), nv.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR));
		assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL));
		assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY));
		assertEquals(new Color(0x000000), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
		assertEquals(Integer.valueOf(12), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		
		View<CyEdge> ev = view.getEdgeView(getEdgeByName(view.getModel(), "Node 1 (interaction) Node 2"));
		assertEquals(Double.valueOf(2.0d), ev.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH));
		assertEquals(new Color(0x333333), ev.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Integer.valueOf(255), ev.getVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY));
		assertEquals("", ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL));
		assertEquals(Integer.valueOf(255), ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY));
		assertEquals(new Color(0x000000), ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_COLOR));
		assertEquals(Integer.valueOf(10), ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE));
		
		// Bypass
		nv = view.getNodeView(getNodeByName(view.getModel(), "Node 3"));
		assertEquals(NodeShapeVisualProperty.TRIANGLE, nv.getVisualProperty(BasicVisualLexicon.NODE_SHAPE));
		assertEquals(Integer.valueOf(80), nv.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY));
		assertEquals(Double.valueOf(5.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(Integer.valueOf(150), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY));
		assertEquals(new Color(0x000099), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT));
		assertEquals(new Color(0x0099ff), nv.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR));
		assertEquals("Node 3 (BYPASS)", nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL));
		assertEquals(new Color(0x9900ff), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
		assertEquals(Integer.valueOf(120), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY));
		assertEquals(Integer.valueOf(16), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		
		ev = view.getEdgeView(getEdgeByName(view.getModel(), "Node 2 (interaction) Node 3"));
		assertEquals(Double.valueOf(5.0d), ev.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH));
		assertEquals(new Color(0xff6699), ev.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Integer.valueOf(95), ev.getVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY));
		assertEquals("2::3 (BYPASS)", ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL));
		assertEquals(Integer.valueOf(100), ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY));
		assertEquals(new Color(0xff0099), ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_COLOR));
		assertEquals(Integer.valueOf(8), ev.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE));
	}
	
	private void checkRootNetwork(final CyRootNetwork net) {
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		
		assertNotNull(net.getTable(CyNetwork.class, DEFAULT_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, LOCAL_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, HIDDEN_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, SHARED_ATTRS));
		
		Set<CyTable> allTables = tableManager.getAllTables(true);
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, DEFAULT_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, LOCAL_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, HIDDEN_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, SHARED_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNode.class, SHARED_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyEdge.class, SHARED_ATTRS)));
	}
	
	private void checkVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals(vmm.getDefaultVisualStyle(), style);
		
		assertEquals(3, style.getAllVisualPropertyDependencies().size());
		
		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(1, mappings.size());
		
		VisualMappingFunction<?, ?> labelMapping = mappings.iterator().next();
		assertTrue(labelMapping instanceof PassthroughMapping);
		assertEquals(BasicVisualLexicon.NODE_LABEL, labelMapping.getVisualProperty());
		assertEquals(NAME, labelMapping.getMappingColumnName());
		assertEquals(String.class, labelMapping.getMappingColumnType());
	}
	
	private void checkNetworkTables(final CyNetwork net) {
		Map<String, CyTable> tables = networkTableManager.getTables(net, CyNetwork.class);
		
		for (Map.Entry<String, CyTable> entry : tables.entrySet()) {
			String namespace = entry.getKey();
			CyTable tbl = entry.getValue();
			
			if (namespace.equals(LOCAL_ATTRS) || namespace.equals(SHARED_ATTRS) || namespace.equals(HIDDEN_ATTRS))
				assertEquals(SavePolicy.SESSION_FILE, tbl.getSavePolicy());
			else
				assertEquals(namespace + " should have DO_NOT_SAVE policy", SavePolicy.DO_NOT_SAVE, tbl.getSavePolicy());
		}
		
		assertTrue(tables.containsValue(net.getTable(CyNetwork.class, DEFAULT_ATTRS)));
		assertTrue(tables.containsValue(net.getTable(CyNetwork.class, LOCAL_ATTRS)));
		assertTrue(tables.containsValue(net.getTable(CyNetwork.class, HIDDEN_ATTRS)));
		// These tables are always private
		assertFalse(net.getTable(CyNetwork.class, LOCAL_ATTRS).isPublic());
		assertFalse(net.getTable(CyNetwork.class, HIDDEN_ATTRS).isPublic());
		assertEquals(1, net.getTable(CyNetwork.class, DEFAULT_ATTRS).getAllRows().size());
		
		if (net instanceof CyRootNetwork) {
			assertTrue(tables.containsValue(net.getTable(CyNetwork.class, SHARED_ATTRS)));
			assertTrue(tables.containsValue(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS)));
			assertFalse(net.getTable(CyNetwork.class, SHARED_ATTRS).isPublic());
			assertFalse(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS).isPublic());
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, SHARED_ATTRS).getAllRows().size());
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, SHARED_DEFAULT_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, SHARED_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, SHARED_DEFAULT_ATTRS).getAllRows().size());
		} else {
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, LOCAL_ATTRS).getAllRows().size());
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, DEFAULT_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, LOCAL_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, DEFAULT_ATTRS).getAllRows().size());
		}
		
		Map<String, CyTable> nodeTables = networkTableManager.getTables(net, CyNode.class);
		assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, DEFAULT_ATTRS)));
		assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, LOCAL_ATTRS)));
		assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, HIDDEN_ATTRS)));
		assertFalse(net.getTable(CyNode.class, LOCAL_ATTRS).isPublic());
		assertFalse(net.getTable(CyNode.class, HIDDEN_ATTRS).isPublic());
		
		if (net instanceof CyRootNetwork) {
			assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, SHARED_ATTRS)));
			assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, SHARED_DEFAULT_ATTRS)));
			assertFalse(net.getTable(CyNode.class, SHARED_ATTRS).isPublic());
			assertFalse(net.getTable(CyNode.class, SHARED_DEFAULT_ATTRS).isPublic());
		}
		
		Map<String, CyTable> edgeTables = networkTableManager.getTables(net, CyEdge.class);
		assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, DEFAULT_ATTRS)));
		assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, LOCAL_ATTRS)));
		assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, HIDDEN_ATTRS)));
		assertFalse(net.getTable(CyEdge.class, LOCAL_ATTRS).isPublic());
		assertFalse(net.getTable(CyEdge.class, HIDDEN_ATTRS).isPublic());
		
		if (net instanceof CyRootNetwork) {
			assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, SHARED_ATTRS)));
			assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, SHARED_DEFAULT_ATTRS)));
			assertFalse(net.getTable(CyEdge.class, SHARED_ATTRS).isPublic());
			assertFalse(net.getTable(CyEdge.class, SHARED_DEFAULT_ATTRS).isPublic());
		}
	}
}
