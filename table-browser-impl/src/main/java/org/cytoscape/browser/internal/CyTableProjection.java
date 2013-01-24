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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;


/** This class provides a view into an immutable subset of rows and columns of an associated CyTable.
 *  (This view is only immutable when going through the methods of this class!)
 */
public final class CyTableProjection implements CyTable {
	private final CyTable underlyingTable;
	private final Set<String> columnNames;
	private final Set<Object> primaryKeys;
	private SavePolicy savePolicy;

	/**
	 *  @param underlyingTable  the reference table that we forward operations to.
	 *  @param columnNames      the subset of columns of the reference table that we provide
	 *                          access to via this warpper class
	 */
	public CyTableProjection(final CyTable underlyingTable, final Set<String> columnNames) {
		this.underlyingTable = underlyingTable;
		this.columnNames = columnNames;
		this.primaryKeys = new HashSet<Object>();
		checkColumnNames(columnNames);
	}

	private void checkColumnNames(final Set<String> columnNames) {
		for (final String columnName : columnNames) {
			if (underlyingTable.getColumn(columnName) == null)
				throw new IllegalArgumentException("\"" + columnName
				                                   + "\" is not a known column in the \""
				                                   + underlyingTable.getTitle()
				                                   + "\" table.");
		}
	}

	/** Adds a new row, contained in the underlying reference table, identified by its primary
	 *  key to this table. 
	 *
	 *  @param primaryKey  the primary key of the row that will be added
	 */
	public void add(final Object primaryKey) {
		if (!underlyingTable.rowExists(primaryKey))
			throw new IllegalArgumentException("\"" + primaryKey
			                                   + "\" is not a primary key for the underlying reference table \""
			                                   + underlyingTable.getTitle() + "\".");
		primaryKeys.add(primaryKey);
	}

	/** Warning: This returns the SUID of the underlying reference table!
	 *  @return the SUID of the underlying table
	 */
	@Override
	public Long getSUID() {
		return underlyingTable.getSUID();
	}

	public Set<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * A public CyTable is a table that is accessible to the user through the user
	 * interface.  Private or non-public CyTables will not be visible to the user from the
	 * normal user interface, although they will be accessible to app writers through the API.
	 *
	 * @return Whether or not this CyTable should be publicly accessible.
	 */
	@Override
	public boolean isPublic() { return false; }

	/** The table can be deleted if this returns Mutability.MUTABLE, otherwise it cannot be
	 *  deleted!
	 *  @return the current mutablity state
	 */
	@Override
	public CyTable.Mutability getMutability() { return CyTable.Mutability.PERMANENTLY_IMMUTABLE; }

	/**
	 * Returns a human readable name for the CyTable.
	 * @return A human readable name for the CyTable.
	 */
	@Override
	public String getTitle() { return underlyingTable.getTitle(); }

	/**
	 * Allows the title of the table to be set. The title is meant to be
	 * human readable and suitable for use in a user interface.
	 * @param title The human readable title for the CyTable suitable for use in a user
	 *        interface.
	 */
	@Override
	public void setTitle(final String title) {
		throw new UnsupportedOperationException("setTitle() method not supported.");
	}

	/**
	 * Returns the column type of the primary key for this table.
	 * @return The column type of the primary key for this table.
	 */
	@Override
	public CyColumn getPrimaryKey() {
		return underlyingTable.getPrimaryKey();
	}

	/**
	 * Returns the type of a column for this table.
	 * @param columnName  The name of the column whose type we desire.
	 * @return The column type of the column whose column name was provided, or null if there is
	 *         no column named "columnName".
	 */
	@Override
	public CyColumn getColumn(final String columnName) {
		return columnNames.contains(columnName) ? underlyingTable.getColumn(columnName) : null;
	}

	/**
	 * Returns the column types for all columns in this table.
	 * @return A set of {@link CyColumn} objects that describe all columns in this table.
	 */
	@Override
	public Collection<CyColumn> getColumns() {
		final List<CyColumn> columns = new ArrayList<CyColumn>(columnNames.size());
		for (final String columnName : columnNames)
			columns.add(underlyingTable.getColumn(columnName));

		return columns;
	}

