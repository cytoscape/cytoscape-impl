package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2011 - 2013 The Cytoscape Consortium
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


final class CyRowProjection implements CyRow {
	private final CyTableProjection tableProjection;
	private final CyRow underlyingRow;

	CyRowProjection(final CyTableProjection tableProjection, final CyRow underlyingRow) {
		this.tableProjection = tableProjection;
		this.underlyingRow   = underlyingRow;
	}
	/**
	 * Returns the value found for this row in the specified column
	 * with the specified type.
	 * @param columnName The name identifying the attribute.
	 * @param type The type of the column.
	 * @return the value found for this row in the specified column
	 * Please not that this method cannot be used to retrieve values that are Lists!
	 */
	public <T> T get(final String columnName, final Class<?extends T> type) {
		checkColumnName(columnName);
		return underlyingRow.get(columnName, type);
	}

	/**
	 * Returns the value found for this row in the specified column
	 * with the specified type.
	 * @param columnName The name identifying the attribute.
	 * @param type The type of the column.
	 * @param defValue A default value to be returned if the row has not been set. 
	 * @return the value found for this row in the specified column
	 * Please not that this method cannot be used to retrieve values that are Lists!
	 */
	public <T> T get(final String columnName, final Class<?extends T> type, T defValue) {
		checkColumnName(columnName);
		return underlyingRow.get(columnName, type,defValue);
	}

	private void checkColumnName(final String columnName) {
		if (!tableProjection.getColumnNames().contains(columnName))
			throw new IllegalArgumentException("\"" + columnName
							   + "\" is not a valid column in the \""
							   + tableProjection.getTitle() + "\" table.");
	}

	/**
	 * Returns the value found for this row in the specified column
	 * with the specified type.
	 * @param columnName The name identifying the attribute.
	 * @param listElementType  The type of the elements of the list that we wish to retrieve.
	 * @return the value found for this row in the specified column
	 * Please not that this method can only be used to retrieve values that are Lists!
	 */
	public <T> List<T> getList(final String columnName, final Class<T> listElementType) {
		checkColumnName(columnName);
		return underlyingRow.getList(columnName, listElementType);
	}

	/**
	 * Returns the value found for this row in the specified column
	 * with the specified type.
	 * @param columnName The name identifying the attribute.
	 * @param listElementType  The type of the elements of the list that we wish to retrieve.
	 * @param defValue A default value to be returned if the row has not been set. 
	 * @return the value found for this row in the specified column
	 * Please not that this method can only be used to retrieve values that are Lists!
	 */
	public <T> List<T> getList(final String columnName, final Class<T> listElementType, final List<T> defValue) {
		checkColumnName(columnName);
		return underlyingRow.getList(columnName, listElementType,defValue);
	}

	/**
	 * Set the specified column for this row to the specified value.
	 * To unset a column entry use null for value.
	 * @param columnName The name identifying the attribute.
	 * @param value The value to assign the specified column in this row
	 * Please note that if "value" is a List it is your responsibility that all the
	 * elements are of the type specified when the column was created with
	 * {@link CyTable#createListColumn}!
	 */
	public <T> void set(final String columnName, final T value) {
		checkColumnName(columnName);
		underlyingRow.set(columnName, value);
	}

	/**
	 * Indicates whether the column of the specified type contains
	 * a non-null value.
	 * @param columnName The name identifying the attribute.
	 * @return true if the value specified in this row at this column
	 * of the specified type is not null.
	 */
	public boolean isSet(final String columnName) {
		checkColumnName(columnName);
		return underlyingRow.isSet(columnName);
	}

	/**
	 * Returns a map of column names to Objects that contain the values
	 * contained in this Row.
	 * @return A map of column names to Objects that contain the values
	 * contained in this Row.
	 */
	public Map<String, Object> getAllValues() {
		final Map<String, Object> nameToValueMap = new HashMap<String, Object>();
		final Set<String> validNames = tableProjection.getColumnNames();
		for (final Map.Entry<String, Object> nameAndValue : underlyingRow.getAllValues().entrySet()) {
			if (validNames.contains(nameAndValue.getKey()))
				nameToValueMap.put(nameAndValue.getKey(), nameAndValue.getValue());
		}

		return nameToValueMap;
	}

	/**
	 * Note that the returned object may well not be of the type that get() for this column might
	 * return.  You should therefore almost always use get() instead!
	 * @return The row Object that represents the value in a column.
	 */
	public Object getRaw(final String columnName) {
		checkColumnName(columnName);
		return underlyingRow.getRaw(columnName);
	}

	/**
	 * Returns the {@link CyTable} that this row belongs to.
	 * @return the {@link CyTable} that this row belongs to.
	 */
	public CyTable getTable() {
		return  tableProjection;
	}
}
