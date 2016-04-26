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

/**
 * Routines to handle edge bends. There are two different formats for edge
 * bends. The original XGMML code encoded edge bends as: <att
 * name="edgeBend"> <att name="handle"> <att value="15277.6748046875"
 * name="x"/> <att value="17113.919921875" name="y"/> </att> <att
 * name="handle"> <att value="15277.6748046875" name="x"/> <att
 * value="17113.919921875" name="y"/> </att> </att>
 * 
 * In version 1.1, which was simplified to: <att name="edgeBend"> <att
 * name="handle" x="15277.6748046875" y="17113.919921875" /> <att
 * name="handle" x="15277.6748046875" y="17113.919921875" /> </att>
 */

/**
 * Handle the "handle" attribute. If this is an original format XGMML file (1.0)
 * we just punt to the next level down. If this is a newer format file, we
 * handle the attributes directly.
 */
public class HandleEdgeHandle extends AbstractHandler {
	
	private static final String HANDLE = "handle";

	
	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {

		final String name = attributeValueUtil.getAttribute(atts, "name");
		
		if (manager.getDocumentVersion() == 1.0) {
			// This is the outer "handle" attribute
			if (!name.equals(HANDLE)) {
				// OK, this is one of our "data" attributes
				if (attributeValueUtil.getAttributeValue(atts, "x") != null) {
					manager.edgeBendX = attributeValueUtil.getAttributeValue(atts, "x");
				} else if (attributeValueUtil.getAttributeValue(atts, "y") != null) {
					manager.edgeBendY = attributeValueUtil.getAttributeValue(atts, "y");
				} else {
					throw new SAXException("expected x or y value for edgeBend handle - got " + atts.getValue("name"));
				}
			}
		} else {
			// New format -- get the x and y values directly
			if (attributeValueUtil.getAttribute(atts, "x") != null)
				manager.edgeBendX = attributeValueUtil.getAttribute(atts, "x");
			
			if (attributeValueUtil.getAttribute(atts, "y") != null)
				manager.edgeBendY = attributeValueUtil.getAttribute(atts, "y");
			
			if (manager.edgeBendX != null && manager.edgeBendY != null) {
				if (manager.handleList == null)
					manager.handleList = new ArrayList<>();
				
				manager.handleList.add(manager.edgeBendX + "," + manager.edgeBendY);
				manager.edgeBendX = null;
				manager.edgeBendY = null;
			}
		}
		
		return current;
	}
}
