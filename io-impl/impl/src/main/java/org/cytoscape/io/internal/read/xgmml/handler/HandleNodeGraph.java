package org.cytoscape.io.internal.read.xgmml.handler;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This handler parses nested node graph elements in order to create CyGroups.
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
			
			addCurrentNetwork(netId, network, atts, isRegistered(atts));
		} else {
			netId = getId(atts);
			
			// Create network
			final CyRootNetwork rootNet = manager.getRootNetwork();
			network = rootNet.addSubNetwork();
			netId = addCurrentNetwork(netId, network, atts, isRegistered(atts));
		}
		
		if (netId != null)
			manager.getCache().addNetworkPointer(node, netId);

		return current;
    }
	
	@Override
	protected boolean isRegistered(Attributes atts) {
		// 2.x nested graphs are group-networks, so they should be private.
		return super.isRegistered(atts) && manager.getDocumentVersion() >= 3.0;
	}
}