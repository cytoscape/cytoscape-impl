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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
		File file = new File("src/test/resources/testData/gml/example1.gml");
		GMLNetworkReader reader = new GMLNetworkReader(new FileInputStream(file), netFactory, viewFactory,
													   renderingEngineManager, unrecognizedVisualPropertyMgr,
													   networkManager, rootNetworkManager, cyApplicationManager);
		reader.run(taskMonitor);
		
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
		File file = new File("src/test/resources/testData/gml/example2.gml");
		GMLNetworkReader reader = new GMLNetworkReader(new FileInputStream(file), netFactory, viewFactory,
													   renderingEngineManager, unrecognizedVisualPropertyMgr, 
													   networkManager, rootNetworkManager, cyApplicationManager);
		reader.run(taskMonitor);
		
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
}
