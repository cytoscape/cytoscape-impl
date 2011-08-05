package org.cytoscape.io.internal.read.xgmml.handler;

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
        	manager.addGraphicsAttributes(manager.currentNode, atts);
        } else if (tag.equals("att")) {
            // Handle special node graphics attributes
            String name = atts.getValue("name");

            if (name != null && !name.equals("cytoscapeNodeGraphicsAttributes")) {
                manager.addGraphicsAttribute(manager.currentNode, name, atts.getValue("value"));
            }
        }
        
        return current;
    }
}
