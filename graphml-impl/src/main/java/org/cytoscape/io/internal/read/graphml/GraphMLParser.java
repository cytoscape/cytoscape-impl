/**
 * A SAX Parser for GraphML data file.
 * @author Kozo.Nishida
 *
 */

package org.cytoscape.io.internal.read.graphml;

import static org.cytoscape.io.internal.read.graphml.GraphMLToken.ATTRNAME;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.ATTRTYPE;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.DATA;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.DIRECTED;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.EDGE;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.EDGEDEFAULT;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.GRAPH;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.ID;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.KEY;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.NODE;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.SOURCE;
import static org.cytoscape.io.internal.read.graphml.GraphMLToken.TARGET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GraphMLParser extends DefaultHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphMLParser.class);

	// Map of data type to nodes or edges
	private Map<String, String> datatypeMap;
	private Map<String, String> datanameMap;

	private CyTableEntry currentObject = null;

	// Attribute values
	private String currentAttributeID = null;
	private String currentAttributeKey = null;
	private String currentAttributeData = null;
	private String currentAttributeType = null;
	private String currentEdgeSourceName = null;
	private String currentEdgeTargetName = null;
	private String currentObjectTarget = null;

	private final TaskMonitor tm;
	private final CyNetworkFactory networkFactory;
	private final CyRootNetworkFactory rootNetworkFactory;

	private boolean directed = true;

	private final Stack<CyNetwork> networkStack;
	private final List<CyNetwork> cyNetworks;

	// Current CyNetwork. GraphML can have multiple networks in a file.
	private CyNetwork currentNetwork;

	private final Map<String, CyTableEntry> nodeid2CyNodeMap;
	
	private String lastTag;
	private CyNode lastNode;

	/**
	 * Main constructor for our parser. Initialize any local arrays. Note that
	 * this parser is designed to be as memory efficient as possible. As a
	 * result, a minimum number of local data structures
	 */
	GraphMLParser(final TaskMonitor tm, final CyNetworkFactory networkFactory,
			final CyRootNetworkFactory rootNetworkFactory) {
		this.tm = tm;
		this.networkFactory = networkFactory;
		this.rootNetworkFactory = rootNetworkFactory;

		networkStack = new Stack<CyNetwork>();
		cyNetworks = new ArrayList<CyNetwork>();

		datatypeMap = new HashMap<String, String>();
		datanameMap = new HashMap<String, String>();

		this.nodeid2CyNodeMap = new HashMap<String, CyTableEntry>();
	}

	CyNetwork[] getCyNetworks() {
		return cyNetworks.toArray(new CyNetwork[0]);
	}

	/********************************************************************
	 * Handler routines. The following routines are called directly from the SAX
	 * parser.
	 *******************************************************************/

	@Override
	public void startDocument() {
	}

	@Override
	public void endDocument() throws SAXException {
		// Clear
		datatypeMap.clear();
		datatypeMap = null;
	}

	@Override
	public void startElement(String namespace, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals(GRAPH.getTag()))
			createGraph(atts);
		else if (qName.equals(KEY.getTag())) {
			// Attribute definition found:
			datatypeMap.put(atts.getValue(ID.getTag()), atts.getValue(ATTRTYPE.getTag()));
			datanameMap.put(atts.getValue(ID.getTag()), atts.getValue(ATTRNAME.getTag()));
		} else if (qName.equals(NODE.getTag()))
			createNode(atts);
		else if (qName.equals(EDGE.getTag()))
			createEdge(atts);
		else if (qName.equals(DATA.getTag())) {
			currentAttributeKey = atts.getValue(KEY.getTag());
			currentAttributeType = datatypeMap.get(currentAttributeKey);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		lastTag = qName;

		if (qName != DATA.getTag())
			currentObjectTarget = null;

		// Check nest
		if (networkStack.size() > 1 && qName == GRAPH.getTag()) {
			networkStack.pop();
			currentNetwork = networkStack.peek();
		}
	}

	private void createGraph(final Attributes atts) {
		if (networkStack.size() == 0) {
			// Root network.
			currentNetwork = networkFactory.getInstance();
		} else {
			final CyNetwork parentNetwork = networkStack.peek();
			currentNetwork = rootNetworkFactory.convert(parentNetwork).addSubNetwork();
			if(lastTag != null && lastTag.equals(NODE.getTag())) {
				Collection<CyNode> toBeRemoved = new ArrayList<CyNode>();
				toBeRemoved.add(lastNode);
				
				for(CyNetwork network: networkStack)
					network.removeNodes(toBeRemoved);
			}
		}
		networkStack.push(currentNetwork);
		cyNetworks.add(currentNetwork);
		// parse directed or undirected
		String edef = atts.getValue(EDGEDEFAULT.getTag());
		directed = DIRECTED.getTag().equalsIgnoreCase(edef);
	}

	private void createNode(final Attributes atts) {
		// Parse node entry.
		currentObjectTarget = NODE.getTag();
		currentAttributeID = atts.getValue(ID.getTag());

		// If nested, add them to all parent networks.
		if (networkStack.size() > 1) {
			final CyNetwork rootNetwork = networkStack.get(0);
			currentObject = nodeid2CyNodeMap.get(currentAttributeID);
			if (currentObject == null) {
				currentObject = rootNetwork.addNode();
				currentObject.getCyRow().set(CyTableEntry.NAME, currentAttributeID);
				nodeid2CyNodeMap.put(currentAttributeID, currentObject);
			}

			for (CyNetwork network : networkStack) {
				if (network != rootNetwork) {
					((CySubNetwork) network).addNode((CyNode) currentObject);
				}
			}
		} else {
			// Root
			currentObject = nodeid2CyNodeMap.get(currentAttributeID);
			if (currentObject == null) {
				currentObject = currentNetwork.addNode();
				currentObject.getCyRow().set(CyTableEntry.NAME, currentAttributeID);
				nodeid2CyNodeMap.put(currentAttributeID, currentObject);
			}
		}

		lastNode = (CyNode) currentObject;
	}

	private void createEdge(final Attributes atts) {
		// Parse edge entry
		currentObjectTarget = EDGE.getTag();
		currentEdgeSourceName = atts.getValue(SOURCE.getTag());
		currentEdgeTargetName = atts.getValue(TARGET.getTag());
		final CyNode sourceNode = (CyNode) nodeid2CyNodeMap.get(currentEdgeSourceName);
		final CyNode targetNode = (CyNode) nodeid2CyNodeMap.get(currentEdgeTargetName);
		
		if (networkStack.size() > 1) {
			final CyNetwork rootNetwork = networkStack.get(0);
			currentObject = rootNetwork.addEdge(sourceNode, targetNode, directed);
			currentObject.getCyRow().set(CyTableEntry.NAME, currentEdgeSourceName + " (-) " + currentEdgeTargetName);
			currentObject.getCyRow().set(CyEdge.INTERACTION, "-");

			for (CyNetwork network : networkStack) {
				if (network != rootNetwork) {
					((CySubNetwork) network).addEdge((CyEdge) currentObject);
				}
			}
		} else {
			try {
				currentObject = currentNetwork.addEdge(sourceNode, targetNode, directed);
				currentObject.getCyRow().set(CyTableEntry.NAME, currentEdgeSourceName + " (-) " + currentEdgeTargetName);
				currentObject.getCyRow().set(CyEdge.INTERACTION, "-");
			} catch (Exception e) {
				logger.warn("Edge entry ignored: " + currentEdgeSourceName + " (-) " + currentEdgeTargetName, e);
			}
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		currentAttributeData = new String(ch, start, length);

		if (currentObjectTarget != null) {
			if (currentObjectTarget.equals(NODE.getTag()) || currentObjectTarget.equals(EDGE.getTag())) {
				if (currentAttributeType != null && currentAttributeData.trim().length() != 0) {
					final String columnName = datanameMap.get(currentAttributeKey);
					final CyColumn column = currentObject.getCyRow().getTable().getColumn(columnName);
					final GraphMLToken attrTag = GraphMLToken.getType(currentAttributeType);
					if (attrTag != null && attrTag.getDataType() != null) {
						if (column == null)
							currentObject.getCyRow().getTable().createColumn(columnName, attrTag.getDataType(), false);
						currentObject.getCyRow().set(columnName, attrTag.getObjectValue(currentAttributeData));
					}
				}
			}
		}
	}
}
