package org.cytoscape.task.internal.table;


import java.util.Collection;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the copying of a value to an entire column. */
final class CopyValueToColumnEdit extends AbstractCyEdit {
	private final CyTable table;
	private final String columnName;
	private final Class<?> columnType;
	private final Object value;
	private SaveColumn savedColumn;
	CopyValueToColumnEdit(final CyColumn column, final Object value, String taskFactoryName) {
		super(taskFactoryName);

		this.table       = column.getTable();
		this.columnName  = column.getName();
		this.columnType  = column.getType();
		this.value       = value;
		this.savedColumn = new SaveColumn(table, columnName);
		
	}

	public void redo() {
		;

		savedColumn = new SaveColumn(table, columnName);

		final Collection<CyRow> rows = table.getAllRows();
		for (final CyRow row : rows)
			row.set(columnName, value);
	}

	public void undo() {
		;

		savedColumn.restoreColumn(table, columnName);
		savedColumn = null;
	}
}
