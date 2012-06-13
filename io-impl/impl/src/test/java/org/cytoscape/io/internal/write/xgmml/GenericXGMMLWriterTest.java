package org.cytoscape.io.internal.write.xgmml;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.junit.Before;
import org.junit.Test;

public class GenericXGMMLWriterTest extends AbstractXGMMLWriterTest {

	@Before
	public void init(){
		super.init();
	}
	
	@Test
	public void testRootGraph() throws UnsupportedEncodingException {
		write(net);
		assertEquals(1, evalNumber("count(//x:graph)")); // no nested graph elements
		assertEquals(""+net.getSUID(), evalString("/x:graph/@id"));
		assertEquals(""+GenericXGMMLWriter.VERSION, evalString("/x:graph/@cy:documentVersion"));
	}
	
	@Test
	public void testNumberOfNodeElements() {
		write(net);
		assertEquals(NODE_COUNT, evalNumber("count(/x:graph/x:node)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node)"));
	}
	
	@Test
	public void testNumberOfEdgeElements() {
		write(net);
		assertEquals(2, evalNumber("count(/x:graph/x:edge)"));
		assertEquals(2, evalNumber("count(//x:edge)"));
	}
	
	@Test
	public void testNoGraphicsElementWhenNoView() {
		write(net);
		assertEquals(0, evalNumber("count(//x:node/graphics)"));
		assertEquals(0, evalNumber("count(//x:edge/graphics)"));
	}
	
	@Test
	public void testNetworkPointerSavedIfSameRootNetwork() throws UnsupportedEncodingException {
		// Create a subnetwork from the same root
		CySubNetwork sn1 = rootNet.addSubNetwork();
		CySubNetwork sn2 = rootNet.addSubNetwork();
		setRegistered(sn1, true);
		setRegistered(sn2, false);
		// Set it as network pointer
		CyNode n1 = net.getNodeList().get(0);
		CyNode n2 = net.getNodeList().get(1);
		n1.setNetworkPointer(sn1);
		n2.setNetworkPointer(sn2);
		write(net);
		// Test:
		// It is saved as a full nested node graph, not an XLink
		assertEquals(""+sn1.getSUID(), evalString("//x:node[@id="+n1.getSUID()+"]/x:att/x:graph/@id"));
		assertEquals("1", evalString("//x:graph[@id="+sn1.getSUID()+"]/@cy:registered"));
		// Unregistered net pointer is also saved:
		assertEquals(""+sn2.getSUID(), evalString("//x:node[@id="+n2.getSUID()+"]/x:att/x:graph/@id"));
		assertEquals("0", evalString("//x:graph[@id="+sn2.getSUID()+"]/@cy:registered")); // But it must NOT be registered!
	}
	
	@Test
	public void testNetworkPointerIgnoredIfAnotherRootNetwork() throws UnsupportedEncodingException {
		// Create a subnetwork from another root network
		CyNetwork subNet = netFactory.createNetwork();
		// Set it as network pointer
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(subNet);
		write(net);
		// Test
		assertEquals(1, evalNumber("count(//x:graph)")); // no nested graph elements
	}
	
	@Test
	public void testSecondNetworkPointerRefenceSavedAsXLink() throws UnsupportedEncodingException {
		CySubNetwork sn = rootNet.addSubNetwork(); // Same root!
		setRegistered(sn, true);
		CyNode n1 = net.getNodeList().get(0);
		CyNode n2 = net.getNodeList().get(1);
		n1.setNetworkPointer(sn);
		n2.setNetworkPointer(sn);
		write(net);
		// Only one of the nested graphs is an XLink!
		assertEquals(1, evalNumber("count(//x:node/x:att/x:graph[@id="+sn.getSUID()+"])"));
		// The other one must be an XLINK
		assertEquals(1, evalNumber("count(//x:node/x:att/x:graph[@xlink:href=\"#"+sn.getSUID()+"\"])"));
		assertEquals(0, evalNumber("count(//x:node/x:att/x:graph[@xlink:href]/*)")); // it should have no children, because it's an XLink
	}
	
	@Test
	public void testEdgeDirectedAttribute() {
		write(net);
		assertEquals("0", evalString("//x:edge[@id="+undirEdge.getSUID()+"]/@cy:directed"));
		assertEquals("1", evalString("//x:edge[@id="+dirEdge.getSUID()+"]/@cy:directed"));
	}
	
	@Test
	public void testLabelEqualsIdByDefault() {
		write(net);
		// Assuming the name attribute is not set!
		assertEquals(""+net.getSUID(), evalString("/x:graph/@label"));
		for (CyNode n : net.getNodeList())
			assertEquals(""+n.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/@label"));
		for (CyEdge e : net.getEdgeList())
			assertEquals(""+e.getSUID(), evalString("//x:edge[@id="+e.getSUID()+"]/@label"));
	}
	
	@Test
	public void testLabelAttribute() {
		// Set name attributes first
		net.getRow(net).set(CyNetwork.NAME, "Test Network");
		for (CyNode n : net.getNodeList())
			net.getRow(n).set(CyNetwork.NAME, "NODE_"+n.getSUID());
		for (CyEdge e : net.getEdgeList())
			net.getRow(e).set(CyNetwork.NAME, "EDGE_"+e.getSUID());
		write(net);
		// Now test
		assertEquals("Test Network", evalString("/x:graph/@label"));
		for (CyNode n : net.getNodeList())
			assertEquals("NODE_"+n.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/@label"));
		for (CyEdge e : net.getEdgeList())
			assertEquals("EDGE_"+e.getSUID(), evalString("//x:edge[@id="+e.getSUID()+"]/@label"));
	}
}
