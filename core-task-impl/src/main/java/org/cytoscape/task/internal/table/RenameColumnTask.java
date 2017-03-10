package org.cytoscape.task.internal.table;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2016 The Cytoscape Consortium
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

public final class RenameColumnTask extends AbstractTableColumnTask implements TunableValidator {
	
	private final UndoSupport undoSupport;

	@ProvidesTitle
	public String getTitle() {
		return "Rename Column";
	}
	
	@Tunable(description="New Column Name:")
	public String newColumnName;

	RenameColumnTask(final UndoSupport undoSupport, final CyColumn column) {
		super(column);
		this.undoSupport = undoSupport;
		// Set the original column name
		newColumnName = column.getName();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		undoSupport.postEdit(new RenameColumnEdit(column));
		taskMonitor.setProgress(0.4);
		column.setName(newColumnName);
		taskMonitor.setProgress(1.0);
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		if (newColumnName == null) {
			try {
				errMsg.append("You must provide a new column name.");
			} catch (Exception e) {
			}
			return ValidationState.INVALID;
		}
		
		newColumnName = newColumnName.trim();
		
		if (newColumnName.isEmpty()) {
			try {
				errMsg.append("Column name must not be blank.");
			} catch (Exception e) {
			}
			return ValidationState.INVALID;
		}
		
		final CyTable table = column.getTable();
		final CyColumn foundColumn = table.getColumn(newColumnName);
		
		if (foundColumn != null && !foundColumn.equals(column)) {
			try {
				errMsg.append("Column name is a duplicate.");
			} catch (Exception e) {
			}
			return ValidationState.INVALID;
		}

		if (column.isImmutable()) {
			try {
				errMsg.append("Cannot rename an immutable column.");
			} catch (Exception e) {
			}
			return ValidationState.INVALID;
		}

		return ValidationState.OK;
	}
}