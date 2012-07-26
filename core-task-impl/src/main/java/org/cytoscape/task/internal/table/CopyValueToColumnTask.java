/*
 Copyright (c) 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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


import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.AbstractTableCellTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


final class CopyValueToColumnTask extends AbstractTableCellTask {
	private final UndoSupport undoSupport;
	private final boolean selectedOnly;
	
	CopyValueToColumnTask(final UndoSupport undoSupport, final CyColumn column,
				    final Object primaryKeyValue, final boolean selectedOnly)
	{
		super(column, primaryKeyValue);
		this.undoSupport = undoSupport;
		this.selectedOnly = selectedOnly;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Copying...");

		final CyRow sourceRow = column.getTable().getRow(primaryKeyValue);
		final String columnName = column.getName();
		final Object sourceValue = sourceRow.getRaw(columnName);
		
		undoSupport.postEdit(
			new CopyValueToColumnEdit(column, sourceValue));

		final List<CyRow> rows = column.getTable().getAllRows();
		final int total = rows.size() - 1;
		int count = 0;
		for (final CyRow row : rows) {
			if (row == sourceRow)
				continue;
			if (selectedOnly && !row.get(CyNetwork.SELECTED, Boolean.class))
				continue;
			row.set(columnName, sourceValue);
			if ((++count % 1000) == 0)
				taskMonitor.setProgress((100.0 * count) / total);
		}
	}
}