package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGraph extends AbstractHandler {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
		
		final String name = getLabel(atts);
		if (name != null) manager.networkName = name;

		return current;
	}
	
	private String getLabel(Attributes att) {
		String label = att.getValue("label");
		if (label != null) return label;

		return att.getValue("id");
	}
}