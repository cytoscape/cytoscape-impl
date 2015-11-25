package org.cytoscape.io.internal.read.gml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;

import org.cytoscape.io.internal.read.AbstractNetworkReaderTest;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GMLNetworkReaderTest extends AbstractNetworkReaderTest {
	
	@Mock private RenderingEngineManager renderingEngineManager;
	private VisualLexicon lexicon;
	private UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);
		
		lexicon = new BasicVisualLexicon(new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property"));
		when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(lexicon);
		
		TableTestSupport tblTestSupport = new TableTestSupport();
		CyTableFactory tableFactory = tblTestSupport.getTableFactory();
		CyTableManager tableMgr = mock(CyTableManager.class);
		
		unrecognizedVisualPropertyMgr = new UnrecognizedVisualPropertyManager(tableFactory, tableMgr);
	}
	
	@Test
	public void testReadGml() throws Exception {
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/example1.gml");
		final CyNetwork[] networks = reader.getNetworks();
		final CyNetworkView[] networkViews = new CyNetworkView[networks.length];
		int i = 0;
		
		for (CyNetwork net: networks) {
			networkViews[i] = reader.buildCyNetworkView(net);
			i++;
		}
		
		assertEquals(1, networkViews.length);
		
		final CyNetworkView view = networkViews[0];
		assertNotNull(view);
		
		final CyNetwork net = view.getModel();
		assertNotNull(net);
		
		assertEquals(3, net.getNodeCount());
		assertEquals(3, net.getEdgeCount());
	}
	
	@Test
	public void testReadGmlAttributes() throws Exception {
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/example2.gml");
		final CyNetwork[] networks = reader.getNetworks();
		final CyNetworkView[] networkViews = new CyNetworkView[networks.length];
		int i = 0;
		
		for (CyNetwork net: networks) {
			networkViews[i] = reader.buildCyNetworkView(net);
			i++;
		}
		
		assertEquals(1, networkViews.length);
		
		final CyNetworkView view = networkViews[0];
		final CyNetwork net = view.getModel();
		
		assertEquals(2, net.getNodeCount());
		assertEquals(1, net.getEdgeCount());
		
		final CyNode n1 = getNodeByName(net, "node1");
		final CyNode n2 = getNodeByName(net, "node2");
		final CyEdge e = getEdgeByName(net, "node1 (pp) node2");
		assertNotNull(n1);
		assertNotNull(n2);
		assertNotNull(e);
		
		// CyTable Data:
		final CyRootNetwork rootNet = ((CySubNetwork) net).getRootNetwork();
		CyRow row = rootNet.getRow(n1, CyRootNetwork.SHARED_ATTRS);
		
		assertEquals(new Integer(0), row.get("att1", Integer.class));
		assertEquals(new Double(0.0), row.get("att2", Double.class));
		assertEquals("", row.get("att3", String.class));
		
		row = rootNet.getRow(n2, CyRootNetwork.SHARED_ATTRS);
		
		assertEquals(new Integer(10), row.get("att1", Integer.class));
		assertEquals(new Double(10.1), row.get("att2", Double.class));
		assertEquals("MyString", row.get("att3", String.class));
		
		row = rootNet.getRow(e, CyRootNetwork.SHARED_ATTRS);
		
		assertEquals(new Integer(2), row.get("att4", Integer.class));
		assertEquals(new Double(2.2), row.get("att5", Double.class));
		assertEquals("Another string...", row.get("att6", String.class));
		
		// Visual Properties:
		final View<CyNode> nv1 = view.getNodeView(n1);
		final View<CyNode> nv2 = view.getNodeView(n2);
		final View<CyEdge> ev = view.getEdgeView(e);
		assertNotNull(nv1);
		assertNotNull(nv2);
		assertNotNull(ev);
		
		assertEquals("My GML Network", view.getVisualProperty(NETWORK_TITLE));
		
		assertEquals(64.0, nv1.getVisualProperty(NODE_WIDTH), 0.0);
		assertEquals(32.0, nv1.getVisualProperty(NODE_HEIGHT), 0.0);
		assertEquals(-66.0, nv1.getVisualProperty(NODE_X_LOCATION), 0.0);
		assertEquals(-71.0, nv1.getVisualProperty(NODE_Y_LOCATION), 0.0);
		assertEquals(Color.decode("#ffcccc"), nv1.getVisualProperty(NODE_FILL_COLOR));
		assertEquals(Color.decode("#ff6666"), nv1.getVisualProperty(NODE_BORDER_PAINT));
		assertEquals(3.0, nv1.getVisualProperty(NODE_BORDER_WIDTH), 0.0);
		assertEquals(NodeShapeVisualProperty.TRIANGLE, nv1.getVisualProperty(NODE_SHAPE));
		
		assertEquals(40.0, nv2.getVisualProperty(NODE_WIDTH), 0.0);
		assertEquals(39.999, nv2.getVisualProperty(NODE_HEIGHT), 0.001);
		assertEquals(60.5289, nv2.getVisualProperty(NODE_X_LOCATION), 0.0001);
		assertEquals(77.4868, nv2.getVisualProperty(NODE_Y_LOCATION), 0.0001);
		assertEquals(Color.decode("#ffffcc"), nv2.getVisualProperty(NODE_FILL_COLOR));
		assertEquals(Color.decode("#999900"), nv2.getVisualProperty(NODE_BORDER_PAINT));
		assertEquals(4.0, nv2.getVisualProperty(NODE_BORDER_WIDTH), 0.0);
		assertEquals(NodeShapeVisualProperty.OCTAGON, nv2.getVisualProperty(NODE_SHAPE));

		assertEquals(4.0, ev.getVisualProperty(EDGE_WIDTH), 0.0);
		assertEquals(Color.decode("#660066"), ev.getVisualProperty(EDGE_UNSELECTED_PAINT));
		assertEquals(Color.decode("#660066"), ev.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(LineTypeVisualProperty.SOLID, ev.getVisualProperty(EDGE_LINE_TYPE));
		assertEquals(ArrowShapeVisualProperty.CIRCLE, ev.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.DIAMOND, ev.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
	
	@Test
	public void testReadYEdArrows() throws Exception {
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/yed_arrows.gml");
		
		final CyNetwork[] networks = reader.getNetworks();
		assertEquals(1, networks.length);
		
		final CyNetwork net = networks[0];
		final CyNetworkView view = reader.buildCyNetworkView(net);
		
		final CyEdge e1 = getEdgeByName(net, "n1 () n2");
		final CyEdge e2 = getEdgeByName(net, "n2 () n3");
		final CyEdge e3 = getEdgeByName(net, "n3 () n1");
		final CyEdge e4 = getEdgeByName(net, "n3 () n4");
		
		// Test arrows as specified here: http://docs.yworks.com/yfiles/doc/developers-guide/gml.html
		final View<CyEdge> ev1 = view.getEdgeView(e1);
		assertEquals(ArrowShapeVisualProperty.NONE, ev1.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.ARROW, ev1.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev2 = view.getEdgeView(e2);
		assertEquals(ArrowShapeVisualProperty.DELTA, ev2.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev2.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev3 = view.getEdgeView(e3);
		assertEquals(ArrowShapeVisualProperty.DIAMOND, ev3.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.CIRCLE, ev3.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev4 = view.getEdgeView(e4);
		assertEquals(ArrowShapeVisualProperty.NONE, ev4.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev4.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
	
	@Test
	public void testReadCy2Arrows() throws Exception {
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/cy2_arrows.gml");
		
		final CyNetwork[] networks = reader.getNetworks();
		assertEquals(1, networks.length);
		
		final CyNetwork net = networks[0];
		final CyNetworkView view = reader.buildCyNetworkView(net);
		
		final CyEdge e1 = getEdgeByName(net, "n1 () n2");
		final CyEdge e2 = getEdgeByName(net, "n2 () n3");
		final CyEdge e3 = getEdgeByName(net, "n3 () n1");
		final CyEdge e4 = getEdgeByName(net, "n3 () n4");
		final CyEdge e5 = getEdgeByName(net, "n1 (DirectedEdge) n4");
		final CyEdge e6 = getEdgeByName(net, "n2 (DirectedEdge) n4");
		
		// Test arrows as specified here: http://docs.yworks.com/yfiles/doc/developers-guide/gml.html
		final View<CyEdge> ev1 = view.getEdgeView(e1);
		assertEquals(ArrowShapeVisualProperty.NONE, ev1.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.ARROW, ev1.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev2 = view.getEdgeView(e2);
		assertEquals(ArrowShapeVisualProperty.DELTA, ev2.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev2.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev3 = view.getEdgeView(e3);
		assertEquals(ArrowShapeVisualProperty.DIAMOND, ev3.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.CIRCLE, ev3.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev4 = view.getEdgeView(e4);
		assertEquals(ArrowShapeVisualProperty.NONE, ev4.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev4.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev5 = view.getEdgeView(e5);
		assertEquals(ArrowShapeVisualProperty.T, ev5.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev5.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev6 = view.getEdgeView(e6);
		assertEquals(ArrowShapeVisualProperty.HALF_BOTTOM, ev6.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.HALF_TOP, ev6.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
	
	@Test
	public void testReadOldGalFilteredArrows() throws Exception {
		// This file is distributed in the sampleData folder of previous Cy2 versions
		// and uses only the attribute key "arrow" (not "sourceArrow" or "source_Arrow", as yEd and Cy3)
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/galFiltered_old.gml");
		
		final CyNetwork[] networks = reader.getNetworks();
		assertEquals(1, networks.length);
		
		final CyNetwork net = networks[0];
		final CyNetworkView view = reader.buildCyNetworkView(net);
		
		final CyEdge e1 = getEdgeByName(net, "YPR124W (pd) YMR021C");
		final CyEdge e2 = getEdgeByName(net, "YGR074W (pp) YBR043C");
		
		final View<CyEdge> ev1 = view.getEdgeView(e1);
		assertEquals(ArrowShapeVisualProperty.NONE, ev1.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.ARROW, ev1.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
		
		final View<CyEdge> ev2 = view.getEdgeView(e2);
		assertEquals(ArrowShapeVisualProperty.NONE, ev2.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE));
		assertEquals(ArrowShapeVisualProperty.NONE, ev2.getVisualProperty(EDGE_TARGET_ARROW_SHAPE));
	}
	
	@Test
	public void testDirectedGraph() throws Exception {
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/example-directed1.gml");
		final CyNetwork[] networks = reader.getNetworks();
		assertEquals(1, networks.length);
		final CyNetwork net = networks[0];
		assertEquals(3, net.getEdgeCount());
		
		CyEdge e1 = getEdgeByName(net, "1 (interacts with) 2");
		CyEdge e2 = getEdgeByName(net, "1 (interacts with) 3");
		CyEdge e3 = getEdgeByName(net, "2 (interacts with) 3");
		
		assertFalse(e1.isDirected());
		assertTrue(e2.isDirected());
		assertTrue(e3.isDirected());
	}
	
	@Test
	public void testUndirectedGraph() throws Exception {
		final GMLNetworkReader reader = readGML("src/test/resources/testData/gml/example-directed2.gml");
		final CyNetwork[] networks = reader.getNetworks();
		assertEquals(1, networks.length);
		final CyNetwork net = networks[0];
		assertEquals(3, net.getEdgeCount());
		
		CyEdge e1 = getEdgeByName(net, "1 (interacts with) 2");
		CyEdge e2 = getEdgeByName(net, "1 (interacts with) 3");
		CyEdge e3 = getEdgeByName(net, "2 (interacts with) 3");
		
		assertFalse(e1.isDirected());
		assertTrue(e2.isDirected());
		assertFalse(e3.isDirected());
	}
	
	
	private GMLNetworkReader readGML(final String filename) throws Exception {
		final File file = new File(filename);
		GMLNetworkReader reader = new GMLNetworkReader(new FileInputStream(file), applicationManager, netFactory,
													   renderingEngineManager, unrecognizedVisualPropertyMgr, 
													   networkManager, rootNetworkManager);
		reader.run(taskMonitor);
		
		return reader;
	}
}
