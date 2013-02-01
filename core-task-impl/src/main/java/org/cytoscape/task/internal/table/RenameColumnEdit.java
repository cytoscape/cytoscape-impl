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
