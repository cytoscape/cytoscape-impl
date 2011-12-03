package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGraphAttribute extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (atts == null)
			return current;
		
		manager.attState = current;
		ParseState nextState = current;

		String attName = atts.getValue(AttributeValueUtil.ATTR_NAME);
		
		if (attName == null && atts.getValue("value") == null)
			return current;

		// Look for "special" network attributes
		if (attName.equals("documentVersion")) {
			// Old format only!
			manager.setDocumentVersion(attributeValueUtil.getAttributeValue(atts, attName));
		} else if (attName.matches("backgroundColor|GRAPH_VIEW_ZOOM|GRAPH_VIEW_CENTER_[XY]|NODE_SIZE_LOCKED")) {
			String attValue = attributeValueUtil.getAttributeValue(atts, attName);
			manager.addNetworkGraphicsAttribute(attName, attValue);
		} else {
			manager.currentAttributes = manager.getCurrentNetwork().getCyRow();
			nextState = attributeValueUtil.handleAttribute(atts, manager.currentAttributes);
		}

		if (nextState != ParseState.NONE)
			return nextState;

		return current;
	}
}
