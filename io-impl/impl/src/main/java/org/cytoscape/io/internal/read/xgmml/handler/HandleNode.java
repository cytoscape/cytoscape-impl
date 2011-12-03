package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNode extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		final String href = atts.getValue(ReadDataManager.XLINK, "href");
		
		if (href == null) {
			// Create the node
			final String id = atts.getValue("id");
			String label = atts.getValue("label");
			
			if (label == null)
				label = atts.getValue("name"); // For backwards compatibility
			
			final CyNode node = manager.createNode(id, label);
			manager.currentNode = node;
			
			if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
				node.getCyRow().set(CyNode.NAME, label);
			}
		} else {
			// The node might not have been created yet!
			// So just save the reference so it can be added to the network after the whole graph is parsed.
			manager.addElementLink(href, CyNode.class);
		}
		
		return current;
	}
}