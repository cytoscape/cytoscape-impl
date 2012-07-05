/*
 Copyright (c) 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.browser.internal;


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
