package org.cytoscape.view.layout;

import static org.junit.Assert.*;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LayoutNodeTest {
	
	private final NetworkViewTestSupport support = new NetworkViewTestSupport();
	final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
	CyNetworkFactory nFactory = support.getNetworkFactory();
	
	CyNetwork newNetwork = nFactory.createNetwork();
	CyNode s = newNetwork.addNode();
	CyNode neighbor  = newNetwork.addNode();
	
	CyNetworkView newView = viewFactory.createNetworkView(newNetwork);
	final View<CyNode> sourceView = newView.getNodeView(s);
	
	CyRow sRow = newNetwork.getRow(s);
	
	private LayoutNode layoutNode;

	@Before
	public void setUp() throws Exception {
		layoutNode = new LayoutNode(sourceView, 0, sRow);
	}


	@Test
	public void testLayoutNode() {
		assertNotNull(layoutNode);
	}

	@Test
	public void testGetNode() {
		assertEquals(s, layoutNode.getNode());
	}

	@Test
	public void testGetRow() {
		assertEquals(sRow, layoutNode.getRow());
	}

	@Test
	public void testGetNodeView() {
		assertEquals(sourceView, layoutNode.getNodeView());
	}

	@Test
	public void testSetLocation() {
		double x = 10;
		double y = -222;
		layoutNode.setLocation(x, y);
		assertTrue(x == layoutNode.getX());
		assertTrue(y == layoutNode.getY());
		
		x = -1000;
		y = 50000;
		layoutNode.setLocation(x, y);
		assertTrue(x == layoutNode.getX());
		assertTrue(y == layoutNode.getY());
	}

	@Test
	public void testSetX() {
		double x = 10;
		layoutNode.setX(x);
		assertTrue(x == layoutNode.getX());
	}

	@Test
	public void testSetY() {
		double y = 10;
		layoutNode.setY(y);
		assertTrue(y == layoutNode.getY());
	}

	@Test
	public void testSetDisp() {
		double x = -1000;
		double y = 50000;
		layoutNode.setDisp(x, y);
		assertTrue(x == layoutNode.getXDisp());
		assertTrue(y == layoutNode.getYDisp());
	}

	@Test
	public void testAddNeighbor() {
		
		final View<CyNode> nView = newView.getNodeView(neighbor);
		CyRow nRow = newNetwork.getRow(neighbor);
		LayoutNode neighborNode = new LayoutNode(nView, 1, nRow);
		layoutNode.addNeighbor(neighborNode);
		
		assertEquals(1, layoutNode.getNeighbors().size());
	}

	@Test
	public void testGetIndex() {
		assertTrue( 0 == layoutNode.getIndex());
	}

	@Test
	public void testLock() {
		layoutNode.lock();
		assertTrue(layoutNode.isLocked());
	}

	@Test
	public void testUnLock() {
		layoutNode.lock();
		layoutNode.unLock();
		assertFalse(layoutNode.isLocked());
	}

	@Test
	public void testIncrementDisp() {
		
		double x = -1000;
		double y = 50000;
		layoutNode.setDisp(x, y);
		layoutNode.incrementDisp(10d, 10d);
		assertTrue((x+10d) == layoutNode.getXDisp());
		assertTrue((y+10d) == layoutNode.getYDisp());
	}

	@Test
	public void testIncrement() {
		double x = 10;
		double y = -222;
		layoutNode.setLocation(x, y);
		assertTrue(x == layoutNode.getX());
		assertTrue(y == layoutNode.getY());
		
		layoutNode.increment(10d, 10d);
		assertTrue((x+10d) == layoutNode.getX());
		assertTrue((y+10d) == layoutNode.getY());
	}

	@Test
	public void testDecrementDisp() {
		double x = -1000;
		double y = 50000;
		layoutNode.setDisp(x, y);
		layoutNode.decrementDisp(10d, 10d);
		assertTrue((x-10d) == layoutNode.getXDisp());
		assertTrue((y-10d) == layoutNode.getYDisp());
	}

	@Test
	public void testDecrement() {
		double x = 10;
		double y = -222;
		layoutNode.setLocation(x, y);
		assertTrue(x == layoutNode.getX());
		assertTrue(y == layoutNode.getY());
		
		layoutNode.decrement(10d, 10d);
		assertTrue((x-10d) == layoutNode.getX());
		assertTrue((y-10d) == layoutNode.getY());
	}


	@Test
	public void testGetWidth() {
		final Double originalWidth = sourceView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		assertEquals(originalWidth, Double.valueOf(layoutNode.getWidth()));
	}

	@Test
	public void testGetHeight() {
		final Double originalHeight = sourceView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
		assertEquals(originalHeight, Double.valueOf(layoutNode.getHeight()));
	}

	@Test
	public void testDistanceLayoutNode() {
		final View<CyNode> nView = newView.getNodeView(neighbor);
		CyRow nRow = newNetwork.getRow(neighbor);
		LayoutNode neighborNode = new LayoutNode(nView, 1, nRow);
		layoutNode.addNeighbor(neighborNode);
		
		double x = 100;
		double y = 0;
		layoutNode.setLocation(x, y);
		double xN = 0;
		double yN = 0;
		neighborNode.setLocation(xN, yN);
		
		assertTrue(100d == layoutNode.distance(neighborNode));
	}

	@Test
	public void testDistanceDoubleDouble() {
		
		double x = 100;
		double y = 0;
		layoutNode.setLocation(x, y);
		
		assertTrue(100d == layoutNode.distance(0,0));
	}

	@Test
	public void testMoveToLocation() {
		double x = 10;
		double y = -222;
		layoutNode.setLocation(x, y);
		
		layoutNode.moveToLocation();
		assertTrue(x == layoutNode.getNodeView().getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
		assertTrue(y == layoutNode.getNodeView().getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));
	}

	@Test
	public void testGetIdentifier() {
		assertEquals(s.getSUID().toString(), layoutNode.getIdentifier());
	}

	@Test
	public void testGetDegree() {
		final View<CyNode> nView = newView.getNodeView(neighbor);
		CyRow nRow = newNetwork.getRow(neighbor);
		LayoutNode neighborNode = new LayoutNode(nView, 1, nRow);
		layoutNode.addNeighbor(neighborNode);
		assertEquals(1, layoutNode.getDegree());
	}

	@Test
	public void testToString() {
		double x = 1;
		double y = 2;
		layoutNode.setLocation(x, y);
		assertEquals("Node " + s.getSUID() + " at 1.0, 2.0", layoutNode.toString());
	}

	@Test
	public void testPrintDisp() {
		double x = 10;
		double y = 22;
		layoutNode.setDisp(x, y);
		assertEquals("10.0, 22.0", layoutNode.printDisp());
	}

	@Test
	public void testPrintLocation() {
		double x = 10;
		double y = -222;
		layoutNode.setLocation(x, y);
		assertEquals("10.0, -222.0", layoutNode.printLocation());
	}

}
