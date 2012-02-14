/*
 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNode extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		final String href = atts.getValue(ReadDataManager.XLINK, "href");
		final CyNode node;
		String label = null;
		
		if (href == null) {
			// Create the node
			final Object id = getId(atts);
			label = atts.getValue("label");
			
			if (label == null)
				label = atts.getValue("name"); // For backwards compatibility
			
			node = manager.createNode(id, label);
			
			if ( label != null && (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) ) {
				manager.getCurrentNetwork().getRow(node).set(CyNode.NAME, label);
				
				if (manager.getRootNetwork() != null && manager.getCurrentNetwork() != manager.getRootNetwork()) {
					manager.getRootNetwork().getRow(node).set(CyNode.NAME, label);
				}
			}
		} else {
			// Try to get the node from the internal cache
			final Long id = AttributeValueUtil.getIdFromXLink(href);
			node = manager.getCache().getNode(id);
			
			if (node != null) {
				CyNetwork net = manager.getCurrentNetwork();
				
				if (net instanceof CySubNetwork)
					((CySubNetwork) net).addNode(node);
				else
					logger.error("Cannot add existing node \"" + id	+ "\" to a network which is not a CySubNetwork");
			} else {
				// The node might not have been created yet!
				// So just save the reference so it can be added to the network after the whole graph is parsed.
				manager.addElementLink(href, CyNode.class);
			}
		}
		
		return current;
	}
}
