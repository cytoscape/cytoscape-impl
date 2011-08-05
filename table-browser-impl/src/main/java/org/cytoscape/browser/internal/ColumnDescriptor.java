package org.cytoscape.browser.internal;


class ColumnDescriptor {
	private final String columnName;
	private final int columnIndex;
	private final int columnWidth;

	ColumnDescriptor(final String columnName, final int columnIndex, final int columnWidth) {
		this.columnName  = columnName;
		this.columnIndex = columnIndex;
		this.columnWidth = columnWidth;
	}

	String getColumnName() { return columnName; }
	int getColumnIndex() { return columnIndex; }
	int getColumnWidth() { return columnWidth; }
}