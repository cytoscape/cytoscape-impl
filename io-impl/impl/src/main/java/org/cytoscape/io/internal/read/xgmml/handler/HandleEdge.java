package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdge extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		// Get the label, id, source and target
		String id = atts.getValue("id");
		String label = atts.getValue("label");
		String sourceId = atts.getValue("source");
		String targetId = atts.getValue("target");
		String isDirected = atts.getValue("cy:directed");
		String sourceAlias = null;
		String targetAlias = null;
		String interaction = ""; // no longer users

		// Parse out the interaction (if this is from Cytoscape)
		// parts[0] = source alias
		// parts[1] = interaction
		// parts[2] = target alias
		if (label != null) {
    		String[] parts = label.split("[()]");
    
    		if (parts.length == 3) {
    			sourceAlias = parts[0];
    			interaction = parts[1];
    			targetAlias = parts[2];
    		}
		}

		boolean directed;

		if (isDirected == null) {
			// xgmml files made by pre-3.0 cytoscape and strictly upstream-XGMML conforming files
			// won't have directedness flag, in which case use the graph-global directedness setting.
			//
			// (org.xml.sax.Attributes.getValue() returns null if attribute does not exists)
			//
			// This is the correct way to read the edge-directionality of non-cytoscape xgmml files as well.
			directed = manager.currentNetworkIsDirected;
		} else { // parse directedness flag
			directed = !"0".equals(isDirected);
		}

		CyNode sourceNode = null;
		CyNode targetNode = null;

		if (sourceId != null) sourceNode = manager.idMap.get(sourceId);
		if (targetId != null) targetNode = manager.idMap.get(targetId);

		if (sourceNode == null && sourceAlias != null) sourceNode = manager.idMap.get(sourceAlias);
		if (targetNode == null && targetAlias != null) targetNode = manager.idMap.get(targetAlias);

		if (sourceNode != null && targetNode != null) {
			manager.currentEdge = attributeValueUtil.createEdge(sourceNode,
																targetNode,
																id,
																label,
																interaction,
																directed);
		} else {
			throw new SAXException("Cannot create edge from XGMML (id=" + id + " label=" + label + " source=" +
								   sourceId + " target=" + targetId + "): source or target node not found");
		}

		return current;
	}
}
