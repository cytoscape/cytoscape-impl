package org.cytoscape.io.internal.write.xgmml;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.junit.Before;
import org.junit.Test;

public class SessionXGMMLNetworkWriterTest extends AbstractXGMMLWriterTest {

	@Before
	public void init(){
		super.init();
	}
	
	@Test
	public void testRootNetworkGraph() throws UnsupportedEncodingException {
		write(rootNet);
		assertEquals("0", evalString("/x:graph/@cy:view"));
		assertEquals(""+GenericXGMMLWriter.VERSION, evalString("/x:graph/@cy:documentVersion"));
		// Making sure that the root graph also has the registered attribute
		assertEquals("0", evalString("/x:graph/@cy:registered"));
	}
	
	@Test
	public void testBaseNetworkGraph() throws UnsupportedEncodingException {
		write(rootNet);
		assertEquals(2, evalNumber("count(//x:graph)")); // Only the graph elements for the root and the base network
		assertEquals(""+net.getSUID(), evalString("/x:graph/x:att/x:graph/@id"));
		assertEquals("1", evalString("/x:graph/x:att/x:graph/@cy:registered"));
	}
	
	@Test
	public void testNumberOfNodeElements() {
		write(rootNet);
		assertEquals(NODE_COUNT, evalNumber("count(/x:graph/x:att/x:graph/x:node)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node)"));
	}
	
	@Test
	public void testNumberOfEdgeElements() {
		write(rootNet);
		assertEquals(2, evalNumber("count(/x:graph/x:att/x:graph/x:edge)"));
		assertEquals(2, evalNumber("count(//x:edge)"));
	}
	
	@Test
	public void testNodeHasNoAttElements() {
		write(rootNet);
		assertEquals(0, evalNumber("count(//x:node/x:att)"));
	}
	
	@Test
	public void testEdgeHasNoAttElements() {
		write(rootNet);
		assertEquals(0, evalNumber("count(//x:edge/x:att)"));
	}
	
