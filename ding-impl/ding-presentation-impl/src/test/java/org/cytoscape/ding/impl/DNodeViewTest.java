package org.cytoscape.ding.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.IconManager;
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

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class DNodeViewTest {
	
	private VisualLexicon dingLexicon;

	@Mock
	private DGraphView graphView;

	private CyNetwork network;
	
	private CyRootNetworkManager rootNetworkManager;
	private SpacialIndex2DFactory spacialFactory;
	
	@Mock
	private UndoSupport undoSupport;
	@Mock
	private ViewTaskFactoryListener vtfListener;
	@Mock
	private Map<NodeViewTaskFactory, Map> nodeViewTFs;
	@Mock
	private Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	@Mock
	private Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	@Mock
	private DialogTaskManager dialogTaskManager;
	@Mock
	private CyEventHelper eventHelper;
	@Mock
	private IconManager iconManager;
	@Mock
	private AnnotationFactoryManager annotationFactoryManager;
	@Mock
	private DingGraphLOD dingGraphLOD;
	@Mock
	private CyNetworkViewManager networkViewManager; 
	@Mock
	private VisualMappingManager visualMappingManager;
	@Mock
	private CyApplicationConfiguration applicationConfiguration;
	@Mock
	private CyServiceRegistrar serviceRegistrar;
	@Mock
	private HandleFactory handleFactory;
	
	private final NetworkTestSupport netSupport = new NetworkTestSupport();

	private DGraphView networkView;

	private CyNode node1;

	private CyNode node2;

	private CyEdge edge1;
	
	private DNodeView dnv1;
	private DNodeView dnv2;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		rootNetworkManager = netSupport.getRootNetworkFactory();
		spacialFactory = new RTreeFactory();
		
		when(serviceRegistrar.getService(CyRootNetworkManager.class)).thenReturn(rootNetworkManager);
		when(serviceRegistrar.getService(UndoSupport.class)).thenReturn(undoSupport);
		when(serviceRegistrar.getService(SpacialIndex2DFactory.class)).thenReturn(spacialFactory);
		when(serviceRegistrar.getService(DialogTaskManager.class)).thenReturn(dialogTaskManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(IconManager.class)).thenReturn(iconManager);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(visualMappingManager);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(networkViewManager);
		when(serviceRegistrar.getService(CyApplicationConfiguration.class)).thenReturn(applicationConfiguration);
		
		dingLexicon = new DVisualLexicon(mock(CustomGraphicsManager.class));
		
		buildNetwork();
		
		networkView = new DGraphView(network, dingLexicon, vtfListener, annotationFactoryManager, dingGraphLOD, handleFactory,
				serviceRegistrar);
		
		dnv1 = (DNodeView) networkView.getDNodeView(node1);
		dnv2 = (DNodeView) networkView.getDNodeView(node2);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	public void buildNetwork() {
		network = netSupport.getNetwork();
		
		node1 = network.addNode();
		node2 = network.addNode();

		List<CyNode> nl = new ArrayList<CyNode>();
		nl.add(node1);
		nl.add(node2);

		edge1 = network.addEdge(node1, node2, true);

		List<CyEdge> el = new ArrayList<CyEdge>();
		el.add(edge1);
	}

	@Test
	public void testDNodeView() {
		assertNotNull(dnv1);
		assertNotNull(dnv2);
	}

	@Test
	public void testGetGraphView() {
		final GraphView dgv = dnv1.getGraphView();
		assertEquals(this.networkView, dgv);
	}

	@Test
	public void testGetGraphPerspectiveIndex() {
		long index1 = dnv1.getCyNode().getSUID();
		long index2 = dnv2.getCyNode().getSUID();
		// assertEquals(0, index1);
		// assertEquals(1, index2);
	}

	@Test
	public void testSetSelectedPaint() {
		Paint paint = Color.BLUE;
		dnv1.setSelectedPaint(paint);
		
		final Paint selectedPaint = ((DGraphView)dnv1.getGraphView()).m_nodeDetails.getSelectedPaint(dnv1.getCyNode());
		assertEquals(paint, selectedPaint);
	}

	@Test
	public void testSetUnselectedPaint() {
		dnv1.setUnselectedPaint(Color.PINK);
		assertEquals(Color.PINK, ((DGraphView)dnv1.getGraphView()).m_nodeDetails.getUnselectedPaint(dnv1.getCyNode()));
	}

	@Test
	public void testSetBorderPaint() {
		dnv1.setBorderPaint(Color.RED);
		assertEquals(Color.RED, ((DGraphView)dnv1.getGraphView()).m_nodeDetails.getBorderPaint(dnv1.getCyNode()));
	}

	@Test
	public void testSetBorderWidth() {
		float width = 10f;
		dnv1.setBorderWidth(width);
		float newWidth = ((DGraphView)dnv1.getGraphView()).m_nodeDetails.getBorderWidth(dnv1.getCyNode());
		assertTrue(width == newWidth);
		
		// Make sure stroke is also updated.
		// FIXME: border rendering is broken.
		
//		final Stroke stroke = DLineType.getDLineType(dnv1.getVisualProperty(DVisualLexicon.NODE_BORDER_LINE_TYPE)).getStroke(10f);
//		assertNotNull(stroke);
//		assertTrue(stroke instanceof BasicStroke);
//		BasicStroke bs = (BasicStroke) stroke;
//		assertEquals(width, Float.valueOf(bs.getLineWidth()));
	}

	@Test
	public void testSetBorder() {
		final BasicStroke stroke = new BasicStroke(10f);
		dnv1.setBorder(stroke);
		// FIXME
//		assertEquals(stroke, ((DGraphView)dnv1.getGraphView()).m_nodeDetails.);
//		assertEquals(Float.valueOf(10f), Float.valueOf(stroke.getLineWidth()));
	}

	@Test()
	public void testSetTransparency() {
		Integer trans = 200;
		dnv1.setTransparency(trans);
		//FIXME
//		assertEquals(trans, ((DGraphView)dnv1.getGraphView()).m_nodeDetails.);
	}


	@Test
	public void testSetWidth() {
		dnv1.setWidth(150);
		assertEquals(Double.valueOf(150), Double.valueOf(dnv1.getWidth()));
	}

	@Test
	public void testGetWidth() {
	}

	@Test
	public void testSetHeight() {
	}

	@Test
	public void testGetHeight() {
	}

	@Test
	public void testGetLabel() {
	}

	@Test
	public void testSetOffset() {
	}

	@Test
	public void testGetOffset() {
	}

	@Test
	public void testSetXPosition() {
	}

	@Test
	public void testGetXPosition() {
	}

	@Test
	public void testSetYPosition() {
	}

	@Test
	public void testGetYPosition() {
	}

	@Test
	public void testSelect() {
	}

	@Test
	public void testSelectInternal() {
	}

	@Test
	public void testUnselect() {
	}

	@Test
	public void testUnselectInternal() {
	}

	@Test
	public void testIsSelected() {
	}

	@Test
	public void testSetSelected() {
	}

	@Test
	public void testIsHidden() {
	}

	@Test
	public void testSetShape() {
	}

	@Test
	public void testSetToolTip() {
	}

	@Test
	public void testGetToolTip() {
	}

	@Test
	public void testGetTextPaint() {
	}

	@Test
	public void testSetTextPaint() {
	}

	@Test
	public void testSetGreekThreshold() {
	}

	@Test
	public void testGetText() {
	}

	@Test
	public void testSetText() {
	}

	@Test
	public void testGetFont() {
	}

	@Test
	public void testSetFont() {
	}

	@Test
	public void testAddCustomGraphic() {
	}

	@Test
	public void testContainsCustomGraphic() {
	}

	@Test
	public void testCustomGraphicIterator() {
	}

	@Test
	public void testRemoveCustomGraphic() {
	}

	@Test
	public void testRemoveAllCustomGraphics() {
	}

	@Test
	public void testGetNumCustomGraphics() {
	}

	@Test
	public void testCustomGraphicLock() {
	}

	@Test
	public void testGetLabelWidth() {
	}

	@Test
	public void testSetLabelWidth() {
	}

	@Test
	public void testGetNestedNetworkTexturePaint() {
	}

	@Test
	public void testSetNestedNetworkView() {
	}

	@Test
	public void testNestedNetworkIsVisible() {
	}

	@Test
	public void testShowNestedNetwork() {
	}

	@Test
	public void testGetLabelPosition() {
	}

	@Test
	public void testSetLabelPosition() {
	}

	@Test
	public void testSetVisualProperty() {
		// Color
		final Color fillColor = Color.GREEN;
		final Color selectedColor = Color.MAGENTA;
		final Integer transparency  = 125;
		
		final Double width = 123d;
		final Double height = 90d;
		final NodeShape shape = NodeShapeVisualProperty.HEXAGON;
		
		dnv1.setVisualProperty(DVisualLexicon.NODE_FILL_COLOR, fillColor);
		dnv1.setVisualProperty(DVisualLexicon.NODE_SELECTED_PAINT, selectedColor);
		dnv1.setVisualProperty(DVisualLexicon.NODE_TRANSPARENCY, transparency);
		
		dnv1.setVisualProperty(DVisualLexicon.NODE_WIDTH, width);
		dnv1.setVisualProperty(DVisualLexicon.NODE_HEIGHT, height);
		dnv1.setVisualProperty(DVisualLexicon.NODE_SHAPE, shape);

		final Color trasnparentFillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(),
				transparency);
		
//		assertEquals(trasnparentFillColor, dnv1.getUnselectedPaint());
//		assertEquals(selectedColor, dnv1.getSelectedPaint());
//		assertEquals(transparency, Integer.valueOf(dnv1.getTransparency()));
//
//		assertEquals(width, Double.valueOf(dnv1.getWidth()));
//		assertEquals(height, Double.valueOf(dnv1.getHeight()));
	}
}
