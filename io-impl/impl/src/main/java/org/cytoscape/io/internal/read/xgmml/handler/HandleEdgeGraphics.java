package org.cytoscape.io.internal.read.xgmml.handler;

import java.util.ArrayList;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdgeGraphics extends AbstractHandler {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        if (tag.equals("graphics")) {
        	manager.addGraphicsAttributes(manager.currentEdge, atts);
        } else if (tag.equals("att")) {
            // Handle special edge graphics attributes
            String name = atts.getValue("name");
            
            if (name != null && name.equals("edgeBend")) {
                manager.handleList = new ArrayList<String>();
                return ParseState.EDGEBEND;
            } else if (name != null && !name.equals("cytoscapeEdgeGraphicsAttributes")) {
            	manager.addGraphicsAttribute(manager.currentEdge, name, atts.getValue("value"));
            }
        }
        
        return current;
    }
}