	@Test
	public void testNoGraphicsElements() { // It's saving a network, not a view!
		write(rootNet);
		assertEquals(0, evalNumber("count(//x:node/graphics)"));
		assertEquals(0, evalNumber("count(//x:edge/graphics)"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testRootNetworkOnlySavedIfSessionSavePolicy() throws UnsupportedEncodingException {
		CyRootNetwork newRoot = ((CySubNetwork)netFactory.createNetwork(SavePolicy.DO_NOT_SAVE)).getRootNetwork();
		setRegistered(newRoot, true); // Registering it doesn't make any difference
		write(newRoot);
	}
	
	@Test
	public void testRegisteredSubNetworkSavedIfSessionSavePolicy() throws UnsupportedEncodingException {
		CySubNetwork sn = rootNet.addSubNetwork();
		setRegistered(sn, true); // It doesn't matter
		write(rootNet);
		assertEquals("1", evalString("/x:graph/x:att/x:graph[@id="+sn.getSUID()+"]/@cy:registered"));
	}
	
	@Test
	public void testUnregisteredSubNetworkSavedIfSessionSavePolicy() throws UnsupportedEncodingException {
		CySubNetwork sn = rootNet.addSubNetwork();
		setRegistered(sn, false); // It doesn't matter
		write(rootNet);
		assertEquals("0", evalString("/x:graph/x:att/x:graph[@id="+sn.getSUID()+"]/@cy:registered"));
	}
	
	@Test
	public void testSubNetworkIgnoredIfNotSessionSavePolicy() throws UnsupportedEncodingException {
		CySubNetwork sn = rootNet.addSubNetwork(SavePolicy.DO_NOT_SAVE);
		setRegistered(sn, true); // It doesn't matter
		write(rootNet);
		assertEquals(2, evalNumber("count(//x:graph)"));
		assertEquals(0, evalNumber("count(//x:graph[@id="+sn.getSUID()+"])"));
	}
	
	@Test
	public void testUnregisteredNetworkPointerSavedIfSessionSavePolicy() throws UnsupportedEncodingException {
		// Create a subnetwork from the same root
		CySubNetwork sn = rootNet.addSubNetwork();
		setRegistered(sn, false); // It doesn't matter
		// Set it as network pointer
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(sn);
		write(rootNet);
		// Test
		assertEquals(""+sn.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/x:att/x:graph/@id")); // It is saved as a nested node graph
		assertEquals("0", evalString("/x:graph//x:att/x:graph[@id="+sn.getSUID()+"]/@cy:registered")); // But it must NOT be registered!
	}
	
	@Test
	public void testRegisteredNetworkPointerSavedUnderRootGraph() throws UnsupportedEncodingException {
		CySubNetwork sn = rootNet.addSubNetwork();
		setRegistered(sn, true);
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(sn);
		write(rootNet);
		// The subnetwork is saved under the root graph, because it is registered
		assertEquals(0, evalNumber("count(/x:graph/x:att/x:graph[@id="+n.getSUID()+"])"));
		// The nested node graph (network pointer) is an XLink to that graph
		assertEquals("#"+sn.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/x:att/x:graph/@xlink:href"));
	}
	
	@Test
	public void testEdgeDirectedAttribute() {
		write(rootNet);
		assertEquals("0", evalString("//x:edge[@id="+undirEdge.getSUID()+"]/@cy:directed"));
		assertEquals("1", evalString("//x:edge[@id="+dirEdge.getSUID()+"]/@cy:directed"));
	}
	
	@Test
	public void testLabelEqualsIdByDefault() {
		write(rootNet);
		// Assuming the name attribute is not set!
		assertEquals(""+rootNet.getSUID(), evalString("/x:graph/@label"));
		assertEquals(""+net.getSUID(), evalString("/x:graph/x:att/x:graph/@label"));
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
		write(rootNet);
		// Now test
		assertEquals("Test Network", evalString("/x:graph/x:att/x:graph/@label"));
		for (CyNode n : net.getNodeList())
			assertEquals("NODE_"+n.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/@label"));
		for (CyEdge e : net.getEdgeList())
			assertEquals("EDGE_"+e.getSUID(), evalString("//x:edge[@id="+e.getSUID()+"]/@label"));
	}
	
	@Test
	public void testNestedGraphReferenceHasNoChildren(){
		// Add a couple of network pointers
		CyNetwork subNet = rootNet.addSubNetwork();
		net.getNodeList().get(0).setNetworkPointer(subNet);
		net.getNodeList().get(1).setNetworkPointer(subNet);
		write(rootNet);
		// Test: nested XLink-graphs must have no children
		assertEquals(2, evalNumber("count(/x:graph//x:node/x:att/x:graph)"));
		assertTrue(evalBoolean("count(//x:node/x:att/x:graph[@xlink:href]) >= 1")); // There should be at least one node graph with XLink...
		assertEquals(0, evalNumber("count(//x:node/x:att/x:graph[@xlink:href]/*)")); // ... and it should have no children, because it's an XLink
	}
	
	@Test
	public void testNetworkPointerFromSameRootNetwork(){
		// Create a subnetwork from the same root
		CySubNetwork subNet = rootNet.addSubNetwork();
		setRegistered(subNet, true); // To force all nested node graphs to be written as XLinks!
		// Set it as network pointer to 2 nodes
		CyNode n1 = net.getNodeList().get(0);
		CyNode n2 = net.getNodeList().get(1);
		n1.setNetworkPointer(subNet);
		n2.setNetworkPointer(subNet);
		write(rootNet);
		// Test
		assertEquals(2, evalNumber("count(/x:graph//x:node/x:att/x:graph)"));
		assertEquals(""+subNet.getSUID(), getElementId("//x:node[@id="+n1.getSUID()+"]/x:att/x:graph"));
		assertEquals(""+subNet.getSUID(), getElementId("//x:node[@id="+n2.getSUID()+"]/x:att/x:graph"));
	}
	
	@Test
	public void testNetworkPointerFromAnotherRootNetwork() throws UnsupportedEncodingException {
		// Create a subnetwork from another root network
		CyNetwork subNet = netFactory.createNetwork();
		// Set it as network pointer
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(subNet);
		write(rootNet);
		// Test
		String filename = SessionUtil.getXGMMLFilename(rootNetMgr.getRootNetwork(subNet)); // It's the name of the other root network file
		assertEquals(filename+"#"+subNet.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/x:att/x:graph/@xlink:href"));
	}
	
	// PRIVATE Methods:

	@Override
	protected GenericXGMMLWriter newWriter(CyIdentifiable netOrView) {
		GenericXGMMLWriter writer = null;
		
		if (netOrView instanceof CyNetwork)
			writer = new SessionXGMMLNetworkWriter(out, renderingEngineMgr, (CyNetwork)netOrView, unrecogVisPropMgr, netMgr, rootNetMgr);
		else
			throw new IllegalArgumentException("netOrView must be a CyNetwork.");
		
		return writer;
	}
}
