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
import org.cytoscape.model.CyNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleNodeAttribute extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (atts == null) return current;
		manager.attState = current;

		// Since version 3.0, the CYS file's XGMML should not contain node or edge <att> tags,
		// since node and edge attributes are now serialized as CyTables.
		// Let's just add this condition for the unlikely scenario where another application generates
		// the CYS file with redundant <att> tags for 3.0+.
		if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
			// Is this a graphics override?
			String name = atts.getValue("name");

			// Check for blank attribute (e.g. surrounding a group)
			if (name == null && atts.getValue("value") == null) return current;

			if (manager.getDocumentVersion() < 3.0) {
				if (name.startsWith("node.")) {
					// It is a bypass attribute...
					// Writing locked visual properties as regular <att> tags is deprecated!
					name = name.replace(".", "").toLowerCase();
					String value = atts.getValue("value");
					manager.addGraphicsAttribute(manager.getCurrentNode(), name, value);
				} else if (name.equals("nested_network_id")) {
					// Handle 2.x nested network as network pointer
					final String netId = atts.getValue("value");
					final CyNode node = manager.getCurrentNode();
					
					// Don't add a network pointer again, if there is already one,
					// because the first one may be a 2.x group-network
					if (netId != null && !netId.equals(manager.getCache().getNetworkPointerId(node)))
						manager.getCache().addNetworkPointer(node, netId);
				}
			}

			ParseState nextState = attributeValueUtil.handleAttribute(atts);

			if (nextState != ParseState.NONE) return nextState;
		}

		return current;
	}
}
