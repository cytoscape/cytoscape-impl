package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.browser.internal.util.TableBrowserUtil;
import org.cytoscape.browser.internal.util.ValidatedObjectAndEditString;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;


public final class BrowserTableModel extends AbstractTableModel
									 implements RowsCreatedListener, RowsDeletedListener, TableAboutToBeDeletedListener {
	
	public static enum ViewMode {
		ALL,
		SELECTED,
		AUTO
	}

	private static final long serialVersionUID = -517521404005631245L;

	private final CyTable dataTable;
	private final Class<? extends CyIdentifiable> tableType;
	private final EquationCompiler compiler;

	private ViewMode viewMode;

	private List<String> attrNames;
	private List<CyRow> selectedRows;
	private Object[] rowIndexToPrimaryKey;
	private int maxRowIndex;

	private boolean disposed;

	private ReadWriteLock lock;

	public BrowserTableModel(final CyTable dataTable, final Class<? extends CyIdentifiable> tableType,
			final EquationCompiler compiler) {
		if (dataTable == null)
			throw new IllegalArgumentException("'dataTable' must not be null");
		
		this.dataTable = dataTable;
		this.compiler = compiler;
		this.viewMode = ViewMode.ALL; 
		this.tableType = tableType;

		attrNames = getAttributeNames(dataTable);
		lock = new ReentrantReadWriteLock();
		
		// add each row to an array to allow fast lookup from an index
		final Collection<CyRow> rows = dataTable.getAllRows();
		this.rowIndexToPrimaryKey = new Object[rows.size()]; 
		this.maxRowIndex = 0;
		final String primaryKey = dataTable.getPrimaryKey().getName();
		
		for (CyRow row : rows)
			rowIndexToPrimaryKey[maxRowIndex++] = row.getRaw(primaryKey);
	}

	private List<String> getAttributeNames(final CyTable table) {
		ArrayList<String> names = new ArrayList<>();
		
		for (CyColumn column : table.getColumns())
			names.add(column.getName());
		
		return names;
	}

	public CyTable getDataTable() {
		return dataTable;
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return ValidatedObjectAndEditString.class;
	}
	
	List<String> getAllAttributeNames() {
		return new ArrayList<>(attrNames);
	}

	@Override
	public int getRowCount() {
		final Collection<CyColumn> columns = dataTable.getColumns();
		if (columns.isEmpty())
			return 0;

		// Show selection mode OR all rows
		int count = 0;
		switch (viewMode) {
			case SELECTED:
				count = dataTable.getMatchingRows(CyNetwork.SELECTED, Boolean.TRUE).size();
				break;
			case ALL:
				count = dataTable.getRowCount();
				break;
			case AUTO:
				count = dataTable.getMatchingRows(CyNetwork.SELECTED, Boolean.TRUE).size();
				if (count == 0)
					count = dataTable.getRowCount();
				break;
		}
		
		return count;
	}

	// this should return the number of columns in model
	@Override
	public int getColumnCount() {
		return attrNames.size();
	}

	public Object getValueAt(final int rowIndex, final String columnName) {
		final CyRow row = getCyRow(rowIndex);
		return getValidatedObjectAndEditString(row, columnName);
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {	
		final String columnName = getColumnName(columnIndex);
		final CyRow row = getCyRow(rowIndex);	
		
		return getValidatedObjectAndEditString(row, columnName);
	}

	CyColumn getColumn(final int columnIndex)  {
		final String columnName = getColumnName(columnIndex);

		return dataTable.getColumn(columnName);
	}

	CyColumn getColumnByModelIndex(final int modelIndex)  {
		final String columnName = getColumnName(modelIndex);

		return dataTable.getColumn(columnName);
	}

	public CyRow getCyRow(final int rowIndex) {
		try {
			switch (viewMode) {
				case SELECTED:
					if (selectedRows == null)
						selectedRows = new ArrayList<CyRow>(dataTable.getMatchingRows(CyNetwork.SELECTED, true));
					return selectedRows.get(rowIndex);
				case ALL:
					return dataTable.getRow(rowIndexToPrimaryKey[rowIndex]);
				case AUTO:
					if (selectedRows == null)
						selectedRows = new ArrayList<CyRow>(dataTable.getMatchingRows(CyNetwork.SELECTED, true));
					if (selectedRows.size() > 0)
						return selectedRows.get(rowIndex);
					else
						return dataTable.getRow(rowIndexToPrimaryKey[rowIndex]);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
		
		return null;
	}

	private ValidatedObjectAndEditString getValidatedObjectAndEditString(final CyRow row, final String columnName) {
		if (row == null)
			return null;

		Object raw = row.getRaw(columnName);
		
		if (raw == null) {
			CyColumn column = row.getTable().getColumn(columnName);
			
			if (column != null)
				raw = column.getDefaultValue();
		}
		
		if (raw == null)
			return null;

		// Optimisation hack:
		final boolean isEquation = raw instanceof Equation;
		final Object cooked = !isEquation ? raw : getColumnValue(row, columnName);
		final String editString = createEditString(raw);
		
		if (cooked != null)
			return new ValidatedObjectAndEditString(cooked, editString, isEquation);

		final String lastInternalError = dataTable.getLastInternalError();
		
		return new ValidatedObjectAndEditString(cooked, editString, lastInternalError, isEquation);
	}

	@Override
	public void fireTableStructureChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableStructureChanged();
		} else {
			final AbstractTableModel model = (AbstractTableModel) this;
			SwingUtilities.invokeLater (new Runnable () {
				public void run() {
					model.fireTableStructureChanged();
				}
			});
		}
	}

	@Override
	public void fireTableDataChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableDataChanged();
		} else {
			final AbstractTableModel model = (AbstractTableModel) this;
			SwingUtilities.invokeLater (new Runnable () {
				public void run() {
					model.fireTableDataChanged();
				}
			});
		}
	}

	@Override
	public void fireTableChanged (final TableModelEvent event) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableChanged(event);
		} else {
			final AbstractTableModel model = (AbstractTableModel) this;
			SwingUtilities.invokeLater (new Runnable () {
				public void run() {
					model.fireTableChanged(event);
				}
			});
		}
	}

	private String createEditString(Object raw) {
		if (raw instanceof List) {
			StringBuilder builder = new StringBuilder();
			builder.append('[');
			boolean first = true;
			for (Object item : (List<?>) raw) {
				if (first) {
					first = false;
				} else {
					builder.append(',');
				}
				if (item instanceof String) {
					builder.append('"');
					escape(item.toString(), builder);
					builder.append('"');
				} else {
					builder.append(item.toString());
				}
			}
			builder.append(']');
			return builder.toString();
		}
		return raw.toString();
	}

	private void escape(String string, StringBuilder builder) {
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			switch (c) {
			case '\b':
				builder.append("\\b");
				break;
			case '\t':
				builder.append("\\t");
				break;
			case '\n':
				builder.append("\\n");
				break;
			case '\f':
				builder.append("\\f");
				break;
			case '\r':
				builder.append("\\r");
				break;
			case '\\':
				builder.append("\\\\");
				break;
			case '"':
				builder.append("\\\"");
				break;
			default:
				builder.append(c);
			}
		}
	}

	private static Object getColumnValue(final CyRow row, final String columnName) {
		final CyColumn column = row.getTable().getColumn(columnName);
		if (column.getType() == List.class) {
			final Class<?> listElementType = column.getListElementType();
			return row.getList(columnName, listElementType);
		} else{
			return row.get(columnName, column.getType());
		}
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		lock.writeLock().lock();
		try {
			if(!e.getSource().equals(this.dataTable))
				return;
	
			selectedRows = null;
	
			// add new rows to rowIndexToPrimaryKey array
			Object[] newRowIndex = new Object[rowIndexToPrimaryKey.length + e.getPayloadCollection().size()];
			System.arraycopy(rowIndexToPrimaryKey,0,newRowIndex,0,rowIndexToPrimaryKey.length);
			rowIndexToPrimaryKey = newRowIndex;
			for ( Object pk : e.getPayloadCollection() )
				rowIndexToPrimaryKey[maxRowIndex++] = pk;
	
			fireTableDataChanged();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void handleEvent(RowsDeletedEvent e) {
		lock.writeLock().lock();
		try {
			if (!e.getSource().equals(this.dataTable))
				return;
	
			int index=0;
			
			final Collection<CyRow> rows = dataTable.getAllRows();
			
			final String primaryKey = dataTable.getPrimaryKey().getName();

			rowIndexToPrimaryKey = new Object[rows.size()];
			
			for ( CyRow row : rows ) 
				rowIndexToPrimaryKey[index++] = row.getRaw(primaryKey);
			maxRowIndex = index;
			selectedRows = null;
			
	
			fireTableDataChanged();
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void handleEvent(TableAboutToBeDeletedEvent e) {
		lock.writeLock().lock();
		try {
			if (!e.getSource().equals(dataTable)) {
				return;
			}
			disposed = true;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Switch view mode.
	 * 
	 * @param viewMode
	 */
	void setViewMode(ViewMode viewMode) {
		selectedRows = null;
		final CyColumn selectedColumn = dataTable.getColumn(CyNetwork.SELECTED);
		
		if (viewMode != ViewMode.ALL && selectedColumn != null && selectedColumn.getType() == Boolean.class)
			this.viewMode = viewMode;
		else
			this.viewMode = ViewMode.ALL;
	}

	void updateViewMode() {
		fireTableDataChanged();
	}

	ViewMode getViewMode() {
		return viewMode;
	}

	public String getCyColumnName( final int column){
		return (String) dataTable.getColumns().toArray()[column];
	}

	@Override
	public String getColumnName(final int column) {
		return mapColumnIndexToColumnName(column);
	}

	int mapColumnNameToColumnIndex(final String columnName) {
		if (attrNames.contains(columnName))
			return attrNames.indexOf(columnName);
		
		return -1;
	}

	private String mapColumnIndexToColumnName(final int index) {		
		if (index <= attrNames.size())
			return attrNames.get(index);

		throw new ArrayIndexOutOfBoundsException();
	}

	CyRow getRow(final Object suid) {
		return dataTable.getRow(suid);
	}

	@Override
	public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
		final String text = (String)value;
		final CyRow row = getCyRow(rowIndex);
		final String columnName = mapColumnIndexToColumnName(columnIndex);
		final Class<?> columnType = dataTable.getColumn(columnName).getType();
		final Class<?> listElementType = dataTable.getColumn(columnName).getListElementType();

		if (text.isEmpty()) {
			if (!row.isSet(columnName))
				return;
			row.set(columnName, null);
		} else if (text.startsWith("=")) {
			final Map<String, Class<?>> attrNameToTypeMap = TableBrowserUtil.getAttNameToTypeMap(dataTable, null);
			
			if (compiler.compile(text, attrNameToTypeMap)) {
				final Equation eqn = compiler.getEquation();
				final Class<?> eqnType = eqn.getType();

				// Is the equation type compatible with the column type?
				if (eqnTypeIsCompatible(columnType, listElementType, eqnType)){
					row.set(columnName, eqn);
				}
				else { // The equation type is incompatible w/ the column type!
					final Class<?> expectedType = columnType == Integer.class ? Long.class : columnType;
					final String errorMsg = "Equation result type is "
						+ getUnqualifiedName(eqnType) + ", column type is "
						+ getUnqualifiedName(columnType) + ".";
					final Equation errorEqn = compiler.getErrorEquation(text, expectedType, errorMsg);
					row.set(columnName, errorEqn);
				}
			} else {
				final Class<?> eqnType = columnType == Integer.class ? Long.class : columnType;
				final String errorMsg = compiler.getLastErrorMsg();
				final Equation errorEqn = compiler.getErrorEquation(text, eqnType, errorMsg);
				row.set(columnName, errorEqn);
			}
		} else { // Not an equation!
			final List<Object> parsedData = TableBrowserUtil.parseCellInput(dataTable, columnName, value);
			
			if (parsedData.get(0) != null)
				row.set(columnName, parsedData.get(0));
			else {
				// Error!
				showErrorWindow(parsedData.get(1).toString());
				// + " should be an Integer (or the number is too big/small).");
			}
		}

		final TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
		fireTableChanged(event);
		fireTableDataChanged();
	}

	// Pop-up window for error message
	private static void showErrorWindow(final String errMessage) {
		JOptionPane.showMessageDialog(null, errMessage, "Invalid Value",
				JOptionPane.ERROR_MESSAGE);
	}

	private boolean eqnTypeIsCompatible(final Class<?> columnType, final Class<?> listElementType, 
	                                    final Class<?> eqnType) {
		if (columnType == eqnType)
			return true;
		if (columnType == String.class) // Anything can be trivially converted to a string.
			return true;
		if (columnType == Integer.class && (eqnType == Long.class || eqnType == Double.class))
			return true;
		if (columnType == Double.class && eqnType == Long.class)
			return true;
		if (columnType == Boolean.class && (eqnType == Long.class || eqnType == Double.class))
			return true;
		if (columnType != List.class || !columnType.isAssignableFrom(eqnType))
			return false;

		// HACK!!!!!!  We don't know the type of the List, but we can do some type checking
		// for our own builtins.  We need to do this as a negative evaluation in case
		// an App wants to add a new List function
		if (eqnType.getSimpleName().equals("DoubleList") && listElementType != Double.class)
			return false;

		if (eqnType.getSimpleName().equals("LongList") && 
		    (listElementType != Integer.class && listElementType != Long.class))
			return false;

		if (eqnType.getSimpleName().equals("BooleanList") && listElementType != Boolean.class)
			return false;

		if (eqnType.getSimpleName().equals("StringList") && listElementType != String.class)
			return false;

		return true;
	}

	private String getUnqualifiedName(final Class<?> type) {
		final String typeName = type.getName();
		final int lastDotPos = typeName.lastIndexOf('.');
		return lastDotPos == -1 ? typeName : typeName.substring(lastDotPos + 1);
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		CyColumn column = getColumnByModelIndex(columnIndex);
		if (column == null) {
			return false;
		}
		return !column.isPrimaryKey();
	}

	public boolean isPrimaryKey(int columnIndex) {
		CyColumn column = getColumnByModelIndex(columnIndex);
		if (column == null) {
			return false;
		}
		return column.isPrimaryKey();
	}

	void clearSelectedRows() {
		selectedRows = null;
	}

	void setColumnName(int index, String name) {
		attrNames.set(index, name);
	}

	void removeColumn(int index) {
		attrNames.remove(index);
	}

	void addColumn(String name) {
		attrNames.add(name);
	}

	public Class<? extends CyIdentifiable> getTableType() {
		return tableType;
	}
	
	public ReadWriteLock getLock() {
		return lock;
	}

	private static void dumpTable(final CyTable table) {
		System.out.println("Begin Table: " + table.getTitle());
		final Collection<CyColumn> cols = table.getColumns();
		for (final CyColumn col : cols) {
			System.out.print(col.getName());
			System.out.print('\t');
		}
		System.out.println();
		for (final CyColumn col : cols) {
			System.out.print(col.getType().getSimpleName());
			System.out.print('\t');
		}
		System.out.println();
		for (final CyRow row : table.getAllRows()) {
			for (final CyColumn col : cols) {
				System.out.print(row.getRaw(col.getName()));
				System.out.print('\t');
			}
			System.out.println();
		}
		System.out.println("End Table");
		System.out.println();
	}
}
