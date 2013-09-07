package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DGraphViewApplyTest {

	private DGraphView dgv;

	NetworkViewTestSupport testSupport = new NetworkViewTestSupport();
	// Mocks

	private CyNetwork network;
	private CyRootNetworkManager cyRoot;

	@Mock
	private UndoSupport undo;

	private SpacialIndex2DFactory spacialFactory;
	private VisualLexicon dingLexicon;

	@Mock
	private ViewTaskFactoryListener vtfl;

	@Mock
	private DialogTaskManager manager;
	@Mock
	private CyEventHelper cyEventHelper;
	@Mock
	private AnnotationFactoryManager annMgr;
	@Mock
	private DingGraphLOD dingGraphLOD;

	@Mock
	private VisualMappingManager vmm;

	@Mock
	private CyNetworkViewManager netViewMgr;

	private HandleFactory handleFactory;

	@Mock
	private CyServiceRegistrar registrar;

	@Mock
	private CustomGraphicsManager cgManager;

	// Network contents
	CyNode node1;
	CyNode node2;
	CyNode node3;

	CyEdge edge12;
	CyEdge edge23;
	CyEdge edge13;

	DEdgeView ev1;
	DEdgeView ev2;
	DEdgeView ev3;

	DNodeView nodeView1;
	DNodeView nodeView2;
	DNodeView nodeView3;

	@Before
	public void setUp() throws Exception {
		// Build real DGraphView object.
		MockitoAnnotations.initMocks(this);

		testSupport.getNetworkTableManager();

		network = buildNetworkModel();
		cyRoot = new CyRootNetworkManagerImpl();
		spacialFactory = new RTreeFactory();
		dingLexicon = new DVisualLexicon(cgManager);
		handleFactory = new HandleFactoryImpl();

		dgv = new DGraphView(network, cyRoot, undo, spacialFactory, dingLexicon, vtfl, manager, cyEventHelper, annMgr,
				dingGraphLOD, vmm, netViewMgr, handleFactory, registrar);

		assertNotNull(dgv);
		assertEquals(3, dgv.getModel().getNodeCount());
		assertEquals(3, dgv.getModel().getEdgeCount());

		nodeView1 = dgv.getDNodeView(node1);
		nodeView2 = dgv.getDNodeView(node2);
		nodeView3 = dgv.getDNodeView(node3);

		ev1 = dgv.getDEdgeView(edge12);
		ev2 = dgv.getDEdgeView(edge13);
		ev3 = dgv.getDEdgeView(edge23);
		
	}

	private CyNetwork buildNetworkModel() {
		final CyNetwork net = testSupport.getNetwork();
		node1 = net.addNode();
		node2 = net.addNode();
		node3 = net.addNode();
		net.getRow(node1).set(CyNetwork.NAME, "node1");
		net.getRow(node2).set(CyNetwork.NAME, "node2");
		net.getRow(node3).set(CyNetwork.NAME, "node3");
		edge12 = net.addEdge(node1, node2, true);
		edge23 = net.addEdge(node2, node3, true);
		edge13 = net.addEdge(node1, node3, true);
		net.getRow(edge12).set(CyEdge.INTERACTION, "pp");
		net.getRow(edge13).set(CyEdge.INTERACTION, "pp");
		net.getRow(edge23).set(CyEdge.INTERACTION, "pd");

		return net;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEdgeView() {

		// Transparency Test
		final Color strokeColor = new Color(10, 20, 200);
		final Color targetArrowColor = new Color(222, 100, 30);
		final Color targetArrowColorM1 = Color.red;
		final Color targetArrowColorM2 = Color.magenta;

		dgv.setViewDefault(DVisualLexicon.EDGE_UNSELECTED_PAINT, strokeColor);
		assertEquals(255, ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge12)).getAlpha());
		assertEquals(255, ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge13)).getAlpha());
		assertEquals(255, ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge23)).getAlpha());

		dgv.setViewDefault(DVisualLexicon.EDGE_TRANSPARENCY, 100);

		assertEquals(100, ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge12)).getAlpha());
		assertEquals(100, ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge13)).getAlpha());
		assertEquals(100, ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge23)).getAlpha());

		dgv.setViewDefault(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, targetArrowColor);
		assertEquals(targetArrowColor.getRed(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getRed());
		assertEquals(targetArrowColor.getGreen(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getGreen());
		assertEquals(targetArrowColor.getBlue(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getBlue());

		assertEquals(100, ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getAlpha());
		assertEquals(100, ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge13)).getAlpha());
		assertEquals(100, ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge23)).getAlpha());

		// Create mapping
		ev1.setVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, targetArrowColorM1);
		ev2.setVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, targetArrowColorM2);

		assertEquals(targetArrowColorM1.getRed(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getRed());
		assertEquals(targetArrowColorM1.getGreen(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getGreen());
		assertEquals(targetArrowColorM1.getBlue(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getBlue());
		assertEquals(targetArrowColorM2.getRed(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge13)).getRed());
		assertEquals(targetArrowColorM2.getGreen(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge13)).getGreen());
		assertEquals(targetArrowColorM2.getBlue(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge13)).getBlue());
		assertEquals(targetArrowColor.getRed(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge23)).getRed());
		assertEquals(targetArrowColor.getGreen(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge23)).getGreen());
		assertEquals(targetArrowColor.getBlue(), ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge23)).getBlue());

		assertEquals(100, ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge12)).getAlpha());
		assertEquals(100, ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge13)).getAlpha());
		assertEquals(100, ((Color) dgv.m_edgeDetails.getTargetArrowPaint(edge23)).getAlpha());

		// TODO: Add more tests (mixing defaults and mappings)

	}

	@Test
	public void testNodeView() {
		final Color fillColor = new Color(10, 20, 200);
		final Color borderColor = new Color(222, 100, 30);
		final Color fillColorM1 = Color.red;
		final Color fillColorM2 = Color.magenta;
		final Color labelColor = Color.orange;

		dgv.setViewDefault(DVisualLexicon.NODE_FILL_COLOR, fillColor);
		assertEquals(255, ((Color) dgv.m_nodeDetails.getFillPaint(node1)).getAlpha());
		assertEquals(255, ((Color) dgv.m_nodeDetails.getFillPaint(node2)).getAlpha());
		assertEquals(255, ((Color) dgv.m_nodeDetails.getFillPaint(node3)).getAlpha());

		// Test labels
		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_COLOR, labelColor);
		assertEquals(labelColor.getRed(), ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getRed());
		assertEquals(labelColor.getGreen(), ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getGreen());
		assertEquals(labelColor.getBlue(), ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getBlue());
		assertEquals(255, ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getAlpha());

		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_TRANSPARENCY, 100);
		assertEquals(100, ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getAlpha());
		assertEquals(100, ((Color) dgv.m_nodeDetails.getLabelPaint(node2, 0)).getAlpha());
		assertEquals(100, ((Color) dgv.m_nodeDetails.getLabelPaint(node3, 0)).getAlpha());

		// Test mappings

		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_COLOR, Color.blue);
		assertEquals(Color.blue.getBlue(), ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getBlue());

		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_TRANSPARENCY, 20);
		assertEquals(20, ((Color) dgv.m_nodeDetails.getLabelPaint(node1, 0)).getAlpha());
		assertEquals(100, ((Color) dgv.m_nodeDetails.getLabelPaint(node2, 0)).getAlpha());
		assertEquals(100, ((Color) dgv.m_nodeDetails.getLabelPaint(node3, 0)).getAlpha());

		// Font
		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_FONT_SIZE, 20);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_SIZE, 10);
		nodeView2.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_SIZE, 8);

		assertEquals(10, dgv.m_nodeDetails.getLabelFont(node1, 0).getSize());
		assertEquals(8, dgv.m_nodeDetails.getLabelFont(node2, 0).getSize());
		assertEquals(20, dgv.m_nodeDetails.getLabelFont(node3, 0).getSize());

		final Font defFont = new Font("SansSerif", Font.PLAIN, 22);
		final Font mapFont = new Font("Serif", Font.PLAIN, 40);
		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_FONT_FACE, defFont);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_FACE, mapFont);
		nodeView2.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_FACE, mapFont);

		assertEquals(mapFont.getFontName(), dgv.m_nodeDetails.getLabelFont(node1, 0).getFontName());
		assertEquals(mapFont.getFontName(), dgv.m_nodeDetails.getLabelFont(node2, 0).getFontName());
		assertEquals(defFont.getFontName(), dgv.m_nodeDetails.getLabelFont(node3, 0).getFontName());

	}

	@Test
	public void testDefaultValues() {
		dgv.setViewDefault(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.cyan);
		assertEquals(Color.cyan, ev1.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan, ev2.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan, ev3.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan.getRed(), ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge12)).getRed());
		assertEquals(Color.cyan.getGreen(), ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge12)).getGreen());
		assertEquals(Color.cyan.getBlue(), ((Color) dgv.m_edgeDetails.getUnselectedPaint(edge12)).getBlue());

		ev1.setVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.yellow);
		assertEquals(Color.yellow, ev1.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan, ev2.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan, ev3.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));

		Double defEdgeWidth = 20d;
		dgv.setViewDefault(DVisualLexicon.EDGE_WIDTH, defEdgeWidth);
		assertEquals(defEdgeWidth, ev1.getVisualProperty(DVisualLexicon.EDGE_WIDTH));
		assertEquals(defEdgeWidth, ev2.getVisualProperty(DVisualLexicon.EDGE_WIDTH));
		assertEquals(defEdgeWidth, ev3.getVisualProperty(DVisualLexicon.EDGE_WIDTH));

		dgv.setViewDefault(DVisualLexicon.NODE_FILL_COLOR, Color.magenta);
		assertEquals(Color.magenta, nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR));

		dgv.setViewDefault(DVisualLexicon.NODE_WIDTH, 35d);
		assertTrue(35d == nodeView1.getVisualProperty(DVisualLexicon.NODE_WIDTH));

		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_WIDTH, 8d);
		assertTrue(8d == nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));

		dgv.setViewDefault(DVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.DIAMOND);
		NodeShape returnedShape = nodeView1.getVisualProperty(DVisualLexicon.NODE_SHAPE);
		System.out.println("Shape ==> " + returnedShape);
		assertEquals(NodeShapeVisualProperty.DIAMOND, returnedShape);
	}

	@Test
	public void testNodeBorder() {
		final Double borderW = Double.valueOf(7d);
		final Integer trans = Integer.valueOf(123);
		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_WIDTH, borderW);
		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_PAINT, Color.magenta);
		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_TRANSPARENCY, trans);

		final Color resultColor = new Color(Color.magenta.getRed(), Color.magenta.getGreen(), Color.magenta.getBlue(),
				trans);
		assertEquals(borderW, nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(resultColor, nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT));
		assertEquals(resultColor, dgv.m_nodeDetails.getBorderPaint(node1));
		assertEquals(resultColor.getAlpha(), ((Color) dgv.m_nodeDetails.getBorderPaint(node1)).getAlpha());
		assertEquals(trans, dgv.m_nodeDetails.getBorderTransparency(node1));

		nodeView1.setVisualProperty(DVisualLexicon.NODE_BORDER_PAINT, Color.cyan);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_BORDER_TRANSPARENCY, 222);
		final Color resultColor2 = new Color(Color.cyan.getRed(), Color.cyan.getGreen(), Color.cyan.getBlue(), 222);

		assertEquals(resultColor2.getAlpha(), ((Color) dgv.m_nodeDetails.getBorderPaint(node1)).getAlpha());
		assertEquals(resultColor2.getRed(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getRed());
		assertEquals(resultColor2.getGreen(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getGreen());
		assertEquals(resultColor2.getBlue(),
				((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getBlue());
		assertEquals(resultColor2, dgv.m_nodeDetails.getBorderPaint(node1));

		assertEquals(222, dgv.m_nodeDetails.getBorderTransparency(node1).intValue());

		assertEquals(borderW, nodeView2.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(resultColor, nodeView2.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT));
		assertEquals(resultColor, dgv.m_nodeDetails.getBorderPaint(node2));
		assertEquals(resultColor.getAlpha(), ((Color) dgv.m_nodeDetails.getBorderPaint(node2)).getAlpha());
		assertEquals(trans, dgv.m_nodeDetails.getBorderTransparency(node2));
	}
}
