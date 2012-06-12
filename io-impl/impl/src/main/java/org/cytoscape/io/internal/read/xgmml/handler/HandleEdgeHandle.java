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
					manager.handleList = new ArrayList<String>();
				
				manager.handleList.add(manager.edgeBendX + "," + manager.edgeBendY);
				manager.edgeBendX = null;
				manager.edgeBendY = null;
			}
		}
		
		return current;
	}
}
