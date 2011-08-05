package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNetworkAttribute extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		manager.attState = current;
		ParseState nextState = current;

		String attName = atts.getValue(AttributeValueUtil.ATTR_NAME);

		// Look for "special" network attributes
		if (attName.equals("documentVersion")) {
			double docVersion = parseDocumentVersion(attributeValueUtil.getAttributeValue(atts, attName));
			if (docVersion > 0) manager.documentVersion = docVersion;
		} else if (attName.matches("backgroundColor|GRAPH_VIEW_ZOOM|GRAPH_VIEW_CENTER_X|GRAPH_VIEW_CENTER_Y")) {
			String attValue = attributeValueUtil.getAttributeValue(atts, attName);
			manager.addNetworkGraphicsAttribute(attName, attValue);
		} else {
			manager.objectTarget = manager.networkName;
			manager.currentAttributes = manager.network.getCyRow();
			nextState = attributeValueUtil.handleAttribute(atts, manager.currentAttributes);
		}

		if (nextState != ParseState.NONE) return nextState;

		return current;
	}

	private double parseDocumentVersion(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
			return 0.0;
		}
	}
}
