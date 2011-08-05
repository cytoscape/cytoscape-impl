package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGroupNode extends AbstractHandler {
	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		String id = atts.getValue("id");
		String label = atts.getValue("label");
		String href = atts.getValue(ReadDataManager.XLINK, "href");

		// logger.debug("<node");
		if (href == null) {
			// Create the node
			manager.currentNode = attributeValueUtil.createNode(id, label);
		} else {
			id = href.substring(1);
		}

		// Note that we don't want to add the node to the group until later,
		// even if we know about it now, so that we can get the edges
		// associated with the group when we add it.

		// Add it to the list of nodes to be resolved for this group
		if (manager.currentGroupNode == null)
			throw new SAXException("No group to add node reference to");

		if (manager.idMap.containsKey(id)) {
			CyNode node = manager.idMap.get(id);
			List<CyNode> nodeList = manager.groupMap.get(manager.currentGroupNode);
			nodeList.add(node);
		} else {
			// Remember it for later -- we'll fix this up in handleGraphDone
			if (!manager.nodeLinks.containsKey(manager.currentGroupNode)) {
				manager.nodeLinks.put(manager.currentGroupNode, new ArrayList<String>());
			}

			List<String> links = manager.nodeLinks.get(manager.currentGroupNode);
			links.add(id);
		}
		
		return current;
	}
}