	/**
	 * Will delete the column of the specified name.
	 * @param columnName The name identifying the attribute.
	 */
	@Override
	public void deleteColumn(String columnName) {
		throw new UnsupportedOperationException("deleteColumn() method not supported.");
	}

	/**
	 * Create a column of the specified name and the specified type. The column
	 * type is limited to Integer, Long, Double, String, and Boolean.
	 * @param columnName The name identifying the attribute.
	 * @param type The type of the column.
	 * @param isImmutable  if true, this column can never be deleted
	 */
	@Override
	public <T> void createColumn(String columnName, Class<?extends T> type,
	                             boolean isImmutable)
	{
		throw new UnsupportedOperationException("createColumn() method not supported.");
	}

	/**
	 * Create a column of the specified name and the specified type. The column
	 * type is limited to Integer, Long, Double, String, and Boolean.
	 * @param columnName The name identifying the attribute.
	 * @param type The type of the column.
	 * @param isImmutable  if true, this column can never be deleted
	 * @param defValue Default value for column 
	 */
	@Override
	public <T> void createColumn(String columnName, Class<?extends T> type,
	                             boolean isImmutable, T defValue)
	{
		throw new UnsupportedOperationException("createColumn() method not supported.");
	}

	/**
	 * Create a column of Lists with the specified name and the specified element type.
	 * The column type is limited to Integer, Long, Double, String, and Boolean.
	 * @param columnName The name identifying the attribute.
	 * @param listElementType The type of the elements of the list.
	 * @param isImmutable  if true, this column can never be deleted
	 */
	@Override
	public <T> void createListColumn(String columnName, Class<T> listElementType,
	                                 boolean isImmutable)
	{
		throw new UnsupportedOperationException("createListColumn() method not supported.");
	}

	/**
	 * Create a column of Lists with the specified name and the specified element type.
	 * The column type is limited to Integer, Long, Double, String, and Boolean.
	 * @param columnName The name identifying the attribute.
	 * @param listElementType The type of the elements of the list.
	 * @param isImmutable  if true, this column can never be deleted
	 * @param defValue Default value for column 
	 */
	@Override
	public <T> void createListColumn(String columnName, Class<T> listElementType,
	                                 boolean isImmutable, List<T> defValue)
	{
		throw new UnsupportedOperationException("createListColumn() method not supported.");
	}

	/**
	 * Returns the row specified by the primary key object and if a row
	 * for the specified key does not yet exist in the table, a new row
	 * will be created and the new row will be returned.
	 * @param primaryKey The primary key index of the row to return.
	 * @return The {@link CyRow} identified by the specified key or a new
	 * row identified by the key if one did not already exist.
	 */
	@Override
	public CyRow getRow(Object primaryKey) {
		if (!rowExists(primaryKey))
			throw new UnsupportedOperationException("row creation is not supported by CyTableProjection.");
		
		return underlyingTable.getRow(primaryKey);
	}

	/**
	 * Returns true if a row exists for the specified primary key and false otherwise. 
	 * @param primaryKey The primary key index of the row.
	 * @return True if a row exists for the specified primary key and false otherwise. 
	 */
	@Override
	public boolean rowExists(final Object primaryKey) {
		return primaryKeys.contains(primaryKey);
	}

	/**
	 * Return a list of all the rows stored in this data table.
	 * @return a list of all the rows stored in this data table.
	 */
	@Override
	public List<CyRow> getAllRows() {
		final List<CyRow> rows = new ArrayList<CyRow>();

		for (final Object primaryKey : primaryKeys)
			rows.add(new CyRowProjection(this, underlyingTable.getRow(primaryKey)));

		return rows;
	}

	/**
	 * Returns a descriptive message for certain internal errors.  Please
	 * note that only the very last message will be retrieved.
	 * @return if available, a message describing an internal error, otherwise null
	 */
	@Override
	public String getLastInternalError() {
		return underlyingTable.getLastInternalError();
	}

