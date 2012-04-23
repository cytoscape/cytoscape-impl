package org.cytoscape.io.internal.write.xgmml;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XGMMLWriterTest {

	private final int NODE_COUNT = 3;
	
	private CyNetworkView view;
	private CyNetwork net;
	private CyRootNetwork rootNet;
	private CyEdge dirEdge;
	private CyEdge undirEdge;
	
	private ByteArrayOutputStream out;
	private XGMMLWriter writter;
	private XPathFactory xpathFactory;
	private Document doc;
	private XPath xpath;
	
	private NetworkViewTestSupport netViewTestSupport;
	private RenderingEngineManager renderingEngineMgr;
	private UnrecognizedVisualPropertyManager unrecogVisPropMgr;
	private TaskMonitor tm;
	private CyRootNetworkManager rootNetMgr;
	private CyNetworkManager netMgr;
	private CyNetworkFactory netFactory;

	@Before
	public void init(){
		this.netViewTestSupport = new NetworkViewTestSupport();
		this.netMgr = mock(CyNetworkManager.class); 
		this.netFactory = netViewTestSupport.getNetworkFactory();
		this.rootNetMgr = netViewTestSupport.getRootNetworkFactory();
		
		this.renderingEngineMgr = mock(RenderingEngineManager.class);
		when(this.renderingEngineMgr.getDefaultVisualLexicon()).thenReturn(
				new BasicVisualLexicon(new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property")));
		
		this.unrecogVisPropMgr = mock(UnrecognizedVisualPropertyManager.class);
		this.tm = mock(TaskMonitor.class);
		
		createBaseNetwork();
		
		this.out = new ByteArrayOutputStream();
		this.xpathFactory = XPathFactory.newInstance();
		this.doc = null;
	}
	
	// Session Network Format:
	
	@Test
	public void testNumberOfNodeElements() {
		write(rootNet, true);
		assertEquals(NODE_COUNT, evalNumber("count(/x:graph/x:att/x:graph/x:node)"));
		assertEquals(NODE_COUNT, evalNumber("count(//x:node)"));
	}
	
	@Test
	public void testNumberOfEdgeElements() {
		write(rootNet, true);
		assertEquals(2, evalNumber("count(/x:graph/x:att/x:graph/x:edge)"));
		assertEquals(2, evalNumber("count(//x:edge)"));
	}
	
	@Test
	public void testNodeHasNoAttElementInSessionFile() {
		write(rootNet, true);
		assertEquals(0, evalNumber("count(//x:node/x:att)"));
	}
	
	@Test
	public void testEdgeHasNoAttElementInSessionFile() {
		write(rootNet, true);
		assertEquals(0, evalNumber("count(//x:edge/x:att)"));
	}
	
	@Test
	public void testEdgeDirectedAttribute() {
		write(rootNet, true);
		assertEquals("0", evalString("//x:edge[@id="+undirEdge.getSUID()+"]/@cy:directed"));
		assertEquals("1", evalString("//x:edge[@id="+dirEdge.getSUID()+"]/@cy:directed"));
	}
	
	@Test
	public void testLabelEqualsIdByDefault() {
		write(rootNet, true);
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
		write(rootNet, true);
		// Now test
		assertEquals("Test Network", evalString("/x:graph/x:att/x:graph/@label"));
		for (CyNode n : net.getNodeList())
			assertEquals("NODE_"+n.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/@label"));
		for (CyEdge e : net.getEdgeList())
			assertEquals("EDGE_"+e.getSUID(), evalString("//x:edge[@id="+e.getSUID()+"]/@label"));
	}
	
	@Test
	public void testNoGraphicsElementInSessionFile() {
		write(rootNet, true);
		assertEquals(0, evalNumber("count(//x:node/graphics)"));
		assertEquals(0, evalNumber("count(//x:edge/graphics)"));
	}
	
	@Test
	public void testNetworkPointerFromSameRootNetwork(){
		// Create a subnetwork from the same root
		CyNetwork subNet = rootNet.addSubNetwork();
		// Set it as network pointer to 2 nodes
		CyNode n1 = net.getNodeList().get(0);
		CyNode n2 = net.getNodeList().get(1);
		n1.setNetworkPointer(subNet);
		n2.setNetworkPointer(subNet);
		write(rootNet, true);
		// Test
		assertEquals(2, evalNumber("count(/x:graph//x:node/x:att/x:graph)"));
		assertEquals("#"+subNet.getSUID(), evalString("//x:node[@id="+n1.getSUID()+"]/x:att/x:graph/@xlink:href"));
		assertEquals("#"+subNet.getSUID(), evalString("//x:node[@id="+n2.getSUID()+"]/x:att/x:graph/@xlink:href"));
	}
	
	@Test
	public void testNetworkPointerFromAnotherRootNetwork() throws UnsupportedEncodingException {
		// Create a subnetwork from another root network
		CyNetwork subNet = netFactory.createNetwork();
		// Set it as network pointer to 2 nodes
		CyNode n = net.getNodeList().get(0);
		n.setNetworkPointer(subNet);
		write(rootNet, true); // session format only!
		// Test
		String filename = SessionUtil.getXGMMLFilename(rootNetMgr.getRootNetwork(subNet)); // It's the name of the other root network file
		assertEquals(filename+"#"+subNet.getSUID(), evalString("//x:node[@id="+n.getSUID()+"]/x:att/x:graph/@xlink:href"));
	}
	
	@Test
	public void testNestedGraphReferenceHasNoChildren(){
		// Add a couple of network pointers
		CyNetwork subNet = rootNet.addSubNetwork();
		net.getNodeList().get(0).setNetworkPointer(subNet);
		net.getNodeList().get(1).setNetworkPointer(subNet);
		write(rootNet, true);
		// Test: nested XLink-graphs must have no children
		assertEquals(2, evalNumber("count(//x:node/x:att/x:graph[@xlink:href])"));
		assertEquals(0, evalNumber("count(//x:node/x:att/x:graph[@xlink:href]/*)"));
	}
	
	// Session Network View Format:
	
	@Test
	public void testNodeGraphicsElement() {
		// TODO
//		write(view, true);
//		assertEquals(NODE_COUNT, evalNumber("count(//x:node/graphics)"));
	}
	
	@Test
	public void testEdgeGraphicsElement() {
//		write(view, true);
		// TODO
	}
	
	// PRIVATE Methods:
	
	private NodeList evalNodeList(String expression) {
		return (NodeList) eval(expression, XPathConstants.NODESET);
	}
	
	private boolean evalBoolean(String expression) {
		return (Boolean) eval(expression, XPathConstants.BOOLEAN);
	}
	
	private long evalNumber(String expression) {
		return ((Double) eval(expression, XPathConstants.NUMBER)).longValue();
	}
	
	private String evalString(String expression) {
		return (String) eval(expression, XPathConstants.STRING);
	}
	
	private Object eval(String expression, QName returnType) {
		try {
			XPathExpression expr = xpath.compile(expression);
			return expr.evaluate(doc, returnType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void write(CyIdentifiable netOrView, boolean sessionFormat) {
		try {
			if (netOrView instanceof CyNetworkView)
				writter = new XGMMLWriter(out, renderingEngineMgr, (CyNetworkView)netOrView, unrecogVisPropMgr, netMgr, rootNetMgr);
			else if (netOrView instanceof CyNetwork)
				writter = new XGMMLWriter(out, renderingEngineMgr, (CyNetwork)netOrView, unrecogVisPropMgr, netMgr, rootNetMgr);
			else
				throw new IllegalArgumentException("netOrView must be a CyNetworkView or a CyNetwork!");
			
			writter.setSessionFormat(sessionFormat);
			writter.run(tm);
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		    domFactory.setNamespaceAware(true);
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    String s = out.toString("UTF-8");
		    System.out.println(s);
		    InputSource is = new InputSource(new StringReader(s));
			doc = builder.parse(is);
			xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				@Override
			    public String getNamespaceURI(String prefix) {
					if (prefix == null) throw new NullPointerException("Null prefix");
			        if (prefix.isEmpty() || "x".equals(prefix) || "xgmml".equals(prefix)) return "http://www.cs.rpi.edu/XGMML";
			        else if ("cy".equals(prefix)) return "http://www.cytoscape.org";
			        else if ("xlink".equals(prefix)) return "http://www.w3.org/1999/xlink";
			        else if ("rdf".equals(prefix)) return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
			        else if ("dc".equals(prefix)) return "http://purl.org/dc/elements/1.1/";
			        return XMLConstants.NULL_NS_URI;
			    }
				@Override
			    public String getPrefix(String uri) {
			        throw new UnsupportedOperationException();
			    }
				@Override
			    public Iterator<?> getPrefixes(String uri) {
			        throw new UnsupportedOperationException();
			    }
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createBaseNetwork() {
		view = netViewTestSupport.getNetworkView();
		net = view.getModel();
		rootNet = rootNetMgr.getRootNetwork(net);
		// Add nodes
		for (int i = 0; i < NODE_COUNT; i++) {
			net.addNode();
		}
		// Add 2 edges
		List<CyNode> allNodes = net.getNodeList();
		undirEdge = net.addEdge(allNodes.get(0), allNodes.get(1), false);
		dirEdge = net.addEdge(allNodes.get(1), allNodes.get(2), true);
		
		rootNet = rootNetMgr.getRootNetwork(net);
	}
}
