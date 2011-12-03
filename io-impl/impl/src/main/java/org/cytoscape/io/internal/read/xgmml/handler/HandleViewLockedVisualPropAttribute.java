package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewLockedVisualPropAttribute extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		final String modelId = manager.getCurrentElementId();
		String name = atts.getValue("name");
		String value = atts.getValue("value");

		if (name != null && value != null)
			manager.addViewGraphicsAttribute(modelId, name, value, true);

		return current;
	}
}
