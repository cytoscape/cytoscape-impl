package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdgeAttribute extends AbstractHandler {

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

			// Check for blank attribute
			if (name == null && atts.getValue("value") == null) return current;

			if (manager.getDocumentVersion() < 3.0) {
				// Writing locked visual properties as regular <att> tags is deprecated!
    			if (name.startsWith("edge.")) {
    				// It is a bypass attribute...
    				name = name.replace(".", "").toLowerCase();
    				String value = atts.getValue("value");
    				manager.addGraphicsAttribute(manager.currentEdge, name, value);
    			}
			}

			manager.currentAttributes = manager.getCurrentNetwork().getCyRow(manager.currentEdge);
			ParseState nextState = attributeValueUtil.handleAttribute(atts, manager.currentAttributes);

			if (nextState != ParseState.NONE) return nextState;
		}

		return current;
	}
}
