package org.cytoscape.ding.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.graph.render.stateful.EdgeDetails;
import org.cytoscape.graph.render.stateful.NodeDetails;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
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

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class DGraphViewApplyTest {

	private CyNetworkView dgv;
	private DRenderingEngine re;
	private EdgeDetails edgeDetails;
	private NodeDetails nodeDetails;

	NetworkViewTestSupport testSupport = new NetworkViewTestSupport();

	private CyNetwork network;
	private DVisualLexicon dingLexicon;
	private HandleFactory handleFactory;

	@Mock private UndoSupport undoSupport;
	@Mock private DialogTaskManager dialogTaskManager;
	@Mock private CyEventHelper eventHelper;
	@Mock private IconManager iconManager;
	@Mock private AnnotationFactoryManager annotationFactoryManager;
	@Mock private DingGraphLOD dingGraphLOD;
	@Mock private VisualMappingManager visualMappingManager;
	@Mock private CyNetworkViewManager networkViewManager;
	@Mock private CyServiceRegistrar serviceRegistrar;
	
	// Network contents
	CyNode node1;
	CyNode node2;
	CyNode node3;

	CyEdge edge12;
	CyEdge edge23;
	CyEdge edge13;

	View<CyEdge> ev12;
	View<CyEdge> ev13;
	View<CyEdge> ev23;

	View<CyNode> nodeView1;
	View<CyNode> nodeView2;
	View<CyNode> nodeView3;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		testSupport.getNetworkTableManager();
		network = buildNetworkModel();
		dingLexicon = new DVisualLexicon();
		handleFactory = new HandleFactoryImpl();
		
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(DialogTaskManager.class)).thenReturn(dialogTaskManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(IconManager.class)).thenReturn(iconManager);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visualMappingManager);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(networkViewManager);
		
		dgv = testSupport.getNetworkViewFactoryProvider().createNetworkViewFactory(dingLexicon, DingRenderer.ID).createNetworkView(network);
		re = new DRenderingEngine(dgv, dingLexicon, annotationFactoryManager, dingGraphLOD, handleFactory, serviceRegistrar);
		nodeDetails = re.getNodeDetails();
		edgeDetails = re.getEdgeDetails();

		assertNotNull(dgv);
		assertEquals(3, dgv.getModel().getNodeCount());
		assertEquals(3, dgv.getModel().getEdgeCount());

		nodeView1 = dgv.getNodeView(node1);
		nodeView2 = dgv.getNodeView(node2);
		nodeView3 = dgv.getNodeView(node3);

		ev12 = dgv.getEdgeView(edge12);
		ev13 = dgv.getEdgeView(edge13);
		ev23 = dgv.getEdgeView(edge23);
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

		EdgeDetails edgeDetails = re.getEdgeDetails();
		dgv.setViewDefault(DVisualLexicon.EDGE_UNSELECTED_PAINT, strokeColor);
		assertEquals(255, ((Color) edgeDetails.getUnselectedPaint(ev12)).getAlpha());
		assertEquals(255, ((Color) edgeDetails.getUnselectedPaint(ev13)).getAlpha());
		assertEquals(255, ((Color) edgeDetails.getUnselectedPaint(ev23)).getAlpha());

		dgv.setViewDefault(DVisualLexicon.EDGE_TRANSPARENCY, 100);
		assertEquals(100, ((Color) edgeDetails.getUnselectedPaint(ev12)).getAlpha());
		assertEquals(100, ((Color) edgeDetails.getUnselectedPaint(ev13)).getAlpha());
		assertEquals(100, ((Color) edgeDetails.getUnselectedPaint(ev23)).getAlpha());

		dgv.setViewDefault(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, targetArrowColor);
		assertEquals(targetArrowColor.getRed(),   ((Color) edgeDetails.getTargetArrowPaint(ev12)).getRed());
		assertEquals(targetArrowColor.getGreen(), ((Color) edgeDetails.getTargetArrowPaint(ev12)).getGreen());
		assertEquals(targetArrowColor.getBlue(),  ((Color) edgeDetails.getTargetArrowPaint(ev12)).getBlue());

		assertEquals(100, ((Color) edgeDetails.getTargetArrowPaint(ev12)).getAlpha());
		assertEquals(100, ((Color) edgeDetails.getTargetArrowPaint(ev13)).getAlpha());
		assertEquals(100, ((Color) edgeDetails.getTargetArrowPaint(ev23)).getAlpha());

		// Create mapping
		ev12.setVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, targetArrowColorM1);
		ev13.setVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, targetArrowColorM2);

		assertEquals(targetArrowColorM1.getRed(),   ((Color) edgeDetails.getTargetArrowPaint(ev12)).getRed());
		assertEquals(targetArrowColorM1.getGreen(), ((Color) edgeDetails.getTargetArrowPaint(ev12)).getGreen());
		assertEquals(targetArrowColorM1.getBlue(),  ((Color) edgeDetails.getTargetArrowPaint(ev12)).getBlue());
		assertEquals(targetArrowColorM2.getRed(),   ((Color) edgeDetails.getTargetArrowPaint(ev13)).getRed());
		assertEquals(targetArrowColorM2.getGreen(), ((Color) edgeDetails.getTargetArrowPaint(ev13)).getGreen());
		assertEquals(targetArrowColorM2.getBlue(),  ((Color) edgeDetails.getTargetArrowPaint(ev13)).getBlue());
		assertEquals(targetArrowColor.getRed(),   ((Color) edgeDetails.getTargetArrowPaint(ev23)).getRed());
		assertEquals(targetArrowColor.getGreen(), ((Color) edgeDetails.getTargetArrowPaint(ev23)).getGreen());
		assertEquals(targetArrowColor.getBlue(),  ((Color) edgeDetails.getTargetArrowPaint(ev23)).getBlue());

		assertEquals(100, ((Color) edgeDetails.getTargetArrowPaint(ev12)).getAlpha());
		assertEquals(100, ((Color) edgeDetails.getTargetArrowPaint(ev13)).getAlpha());
		assertEquals(100, ((Color) edgeDetails.getTargetArrowPaint(ev23)).getAlpha());

		// TODO: Add more tests (mixing defaults and mappings)
	}

	@Test
	public void testNodeView() {
		final Color fillColor = new Color(10, 20, 200);
		final Color labelColor = Color.orange;

		dgv.setViewDefault(DVisualLexicon.NODE_FILL_COLOR, fillColor);
		assertEquals(255, ((Color) nodeDetails.getFillPaint(nodeView1)).getAlpha());
		assertEquals(255, ((Color) nodeDetails.getFillPaint(nodeView2)).getAlpha());
		assertEquals(255, ((Color) nodeDetails.getFillPaint(nodeView3)).getAlpha());

		// Test labels
		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_COLOR, labelColor);
		assertEquals(labelColor.getRed(),   ((Color) nodeDetails.getLabelPaint(nodeView1)).getRed());
		assertEquals(labelColor.getGreen(), ((Color) nodeDetails.getLabelPaint(nodeView1)).getGreen());
		assertEquals(labelColor.getBlue(),  ((Color) nodeDetails.getLabelPaint(nodeView1)).getBlue());
		assertEquals(255, ((Color) nodeDetails.getLabelPaint(nodeView1)).getAlpha());

		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_TRANSPARENCY, 100);
		assertEquals(100, ((Color) nodeDetails.getLabelPaint(nodeView1)).getAlpha());
		assertEquals(100, ((Color) nodeDetails.getLabelPaint(nodeView2)).getAlpha());
		assertEquals(100, ((Color) nodeDetails.getLabelPaint(nodeView3)).getAlpha());

		// Test mappings

		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_COLOR, Color.blue);
		assertEquals(Color.blue.getBlue(), ((Color) nodeDetails.getLabelPaint(nodeView1)).getBlue());

		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_TRANSPARENCY, 20);
		assertEquals(20,  ((Color) nodeDetails.getLabelPaint(nodeView1)).getAlpha());
		assertEquals(100, ((Color) nodeDetails.getLabelPaint(nodeView2)).getAlpha());
		assertEquals(100, ((Color) nodeDetails.getLabelPaint(nodeView3)).getAlpha());

		// Font
		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_FONT_SIZE, 20);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_SIZE, 10);
		nodeView2.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_SIZE, 8);

		assertEquals(10, nodeDetails.getLabelFont(nodeView1).getSize());
		assertEquals(8,  nodeDetails.getLabelFont(nodeView2).getSize());
		assertEquals(20, nodeDetails.getLabelFont(nodeView3).getSize());

		final Font defFont = new Font("SansSerif", Font.PLAIN, 22);
		final Font mapFont = new Font("Serif", Font.PLAIN, 40);
		dgv.setViewDefault(DVisualLexicon.NODE_LABEL_FONT_FACE, defFont);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_FACE, mapFont);
		nodeView2.setVisualProperty(DVisualLexicon.NODE_LABEL_FONT_FACE, mapFont);

		assertEquals(mapFont.getFontName(), nodeDetails.getLabelFont(nodeView1).getFontName());
		assertEquals(mapFont.getFontName(), nodeDetails.getLabelFont(nodeView2).getFontName());
		assertEquals(defFont.getFontName(), nodeDetails.getLabelFont(nodeView3).getFontName());
	}

	@Test
	public void testDefaultValues() {
		dgv.setViewDefault(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.cyan);
		assertEquals(Color.cyan, ev12.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan, ev13.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan, ev23.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan.getRed(),   ((Color) edgeDetails.getUnselectedPaint(ev12)).getRed());
		assertEquals(Color.cyan.getGreen(), ((Color) edgeDetails.getUnselectedPaint(ev12)).getGreen());
		assertEquals(Color.cyan.getBlue(),  ((Color) edgeDetails.getUnselectedPaint(ev12)).getBlue());

		ev12.setVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.yellow);
		assertEquals(Color.yellow, ev12.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan,   ev13.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		assertEquals(Color.cyan,   ev23.getVisualProperty(DVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));

		Double defEdgeWidth = 20d;
		dgv.setViewDefault(DVisualLexicon.EDGE_WIDTH, defEdgeWidth);
		assertEquals(defEdgeWidth, ev12.getVisualProperty(DVisualLexicon.EDGE_WIDTH));
		assertEquals(defEdgeWidth, ev13.getVisualProperty(DVisualLexicon.EDGE_WIDTH));
		assertEquals(defEdgeWidth, ev23.getVisualProperty(DVisualLexicon.EDGE_WIDTH));

		dgv.setViewDefault(DVisualLexicon.NODE_FILL_COLOR, Color.magenta);
		assertEquals(Color.magenta, nodeView1.getVisualProperty(DVisualLexicon.NODE_FILL_COLOR));

		dgv.setViewDefault(DVisualLexicon.NODE_WIDTH, 35d);
		assertTrue(35d == nodeView1.getVisualProperty(DVisualLexicon.NODE_WIDTH));

		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_WIDTH, 8d);
		assertTrue(8d == nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));

		dgv.setViewDefault(DVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.DIAMOND);
		NodeShape returnedShape = nodeView1.getVisualProperty(DVisualLexicon.NODE_SHAPE);
