package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdgeHandleDone extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
		if (manager.edgeBendX != null && manager.edgeBendY != null
				&& manager.handleList != null) {
			manager.handleList.add(manager.edgeBendX + "," + manager.edgeBendY);
			manager.edgeBendX = null;
			manager.edgeBendY = null;
		}
		return current;
	}
}