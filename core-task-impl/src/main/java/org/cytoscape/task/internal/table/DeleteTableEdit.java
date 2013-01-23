package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
		;

		final CyTable newTable =
			tableFactory.createTable(table.getTitle(), table.getPrimaryKey().getName(),
						 table.getPrimaryKey().getType(), /* public = */ false,
						 /* isMutable = */ true);
		table.swap(newTable);
		tableManager.deleteTable(table.getSUID());
		table = newTable;
	}

	public void undo() {
		;

		final CyTable newTable =
			tableFactory.createTable(table.getTitle(), table.getPrimaryKey().getName(),
						 table.getPrimaryKey().getType(), /* public = */ false,
						 /* isMutable = */ true);
		table.swap(newTable);
		tableManager.addTable(table);
		table = newTable;
	}
}
