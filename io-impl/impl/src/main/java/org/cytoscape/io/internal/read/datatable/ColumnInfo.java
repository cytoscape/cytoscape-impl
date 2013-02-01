package org.cytoscape.io.internal.read.datatable;

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

import java.util.List;

public class ColumnInfo {

	private String name;
	private Class<?> type;
	private Class<?> elementType;
	private boolean isMutable;

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> getListElementType() {
		return elementType;
	}
	
	public void setListElementType(Class<?> type) {
		this.type = List.class;
		elementType = type;
	}

	public boolean isMutable() {
		return isMutable;
	}
	
	public void setMutable(boolean mutable) {
		isMutable = mutable;
	}
}
