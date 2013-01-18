package org.cytoscape.io.internal.read.xgmml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
				typedValue = fromXGMMLBoolean("" + value);
			break;
		case REAL:
			if (value != null) {
				if (SUIDUpdater.isUpdatable(name))
					typedValue = Double.valueOf(value).longValue();
				else
					typedValue = Double.valueOf(value);
			}
			break;
		case INTEGER:
			if (value != null)
				typedValue = Integer.valueOf(value);
			break;
		case STRING:
			if (value != null) {
				// Make sure we convert our newlines and tabs back
//				typedValue = NEW_LINE_PATTERN.matcher(TAB_PATTERN.matcher(value).replaceFirst(TAB_STRING))
//						.replaceFirst(NEW_LINE_STRING);
				final String sAttr = value.replace("\\t", "\t");
				typedValue = sAttr.replace("\\n", "\n");
			}
			break;
		case LIST:
			typedValue = new ArrayList<Object>();
		default:
			break;
		}

		return typedValue;
	}
	
	private static final String TAB_STRING = "\t";
	private static final String NEW_LINE_STRING = "\n";
	private static final Pattern TAB_PATTERN = Pattern.compile("\\t");
	private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\n");
    
    public static boolean fromXGMMLBoolean(final String s) {
    	// Should be only "1", but let's be nice and also accept "true"
    	// http://www.cs.rpi.edu/research/groups/pb/punin/public_html/XGMML/draft-xgmml-20001006.html#BT
    	// We also accept "yes", because of Cy2 "has_nested_network" attribute
    	return s != null && s.matches("(?i)1|true|yes");
    }

    public static String toXGMMLBoolean(final Boolean value) {
    	return value != null && value ? "1" : "0";
    }
}
