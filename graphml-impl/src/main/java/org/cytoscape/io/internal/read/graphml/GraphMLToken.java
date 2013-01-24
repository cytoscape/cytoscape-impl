package org.cytoscape.io.internal.read.graphml;

/*
 * #%L
 * Cytoscape GraphML Impl (graphml-impl)
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

public enum GraphMLToken {

	// Graph ML Tags
	ID("id"), GRAPH("graph"), EDGEDEFAULT("edgedefault"), DIRECTED("directed"),
	UNDIRECTED("undirected"), KEY("key"), FOR("for"), ALL("all"), ATTRNAME("attr.name"),
	ATTRTYPE("attr.type"), DEFAULT("default"), NODE("node"), EDGE("edge"),
	SOURCE("source"), TARGET("target"), DATA("data"), TYPE("type"),
	
	// Supported attribute data types
	INT("int", Integer.class), LONG("long", Long.class), FLOAT("float", Float.class), DOUBLE("double", Double.class),
	BOOLEAN("boolean", Boolean.class), STRING("string", String.class);
	
	private final String tag;
	final Class<?> dataType;
	
	private GraphMLToken(final String tag) {
		this(tag, null);
	}
	
	private GraphMLToken(final String tag, final Class<?> dataType) {
		this.tag = tag;
		this.dataType = dataType;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public Class<?> getDataType() {
		return this.dataType;
	}
	
	public Object getObjectValue(final String value) { 
		if(dataType == String.class)
			return value;
		else if(dataType == Double.class)
			return Double.parseDouble(value);
		
		return null;
	}
	
	public static GraphMLToken getType(final String tag) {
		for (GraphMLToken token : GraphMLToken.values()) {
			if(token.getTag().equals(tag))
				return token;
		}
		return null;
	}
}
