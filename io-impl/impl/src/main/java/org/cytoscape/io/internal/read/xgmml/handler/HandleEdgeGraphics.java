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

import java.util.ArrayList;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdgeGraphics extends AbstractHandler {
	private static final String EDGE_BEND = "edgeBend";

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (tag.equals("graphics")) {
			manager.addGraphicsAttributes(manager.getCurrentEdge(), atts);
		} else if (tag.equals("att")) {
			// Handle special edge graphics attributes
			final String name = atts.getValue("name");

			if (name != null && name.equals(EDGE_BEND)) {
				manager.handleList = new ArrayList<>();
				return ParseState.EDGE_BEND;
			} else if (name != null && !name.equals("cytoscapeEdgeGraphicsAttributes")) {
				manager.addGraphicsAttribute(manager.getCurrentEdge(), name, atts.getValue("value"));
			}
		}

		return current;
	}
}
