package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGroupDone extends AbstractHandler {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
		manager.currentNode = manager.currentGroupNode;
		
		if (!manager.groupStack.empty())
			manager.currentGroupNode = manager.groupStack.pop();
		else
			manager.currentGroupNode = null;
		
		return current;
	}
}