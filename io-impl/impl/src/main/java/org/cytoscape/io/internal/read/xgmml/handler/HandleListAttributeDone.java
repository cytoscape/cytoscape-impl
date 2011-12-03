package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleListAttributeDone extends AbstractHandler {

    @Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        try {
            if (manager.listAttrHolder != null) {
                manager.currentAttributes.set(manager.currentAttributeID, manager.listAttrHolder);
                manager.listAttrHolder = null;
            }
        } catch (Exception e) {
            String err = "XGMML attribute handling error for attribute '" + manager.currentAttributeID +
                         "' and object '" + manager.getCurrentNetwork() + "': " + e.getMessage();
            throw new SAXException(err);
        }
        
        return current;
    }
}
