package org.cytoscape.task.internal.table;


import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the deletion of a table. */
final class DeleteTableEdit extends AbstractCyEdit {
	private CyTable table;
	private final CyTableManager tableManager;
	private final CyTableFactory tableFactory;

	DeleteTableEdit(final CyTable table, final CyTableManager tableManager,
	                final CyTableFactory tableFactory)
	{
		super("Delete Table");

		this.table = tableFactory.createTable(table.getTitle(), table.getPrimaryKey().getName(),
		                                      table.getPrimaryKey().getType(),
		                                      /* public = */ false, /* isMutable = */ true);
		this.table.swap(table);
		this.tableManager = tableManager;
		this.tableFactory = tableFactory;
	}

	public void redo() {
		super.redo();

		final CyTable newTable =
			tableFactory.createTable(table.getTitle(), table.getPrimaryKey().getName(),
						 table.getPrimaryKey().getType(), /* public = */ false,
						 /* isMutable = */ true);
		table.swap(newTable);
		tableManager.deleteTable(table.getSUID());
		table = newTable;
	}

	public void undo() {
		super.undo();

		final CyTable newTable =
			tableFactory.createTable(table.getTitle(), table.getPrimaryKey().getName(),
						 table.getPrimaryKey().getType(), /* public = */ false,
						 /* isMutable = */ true);
		table.swap(newTable);
		tableManager.addTable(table);
		table = newTable;
	}
}
