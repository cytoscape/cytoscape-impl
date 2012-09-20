package org.cytoscape.io.internal.read.xgmml;

public enum ParseState {
    NONE("none"),
    RDF("RDF"),
    NET_ATT("Network Table Column"),
    NODE_ATT("Node Table Column"),
    EDGE_ATT("Edge Table Column"),

    // Types of attributes that require special handling
    LIST_ATT("List Column Type"),
    LIST_ELEMENT("List Element"),
    NET_GRAPHICS("Network Graphics"),
    NODE_GRAPH("Node Graph"),
    NODE_GRAPHICS("Node Graphics"),
    EDGE_GRAPHICS("Edge Graphics"),
    LOCKED_VISUAL_PROP_ATT("Bypass Column"),

    // Handle edge handles
    EDGE_BEND("Edge Bend"),
    EDGE_HANDLE("Edge Handle"),
    EDGE_HANDLE_ATT("Edge Handle Column"),
    NODE("Node Element"),
    EDGE("Edge Element"),
    GRAPH("Graph Element"),
    RDF_DESC("RDF Description"),
    ANY("any");

    private String name;

    private ParseState(String str) {
        name = str;
    }

    public String toString() {
        return name;
    }
}
