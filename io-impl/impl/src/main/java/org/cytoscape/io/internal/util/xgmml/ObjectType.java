package org.cytoscape.io.internal.util.xgmml;

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

public enum ObjectType {
	LIST("list", "List"),
    STRING("string", "String"),
    REAL("real", "Double"),
    INTEGER("integer", "Integer"),
    LONG("integer", "Long"),
    BOOLEAN("boolean", "Boolean");

    private final String xgmmlValue;
    private final String cyValue;

    private ObjectType(String xgmmlValue, String cyValue) {
    	this.xgmmlValue = xgmmlValue;
    	this.cyValue = cyValue;
    }

    public String getXgmmlValue() {
        return xgmmlValue;
    }
    
    public String getCyValue() {
		return cyValue;
	}

    public static ObjectType fromXgmmlValue(String xgmmlValue) {
		for (ObjectType c : ObjectType.values()) {
			if (c.xgmmlValue.equalsIgnoreCase(xgmmlValue))
				return c;
		}

		return null;
    }
    
    public static ObjectType fromCyValue(String cyValue) {
		for (ObjectType c : ObjectType.values()) {
			if (c.cyValue.equalsIgnoreCase(cyValue))
				return c;
		}

		return null;
    }

    @Override
    public String toString() {
        return xgmmlValue;
    }
}
