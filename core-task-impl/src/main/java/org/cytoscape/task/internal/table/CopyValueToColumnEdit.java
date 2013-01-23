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
