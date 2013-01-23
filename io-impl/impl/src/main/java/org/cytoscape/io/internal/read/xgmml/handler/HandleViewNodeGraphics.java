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

import static org.cytoscape.io.internal.read.xgmml.handler.AttributeValueUtil.LOCKED_VISUAL_PROPS;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewNodeGraphics extends AbstractHandler {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        final Object nodeId = manager.getCurrentElementId();
        
        if (nodeId == null) {
        	logger.error("Cannot parse node view: node id is null");
        	return current;
        }
		
		if (tag.equals("graphics")) {
        	manager.addViewGraphicsAttributes(nodeId, atts, false);
        } else if (tag.equals("att")) {
            String name = atts.getValue("name");

            if (LOCKED_VISUAL_PROPS.equalsIgnoreCase(name))
            	return ParseState.LOCKED_VISUAL_PROP_ATT;
            
            String value = atts.getValue("value");
            
            if (name != null && value != null)
                manager.addViewGraphicsAttribute(nodeId, name, value, false);
        }
        
        return current;
    }
}