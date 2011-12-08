package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
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
		String netId = null;
		CyNetwork network = null;
		
		if (href != null) {
			// The network has already been created
			netId = AttributeValueUtil.getIdFromXLink(href);
			
			if (netId == null)
				logger.error("The node's network pointer will not be created: "
						+ "the network ID cannot be parsed from the XLink reference.");
			
			addCurrentNetwork(netId, network, atts);
		} else {
			netId = getId(atts);
			
			// Create network
			final CyRootNetwork rootNet = manager.getRootNetwork();
			network = rootNet.addSubNetwork();
			netId = addCurrentNetwork(netId, network, atts);
		}
		
		if (netId != null)
			manager.getCache().addNetworkPointer(node.getSUID(), netId);

		return state;
    }
}