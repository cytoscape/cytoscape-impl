package org.cytoscape.view.table.internal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.presentation.property.table.TableMode;
import org.cytoscape.view.presentation.property.table.TableModeVisualProperty;
import org.cytoscape.view.table.internal.util.TableBrowserUtil;
import org.cytoscape.view.table.internal.util.ValidatedObjectAndEditString;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public final class BrowserTableModel extends AbstractTableModel
		implements RowsCreatedListener, RowsDeletedListener, TableAboutToBeDeletedListener {

	public static enum ViewMode {
		ALL,
		SELECTED,
		AUTO;
		
		// MKTODO should probably refactor this class to remove ViewMode and use TableMode directly
		public static ViewMode fromVisualPropertyValue(TableMode tableMode) {
			if (tableMode == TableModeVisualProperty.ALL)
				return ALL;
			else if (tableMode == TableModeVisualProperty.SELECTED)
				return SELECTED;
			else
				return AUTO;
		}
		
		public TableMode toVisualPropertyValue() {
			switch (this) {
				case ALL:      return TableModeVisualProperty.ALL;
				case SELECTED: return TableModeVisualProperty.SELECTED;
				default:       return TableModeVisualProperty.AUTO;
			}
		}
	}

	private final CyTableView tableView;
	private final CyTable dataTable;
	private final EquationCompiler compiler;

	private ViewMode viewMode;

	private List<String> attrNames;
	private List<CyRow> selectedRows;
	private Object[] rowIndexToPrimaryKey;
	private int maxRowIndex;

	private boolean disposed;

	private ReadWriteLock lock;

	public BrowserTableModel(CyTableView tableView, EquationCompiler compiler) {
		this.tableView = Objects.requireNonNull(tableView, "'tableView' must not be null");
		this.dataTable = tableView.getModel();
		this.compiler = compiler;
		this.viewMode = ViewMode.ALL; 

		attrNames = getAttributeNames(dataTable);
		lock = new ReentrantReadWriteLock();
		
		// add each row to an array to allow fast lookup from an index
		var rows = dataTable.getAllRows();
		this.rowIndexToPrimaryKey = new Object[rows.size()]; 
		this.maxRowIndex = 0;
		var pkName = dataTable.getPrimaryKey().getName();
		
		for (CyRow row : rows)
			rowIndexToPrimaryKey[maxRowIndex++] = row.getRaw(pkName);
	}

	private List<String> getAttributeNames(CyTable table) {
		var names = new ArrayList<String>();

		for (var column : table.getColumns())
			names.add(column.getName());

		return names;
	}

	public CyTableView getTableView() {
		return tableView;
	}
	
	public CyTable getDataTable() {
		return dataTable;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return ValidatedObjectAndEditString.class;
	}
	
	public List<String> getAllAttributeNames() {
		return new ArrayList<>(attrNames);
	}

	@Override
	public int getRowCount() {
		var columns = dataTable.getColumns();
		
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

	public Object getValueAt(int rowIndex, String columnName) {
		var row = getCyRow(rowIndex);
		
		return getValidatedObjectAndEditString(row, columnName);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {	
		var columnName = getColumnName(columnIndex);
		var row = getCyRow(rowIndex);
		
		return getValidatedObjectAndEditString(row, columnName);
	}

	public CyColumn getCyColumn(int columnIndex)  {
		var columnName = columnIndex >= 0 && columnIndex < getColumnCount() ? getColumnName(columnIndex) : null;
		
		return columnName != null ? dataTable.getColumn(columnName) : null;
	}

	public CyRow getCyRow(int rowIndex) {
		try {
			switch (viewMode) {
				case SELECTED:
					return rowIndex < getSelectedRows().size() ? getSelectedRows().get(rowIndex) : null;
				case ALL:
					return dataTable.getRow(rowIndexToPrimaryKey[rowIndex]);
				case AUTO:
					var selRows = getSelectedRows();
					
					if (!selRows.isEmpty())
						return rowIndex < selRows.size() ? selRows.get(rowIndex) : null;
					else
						return dataTable.getRow(rowIndexToPrimaryKey[rowIndex]);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
		
		return null;
	}
	
	CyRow getCyRow(Object suid) {
		return dataTable.getRow(suid);
	}
	
	public int indexOfRow(CyRow row) {
		try {
			switch (viewMode) {
				case SELECTED:
					return indexOfRowFromSelected(row);
				case ALL:
					return indexOfRowFromAll(row);
				case AUTO:
					if (!getSelectedRows().isEmpty())
						return indexOfRowFromSelected(row);
					else
						return indexOfRowFromAll(row);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	private int indexOfRowFromAll(CyRow row) {
		var pkName = dataTable.getPrimaryKey().getName();
		var pk = row.getRaw(pkName);
		
		for (int i = 0; i < rowIndexToPrimaryKey.length; i++) {
			if (pk.equals(rowIndexToPrimaryKey[i]))
				return i;
		}
		
		return -1;
	}
	
	private int indexOfRowFromSelected(CyRow row) {
		var selRows = getSelectedRows();
		
		for (int i = 0; i < selRows.size(); i++) {
			if (row.equals(selRows.get(i)))
				return i;
		}
		
		return -1;
	}
	
	private List<CyRow> getSelectedRows() {
		if (selectedRows == null)
			selectedRows = new ArrayList<>(dataTable.getMatchingRows(CyNetwork.SELECTED, true));
		
		return selectedRows;
	}

	private ValidatedObjectAndEditString getValidatedObjectAndEditString(CyRow row, String columnName) {
		if (row == null)
			return null;

		var raw = row.getRaw(columnName);

		if (raw == null) {
			CyColumn column = row.getTable().getColumn(columnName);

			if (column != null)
				raw = column.getDefaultValue();
		}

		if (raw == null)
			return null;

		// Optimisation hack:
		var isEquation = raw instanceof Equation;
		var cooked = isEquation ? getColumnValue(row, columnName) : raw;
		var editString = createEditString(raw);
		
		if (cooked != null)
			return new ValidatedObjectAndEditString(cooked, editString, isEquation);

		var lastInternalError = dataTable.getLastInternalError();
		
		return new ValidatedObjectAndEditString(cooked, editString, lastInternalError, isEquation);
	}

	@Override
	public void fireTableStructureChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableStructureChanged();
		} else {
			var model = (AbstractTableModel) this;
			SwingUtilities.invokeLater(() -> model.fireTableStructureChanged());
		}
	}

	@Override
	public void fireTableDataChanged() {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableDataChanged();
		} else {
			var model = (AbstractTableModel) this;
			SwingUtilities.invokeLater(() -> model.fireTableDataChanged());
		}
	}

	@Override
	public void fireTableChanged(TableModelEvent event) {
		if (SwingUtilities.isEventDispatchThread()) {
			super.fireTableChanged(event);
		} else {
			var model = (AbstractTableModel) this;
			SwingUtilities.invokeLater(() -> model.fireTableChanged(event));
		}
	}

	private String createEditString(Object raw) {
		if (raw instanceof List) {
			var builder = new StringBuilder();
			builder.append('[');
			boolean first = true;
			
			for (var item : (List<?>) raw) {
				if (first)
					first = false;
				else
					builder.append(',');
				
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

	private static Object getColumnValue(CyRow row, String columnName) {
		var column = row.getTable().getColumn(columnName);
		
		if (column.getType() == List.class) {
			var listElementType = column.getListElementType();
			
			return row.getList(columnName, listElementType);
		} else{
			return row.get(columnName, column.getType());
		}
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		lock.writeLock().lock();
		
		try {
			if (!e.getSource().equals(this.dataTable))
				return;

			selectedRows = null;

			// add new rows to rowIndexToPrimaryKey array
			var newRowIndex = new Object[rowIndexToPrimaryKey.length + e.getPayloadCollection().size()];
			System.arraycopy(rowIndexToPrimaryKey, 0, newRowIndex, 0, rowIndexToPrimaryKey.length);
			rowIndexToPrimaryKey = newRowIndex;

			for (var pk : e.getPayloadCollection())
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

			int index = 0;

			var rows = dataTable.getAllRows();

			var pkName = dataTable.getPrimaryKey().getName();

			rowIndexToPrimaryKey = new Object[rows.size()];

			for (var row : rows)
				rowIndexToPrimaryKey[index++] = row.getRaw(pkName);

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
			if (!e.getTable().equals(dataTable))
				return;
			
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
	 */
	public void setViewMode(ViewMode viewMode) {
		selectedRows = null;
		var selectedColumn = dataTable.getColumn(CyNetwork.SELECTED);

		if (viewMode != ViewMode.ALL && selectedColumn != null && selectedColumn.getType() == Boolean.class)
			this.viewMode = viewMode;
		else
			this.viewMode = ViewMode.ALL;
	}

	public void updateViewMode() {
		fireTableDataChanged();
	}

	ViewMode getViewMode() {
		return viewMode;
	}

	public String getCyColumnName(int column) {
		return (String) dataTable.getColumns().toArray()[column];
	}

	@Override
	public String getColumnName(int column) {
		return mapColumnIndexToColumnName(column);
	}

	public int mapColumnNameToColumnIndex(String columnName) {
		if (attrNames.contains(columnName))
			return attrNames.indexOf(columnName);

		return -1;
	}

	private String mapColumnIndexToColumnName(int index) {
		if (index >= 0 && index <= attrNames.size())
			return attrNames.get(index);

		throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		var text = (String)value;
		var row = getCyRow(rowIndex);
		var columnName = mapColumnIndexToColumnName(columnIndex);
		var columnType = dataTable.getColumn(columnName).getType();
		var listElementType = dataTable.getColumn(columnName).getListElementType();

		if (text.isEmpty()) {
			if (!row.isSet(columnName))
				return;
			
			row.set(columnName, null);
		} else if (text.startsWith("=")) {
			var attrNameToTypeMap = TableBrowserUtil.getAttNameToTypeMap(dataTable, null);
			
			if (compiler.compile(text, attrNameToTypeMap)) {
				var eqn = compiler.getEquation();
				var eqnType = eqn.getType();

				// Is the equation type compatible with the column type?
				if (eqnTypeIsCompatible(columnType, listElementType, eqnType)) {
					row.set(columnName, eqn);
				} else { // The equation type is incompatible w/ the column type!
					var expectedType = columnType == Integer.class ? Long.class : columnType;
					var errorMsg = "Equation result type is "
						+ getUnqualifiedName(eqnType) + ", column type is "
						+ getUnqualifiedName(columnType) + ".";
					var errorEqn = compiler.getErrorEquation(text, expectedType, errorMsg);
					row.set(columnName, errorEqn);
				}
			} else {
				var eqnType = columnType == Integer.class ? Long.class : columnType;
				var errorMsg = compiler.getLastErrorMsg();
				var errorEqn = compiler.getErrorEquation(text, eqnType, errorMsg);
				row.set(columnName, errorEqn);
			}
		} else { // Not an equation!
			var parsedData = TableBrowserUtil.parseCellInput(dataTable, columnName, value);
			
			if (parsedData.get(0) != null) {
				row.set(columnName, parsedData.get(0));
			} else {
				// Error!
				showErrorWindow(parsedData.get(1).toString());
				// + " should be an Integer (or the number is too big/small).");
			}
		}

		var event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
		fireTableChanged(event);
		fireTableDataChanged();
	}

	// Pop-up window for error message
	private static void showErrorWindow(String errMessage) {
		JOptionPane.showMessageDialog(null, errMessage, "Invalid Value", JOptionPane.ERROR_MESSAGE);
	}

	private boolean eqnTypeIsCompatible(Class<?> columnType, Class<?> listElementType, Class<?> eqnType) {
		return EquationUtil.eqnTypeIsCompatible(columnType, listElementType, eqnType);
	}

	private String getUnqualifiedName(Class<?> type) {
		var typeName = type.getName();
		var lastDotPos = typeName.lastIndexOf('.');

		return lastDotPos == -1 ? typeName : typeName.substring(lastDotPos + 1);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		var column = getCyColumn(columnIndex);
		
		return column == null ? false : !column.isPrimaryKey();
	}

	public boolean isPrimaryKey(int columnIndex) {
		var column = columnIndex >= 0 && columnIndex < getColumnCount() ? getCyColumn(columnIndex) : null;
		
		return column == null ? false : column.isPrimaryKey();
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

	public ReadWriteLock getLock() {
		return lock;
	}

	private static void dumpTable(CyTable table) {
		System.out.println("Begin Table: " + table.getTitle());
		var cols = table.getColumns();
		for (var col : cols) {
			System.out.print(col.getName());
			System.out.print('\t');
		}
		System.out.println();
		for (var col : cols) {
			System.out.print(col.getType().getSimpleName());
			System.out.print('\t');
		}
		System.out.println();
		for (var row : table.getAllRows()) {
			for (var col : cols) {
				System.out.print(row.getRaw(col.getName()));
				System.out.print('\t');
			}
			System.out.println();
		}
		System.out.println("End Table");
		System.out.println();
	}
}
