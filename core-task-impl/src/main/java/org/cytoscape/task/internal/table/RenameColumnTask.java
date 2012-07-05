/*
 Copyright (c) 2010, 2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.task.internal.table;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.undo.UndoSupport;


public final class RenameColumnTask extends AbstractTableColumnTask implements TunableValidator {
	private final UndoSupport undoSupport;

	@ProvidesTitle
	public String getTitle() {
		return "Rename Column";
	}
	
	@Tunable(description="New column name:")
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
		if (newColumnName == null || newColumnName.isEmpty()) {
			try {
				errMsg.append("You must provide a new column name.");
			} catch (Exception e) {
			}
			return ValidationState.INVALID;
		}

		final CyTable table = column.getTable();
		if (table.getColumn(newColumnName) != null) {
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