	/** Returns all the rows of a specified column that contain a certain value for that column.
	 *  @param columnName  the column for which we want the rows
	 *  @param value       the value for which we want the rows that contain it
	 *  @return the rows, if any that contain the value "value" for the column "columnName"
	 */
	@Override
	public Collection<CyRow> getMatchingRows(final String columnName, final Object value) {
		final ArrayList<CyRow> matchingRows = new ArrayList<CyRow>();
		final String primaryKey = underlyingTable.getPrimaryKey().getName();
		final Class<?> primaryKeyType = underlyingTable.getPrimaryKey().getType();

		for (final CyRow row : underlyingTable.getMatchingRows(columnName, value)) {
			if (primaryKeys.contains(row.get(primaryKey, primaryKeyType)))
				matchingRows.add(row);
		}

		return matchingRows;
	}

	@Override
	public int countMatchingRows(final String columnName, final Object value) {
		return underlyingTable.countMatchingRows(columnName, value);
	}

	/** Returns the number of rows in this table.
	 *  @return The number if rows in the table.
	 */
	@Override
	public int getRowCount() 
	{
		return primaryKeys.size();
	}

	/** Adds a "virtual" column to the the current table.
	 *  @param virtualColumn  the name of the new virtual column, if this name already exists,
	 *                        new column names with -1, -2 and so appended to this name on will
	 *                        be tried until a nonexisting name will be found
	 *  @param sourceColumn   the name of the column in "sourceTable" that will be mapped to
	 *                        "virtualColumn"
	 *  @param sourceTable    the table that really contains the column that we're adding (all
	 *                        updates and lookups of this new column will be redirected to here)
	 *  @param targetJoinKey  the column in current table that will be used for the join
	 *  @param isImmutable    if true, this column cannot be deleted
	 *  @return the actual name of the new virtual column
	 *  Note: The types of "sourceJoinKey" and "targetJoinKey" have to be identical.
	 */
	@Override
	public String addVirtualColumn(String virtualColumn, String sourceColumn,
				       CyTable sourceTable, String targetJoinKey, boolean isImmutable)
	{
		throw new UnsupportedOperationException("addVirtualColumn() method not supported.");
	}

	/** Adds all columns in another table as "virtual" columns to the the current table.
	 *  @param sourceTable    the table that really contains the column that we're adding (all
	 *                        updates and lookups of this new column will be redirected to here)
	 *  @param targetJoinKey  the column in current table that will be used for the join
	 *  @param isImmutable    if true, these columns cannot be deleted
	 *  Note: The types of "sourceJoinKey" and "targetJoinKey" have to be identical.  Also none
	 *        of the column names in "sourceTable" must exist in the current table!
	 */
	@Override
	public void addVirtualColumns(CyTable sourceTable, String targetJoinKey, boolean isImmutable)
	{
		throw new UnsupportedOperationException("addVirtualColumns() method not supported.");
	}
	
	/**
	 * Returns how (or if) this CyTable should be saved.
	 * @return how (or if) this CyTable should be saved.
	 */
	@Override
	public SavePolicy getSavePolicy() { return savePolicy; }
	
	/**
	 * Sets how (or if) this CyTable should be saved.
	 * @param policy the policy to follow during the lifecycle of the CyTable.
	 */
	@Override
	public void setSavePolicy(final SavePolicy policy) {
		savePolicy = policy;
	}

	/** Swaps the contents and properties, like mutability etc. of "otherTable" with this table.
	 *  @param otherTable  the table that we're being swapped with.
	 *  Note: the one "property" that is not being swapped is the SUID.  Also, no events are being
	 *        fired to give any listners a chance to react to the exchange!
	 */
	@Override
	public void swap(final CyTable otherTable) {
		underlyingTable.swap(otherTable);
	}

	@Override
	public void setPublic(boolean isPublic) {
		throw new UnsupportedOperationException("setPublic(boolean isPublic) method not supported.");		
	}
	
	@Override
	public boolean deleteRows(Collection<?> primaryKeys) {
		throw new UnsupportedOperationException("deleteRows() method not supported.");		
	}
}
