package org.cytoscape.io.internal.read.datatable;

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
