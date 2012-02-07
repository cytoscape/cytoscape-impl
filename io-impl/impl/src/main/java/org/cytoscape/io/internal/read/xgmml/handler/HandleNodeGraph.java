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
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This handler parses the node graph elements to extract the network pointer id.
 */
public class HandleNodeGraph extends HandleGraph {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		manager.graphCount++;
		
		final CyNode node = manager.getCurrentNode();
		manager.getCompoundNodeStack().push(node);
		
		final String href = atts.getValue(ReadDataManager.XLINK, "href");
		Object netId = null;
		CyNetwork network = null;
		
		if (href != null) {
			// The network has already been created
			netId = AttributeValueUtil.getIdFromXLink(href);
			
			if (netId == null)
				logger.error("The node's network pointer will not be created: "
						+ "the network ID cannot be parsed from the XLink reference.");
			
			addCurrentNetwork(netId, network, atts);
		} else {
			netId = getId(atts);
			
			// Create network
			final CyRootNetwork rootNet = manager.getRootNetwork();
			network = rootNet.addSubNetwork();
			netId = addCurrentNetwork(netId, network, atts);
		}
		
		if (netId != null)
			manager.getCache().addNetworkPointer(node, netId);

		return current;
    }
}