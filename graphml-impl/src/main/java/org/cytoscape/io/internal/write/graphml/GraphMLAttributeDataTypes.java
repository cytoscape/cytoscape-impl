package org.cytoscape.io.internal.write.graphml;

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

public enum GraphMLAttributeDataTypes {
	BOOLEAN("boolean", Boolean.class), INTEGER("int", Integer.class), LONG(
			"long", Long.class), FLOAT("float", Float.class), DOUBLE("double",
			Double.class), STRING("string", String.class);

	private final String typeTag;
	private final Class<?> type;

	private GraphMLAttributeDataTypes(final String typeTag, final Class<?> type) {
		this.typeTag = typeTag;
		this.type = type;
	}

	public String getTypeTag() {
		return this.typeTag;
	}

	public static String getTag(Class<?> classType) {
		for (GraphMLAttributeDataTypes tag : values()) {
			if (classType.equals(tag.type))
				return tag.typeTag;
		}

		return null;
	}
}
