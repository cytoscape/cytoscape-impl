/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.model.internal;


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
			throw new NullPointerException("\"newName\" must not be null!");

		if (isImmutable)
			throw new IllegalArgumentException("can't rename an immutable column!");

			
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
			throw new NullPointerException("type argument must not be null!");
		if (type != columnType)
			throw new IllegalArgumentException("expected " + columnType.getName()
							   + " got " + type.getName() + "!");
		return table.getColumnValues(columnName, type);
	}
	
	@Override
	public VirtualColumnInfo getVirtualColumnInfo() {
		return virtualInfo;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
