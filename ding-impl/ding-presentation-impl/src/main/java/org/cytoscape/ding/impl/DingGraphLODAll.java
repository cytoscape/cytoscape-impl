package org.cytoscape.ding.impl;

import org.cytoscape.ding.PrintLOD;

public class DingGraphLODAll extends PrintLOD {

    /**
     * textAsShape is called to determine if the text labels should be converted
     * from fonts to text
     * 
     * @param renderNodeCount
     *            the number of nodes
     * @param renderEdgeCount
     *            the number of edges
     * 
     * @return true if text should be converted to shapes, false otherwise
     */
    public boolean textAsShape(int renderNodeCount, int renderEdgeCount) {
	return false;
    }

}
