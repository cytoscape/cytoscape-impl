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
import java.awt.Paint;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

/**
 * Test for sample file in Cytoscape 2.5.2 distribution
 */
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy252SimpleSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session2x/", "v252Session.cys");
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
		// 3 public tables per registered subnetwork
		assertEquals(3, tableManager.getAllTables(false).size());
		// 15 regular tables + 9 table facades (sub+root-networks)
		assertEquals(24, tableManager.getAllTables(true).size());
		
		// Current network and view
		final CyNetwork curNet = getNetworkByName("galFiltered.sif");
		assertEquals(curNet, applicationManager.getCurrentNetwork());
		assertEquals(curNet, applicationManager.getCurrentNetworkView().getModel());
		
		// Visual Style
		assertEquals(5, vmm.getAllVisualStyles().size());
		checkCurrentVisualStyle(vmm.getCurrentVisualStyle());
	}

	private void checkNetwork(final CyNetwork network) {
		checkNodeEdgeCount(network, 331, 362, 0, 0);

		// Non-default columns should not be immutable
		assertCy2CustomColumnsAreMutable(network);

		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		assertEquals(1, views.size());

		final CyNetworkView view = views.iterator().next();
		assertEquals(331, view.getNodeViews().size());
		assertEquals(362, view.getEdgeViews().size());

		// Check updated view
		final VisualStyle style = vmm.getVisualStyle(view);
		style.apply(view);
		checkView(view);
	}

	@SuppressWarnings("unchecked")
	private void checkCurrentVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals("Sample3", style.getTitle());

		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		// Remember that the former "edgeColorCalculator" generates 2 mappings in Cy3
		// (for EDGE_UNSELECTED_PAINT and EDGE_STROKE_UNSELECTED_PAINT):
		assertEquals(7, mappings.size());

		// Test defaults
		NodeShape defaultShape = style.getDefaultValue(NODE_SHAPE);
		Paint nodeColor = style.getDefaultValue(NODE_FILL_COLOR);
		Integer fontSize = style.getDefaultValue(NODE_LABEL_FONT_SIZE);
		Integer transparency = style.getDefaultValue(NODE_TRANSPARENCY);
		Double w = style.getDefaultValue(NODE_WIDTH);
		Double h = style.getDefaultValue(NODE_HEIGHT);
		
		Paint edgeLabelColor = style.getDefaultValue(EDGE_LABEL_COLOR);
		assertEquals(new Color(255,255,204), edgeLabelColor);

		assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, defaultShape);
		assertEquals(Color.WHITE, nodeColor);
		assertEquals(Integer.valueOf(16), fontSize);
		assertEquals(Integer.valueOf(180), transparency);
		assertEquals(Double.valueOf(80), w);
		assertEquals(Double.valueOf(30), h);

		// Check each mapping
		VisualMappingFunction<?, String> nodeLabelMapping = style.getVisualMappingFunction(NODE_LABEL);
		VisualMappingFunction<?, String> edgeLabelMapping = style.getVisualMappingFunction(EDGE_LABEL);

		assertTrue(nodeLabelMapping instanceof PassthroughMapping);
		assertTrue(edgeLabelMapping instanceof PassthroughMapping);

		assertEquals(CyNetwork.NAME, nodeLabelMapping.getMappingColumnName());
		assertEquals(CyEdge.INTERACTION, edgeLabelMapping.getMappingColumnName());
		assertEquals(String.class, nodeLabelMapping.getMappingColumnType());
		assertEquals(String.class, edgeLabelMapping.getMappingColumnType());

		// Node Color mapping
		VisualMappingFunction<?, Paint> nodeColorMapping = style.getVisualMappingFunction(NODE_FILL_COLOR);
		assertTrue(nodeColorMapping instanceof ContinuousMapping);
		assertEquals("gal4RGexp", nodeColorMapping.getMappingColumnName());
		assertEquals(Number.class, nodeColorMapping.getMappingColumnType());

		// Edge Color mapping
		VisualMappingFunction<?, Paint> edgeColorMapping1 = style.getVisualMappingFunction(EDGE_UNSELECTED_PAINT);
		assertTrue(edgeColorMapping1 instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeColorMapping1.getMappingColumnName());
		assertEquals(String.class, edgeColorMapping1.getMappingColumnType());
		DiscreteMapping<String, Paint> disc1 = (DiscreteMapping<String, Paint>) edgeColorMapping1;
		assertEquals(Color.WHITE, disc1.getMapValue("pp"));
		assertEquals(new Color(102, 255, 255), disc1.getMapValue("pd"));
		assertEquals(null, disc1.getMapValue("this is an invalid value"));
		
		VisualMappingFunction<?, Paint> edgeColorMapping2 = style.getVisualMappingFunction(EDGE_STROKE_UNSELECTED_PAINT);
		assertTrue(edgeColorMapping2 instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeColorMapping2.getMappingColumnName());
		assertEquals(String.class, edgeColorMapping2.getMappingColumnType());
		
		// Numbers as Tooltip 
		VisualMappingFunction<?, String> nodeTooltipMapping = style.getVisualMappingFunction(NODE_TOOLTIP);
		assertTrue(nodeTooltipMapping instanceof PassthroughMapping);
		assertEquals("gal4RGexp", nodeTooltipMapping.getMappingColumnName());
		// Cy2 doesn't write the "controllerType" property for PassThroughMappings,
		// so it's always created as String (it shouldn't cause any issues)
		assertEquals(String.class, nodeTooltipMapping.getMappingColumnType());

		VisualMappingFunction<?, LineType> edgeLineStyleMapping = style.getVisualMappingFunction(EDGE_LINE_TYPE);
		assertTrue(edgeLineStyleMapping instanceof DiscreteMapping);
		assertEquals(CyEdge.INTERACTION, edgeLineStyleMapping.getMappingColumnName());
		assertEquals(String.class, edgeLineStyleMapping.getMappingColumnType());
		DiscreteMapping<String, LineType> disc2 = (DiscreteMapping<String, LineType>) edgeLineStyleMapping;
		assertEquals(LineTypeVisualProperty.SOLID, disc2.getMapValue("pp"));
		assertEquals(LineTypeVisualProperty.LONG_DASH, disc2.getMapValue("pd"));
		assertEquals(null, disc2.getMapValue("this is an invalid value"));
	}

	private void checkView(final CyNetworkView view) {
		assertEquals(Color.BLACK, view.getVisualProperty(NETWORK_BACKGROUND_PAINT));
		assertEquals(new Double(639.0d), view.getVisualProperty(NETWORK_WIDTH));
		assertEquals(new Double(624.0d), view.getVisualProperty(NETWORK_HEIGHT));
		assertEquals(new Double(3091.2991395970175d), view.getVisualProperty(NETWORK_CENTER_X_LOCATION));
		assertEquals(new Double(3610.396738076269d), view.getVisualProperty(NETWORK_CENTER_Y_LOCATION));
		assertEquals(new Double(0.05044042295795177d), view.getVisualProperty(NETWORK_SCALE_FACTOR));
	    
		// All nodes have the same size, border and shape
		final View<CyNode> nv = view.getNodeView(view.getModel().getNodeList().iterator().next());
		assertEquals(80, nv.getVisualProperty(NODE_WIDTH).intValue());
		assertEquals(30, nv.getVisualProperty(NODE_HEIGHT).intValue());
		assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, nv.getVisualProperty(NODE_SHAPE));
		assertEquals(180, nv.getVisualProperty(NODE_TRANSPARENCY).intValue());
		assertEquals(2, nv.getVisualProperty(NODE_BORDER_WIDTH).intValue());
		assertEquals(LineTypeVisualProperty.SOLID, nv.getVisualProperty(NODE_BORDER_LINE_TYPE));
		assertEquals(new Color(153,153,255), nv.getVisualProperty(NODE_BORDER_PAINT));
		assertEquals(255, nv.getVisualProperty(NODE_BORDER_TRANSPARENCY).intValue());
		assertEquals(new Color(255,255,255), nv.getVisualProperty(NODE_LABEL_COLOR));
		assertEquals(255, nv.getVisualProperty(NODE_LABEL_TRANSPARENCY).intValue());
		
		// All edges have the same width and other properties
		final View<CyEdge> ev = view.getEdgeView(view.getModel().getEdgeList().iterator().next());
		assertEquals(new Double(1.0), ev.getVisualProperty(EDGE_WIDTH));
		assertEquals(255, ev.getVisualProperty(EDGE_TRANSPARENCY).intValue());
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
}
