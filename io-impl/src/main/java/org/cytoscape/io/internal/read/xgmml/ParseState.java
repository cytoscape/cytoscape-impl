package org.cytoscape.io.internal.read.xgmml;

public enum ParseState {
    NONE("none"),
    RDF("RDF"),
    NETATT("Network Attribute"),
    NODEATT("Node Attribute"),
    EDGEATT("Edge Attribute"),

    // Types of attributes that require special handling
    LISTATT("List Attribute"),
    LISTELEMENT("List Element"),
    NODEGRAPHICS("Node Graphics"),
    EDGEGRAPHICS("Edge Graphics"),

    // Handle edge handles
    EDGEBEND("Edge Bend"),
    EDGEHANDLE("Edge Handle"),
    EDGEHANDLEATT("Edge Handle Attribute"),
    NODE("Node Element"),
    EDGE("Edge Element"),
    GROUP("Group"),
    GRAPH("Graph Element"),
    RDFDESC("RDF Description"),
    ANY("any");

    private String name;

    private ParseState(String str) {
        name = str;
    }

    public String toString() {
        return name;
    }
}
