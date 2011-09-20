package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * handleGraphDone is called when we finish parsing the entire XGMML file. This
 * allows us to do deal with some cleanup line creating all of our groups, etc.
 */
public class HandleGraphDone extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException {
		// Resolve any unresolve node references
		if (manager.nodeLinks != null) {
			for (CyNode groupNode : manager.nodeLinks.keySet()) {
				if (!manager.getGroupMap().containsKey(groupNode)) {
					manager.getGroupMap().put(groupNode,
							new ArrayList<CyNode>());
				}
				List<CyNode> groupList = manager.getGroupMap().get(groupNode);
				for (String ref : manager.nodeLinks.get(groupNode)) {
					if (manager.idMap.containsKey(ref)) {
						groupList.add(manager.idMap.get(ref));
					}
				}
			}
		}
		return current;
	}
}
