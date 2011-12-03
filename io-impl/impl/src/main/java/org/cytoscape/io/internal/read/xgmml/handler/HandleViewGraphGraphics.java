package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleViewGraphGraphics extends AbstractHandler {

	@Override
	public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
		if (atts == null)
			return current;
		
		manager.attState = current;
		ParseState nextState = current;

		if (tag.equals("graphics")) {
        	manager.addViewGraphicsAttributes(manager.getNetworkId(), atts, false);
        } else if (tag.equals("att")) {
			String name = atts.getValue(AttributeValueUtil.ATTR_NAME);
			
			if (AttributeValueUtil.LOCKED_VISUAL_PROPS.equalsIgnoreCase(name))
            	return ParseState.LOCKED_VISUAL_PROP_ATT;
            
            String value = atts.getValue(AttributeValueUtil.ATTR_VALUE);
            
            if (name != null && value != null)
            	manager.addViewGraphicsAttribute(manager.getNetworkId(), name, value, false);
        }

		if (nextState != ParseState.NONE)
			return nextState;

		return current;
	}
}