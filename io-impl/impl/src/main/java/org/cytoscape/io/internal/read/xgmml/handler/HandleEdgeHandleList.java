package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class HandleEdgeHandleList extends AbstractHandler {

	@Override
    public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException {
        if (manager.handleList != null) {
            String list = "";

            for (int i = 0; i < manager.handleList.size(); i++) {
                if (i != (manager.handleList.size() - 1)) {
                    list += manager.handleList.get(i) + ";";
                } else {
                    list += manager.handleList.get(i);
                }
            }

            manager.addGraphicsAttribute(manager.getCurrentEdge(), "edgeHandleList", list);
            manager.handleList = null;
        }
        return current;
    }
}
