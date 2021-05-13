package org.cytoscape.io.internal.read.xgmml.handler;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdgeAttribute extends AbstractHandler {
	
	private static final String NAME = "name";
	private static final String VALUE = "value";

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (atts == null)
			return current;

		manager.attState = current;

		// Since version 3.0, the CYS file's XGMML should not contain node or
		// edge <att> tags, since node and edge attributes are now serialized as CyTables.
		// Let's just add this condition for the unlikely scenario where another
		// application generates the CYS file with redundant <att> tags for 3.0+.
		if (!manager.isSessionFormat() || manager.getDocumentVersion() < 3.0) {
			// Is this a graphics override?
			String name = atts.getValue(NAME);

			// Check for blank attribute
			final String value = atts.getValue(VALUE);
			
			if (name == null && value == null)
				return current;
			
			if (manager.getDocumentVersion() < 3.0) {
				// Writing locked visual properties as regular <att> tags is deprecated!
				if (name.startsWith("edge.")) {
					// It is a bypass attribute...
					name = name.replace(".", "").toLowerCase();
					manager.addGraphicsAttribute(manager.getCurrentEdge(), name, value);
				}
			}

			ParseState nextState = attributeValueUtil.handleAttribute(atts);

			if (nextState != ParseState.NONE)
				return nextState;
		}

		return current;
	}
}
