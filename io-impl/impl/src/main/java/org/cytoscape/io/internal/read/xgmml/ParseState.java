package org.cytoscape.io.internal.read.xgmml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
