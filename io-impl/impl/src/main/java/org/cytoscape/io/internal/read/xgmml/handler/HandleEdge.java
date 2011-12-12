package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdge extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		// Get the label, id, source and target
		String href = atts.getValue(ReadDataManager.XLINK, "href");
		
		if (href == null) {
			// Create the edge:
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
	
			final boolean directed;
	
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
	
			if (sourceId != null)
				sourceNode = manager.getCache().getNode(sourceId);
			if (targetId != null)
				targetNode = manager.getCache().getNode(targetId);
	
			if (sourceNode == null && sourceAlias != null)
				sourceNode = manager.getCache().getNode(sourceAlias);
			if (targetNode == null && targetAlias != null)
				targetNode = manager.getCache().getNode(targetAlias);
			
			if (id == null || id.isEmpty())
				id = label;
			if (id == null || id.isEmpty())
				id = String.format("%s (%s) %s", sourceId, (directed ? "directed" : "undirected"), targetId);
			
			if (sourceNode != null && targetNode != null) {
				manager.currentEdge = manager.createEdge(sourceNode, targetNode, id, label, directed);
				
				if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
					CyRow row = manager.getCurrentNetwork().getRow(manager.currentEdge);
					row.set(CyEdge.NAME, label);
					row.set(CyEdge.INTERACTION, interaction);
				}
			} else {
				throw new SAXException("Cannot create edge from XGMML (id=" + id + " label=" + label + " source=" +
									   sourceId + " target=" + targetId + "): source or target node not found");
			}
		} else {
			// The edge might not have been created yet!
			// Save the reference so it can be added to the network after the whole graph is parsed.
			manager.addElementLink(href, CyEdge.class);
		}

		return current;
	}
}
