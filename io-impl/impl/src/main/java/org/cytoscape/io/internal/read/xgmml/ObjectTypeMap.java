package org.cytoscape.io.internal.read.xgmml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.internal.util.SUIDUpdater;

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
     * @param name the attribute name
     * @return the typed value
     */
    public Object getTypedValue(final ObjectType type, final String value, final String name) {
    	Object typedValue = null; 
    	
        switch (type) {
            case BOOLEAN:
                if (value != null)
                	typedValue = fromXGMMLBoolean(""+value);
                break;
            case REAL:
                if (value != null) {
                	if (SUIDUpdater.isUpdatable(name))
                		typedValue = new Long(value);
                	else
                		typedValue = new Double(value);
                }
                break;
            case INTEGER:
                if (value != null)
                	typedValue = new Integer(value);
                break;
            case STRING:
                if (value != null) {
                    // Make sure we convert our newlines and tabs back
                    String sAttr = value.replace("\\t", "\t");
                    typedValue = sAttr.replace("\\n", "\n");
                }
                break;
            case LIST:
            	typedValue = new ArrayList<Object>();
        }
        
        return typedValue;
    }
    
    public static boolean fromXGMMLBoolean(final String s) {
    	// should be only "1", but let's be nice and also accept "true"
    	// http://www.cs.rpi.edu/research/groups/pb/punin/public_html/XGMML/draft-xgmml-20001006.html#BT
    	return s != null && s.matches("(?i)1|true");
    }

    public static String toXGMMLBoolean(final Boolean value) {
    	return value != null && value ? "1" : "0";
    }
}
