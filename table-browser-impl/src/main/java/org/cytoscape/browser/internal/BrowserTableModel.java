package org.cytoscape.browser.internal;


import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.Equation;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import java.util.Vector;
import org.cytoscape.browser.util.TableBrowserUtil;
import javax.swing.JOptionPane;


public final class BrowserTableModel extends AbstractTableModel implements ColumnCreatedListener,
		ColumnDeletedListener, ColumnNameChangedListener, RowsSetListener, RowsCreatedListener {
	private static final long serialVersionUID = -517521404005631245L;
	
	private static final int EOF = -1;
	private static final int MAX_INITIALLY_VSIBLE_ATTRS = 10;
	private final JTable table;
	
	private final CyTable dataTable;
	
	private final EquationCompiler compiler;
	private boolean tableHasBooleanSelected;
	private List<AttrNameAndVisibility> attrNamesAndVisibilities;
	private Collection<CyRow> selectedRows = null;

	public BrowserTableModel(final JTable table, final CyTable dataTable, final EquationCompiler compiler) {
		this.table = table;
		this.dataTable = dataTable;
		this.compiler = compiler;
		final CyColumn selectedColumn = dataTable.getColumn(CyNetwork.SELECTED);
		this.tableHasBooleanSelected = selectedColumn != null && selectedColumn.getType() == Boolean.class;

		initAttrNamesAndVisibilities();
	}

	private void initAttrNamesAndVisibilities() {
		attrNamesAndVisibilities = new ArrayList<AttrNameAndVisibility>(dataTable.getColumns().size());
		final CyColumn primaryKey = dataTable.getPrimaryKey();
		attrNamesAndVisibilities.add(new AttrNameAndVisibility(primaryKey.getName(), true));
		int visibleColumnCount = 1;
		boolean isVisible = true;
		for (final CyColumn column : dataTable.getColumns()) {
			if (column == primaryKey)
				continue;

			attrNamesAndVisibilities.add(
				new AttrNameAndVisibility(column.getName(), isVisible));
			if (++visibleColumnCount == MAX_INITIALLY_VSIBLE_ATTRS)
				isVisible = false;
		}
	}

	public JTable getTable() { return table; }

	public CyTable getAttributes() { return dataTable; }

	@Override
	public Class getColumnClass(final int columnIndex) {
		return ValidatedObjectAndEditString.class;
	}

	// Note: return value excludes the primary key of the associated CyTable!
	public List<String> getVisibleAttributeNames() {
		final List<String> visibleAttrNames = new ArrayList<String>();

		for (final AttrNameAndVisibility nameAndVisibility : attrNamesAndVisibilities) {
			if (nameAndVisibility.isVisible())// && !nameAndVisibility.getName().equals(primaryKey))
				visibleAttrNames.add(nameAndVisibility.getName());
		}

		return visibleAttrNames;
	}

	// Note: primary key visibility will not be affected by this, whether "visibleAttributes"
	//       contains the primary key or not!
	public void setVisibleAttributeNames(final Collection<String> visibleAttributes) {
		boolean changed = false;
		final String primaryKey = dataTable.getPrimaryKey().getName();
		for (final AttrNameAndVisibility nameAndVisibility : attrNamesAndVisibilities) {
			if (nameAndVisibility.getName().equals(primaryKey))
				continue;

			if (visibleAttributes.contains(nameAndVisibility.getName())) {
				if (!nameAndVisibility.isVisible()) {
					nameAndVisibility.setVisibility(true);
					changed = true;
				}
			} else if (nameAndVisibility.isVisible()) {
				nameAndVisibility.setVisibility(false);
				changed = true;
			}
		}

		if (changed)
			fireTableStructureChanged();
	}

	@Override
	public int getRowCount() {
		final Collection<CyColumn> columns = dataTable.getColumns();
		if (columns.isEmpty())
			return 0;

		if (!tableHasBooleanSelected)
			return dataTable.getRowCount();

		return dataTable.getMatchingRows(CyNetwork.SELECTED, Boolean.valueOf(true)).size();
	}

	@Override
	public int getColumnCount() {
		int count = 0;
		for (final AttrNameAndVisibility nameAndVisibility : attrNamesAndVisibilities) {
			if (nameAndVisibility.isVisible())
				++count;
		}

		return count;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		final String columnName = getColumnName(columnIndex);
		final CyRow row = mapRowIndexToRow(rowIndex);

		return getValidatedObjectAndEditString(row, columnName);
	}

	CyColumn getColumn(final int columnIndex)  {
		final String columnName = getColumnName(columnIndex);
		return dataTable.getColumn(columnName);
	}

	private CyRow mapRowIndexToRow(final int rowIndex) {
		if (tableHasBooleanSelected) {
			if (selectedRows == null)
				selectedRows = dataTable.getMatchingRows(CyNetwork.SELECTED, true);

			int count = 0;
			CyRow cyRow = null;
			for (final CyRow selectedRow : selectedRows) {
				if (count == rowIndex) {
					cyRow = selectedRow;
					break;
				}

				++count;
			}

			return cyRow;
		} else {
			final CyColumn primaryKey = dataTable.getPrimaryKey();
			final List primaryKeyValues = primaryKey.getValues(primaryKey.getType());
			return dataTable.getRow(primaryKeyValues.get(rowIndex));
		}
	}

	/**
	 *  @return the row index for "cyRow" or -1 if there is no matching row.
	 */
	private int mapRowToRowIndex(final CyRow cyRow) {
		final String primaryKey = dataTable.getPrimaryKey().getName();
		final Class<?> primaryKeyType = dataTable.getPrimaryKey().getType();

		int index = 0;
		if (tableHasBooleanSelected) {
			if (selectedRows == null)
				selectedRows = dataTable.getMatchingRows(CyNetwork.SELECTED, true);

			for (final CyRow selectedRow : selectedRows) {
				if (cyRow.get(primaryKey, primaryKeyType)
				    .equals(selectedRow.get(primaryKey, primaryKeyType)))
					return index;
				++index;
			}

			return -1; // Most likely the passed in row was not a selected row!
		} else {
			final List<?> primaryKeyValues = dataTable.getPrimaryKey().getValues(primaryKeyType);
			if(primaryKeyValues.size() == 0)
				return -1;
			
			for (final Object primaryKeyValue : primaryKeyValues) {
				if(cyRow.getAllValues().size() != dataTable.getRow(primaryKeyValue).getAllValues().size())
					return -1;
				
				if (cyRow.get(primaryKey, primaryKeyType).equals(
						dataTable.getRow(primaryKeyValue).get(primaryKey, primaryKeyType)))
					return index;
				++index;
			}
			throw new IllegalStateException("we should *never* get here!");
		}
	}

	private ValidatedObjectAndEditString getValidatedObjectAndEditString(final CyRow row,
									     final String columnName)
	{
		if (row == null)
			return null;

		final Object raw = row.getRaw(columnName);
		if (raw == null)
			return null;

		// Optimisation hack:
		Object cooked;
		if (!(raw instanceof String))
			cooked = raw;
		else {
			final String rawString = (String)raw;
			if (!rawString.startsWith("="))
				cooked = rawString;
			else
				cooked = getColumnValue(row, columnName);
		}

		if (cooked != null)
			return new ValidatedObjectAndEditString(cooked, raw.toString());

		final String lastInternalError = dataTable.getLastInternalError();
		return new ValidatedObjectAndEditString(cooked, raw.toString(), lastInternalError);
	}

	private static Object getColumnValue(final CyRow row, final String columnName) {
		final CyColumn column = row.getTable().getColumn(columnName);
		if (column.getType() == List.class) {
			final Class<?> listElementType = column.getListElementType();
			return row.getList(columnName, listElementType);
		} else
			return row.get(columnName, column.getType());
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		if (e.getSource() != dataTable)
			return;

		attrNamesAndVisibilities.add(new AttrNameAndVisibility(e.getColumnName(), true));
		fireTableStructureChanged();
	}

	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (e.getSource() != dataTable)
			return;

		final String columnName = e.getColumnName();
		for (int i = 0; i < attrNamesAndVisibilities.size(); ++i) {
			if (attrNamesAndVisibilities.get(i).getName().equals(columnName)) {
				attrNamesAndVisibilities.remove(i);
				break;
			}
		}

		fireTableStructureChanged();
	}

	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		if (e.getSource() != dataTable)
			return;

		final String newColumnName = e.getNewColumnName();
		renameColumnName(e.getOldColumnName(), newColumnName);
		final int column = mapColumnNameToColumnIndex(newColumnName);
		if (column != -1)
			table.getColumnModel().getColumn(column).setHeaderValue(newColumnName);
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		selectedRows = null;
		fireTableDataChanged();
	}

	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (e.getSource() != dataTable)
			return;

		if (tableHasBooleanSelected) {
			selectedRows = null;
			boolean foundANonSelectedColumnName = false;
			for (final RowSetRecord rowSet : e.getPayloadCollection()) {
				if (!rowSet.getColumn().equals(CyNetwork.SELECTED)) {
					foundANonSelectedColumnName = true;
					break;
				}
			}

			if (!foundANonSelectedColumnName) {
				fireTableDataChanged();
				return;
			}
		}

		for (final RowSetRecord rowSet : e.getPayloadCollection())
			handleRowValueUpdate(rowSet.getRow(), rowSet.getColumn(),
			                     rowSet.getValue(), rowSet.getRawValue());
	}

	private void handleRowValueUpdate(final CyRow row, final String columnName, final Object newValue,
				  final Object newRawValue)
	{
		final int rowIndex = mapRowToRowIndex(row);
		if (rowIndex == -1)
			return;

		final int columnIndex = mapColumnNameToColumnIndex(columnName);
		if (columnIndex == -1)
			return;

		if (tableHasBooleanSelected && columnName.equals(CyNetwork.SELECTED)) {
/*
			final boolean selected = (Boolean)newValue;
			final int rowIndex = mapRowToRowIndex(row);
			if (!selected && rowIndex == -1)
				return;
*/
//			selectedRows = null;
			fireTableDataChanged();
		} else {
			final TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
			fireTableChanged(event);
		}
	}

	@Override
	public String getColumnName(final int column) {
		return mapColumnIndexToColumnName(column);
	}

	private void renameColumnName(final String oldName, final String newName) {
		for (final AttrNameAndVisibility nameAndVisibility : attrNamesAndVisibilities) {
			if (nameAndVisibility.getName().equals(oldName)) {
				nameAndVisibility.setName(newName);
				return;
			}
		}

		throw new IllegalStateException("We should *never* get here!");
	}

	int mapColumnNameToColumnIndex(final String columnName) {
		final String primaryKey = dataTable.getPrimaryKey().getName();
		if (columnName.equals(primaryKey))
			return 0;

		int index = attrNamesAndVisibilities.get(0).isVisible() ? 1 : 0;
		for (final AttrNameAndVisibility nameAndVisibility : attrNamesAndVisibilities) {
			if (!nameAndVisibility.isVisible() || nameAndVisibility.getName().equals(primaryKey))
				continue;

			if (nameAndVisibility.getName().equals(columnName))
				return index;

			++index;
		}

		return -1;
	}

	private String mapColumnIndexToColumnName(final int index) {
		final String primaryKey = dataTable.getPrimaryKey().getName();
		final boolean primaryKeyIsVisible = attrNamesAndVisibilities.get(0).isVisible();
		if (index == 0 && primaryKeyIsVisible)
			return primaryKey;

		int i = primaryKeyIsVisible ? 1 : 0;
		for (final AttrNameAndVisibility nameAndVisibility : attrNamesAndVisibilities) {
			if (!nameAndVisibility.isVisible() || nameAndVisibility.getName().equals(primaryKey))
				continue;

			if (index == i)
				return nameAndVisibility.getName();

			++i;
		}

		throw new IllegalStateException("We should *never* get here! (index="+index+", i="+i);
	}

	// Because tableModel will disappear if user click on open space on canvas, 
	// we have to remember it before it is gone
	public Vector getCellData(final int rowIndex, final int columnIndex){
		Vector cellVect = new Vector();
		cellVect.add(mapRowIndexToRow(rowIndex));
		cellVect.add( mapColumnIndexToColumnName(columnIndex));
		
		return cellVect;
	}
	
	@Override
	public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
		final String text = (String)value;
		final CyRow row = mapRowIndexToRow(rowIndex);
		final String columnName = mapColumnIndexToColumnName(columnIndex);
		final Class<?> columnType = dataTable.getColumn(columnName).getType();

		if (text.isEmpty()) {
			if (!row.isSet(columnName))
				return;
			row.set(columnName, null);
		} else if (text.startsWith("=")) {
			final Map<String, Class<?>> variableNameToTypeMap = new HashMap<String, Class<?>>();
			initVariableNameToTypeMap(variableNameToTypeMap);
			if (compiler.compile(text, variableNameToTypeMap)) {
				final Equation eqn = compiler.getEquation();
				final Class<?> eqnType = eqn.getType();

				// Is the equation type compatible with the column type?
				if (eqnTypeIsCompatible(columnType, eqnType))
					row.set(columnName, eqn);
				else { // The equation type is incompatible w/ the column type!
					final Class<?> expectedType = columnType == Integer.class ? Long.class : columnType;
					final String errorMsg = "Equation result type is "
						+ getUnqualifiedName(eqnType) + ", column type is "
						+ getUnqualifiedName(columnType) + "!";
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

			ArrayList parsedData = TableBrowserUtil.parseCellInput(dataTable, columnName, value);
			
			if (parsedData.get(0) != null)
				row.set(columnName, parsedData.get(0));
			else {
				//Error!
				showErrorWindow(parsedData.get(1).toString());
						//+ " should be an Integer (or the number is too big/small).");
			}
		}

		final TableModelEvent event = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
		fireTableChanged(event);
	}

	// Pop-up window for error message
	private static void showErrorWindow(final String errMessage) {
		JOptionPane.showMessageDialog(null, errMessage, "Invalid Value!",
		                              JOptionPane.ERROR_MESSAGE);
	}

	
	private boolean eqnTypeIsCompatible(final Class<?> columnType, final Class<?> eqnType) {
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

		return false;
	}

	private String getUnqualifiedName(final Class<?> type) {
		final String typeName = type.getName();
		final int lastDotPos = typeName.lastIndexOf('.');
		return lastDotPos == -1 ? typeName : typeName.substring(lastDotPos + 1);
	}

	private void initVariableNameToTypeMap(final Map<String, Class<?>> variableNameToTypeMap) {
		for (final CyColumn column : dataTable.getColumns()) {
			final Class<?> type = column.getType();
			final String columnName = column.getName();
			if (type == String.class)
				variableNameToTypeMap.put(columnName, String.class);
			else if (type == Double.class)
				variableNameToTypeMap.put(columnName, Double.class);
			else if (type == Integer.class)
				variableNameToTypeMap.put(columnName, Long.class);
			else if (type == Long.class)
				variableNameToTypeMap.put(columnName, Long.class);
			else if (type == Boolean.class)
				variableNameToTypeMap.put(columnName, Boolean.class);
			else if (type == List.class)
				variableNameToTypeMap.put(columnName, List.class);
			else
				throw new IllegalStateException("unknown type \"" + type.getName()
								+ "\"!");
		}
	}


	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return table.convertColumnIndexToModel(columnIndex) != 0;
	}
}


final class AttrNameAndVisibility {
	private String attrName;
	private boolean isVisible;

	AttrNameAndVisibility(final String attrName, final boolean isVisible) {
		this.attrName = attrName;
		this.isVisible = isVisible;
	}

	String getName() {
		return attrName;
	}

	void setName(final String newAttrName) {
		attrName = newAttrName;
	}

	void setVisibility(final boolean isVisible) {
		this.isVisible = isVisible;
	}

	boolean isVisible() {
		return isVisible;
	}
}