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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleGraphAttribute extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (atts == null)
			return current;
		
		manager.attState = current;
		ParseState nextState = current;

		String attName = atts.getValue(AttributeValueUtil.ATTR_NAME);
		
		if (attName == null && atts.getValue("value") == null)
			return current;

		// Look for "special" network attributes
		if (attName.equals("documentVersion")) {
			// Old format only!
			manager.setDocumentVersion(attributeValueUtil.getAttributeValue(atts, attName));
		} else if (attName.matches("backgroundColor|GRAPH_VIEW_ZOOM|GRAPH_VIEW_CENTER_[XY]|NODE_SIZE_LOCKED")) {
			String attValue = attributeValueUtil.getAttributeValue(atts, attName);
			manager.addGraphicsAttribute(manager.getCurrentNetwork(), attName, attValue);
		} else {
			manager.setCurrentElement(manager.getCurrentNetwork());
			nextState = attributeValueUtil.handleAttribute(atts);
		}

		if (nextState != ParseState.NONE)
			return nextState;

		return current;
	}
}
