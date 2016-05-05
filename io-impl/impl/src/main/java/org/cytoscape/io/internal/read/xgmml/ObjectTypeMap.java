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

import java.awt.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.internal.util.xgmml.ObjectType;

public class ObjectTypeMap {

	// Should be only "1", but let's be nice and also accept "true"
	// http://www.cs.rpi.edu/research/groups/pb/punin/public_html/XGMML/draft-xgmml-20001006.html#BT
	// We also accept "yes", because of Cy2 "has_nested_network" attribute
	private static Pattern TRUE_PATTERN = Pattern.compile("(?i)1|true|yes");
	
    public ObjectType fromXgmml(final String cyType, final String xgmmlType) {
        ObjectType type = null;
        
        // First try to get the ObjectType from the custom "cy:type" (or "cy:elementType") attribute value
        if (cyType != null && !cyType.isEmpty())
        	type = ObjectType.fromCyValue(cyType);
        
        // Then try to get it from the standard (XGMML) "type" attribute, if necessary
        if (type == null)
        	type = ObjectType.fromXgmmlValue(xgmmlType);
        
        return type != null ? type : ObjectType.STRING;
    }
    
    /**
     * Return the Java class of the passed ObjectType.
     * 
     * @param type the ObjectType of the value
     * @param name the attribute name
     * @return the Java class
     */
    public Class<?> getClass(final ObjectType objType, final String name) {
		switch (objType) {
	        case LIST:    return List.class;
	        case BOOLEAN: return Boolean.class;
	        case REAL:    return SUIDUpdater.isUpdatable(name) ? Long.class : Double.class;
	        case INTEGER: return SUIDUpdater.isUpdatable(name) ? Long.class : Integer.class;
	        case LONG:    return Long.class;
	        case STRING:
	        default:      return String.class;
	    }
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
			if (value != null) {
				if (SUIDUpdater.isUpdatable(name))
					typedValue = Long.valueOf(value);
				else
					typedValue = Integer.valueOf(value);
			}
			break;
		case LONG:
			if (value != null)
				typedValue = Long.valueOf(value);
			break;
		case STRING:
			if (value != null) {
				// Make sure we convert our newlines and tabs back
				final String sAttr = value.replace("\\t", "\t");
				typedValue = sAttr.replace("\\n", "\n");
			}
			break;
		case LIST:
			typedValue = new ArrayList<>();
		default:
			break;
		}

		return typedValue;
	}
	
    public static boolean fromXGMMLBoolean(final String s) {
    	return s != null && TRUE_PATTERN.matcher(s).matches();
    }

    public static String toXGMMLBoolean(final Boolean value) {
    	return value != null && value ? "1" : "0";
    }
}
