package org.cytoscape.io.internal.read.xgmml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjectTypeMap {

    private Map<String, ObjectType> typeMap;

    public ObjectTypeMap() {
        typeMap = new HashMap<String, ObjectType>();

        for (ObjectType type : ObjectType.values())
            typeMap.put(type.getName(), type);
    }

    public ObjectType getType(String name) {
        final ObjectType type = typeMap.get(name);
        
        if (type != null)
            return type;
        else
            return ObjectType.NONE;
    }

    /**
     * Return the typed value for the passed value.
     * 
     * @param type the ObjectType of the value
     * @param value the value to type
     * @return the typed value
     */
    public Object getTypedValue(ObjectType type, String value) {

        switch (type) {
            case BOOLEAN:
                if (value != null) return new Boolean(value);
                break;
            case REAL:
                if (value != null) return new Double(value);
                break;
            case INTEGER:
                if (value != null) return new Integer(value);
                break;
            case STRING:
                if (value != null) {
                    // Make sure we convert our newlines and tabs back
                    String sAttr = value.replace("\\t", "\t");
                    sAttr = sAttr.replace("\\n", "\n");
                    return sAttr;
                }
                break;
            case LIST:
                return new ArrayList<Object>();
        }

        return null;
    }

}
