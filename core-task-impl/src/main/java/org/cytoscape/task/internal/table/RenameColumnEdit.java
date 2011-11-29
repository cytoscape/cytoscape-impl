package org.cytoscape.task.internal.table;


import org.cytoscape.model.CyColumn;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the renaming of a column. */
final class RenameColumnEdit extends AbstractCyEdit {
	private final CyColumn column;
	private String oldName;

	RenameColumnEdit(final CyColumn column) {
		super("Rename Column");

		this.column  = column;
		this.oldName = column.getName();
	}

	public void redo() {
		;

		final String previousName = column.getName();
		column.setName(oldName);
		oldName = previousName;
	}

	public void undo() {
		;

		final String previousName = column.getName();
		column.setName(oldName);
		oldName = previousName;
	}
}
