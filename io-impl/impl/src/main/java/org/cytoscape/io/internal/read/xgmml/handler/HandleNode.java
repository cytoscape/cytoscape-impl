package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNode extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
		String id = atts.getValue("id");
		String label = atts.getValue("label");
		
		if (label == null)
			label = atts.getValue("name"); // For backwards compatibility
		
		String href = atts.getValue(ReadDataManager.XLINK, "href");

		if (href != null) {
			throw new SAXException("Can't have a node reference outside of a group");
		}
		
		// Create the node
		manager.currentNode = attributeValueUtil.createNode(id, label);

		return current;
	}
}