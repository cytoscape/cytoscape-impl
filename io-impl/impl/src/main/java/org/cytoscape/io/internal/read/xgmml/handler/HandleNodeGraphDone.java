package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNodeGraphDone extends HandleGraphDone {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		graphDone();
		
		CyNode node = manager.getCompoundNodeStack().pop();
		manager.setCurrentNode(node);
		
		CyNetwork currentNet = manager.getCurrentNetwork();
		manager.setCurrentRow(currentNet.getRow(node));
		
		return ParseState.NODE_ATT;
	}
}