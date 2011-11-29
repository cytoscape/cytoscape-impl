package org.cytoscape.task.internal.table;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the deletion of a column. */
final class DeleteColumnEdit extends AbstractCyEdit {
	private final CyTable table;
	private final String columnName;
	private final Class<?> columnType;
	private SaveColumn savedColumn;

	DeleteColumnEdit(final CyColumn column) {
		super("Delete Column");

		this.table       = column.getTable();
		this.columnName  = column.getName();
		this.columnType  = column.getType();
		this.savedColumn = new SaveColumn(table, columnName);
	}

	public void redo() {
		;

		savedColumn = new SaveColumn(table, columnName);
		table.deleteColumn(columnName);
	}

	public void undo() {
		;

		table.createColumn(columnName, columnType, /* isImmutable = */ false);
		savedColumn.restoreColumn(table, columnName);
		savedColumn = null;
	}
}
