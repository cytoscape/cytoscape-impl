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

import org.cytoscape.group.CyGroup;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdge extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		// Get the label, id, source and target
		Object id = null;
		String href = atts.getValue(ReadDataManager.XLINK, "href");
		boolean duplicate = false;
		
		if (href == null) {
			// Create the edge:
			id = getId(atts);
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
				CyNetwork curNet = manager.getCurrentNetwork();
				
				// We need to do this because of groups that are exported from Cytoscape 2.x.
				// The problem is that internal edges are duplicated in the document when the group is expanded,
				// but we don't want to create them twice.
				boolean checkDuplicate = manager.hasGroups() && manager.getDocumentVersion() > 0.0
						&& manager.getDocumentVersion() < 3.0;
				CyEdge edge = checkDuplicate ? manager.getCache().getEdge(id) : null;
				duplicate = edge != null;
				
				if (edge == null) {
					edge = manager.createEdge(sourceNode, targetNode, id, label, directed);
				} else if (curNet instanceof CySubNetwork && !curNet.containsEdge(edge)) {
					((CySubNetwork) curNet).addEdge(edge);
					manager.setCurrentElement(edge);
				}
				
				if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
					// This check is necessary, because meta-edges of 2.x Groups may be written
					// under the group subgraph, but the edge will be created on the root-network only.
					if (!curNet.containsEdge(edge))
						curNet = manager.getRootNetwork();
					
					if (curNet != null && curNet.containsEdge(edge)) {
						CyRow row = curNet.getRow(edge);
						row.set(CyNetwork.NAME, label);
						row.set(CyEdge.INTERACTION, interaction);
					}
					
					if (manager.getRootNetwork() != null && !manager.getRootNetwork().equals(curNet)) {
						CyRow row = manager.getRootNetwork().getRow(edge);
						row.set(CyNetwork.NAME, label);
						row.set(CyEdge.INTERACTION, interaction);
					}
				}
			} else {
				throw new SAXException("Cannot create edge from XGMML (id=" + id + " label=" + label + " source=" +
									   sourceId + " target=" + targetId + "): source or target node not found");
			}
		} else {
			// The edge might not have been created yet!
			// Save the reference so it can be added to the network after the whole graph is parsed.
			manager.addElementLink(href, CyEdge.class);
			id = XGMMLParseUtil.getIdFromXLink(href);
		}

		// Is this edge part of a group?
		final CyGroup group = manager.getCurrentGroup();
		
		if (group != null && !duplicate) {
			// There is a group node, so this edge must be an internal or external group edge.
			manager.addInnerEdge(group, id);
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
