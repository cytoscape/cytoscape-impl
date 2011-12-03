package org.cytoscape.io.internal.read.xgmml.handler;

import static org.cytoscape.io.internal.read.xgmml.handler.AttributeValueUtil.LOCKED_VISUAL_PROPS;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewNodeGraphics extends AbstractHandler {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        final String nodeId = manager.getCurrentElementId();
		
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