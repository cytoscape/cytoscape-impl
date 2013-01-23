package org.cytoscape.filter.internal.quickfind.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


/**
 * IndexType Type.
 * <p/>
 * Used to indicate whether we are currently indexing nodes or edges.
 *
 * @author Ethan Cerami.
 */
public class IndexType {
	private String name;

	/**
	 * Private Constructor. Enumeration Pattern.
	 *
	 * @param name Type Name.
	 */
	private IndexType(String name) {
		this.name = name;
	}

	/**
	 * Gets Type Name.
	 *
	 * @return Type Name.
	 */
	public String toString() {
		return name;
	}

	/**
	 * IndexType Type:  NODE_INDEX.
	 */
	public static final IndexType NODE_INDEX = new IndexType("NODE_INDEX");

	/**
	 * IndexType Type:  EDGE_INDEX.
	 */
	public static final IndexType EDGE_INDEX = new IndexType("EDGE_INDEX");
}
