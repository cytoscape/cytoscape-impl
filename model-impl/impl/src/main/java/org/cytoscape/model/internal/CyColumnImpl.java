package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;


/** This class describes a column in a CyTable. */
final class CyColumnImpl implements CyColumn {
	private final CyTableImpl table;
	private String columnName;
	private final Class<?> columnType;
	private final Class<?> listElementType;
	private final VirtualColumnInfo virtualInfo;
	private final boolean isPrimaryKey;
	private final boolean isImmutable;
	private final Object defaultValue;

	CyColumnImpl(final CyTableImpl table, final String columnName, final Class<?> columnType,
		     final Class<?> listElementType, final VirtualColumnInfo virtualInfo,
		     final boolean isPrimaryKey, final boolean isImmutable, final Object defaultValue)
	{
		this.table           = table;
		this.columnName      = columnName;
		this.columnType      = columnType;
		this.listElementType = listElementType;
		this.virtualInfo     = virtualInfo;
		this.isPrimaryKey    = isPrimaryKey;
		this.isImmutable     = isImmutable;

		if ( defaultValue != null && !columnType.isAssignableFrom(defaultValue.getClass()) )
			throw new IllegalArgumentException("The type of the defaultValue (" + 
			                                   defaultValue.getClass().getName() + 
											   ") cannot be assigned to type of this column (" +
											   columnType.getName() + ")" );
		this.defaultValue    = defaultValue;
	}

	@Override
	public CyTable getTable() { return table; }

	/** @return the name of the column. */
	@Override
	public String getName() { return columnName; }

	@Override
	public void setName(final String newName) {
		if (newName == null)
			throw new NullPointerException("\"newName\" must not be null.");

		if (isImmutable)
			throw new IllegalArgumentException("can't rename an immutable column.");

		final String oldName = columnName;
		table.updateColumnName(oldName, newName);
		columnName = newName;
	}

	/** @return the data type of the column. */
	@Override
	public Class<?> getType() { return columnType; }

	/** @return the data type of the list elements if the column type is List.class otherwise null */
	@Override
	public Class<?> getListElementType() { return listElementType; }

	/** @return true if the column is the primary key, otherwise false. */
	@Override
	public boolean isPrimaryKey() { return isPrimaryKey; }

	/** @return true if the column is immutable i.e. cannot be deleted, otherwise false. */
	@Override
	public boolean isImmutable() { return isImmutable; }

	@Override
	public <T> List<T> getValues(final Class<? extends T> type) {
		if (type == null)
			throw new NullPointerException("type argument must not be null.");
		if (type != columnType)
			throw new IllegalArgumentException("expected " + columnType.getName()
							   + " got " + type.getName() + ".");
		return table.getColumnValues(columnName, type);
	}
	
	@Override
	public VirtualColumnInfo getVirtualColumnInfo() {
		return virtualInfo;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String toString() {
		return "CyColumn{ " + (isPrimaryKey ? "[PK] " : "") + columnName + " (" + columnType.getSimpleName() + ")"
				+ (isImmutable ? " IMMUTABLE" : "") + " }";
	}
}
