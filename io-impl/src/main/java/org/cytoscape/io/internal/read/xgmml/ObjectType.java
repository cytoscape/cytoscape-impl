package org.cytoscape.io.internal.read.xgmml;

public enum ObjectType {
    NONE("none"), STRING("string"), BOOLEAN("boolean"), REAL("real"), INTEGER("integer"), LIST("list");

    private String name;

    private ObjectType(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
