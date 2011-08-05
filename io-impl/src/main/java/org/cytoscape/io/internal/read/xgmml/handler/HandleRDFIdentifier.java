package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleRDFIdentifier extends AbstractHandler {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
		manager.RDFIdentifier = null;
		return current;
	}
}