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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewGraph extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		manager.graphCount++;
		Long netId = Long.valueOf(atts.getValue("cy:networkId"));
		
		// There should be only no nested graph tags!
		if (manager.graphCount > 1) {
			logger.warn("CyNetworkView XGMML does not support nested graphs. The nested graph will be ignored.");
			return current;
		}
		
		final String docVersion = atts.getValue("cy:documentVersion");

		if (docVersion != null)
			manager.setDocumentVersion(docVersion); // version 3.0+

		manager.setNetworkViewId(Long.valueOf(atts.getValue("id")));
		manager.setNetworkId(netId);
		manager.setCurrentElementId(netId);
		manager.setVisualStyleName(atts.getValue("cy:visualStyle"));
		manager.setRendererName(atts.getValue("cy:renderingEngine"));
		
		return current;
	}
}