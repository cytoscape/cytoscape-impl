package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This handler parses the node graph elements to extract the network pointer id.
 */
public class HandleNodeGraph extends HandleGraph {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		ParseState state = current;
		manager.graphCount++;
		
		final CyNode node = manager.currentNode;
		final String href = atts.getValue(ReadDataManager.XLINK, "href");
		final String netId;
		
		if (href != null) {
			// The network has already been created
			netId = AttributeValueUtil.getIdFromXLink(href);
		} else {
			netId = atts.getValue("id");
			// Create network
			final CyRootNetwork rootNet = manager.getRootNetwork();
			final CySubNetwork subNet = rootNet.addSubNetwork();
			addCurrentNetwork(netId, subNet);
		}
		
		if (netId != null) {
			manager.addNetworkPointer(node.getSUID(), netId);
		} else {
			logger.warn("The node's network pointer cannot be created, because the original network ID is null.");
		}

		return state;
    }
}