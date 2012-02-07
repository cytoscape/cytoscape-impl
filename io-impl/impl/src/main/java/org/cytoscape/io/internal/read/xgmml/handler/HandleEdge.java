/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdge extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		// Get the label, id, source and target
		String href = atts.getValue(ReadDataManager.XLINK, "href");
		
		if (href == null) {
			// Create the edge:
			Object id = getId(atts);
			String label = getLabel(atts);
			Object sourceId = asLongOrString(atts.getValue("source"));
			Object targetId = asLongOrString(atts.getValue("target"));
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
			
			if (label == null || label.isEmpty())
				label = String.format("%s (%s) %s", sourceId, (directed ? "directed" : "undirected"), targetId);
			
			if (sourceNode != null && targetNode != null) {
				manager.createEdge(sourceNode, targetNode, id, label, interaction, directed);
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
	
	private Object asLongOrString(String value) {
		if (value != null) {
			value = value.trim();
			
			try {
				return Long.valueOf(value);
			} catch (NumberFormatException nfe) { }
		}
		
		return value;
	}
}
