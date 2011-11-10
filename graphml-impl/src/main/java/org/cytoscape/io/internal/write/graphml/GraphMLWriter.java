package org.cytoscape.io.internal.write.graphml;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GraphMLWriter {
//	
//	private static final String GRAPHML = "graphml";
//	private static final String GRAPH = "graph";
//	private static final String ID = "id";
//	private static final String NODE = "node";
//	private static final String EDGE = "edge";
//	
//	private static final String SOURCE = "source";
//	private static final String TARGET = "target";
//	
//	private static final String directed = "edgedefault";
//	
//	
//	
//
//	private final CyNetwork network;
//	private final Writer writer;
//	private final TaskMonitor monitor;
//
//	public GraphMLWriter(final CyNetwork network, final Writer writer,
//			final TaskMonitor taskMonitor) {
//		this.network = network;
//		this.writer = writer;
//		this.monitor = taskMonitor;
//	}
//
//	public void write() throws IOException, ParserConfigurationException, TransformerException {
//		final Document graphMLDoc = createDocument();
//
//		
//		TransformerFactory transFactory = TransformerFactory.newInstance();
//		transFactory.setAttribute("indent-number", 4);
//		Transformer transformer = transFactory.newTransformer();
//		
//		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//		
//		DOMSource source = new DOMSource(graphMLDoc);
//		
//		StreamResult result = new StreamResult(writer); 
//		transformer.transform(source, result);
//		
//	}
//
//	private Document createDocument() throws ParserConfigurationException {
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = factory.newDocumentBuilder();
//		
//		Document document = builder.newDocument();
//		
//		Element root = document.createElement(GRAPHML);
//		
//		document.appendChild(root);
//		Element graphElm = document.createElement(GRAPH);
//		
//		// For now, everything is directed.
//		graphElm.setAttribute(directed, "directed");
//		graphElm.setAttribute(ID, network.getTitle());
//		
//		root.appendChild(graphElm);
//		
//		writeAttributes(Cytoscape.getNodeAttributes(), document, root);
//		writeAttributes(Cytoscape.getEdgeAttributes(), document, root);
//		writeAttributes(Cytoscape.getNetworkAttributes(), document, root);
//		
//		writeNodes(document, graphElm);
//		writeEdges(document, graphElm);
//		
//		return document;
//		
//	}
//	
//	private void writeAttributes(CyAttributes attr, Document doc, Element parent) {
//		final String[] nodeAttrNames = attr.getAttributeNames();
//		for(String attrName : nodeAttrNames) {
//			final Class<?> type = CyAttributesUtils.getClass(attrName, attr);
//			String tag = GraphMLAttributeDataTypes.getTag(type);
//			
//			if(tag == null)
//				tag = GraphMLAttributeDataTypes.STRING.getTypeTag();
//			
//			Element keyElm = doc.createElement("key");
//			keyElm.setAttribute("for", NODE);
//			keyElm.setAttribute("attr.name", attrName);
//			keyElm.setAttribute("attr.type", tag);
//			keyElm.setAttribute(ID, attrName);
//			parent.appendChild(keyElm);
//		}
//	}
//	
//	private void writeNodes(Document doc, Element parent) {
//		final List<CyNode> nodes = network.nodesList();
//		
//		for(final CyNode node: nodes) {
//			final Element nodeElm = doc.createElement(NODE);
//			nodeElm.setAttribute(ID, node.getIdentifier());
//			appendData(Cytoscape.getNodeAttributes(), doc, nodeElm, node.getIdentifier());
//			parent.appendChild(nodeElm);
//		}
//	}
//	
//	private void writeEdges(Document doc, Element parent) {
//		final List<CyEdge> edges = network.edgesList();
//		
//		for(final CyEdge edge: edges) {
//			final Element edgeElm = doc.createElement(EDGE);
//			edgeElm.setAttribute(SOURCE, edge.getSource().getIdentifier());
//			edgeElm.setAttribute(TARGET, edge.getTarget().getIdentifier());
//			appendData(Cytoscape.getEdgeAttributes(), doc, edgeElm, edge.getIdentifier());
//			parent.appendChild(edgeElm);
//		}
//	}
//	
//	private void appendData(CyAttributes attr, Document doc, Element parent, String id) {
//		final String[] attrNames = attr.getAttributeNames();
//		
//		for(String name: attrNames) {
//			Object val = attr.getAttribute(id, name);
//			if(val != null) {
//				Element dataElm = doc.createElement("data");
//				dataElm.setAttribute("key", name);
//				dataElm.setTextContent(val.toString());
//				parent.appendChild(dataElm);
//			}
//			
//		}
//	}

}
