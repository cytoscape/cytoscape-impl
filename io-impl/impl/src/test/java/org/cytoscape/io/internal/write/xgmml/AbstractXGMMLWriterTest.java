package org.cytoscape.io.internal.write.xgmml;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
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
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class AbstractXGMMLWriterTest {

	protected final int NODE_COUNT = 3;
	protected final int EDGE_COUNT = NODE_COUNT - 1;
	protected CyNetworkView view;
	protected CyNetwork net;
	protected CyRootNetwork rootNet;
	protected CyEdge dirEdge;
	protected CyEdge undirEdge;
	protected ByteArrayOutputStream out;
	protected GenericXGMMLWriter writer;
	protected XPathFactory xpathFactory;
	protected Document doc;
	protected XPath xpath;
	protected NetworkViewTestSupport netViewTestSupport;
	protected RenderingEngineManager renderingEngineMgr;
	protected UnrecognizedVisualPropertyManager unrecogVisPropMgr;
	protected TaskMonitor tm;
	protected CyRootNetworkManager rootNetMgr;
	protected CyNetworkManager netMgr;
	protected CyNetworkFactory netFactory;
	protected VisualMappingManager vmMgr;

	@Before
	public void init() {
		this.netViewTestSupport = new NetworkViewTestSupport();
		this.netMgr = mock(CyNetworkManager.class);
		this.netFactory = netViewTestSupport.getNetworkFactory();
		this.rootNetMgr = netViewTestSupport.getRootNetworkFactory();
		
		this.vmMgr = mock(VisualMappingManager.class);
		VisualStyle style = mock(VisualStyle.class);
		when(style.getTitle()).thenReturn("default");
		when(vmMgr.getDefaultVisualStyle()).thenReturn(style);
		when(vmMgr.getVisualStyle(any(CyNetworkView.class))).thenReturn(style);
		
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
	
	protected String getElementId(String elementPath) {
		String id = evalString(elementPath + "/@id"); // Try the id attribute first
		
		if (id == null || id.isEmpty()) { // It could be an XLink...
			id = evalString(elementPath + "/@xlink:href");
			id = id.replace('#', ' ').trim();
		}
		
		return id;
	}

	protected NodeList evalNodeList(String expression) {
		return (NodeList) eval(expression, XPathConstants.NODESET);
	}

	protected boolean evalBoolean(String expression) {
		return (Boolean) eval(expression, XPathConstants.BOOLEAN);
	}

	protected long evalNumber(String expression) {
		return ((Double) eval(expression, XPathConstants.NUMBER)).longValue();
	}

	protected String evalString(String expression) {
		return (String) eval(expression, XPathConstants.STRING);
	}

	protected Object eval(String expression, QName returnType) {
		try {
			XPathExpression expr = xpath.compile(expression);
			return expr.evaluate(doc, returnType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void write(CyIdentifiable netOrView) {
		try {
			writer = newWriter(netOrView);
			writer.run(tm);
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

	protected GenericXGMMLWriter newWriter(CyIdentifiable netOrView) {
		GenericXGMMLWriter writer = null;
		
		if (netOrView instanceof CyNetworkView)
			writer = new GenericXGMMLWriter(out, renderingEngineMgr, (CyNetworkView) netOrView, unrecogVisPropMgr,
					netMgr, rootNetMgr, vmMgr);
		else if (netOrView instanceof CyNetwork)
			writer = new GenericXGMMLWriter(out, renderingEngineMgr, (CyNetwork) netOrView, unrecogVisPropMgr, netMgr,
					rootNetMgr);
		else
			throw new IllegalArgumentException("netOrView must be a CyNetworkView or a CyNetwork!");
		
		return writer;
	}

	protected void createBaseNetwork() {
		net = netViewTestSupport.getNetwork();
		
		// Add nodes
		for (int i = 0; i < NODE_COUNT; i++) {
			net.addNode();
		}
		
		// Add 2 edges
		List<CyNode> allNodes = net.getNodeList();
		undirEdge = net.addEdge(allNodes.get(0), allNodes.get(1), false);
		dirEdge = net.addEdge(allNodes.get(1), allNodes.get(2), true);
		
		rootNet = rootNetMgr.getRootNetwork(net);
		
		// Root network is UNregistered by default
		setRegistered(rootNet, false);
		// Base network is registered by default
		setRegistered(net, true);
		
		view = netViewTestSupport.getNetworkViewFactory().createNetworkView(net);
	}

	protected void setRegistered(CyNetwork net, boolean registered) {
		when(this.netMgr.networkExists(net.getSUID())).thenReturn(registered);
	}

}