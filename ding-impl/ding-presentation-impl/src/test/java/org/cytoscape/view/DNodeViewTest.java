package org.cytoscape.view;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.ding.DNodeShape;
import org.cytoscape.ding.EdgeView;
import org.cytoscape.ding.GraphView;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DNodeView;
import org.cytoscape.ding.impl.DVisualLexicon;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.graph.render.immed.GraphGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DNodeViewTest {
	
	private static final int IDX = 1;

	private VisualLexicon lexicon;

	@Mock
	private DGraphView graphView;

	private CyNetwork network;
	
	
	private CyTableFactory dataFactory;
	private CyRootNetworkFactory cyRoot;
	private SpacialIndex2DFactory spacialFactory;

	
	@Mock
	private UndoSupport undo;

	@Mock
	private Map<NodeViewTaskFactory, Map> nodeViewTFs;
	@Mock
	private Map<EdgeViewTaskFactory, Map> edgeViewTFs;
	@Mock
	private Map<NetworkViewTaskFactory, Map> emptySpaceTFs;
	@Mock
	private Map<DropNodeViewTaskFactory, Map> dropNodeViewTFs;
	@Mock
	private Map<DropNetworkViewTaskFactory, Map> dropEmptySpaceTFs;
	@Mock
	private TaskManager manager;
	@Mock
	private CyEventHelper eventHelper;
	@Mock
	private CyNetworkTableManager tableMgr;
	
	private final TableTestSupport tableSupport = new TableTestSupport();
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
		lexicon = new DVisualLexicon(mock(CustomGraphicsManager.class));
		
		dataFactory = tableSupport.getTableFactory();
		cyRoot = netSupport.getRootNetworkFactory();
		spacialFactory = new RTreeFactory();
		
		buildNetwork();
		networkView = new DGraphView(network, dataFactory, cyRoot, undo, spacialFactory, lexicon,
				nodeViewTFs, edgeViewTFs, emptySpaceTFs, dropNodeViewTFs, 
				dropEmptySpaceTFs, manager, eventHelper, tableMgr);
		
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
		int index1 = dnv1.getGraphPerspectiveIndex();
		int index2 = dnv2.getGraphPerspectiveIndex();
		assertEquals(0, index1);
		assertEquals(1, index2);
	}

	@Test
	public void testGetEdgeViewsList() {
		final List<EdgeView> edges = dnv1.getEdgeViewsList(dnv2);
		assertNotNull(edges);
		assertEquals(1, edges.size());
		
		final List<EdgeView> selfEdges = dnv1.getEdgeViewsList(dnv1);
		assertNotNull(selfEdges);
		assertEquals(0, selfEdges.size());
	}

	@Test
	public void testGetShape() {
		Integer shape1 = dnv1.getShape();
		Byte expected = GraphGraphics.SHAPE_RECTANGLE;
		
		assertNotNull(shape1);
		assertTrue(expected.byteValue() == shape1.byteValue());
	}

	@Test
	public void testSetSelectedPaint() {
		Paint paint = Color.BLUE;
		dnv1.setSelectedPaint(paint);
		
		final Paint selectedPaint = dnv1.getSelectedPaint();
		assertEquals(paint, selectedPaint);
	}

	@Test
	public void testGetSelectedPaint() {
		final Paint selectedPaint = dnv1.getSelectedPaint();
		assertEquals(Color.YELLOW, selectedPaint);
	}

	@Test
	public void testSetUnselectedPaint() {
		dnv1.setUnselectedPaint(Color.PINK);
		assertEquals(Color.PINK, dnv1.getUnselectedPaint());
	}

	@Test
	public void testGetUnselectedPaint() {
		final Paint unselected = dnv1.getUnselectedPaint();
		assertEquals(Color.RED, unselected);
	}

	@Test
	public void testSetBorderPaint() {
		dnv1.setBorderPaint(Color.RED);
		assertEquals(Color.RED, dnv1.getBorderPaint());
	}

	@Test
	public void testGetBorderPaint() {
		final Paint borderPaint = dnv1.getBorderPaint();
		assertEquals(Color.DARK_GRAY, borderPaint);
	}

	@Test
	public void testSetBorderWidth() {
		Float width = 10f;
		dnv1.setBorderWidth(width);
		Float newWidth = dnv1.getBorderWidth();
		assertEquals(width, newWidth);
		
		// Make sure stroke is also updated.
		final Stroke stroke = dnv1.getBorder();
		assertNotNull(stroke);
		assertTrue(stroke instanceof BasicStroke);
		BasicStroke bs = (BasicStroke) stroke;
		assertEquals(width, Float.valueOf(bs.getLineWidth()));
	}

	@Test
	public void testGetBorderWidth() {
		final Float defWidth = dnv1.getBorderWidth();
		assertEquals(Float.valueOf(0.0f), defWidth);
	}

	@Test
	public void testSetBorder() {
		final BasicStroke stroke = new BasicStroke(10f);
		dnv1.setBorder(stroke);
		assertEquals(stroke, dnv1.getBorder());
		assertEquals(Float.valueOf(10f), Float.valueOf(stroke.getLineWidth()));
	}

	@Test
	public void testGetBorder() {
		final Float width = dnv1.getBorderWidth();
		final Stroke defBorder = dnv1.getBorder();
		assertEquals(defBorder, new BasicStroke(width));
	}

	@Test()
	public void testSetTransparency() {
		Integer trans = 200;
		dnv1.setTransparency(trans);
		assertEquals(trans, Integer.valueOf(dnv1.getTransparency()));
	}

	@Test
	public void testGetTransparency() {
		assertEquals(Integer.valueOf(255), Integer.valueOf(dnv1.getTransparency()));
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
		
		assertEquals(trasnparentFillColor, dnv1.getUnselectedPaint());
		assertEquals(selectedColor, dnv1.getSelectedPaint());
		assertEquals(transparency, Integer.valueOf(dnv1.getTransparency()));

		assertEquals(width, Double.valueOf(dnv1.getWidth()));
		assertEquals(height, Double.valueOf(dnv1.getHeight()));
	}

}