//		System.out.println("Shape ==> " + returnedShape);
		assertEquals(NodeShapeVisualProperty.DIAMOND, returnedShape);
	}

	@Test
	public void testNodeBorder() {
		final Double borderW = Double.valueOf(7d);
		final Integer trans = Integer.valueOf(123);
		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_WIDTH, borderW);
		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_PAINT, Color.magenta);
		dgv.setViewDefault(DVisualLexicon.NODE_BORDER_TRANSPARENCY, trans);

		final Color resultColor = new Color(Color.magenta.getRed(), Color.magenta.getGreen(), Color.magenta.getBlue(), trans);
		assertEquals(borderW, nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(Color.magenta, nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT));
		assertEquals(resultColor, nodeDetails.getBorderPaint(nodeView1));
		assertEquals(resultColor.getAlpha(), ((Color) nodeDetails.getBorderPaint(nodeView1)).getAlpha());
		assertEquals(trans, nodeDetails.getBorderTransparency(nodeView1));

		nodeView1.setVisualProperty(DVisualLexicon.NODE_BORDER_PAINT, Color.cyan);
		nodeView1.setVisualProperty(DVisualLexicon.NODE_BORDER_TRANSPARENCY, 222);
		final Color resultColor2 = new Color(Color.cyan.getRed(), Color.cyan.getGreen(), Color.cyan.getBlue(), 222);

		assertEquals(resultColor2.getAlpha(), ((Color) nodeDetails.getBorderPaint(nodeView1)).getAlpha());
		assertEquals(resultColor2.getRed(),   ((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getRed());
		assertEquals(resultColor2.getGreen(), ((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getGreen());
		assertEquals(resultColor2.getBlue(),  ((Color) nodeView1.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT)).getBlue());
		assertEquals(resultColor2, nodeDetails.getBorderPaint(nodeView1));

		assertEquals(222, nodeDetails.getBorderTransparency(nodeView1).intValue());

		assertEquals(borderW, nodeView2.getVisualProperty(DVisualLexicon.NODE_BORDER_WIDTH));
		assertEquals(Color.magenta, nodeView2.getVisualProperty(DVisualLexicon.NODE_BORDER_PAINT));
		assertEquals(resultColor, nodeDetails.getBorderPaint(nodeView2));
		assertEquals(resultColor.getAlpha(), ((Color) nodeDetails.getBorderPaint(nodeView2)).getAlpha());
		assertEquals(trans, nodeDetails.getBorderTransparency(nodeView2));
	}
}
