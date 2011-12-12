package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNodeAttribute extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (atts == null) return current;
		manager.attState = current;

		// Since version 3.0, the CYS file's XGMML should not contain node or edge <att> tags,
		// since node and edge attributes are now serialized as CyTables.
		// Let's just add this condition for the unlikely scenario where another application generates
		// the CYS file with redundant <att> tags for 3.0+.
		if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
			// Is this a graphics override?
			String name = atts.getValue("name");

			// Check for blank attribute (e.g. surrounding a group)
			if (name == null && atts.getValue("value") == null) return current;

			if (manager.getDocumentVersion() < 3.0) {
				if (name.startsWith("node.")) {
					// It is a bypass attribute...
					// Writing locked visual properties as regular <att> tags is deprecated!
					name = name.replace(".", "").toLowerCase();
					String value = atts.getValue("value");
					manager.addGraphicsAttribute(manager.currentNode, name, value);
				} else if (name.equals("nested_network_id")) {
					// Handle 2.x nested network as network pointer
					final String netId = atts.getValue("value");
					final CyNode node = manager.currentNode;
					manager.getCache().addNetworkPointer(node.getSUID(), netId);
				}
			}

			manager.currentAttributes = manager.getCurrentNetwork().getRow(manager.currentNode);
			ParseState nextState = attributeValueUtil.handleAttribute(atts, manager.currentAttributes);

			if (nextState != ParseState.NONE) return nextState;
		}

		return current;
	}
}
