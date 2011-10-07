package org.cytoscape.browser.internal;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


class TableMetadata {
	final List<ColumnDescriptor> columnDescriptors;

	TableMetadata(final TableColumnModel columnModel, final BrowserTableModel tableModel) {
		final JTable table = tableModel.getTable();
		columnDescriptors = new ArrayList<ColumnDescriptor>();
		final Enumeration<TableColumn> tableColumnsEnumeration = columnModel.getColumns();
		while (tableColumnsEnumeration.hasMoreElements()) {
			final TableColumn column = tableColumnsEnumeration.nextElement();
			final int columnIndex = column.getModelIndex();
			final String columnName = tableModel.getColumnName(columnIndex);
			final int actualIndex = table.convertColumnIndexToView(columnIndex);
			columnDescriptors.add(new ColumnDescriptor(columnName, actualIndex, column.getWidth()));
		}
	}

	Iterator<ColumnDescriptor> getColumnDescriptors() {
		return columnDescriptors.listIterator();
	}
}
