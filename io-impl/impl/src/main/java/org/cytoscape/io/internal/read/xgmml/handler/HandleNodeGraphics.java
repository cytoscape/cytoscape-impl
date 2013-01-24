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

/**
 * handleNodeGraphics builds the objects that will remember the node graphic
 * information until we do the actual layout. Unfortunately, the way the readers
 * work, we can't apply the graphics information until we do the actual layout.
 */
public class HandleNodeGraphics extends AbstractHandler {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        if (tag.equals("graphics")) {
        	manager.addGraphicsAttributes(manager.getCurrentNode(), atts);
        } else if (tag.equals("att")) {
            // Handle special node graphics attributes
            String name = atts.getValue("name");

            if (name != null && !name.equals("cytoscapeNodeGraphicsAttributes")) {
                manager.addGraphicsAttribute(manager.getCurrentNode(), name, atts.getValue("value"));
            }
        }
        
        return current;
    }
}
