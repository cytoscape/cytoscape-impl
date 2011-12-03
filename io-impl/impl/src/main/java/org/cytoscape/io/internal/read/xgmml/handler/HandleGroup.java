package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGroup extends AbstractHandler {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
//		if (manager.groupMap == null)
//			manager.groupMap = new HashMap<CyNode, List<CyNode>>();
//		if (manager.currentGroupNode != null)
//			manager.groupStack.push(manager.currentGroupNode);
//		
//		manager.currentGroupNode = manager.currentNode;
//		manager.groupMap.put(manager.currentGroupNode, new ArrayList<CyNode>());
		
		return current;
	}
}
