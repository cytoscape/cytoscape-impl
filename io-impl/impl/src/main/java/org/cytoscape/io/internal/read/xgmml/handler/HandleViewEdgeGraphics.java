package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewEdgeGraphics extends AbstractHandler {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        final String edgeId = manager.getCurrentElementId();
		
		if (tag.equals("graphics")) {
        	manager.addViewGraphicsAttributes(edgeId, atts, false);
        } else if (tag.equals("att")) {
            String name = atts.getValue("name");

            if (AttributeValueUtil.LOCKED_VISUAL_PROPS.equalsIgnoreCase(name))
            	return ParseState.LOCKED_VISUAL_PROP_ATT;
            
            String value = atts.getValue(AttributeValueUtil.ATTR_VALUE);
            
            if (name != null && value != null)
                manager.addViewGraphicsAttribute(edgeId, name, value, false);
        }
        
        return current;
    }
}