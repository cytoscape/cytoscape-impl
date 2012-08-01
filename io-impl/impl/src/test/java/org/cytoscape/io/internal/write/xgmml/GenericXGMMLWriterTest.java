package org.cytoscape.io.internal.write.xgmml;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;
import static org.junit.Assert.*;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.xml.xpath.XPathConstants;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
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
		assertEquals(EDGE_COUNT, evalNumber("count(/x:graph/x:edge)"));
		assertEquals(EDGE_COUNT, evalNumber("count(//x:edge)"));
	}
	
	@Test
	public void testIntegerAttributeSavedAsInteger() {
		net.getRow(net).getTable().createColumn("test_int", Integer.class, false);
		net.getRow(net).set("test_int", Integer.MAX_VALUE);
		net.getRow(net).getTable().createListColumn("test_list_int", Integer.class, false);
		net.getRow(net).set("test_list_int", Arrays.asList(new Integer[]{ 1 }));
		write(net);
		assertEquals("integer", evalString("/x:graph/x:att[@name=\"test_int\"]/@type"));
		assertEquals(Integer.MAX_VALUE, evalNumber("/x:graph/x:att[@name=\"test_int\"]/@value"));
		assertEquals("list", evalString("/x:graph/x:att[@name=\"test_list_int\"]/@type"));
		assertEquals("integer", evalString("/x:graph/x:att[@name=\"test_list_int\"]/x:att[@value=\"1\"]/@type"));
	}
	
	@Test
	public void testLongAttributeSavedAsReal() {
		net.getRow(net).getTable().createColumn("test_long", Long.class, false);
		net.getRow(net).set("test_long", Long.MAX_VALUE);
		net.getRow(net).getTable().createListColumn("test_list_long", Long.class, false);
		net.getRow(net).set("test_list_long", Arrays.asList(new Long[]{ 1L }));
		write(net);
		assertEquals("real", evalString("/x:graph/x:att[@name=\"test_long\"]/@type"));
		assertEquals(Long.MAX_VALUE, evalNumber("/x:graph/x:att[@name=\"test_long\"]/@value"));
		assertEquals("list", evalString("/x:graph/x:att[@name=\"test_list_long\"]/@type"));
		assertEquals("real", evalString("/x:graph/x:att[@name=\"test_list_long\"]/x:att[@value=\"1\"]/@type"));
	}
	
	@Test
	public void testDoubleAttributeSavedAsReal() {
		net.getRow(net).getTable().createColumn("test_double", Double.class, false);
		net.getRow(net).set("test_double", Double.MAX_VALUE);
		net.getRow(net).getTable().createListColumn("test_list_double", Double.class, false);
		net.getRow(net).set("test_list_double", Arrays.asList(new Double[]{ 1.2D }));
		write(net);
		assertEquals("real", evalString("/x:graph/x:att[@name=\"test_double\"]/@type"));
		assertEquals(Double.MAX_VALUE, new Double(evalString("/x:graph/x:att[@name=\"test_double\"]/@value")), 0.0);
		assertEquals("list", evalString("/x:graph/x:att[@name=\"test_list_double\"]/@type"));
		assertEquals("real", evalString("/x:graph/x:att[@name=\"test_list_double\"]/x:att[@value=\"1.2\"]/@type"));
	}
	
	@Test
	public void testStringAttributeSavedAsString() {
		net.getRow(net).getTable().createColumn("test_str", String.class, false);
		net.getRow(net).set("test_str", "My String");
		net.getRow(net).getTable().createListColumn("test_list_str", String.class, false);
		net.getRow(net).set("test_list_str", Arrays.asList(new String[]{ "A", "B" }));
		write(net);
		assertEquals("string", evalString("/x:graph/x:att[@name=\"test_str\"]/@type"));
		assertEquals("My String", evalString("/x:graph/x:att[@name=\"test_str\"]/@value"));
		assertEquals("list", evalString("/x:graph/x:att[@name=\"test_list_str\"]/@type"));
		assertEquals("string", evalString("/x:graph/x:att[@name=\"test_list_str\"]/x:att[@value=\"B\"]/@type"));
	}
	
	@Test
	public void testBooleanAttributeSavedAsBoolean() {
		net.getRow(net).getTable().createColumn("test_bool", Boolean.class, false);
		net.getRow(net).set("test_bool", true);
		net.getRow(net).getTable().createListColumn("test_list_bool", Boolean.class, false);
		net.getRow(net).set("test_list_bool", Arrays.asList(new Boolean[]{ true, false }));
		write(net);
		// see http://www.cs.rpi.edu/research/groups/pb/punin/public_html/XGMML/draft-xgmml-20010628.html#BT
		assertEquals("boolean", evalString("/x:graph/x:att[@name=\"test_bool\"]/@type"));
		assertEquals("1", evalString("/x:graph/x:att[@name=\"test_bool\"]/@value")); // XGMML boolean [0|1]
		assertEquals("list", evalString("/x:graph/x:att[@name=\"test_list_bool\"]/@type"));
		assertEquals("boolean", evalString("/x:graph/x:att[@name=\"test_list_bool\"]/x:att[1]/@type"));
		assertEquals("1", evalString("/x:graph/x:att[@name=\"test_list_bool\"]/x:att[1]/@value"));
		assertEquals("0", evalString("/x:graph/x:att[@name=\"test_list_bool\"]/x:att[last()]/@value"));
	}
	
	@Test
	public void testSUIDAttNotSaved() throws UnsupportedEncodingException {
		// The SUID should NEVER be saved as an att tag 
		CyNetwork newNet = netFactory.createNetwork(SavePolicy.DO_NOT_SAVE);
		setRegistered(newNet, false); // It shouldn't make any difference either
		write(newNet);
		assertEquals(0, evalNumber("count(//x:att[@name=\"SUID\"])"));
	}
	
	@Test
	public void testTopNetworkSavedEvenIfDoNotSavePolicy() throws UnsupportedEncodingException {
		// The network that is passed to the writer should always be saved, even if its save policy is DO_NOT_SAVE,
		// because it doesn't make sense to prevent an app from exporting it if the app explicitly wants to do so.
		CyNetwork newNet = netFactory.createNetwork(SavePolicy.DO_NOT_SAVE);
		setRegistered(newNet, false); // It shouldn't make any difference either
		write(newNet);
		// Test:
		assertEquals(1, evalNumber("count(//x:graph)")); // No nested graph elements
		assertEquals(""+newNet.getSUID(), evalString("/x:graph/@id"));
	}
	
	@Test
	public void testNetworkPointerSavedIfSameRootNetwork() throws UnsupportedEncodingException {
		// Create a subnetwork from the same root
		CySubNetwork sn1 = rootNet.addSubNetwork(); // Of course the save policy cannot be DO_NOT_SAVE!
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
		CyNetwork net2 = netFactory.createNetwork();
		// Set it as network pointer
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(net2);
		write(net);
		// Test
		assertEquals(1, evalNumber("count(//x:graph)")); // no nested graph elements
	}
	
	@Test
	public void testNetworkPointerIgnoredIfDoNotSavePolicy() throws UnsupportedEncodingException {
		// Create a subnetwork from another root network
		CyNetwork sn = rootNet.addSubNetwork(SavePolicy.DO_NOT_SAVE);
		setRegistered(sn, true); // Ignore even if registered!
		// Set it as network pointer
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(sn);
		write(net);
		// Test
		assertEquals(1, evalNumber("count(//x:graph)")); // no nested graph elements
		assertEquals(""+net.getSUID(), evalString("/x:graph/@id"));
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
	
	@Test
	public void testNoGraphicsElementWhenNoView() {
		write(net);
		assertEquals(0, evalNumber("count(//x:node/x:graphics)"));
		assertEquals(0, evalNumber("count(//x:edge/x:graphics)"));
	}
	
	@Test
	public void testNumberOfGraphicsElementsOfExportedView() {
		write(view);
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics)"));
		assertEquals(EDGE_COUNT, evalNumber("count(//x:edge/x:graphics)"));
	}
	
	@Test
	public void testMandatoryGraphicsAttributes() {
		write(view);
		
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@x)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@y)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@z)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@w)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@h)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@width)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@outline)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@type)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node/x:graphics/@fill)"));
		
		assertEquals(EDGE_COUNT, evalNumber("count(//x:edge/x:graphics/@fill)"));
		assertEquals(EDGE_COUNT, evalNumber("count(//x:edge/x:graphics/@width)"));
	}
	
	@Test
	public void testGraphicsWithDefaultValues() {
		view.setViewDefault(NETWORK_BACKGROUND_PAINT, Color.BLACK);
		view.setVisualProperty(NETWORK_CENTER_X_LOCATION, 200d);
		view.setVisualProperty(NETWORK_CENTER_Y_LOCATION, 300d);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, 0.5d);

		view.setViewDefault(NODE_WIDTH, 100d);
		view.setViewDefault(NODE_HEIGHT, 200d);
		view.setViewDefault(NODE_BORDER_WIDTH, 8d);
		view.setViewDefault(NODE_BORDER_PAINT, Color.RED);
		view.setViewDefault(NODE_SHAPE, NodeShapeVisualProperty.DIAMOND);
		view.setViewDefault(NODE_FILL_COLOR, Color.GREEN);

		view.setViewDefault(EDGE_WIDTH, 4d);
		view.setViewDefault(EDGE_STROKE_UNSELECTED_PAINT, Color.BLUE);
		
		write(view);
		
		assertEquals("#000000", evalString("/x:graph/x:graphics/x:att[@name=\"NETWORK_BACKGROUND_PAINT\"]/@value"));
		assertEquals(200, evalNumber("/x:graph/x:graphics/x:att[@name=\"NETWORK_CENTER_X_LOCATION\"]/@value"));
		assertEquals(300, evalNumber("/x:graph/x:graphics/x:att[@name=\"NETWORK_CENTER_Y_LOCATION\"]/@value"));
		assertEquals(0.5, ((Double) eval("/x:graph/x:graphics/x:att[@name=\"NETWORK_SCALE_FACTOR\"]/@value", XPathConstants.NUMBER)).doubleValue(), 0.02);
		
		for (View<CyNode> v : view.getNodeViews()) {
			Long id = v.getModel().getSUID();
			assertEquals(100, evalNumber("//x:node[@id="+id+"]/x:graphics/@w"));
			assertEquals(200, evalNumber("//x:node[@id="+id+"]/x:graphics/@h"));
			assertEquals(8, evalNumber("//x:node[@id="+id+"]/x:graphics/@width"));
			assertEquals("#FF0000", evalString("//x:node[@id="+id+"]/x:graphics/@outline").toUpperCase());
			assertEquals("DIAMOND", evalString("//x:node[@id="+id+"]/x:graphics/@type"));
			assertEquals("#00FF00", evalString("//x:node[@id="+id+"]/x:graphics/@fill").toUpperCase());
		}
		for (View<CyEdge> v : view.getEdgeViews()) {
			Long id = v.getModel().getSUID();
			assertEquals(4, evalNumber("//x:edge[@id="+id+"]/x:graphics/@width"));
			assertEquals("#0000FF", evalString("//x:edge[@id="+id+"]/x:graphics/@fill").toUpperCase());
		}
	}
}
