package org.cytoscape.task.internal.table;


import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.AbstractCyEdit;


/** An undoable edit that will undo and redo the copying of a value to an entire column. */
final class CopyValueToEntireColumnEdit extends AbstractCyEdit {
	private final CyTable table;
	private final String columnName;
	private final Class<?> columnType;
	private final Object value;
	private SaveColumn savedColumn;

	CopyValueToEntireColumnEdit(final CyColumn column, final Object value) {
		super("Copy Value to Entire Column");

		this.table       = column.getTable();
		this.columnName  = column.getName();
		this.columnType  = column.getType();
		this.value       = value;
		this.savedColumn = new SaveColumn(table, columnName);
		
	}

	public void redo() {
		super.redo();

		savedColumn = new SaveColumn(table, columnName);

		final Collection<CyRow> rows = table.getAllRows();
		for (final CyRow row : rows)
			row.set(columnName, value);
	}

	public void undo() {
		super.undo();

		savedColumn.restoreColumn(table, columnName);
		savedColumn = null;
	}
}
