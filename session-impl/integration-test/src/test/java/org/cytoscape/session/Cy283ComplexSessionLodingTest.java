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
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
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

		// Current network and view
		final CyNetwork curNet = getNetworkByName(NET4);
		assertEquals(curNet, applicationManager.getCurrentNetwork());
		assertEquals(curNet, applicationManager.getCurrentNetworkView().getModel());
		
		// Visual Style
		assertEquals(7, vmm.getAllVisualStyles().size());
		checkCurrentVisualStyle(vmm.getCurrentVisualStyle());
	}

	private void checkNetworks() {
		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Set<CyRootNetwork> rootNetworks = new HashSet<CyRootNetwork>();

		for (final CyNetwork net : networks) {
			// Non-default columns should not be immutable
			assertCy2CustomColumnsAreMutable(net);
			
			if (net instanceof CySubNetwork)
				rootNetworks.add(((CySubNetwork)net).getRootNetwork());
			
			// Pick specific network.
			final String networkName = net.getRow(net).get(CyNetwork.NAME, String.class);
			final Collection<CyNetworkView> networkViews = viewManager.getNetworkViews(net);
			final CyNetworkView view = networkViews.isEmpty() ? null : networkViews.iterator().next();
			
			if (view != null) {
				// Check updated view
				final VisualStyle style = vmm.getVisualStyle(view);
				style.apply(view);
			}
			
			if (networkName.equals(NET1)) {
				checkNodeEdgeCount(net, 419, 1089, 0, 0);
				assertTrue(viewManager.viewExists(net));
				assertEquals("Nested Network Style", vmm.getVisualStyle(view).getTitle());
				checkNetworkVisualProperties(view, NET1, Color.WHITE,
											 862d, 745d, 
											 527.5044028083109d, 1385.3524750425781d,
											 0.3140175756616861d);
			} else if (networkName.equals(NET2)) {
				checkNodeEdgeCount(net, 26, 46, 0, 0);
				assertTrue(viewManager.viewExists(net));
				assertEquals("CustomGraphicsStyle", vmm.getVisualStyle(view).getTitle());
				checkNetworkVisualProperties(view, NET2, Color.WHITE,
						 					 491d, 372d, 
						 					 191.41490960121155d, 1274.3538818359375d,
						 					 1.1192138366786508d);
			} else if (networkName.equals(NET3)) {
				checkNodeEdgeCount(net, 7, 7, 0, 0);
				assertTrue(viewManager.viewExists(net));
				assertEquals("CustomGraphicsStyle", vmm.getVisualStyle(view).getTitle());
				checkNetworkVisualProperties(view, NET3, Color.WHITE,
	 					 					 491d, 372d, 
	 					 					 155.62810531258583d, 1309.49365234375d,
	 					 					 0.5725287735780972d);
			} else if (networkName.equals(NET4)) {
				checkNodeEdgeCount(net, 331, 362, 82, 110);
				assertTrue(viewManager.viewExists(net));
				checkNetworkVisualProperties(view, NET4, Color.WHITE,
	 					 					 491d, 372d, 
	 					 					 1318.6220703125d, 1844.006591796875d,
	 					 					 0.0874460426896186d);
				assertEquals("Sample1", vmm.getVisualStyle(view).getTitle());
				checkGalFilteredView(view);
			} else if (networkName.equals(NET5)) {
				checkNodeEdgeCount(net, 82, 94, 0, 0);
				assertFalse(viewManager.viewExists(net)); // No view!
			}
		}
		
		for (CyRootNetwork rootNet : rootNetworks)
			assertCy2CustomColumnsAreMutable(rootNet);
	}

	@SuppressWarnings("unchecked")
	private void checkCurrentVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals("Sample1", style.getTitle());

		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		// Remember that the former "edgeColorCalculator" generates 2 mappings in Cy3
		// (for EDGE_UNSELECTED_PAINT and EDGE_STROKE_UNSELECTED_PAINT):
		assertEquals(5, mappings.size());

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
		VisualMappingFunction<?, Paint> edgeColorMapping1 = style.getVisualMappingFunction(EDGE_UNSELECTED_PAINT);
		assertTrue(edgeColorMapping1 instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeColorMapping1.getMappingColumnName());
		assertEquals(String.class, edgeColorMapping1.getMappingColumnType());
		DiscreteMapping<String, Paint> disc1 = (DiscreteMapping<String, Paint>) edgeColorMapping1;
		assertEquals(new Color(0, 204, 0), disc1.getMapValue("pp"));
		assertEquals(new Color(255, 0, 51), disc1.getMapValue("pd"));
		assertEquals(null, disc1.getMapValue("this is an invalid value"));
		
		VisualMappingFunction<?, Paint> edgeColorMapping2 = style.getVisualMappingFunction(EDGE_STROKE_UNSELECTED_PAINT);
		assertTrue(edgeColorMapping2 instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeColorMapping2.getMappingColumnName());
		assertEquals(String.class, edgeColorMapping2.getMappingColumnType());

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
	}
	
	private void checkGalFilteredView(final CyNetworkView view) {
		// Test a node view that has no locked visual properties
		View<CyNode> nv = view.getNodeView(getNodeByName(view.getModel(), "YOR264W"));
		assertEquals(40, nv.getVisualProperty(NODE_SIZE).intValue());
		assertEquals(new Color(204,204,255), nv.getVisualProperty(NODE_FILL_COLOR));
		assertEquals(NodeShapeVisualProperty.ELLIPSE, nv.getVisualProperty(NODE_SHAPE));
		assertEquals(255, nv.getVisualProperty(NODE_TRANSPARENCY).intValue());
		assertEquals(new Double(0.0d), nv.getVisualProperty(NODE_BORDER_WIDTH));
		assertEquals(LineTypeVisualProperty.SOLID, nv.getVisualProperty(NODE_BORDER_LINE_TYPE));
		assertEquals(new Color(0,0,0), nv.getVisualProperty(NODE_BORDER_PAINT));
		assertEquals(255, nv.getVisualProperty(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals(255, nv.getVisualProperty(NODE_LABEL_TRANSPARENCY).intValue());
		assertEquals(Font.decode("Dialog-BOLD-12"), nv.getVisualProperty(NODE_LABEL_FONT_FACE));
		assertEquals(12, nv.getVisualProperty(NODE_LABEL_FONT_SIZE).intValue());
		assertEquals(Boolean.TRUE, nv.getVisualProperty(NODE_NESTED_NETWORK_IMAGE_VISIBLE));
		// Node label (passthrough mapping)
		assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(NODE_LABEL));

		// Test an edge view that has no locked visual properties
		View<CyEdge> ev = view.getEdgeView(getEdgeByName(view.getModel(), "YGL013C (pd) YJL219W"));
		assertEquals(1, ev.getVisualProperty(EDGE_WIDTH).intValue());
		assertEquals(new Color(255,0,51), ev.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT)); // From discrete mapping
		assertEquals(LineTypeVisualProperty.LONG_DASH, ev.getVisualProperty(EDGE_LINE_TYPE)); // From discrete mapping
		assertEquals(255, ev.getVisualProperty(EDGE_TRANSPARENCY).intValue());
		assertEquals(Font.decode("Default-PLAIN-10"), ev.getVisualProperty(EDGE_LABEL_FONT_FACE));
		assertEquals(10, ev.getVisualProperty(EDGE_LABEL_FONT_SIZE).intValue());
		assertEquals(255, ev.getVisualProperty(EDGE_LABEL_TRANSPARENCY).intValue());
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		// Edge label (passthrough mapping)
		assertEquals(view.getModel().getRow(ev.getModel()).get(CyEdge.INTERACTION, String.class), ev.getVisualProperty(EDGE_LABEL));
		
		// This edge has a different color and line style, because of thee mappings
		ev = view.getEdgeView(getEdgeByName(view.getModel(), "YHR171W (pp) YNR007C"));
		assertEquals(new Color(0,204,0), ev.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(LineTypeVisualProperty.SOLID, ev.getVisualProperty(EDGE_LINE_TYPE));
		
		// Test a node and an edge with some locked properties
		nv = view.getNodeView(getNodeByName(view.getModel(), "YBR217W"));
		assertEquals(new Color(255,0,0), nv.getVisualProperty(NODE_FILL_COLOR));
		
		ev = view.getEdgeView(getEdgeByName(view.getModel(), "YPL149W (pp) YBR217W"));
		assertEquals(10, ev.getVisualProperty(EDGE_WIDTH).intValue());
	}
}
