package org.cytoscape.webservice.ncbi.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImportNetworkTask<V> implements Callable<V> {

	private static final Logger logger = LoggerFactory.getLogger(ImportNetworkTask.class);

	private static final String TARGET_ID = "";
	private static final String TARGET_DB = "";
	private static final String TARGET_NAMES = "";
	
	private static final String GENE_ID_TAG = "Gene-track_geneid";

	private final InteractionDocNodeProcessor processor;

	final String[] ids;

	private final CyNetwork network;
	
	
	private final ConcurrentMap<String, CyNode> nodeName2CyNodeMap;
	private final Map<String, CyEdge> nodeName2CyEdgeMap;

	public ImportNetworkTask(final String[] ids, final CyNetwork network, final ConcurrentMap<String, CyNode> nodeName2CyNodeMap) {
		this.ids = ids;
		this.network = network;
		this.processor = new InteractionDocNodeProcessor();
		this.nodeName2CyNodeMap = nodeName2CyNodeMap;
		this.nodeName2CyEdgeMap = new HashMap<String, CyEdge>();
	}


	@Override
	public V call() throws Exception {
		final URL url = createURL();

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = url.openStream();

		final Document result = builder.parse(is);

		logger.debug("######## 1 Got Result for = " + url.toString());
		logger.debug("######## 2 Got Result for = " + result.getChildNodes().getLength());

		NodeList geneID = result.getElementsByTagName(GENE_ID_TAG);
		final String geneIDString = geneID.item(0).getTextContent();
		logger.debug("Gene ID ======== " + geneIDString);
		if(geneIDString == null)
			throw new NullPointerException("Could not find NCBI Gene ID for the entry.");
		
		// This is the center of spokes
		final CyNode centerNode = network.addNode();
		centerNode.getCyRow().set(CyTableEntry.NAME, geneIDString);
		this.nodeName2CyNodeMap.put(geneIDString, centerNode);
		
		final Set<String> idSet = new HashSet<String>();
		final NodeList ids = result.getElementsByTagName("Gene-commentary");
		final int dataSize = ids.getLength();

		boolean interactionFound = false;
		Node interactionNode = null;
		for (int i = 0; i < dataSize; i++) {
			// logger.debug("    GC = " +
			// ids.item(i).getChildNodes().getLength());
			NodeList children = ids.item(i).getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j).getNodeName().equals("Gene-commentary_heading")) {

					//logger.debug("HEADING = " + children.item(j).getTextContent());
					if (children.item(j).getTextContent().equals("Interactions")) {
						logger.debug("FOUND interactions");
						interactionFound = true;
						break;
					}
				}
			}
			if (interactionFound) {
				interactionNode = ids.item(i);
				break;
			}
		}

		is.close();
		is = null;

		if (interactionNode == null)
			logger.warn("Interacrtion Not found");
		else
			processInteraction(interactionNode, centerNode);
		return null;
	}

	private void processInteraction(Node node, CyNode centerNode) {
		NodeList children = node.getChildNodes();
		int dataSize = children.getLength();
		NodeList interactionList = null;
		for (int i = 0; i < dataSize; i++) {
			Node item = children.item(i);
			if (item.getNodeName().equals("Gene-commentary_comment")) {
				interactionList = item.getChildNodes();
				break;
			}
		}

		logger.debug("Num interactions: " + interactionList.getLength());
		processNode(interactionList, centerNode);
	}

	private void processNode(NodeList interactionList, CyNode centerNode) {
		int dataSize = interactionList.getLength();
		for (int i = 0; i < dataSize; i++) {
			final Node item = interactionList.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				logger.debug(item.getNodeName() + " Number = " + i);
				// logger.debug("   contents = " + item.getTextContent());

				processor.process(item);
				final String id = processor.getTargetID();
				if (id != null) {
					// Create actual nodes and edges here.
					CyNode targetNode = this.nodeName2CyNodeMap.get(id);;
					if(targetNode == null) {
						targetNode = network.addNode();
						nodeName2CyNodeMap.put(id, targetNode);
					}
					
					targetNode.getCyRow().set(CyTableEntry.NAME, id);
					logger.debug("New Node Name = " + id);
					final CyEdge newEdge = network.addEdge(centerNode, targetNode, false);
					newEdge.getCyRow().set(CyTableEntry.NAME, 
							centerNode.getCyRow().get(CyTableEntry.NAME, String.class) + " (" + processor.getInteractionType() + ") " 
							+ targetNode.getCyRow().get(CyTableEntry.NAME, String.class));
				}
			}
		}

	}


	private URL createURL() throws IOException {

		final StringBuilder builder = new StringBuilder();

		for (final String id : ids) {
			System.out.println("ID = " + id);
			if (id != null)
				builder.append(id + ",");
		}

		String urlString = builder.toString();
		urlString = urlString.substring(0, urlString.length() - 1);
		final URL url = new URL(EntrezRestClient.FETCH_URL + urlString);
		logger.debug("Import Query URL = " + url.toString());
		return url;
	}

}