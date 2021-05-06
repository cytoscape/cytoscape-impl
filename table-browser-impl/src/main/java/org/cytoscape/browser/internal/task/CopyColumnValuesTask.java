package org.cytoscape.browser.internal.task;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.task.AbstractTableColumnTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CopyColumnValuesTask extends AbstractTableColumnTask {

	private static final String LINE_BREAK = "\n";
	
	public CopyColumnValuesTask(CyColumn column) {
		super(column);
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Copy Column Values to Clipboard");
		tm.setStatusMessage("Copying all values from column '" + column.getName() + "'...");
		
		var name = column.getName();
		var type = column.getType();
		var table = column.getTable();
		var sb = new StringBuffer();
		int count = 0;
		int total = table.getRowCount();
		
		for (var row : table.getAllRows()) {
			if (cancelled)
				return;
			
			Object val = null;
			
			if (type == List.class)
				val = row.getList(name, column.getListElementType());
			else
				val = row.get(name, type);
			
			var s = val != null ? escape(val) : "";
			sb.append(s);
			
			if (++count < total)
				sb.append(LINE_BREAK);
		}
		
		var sel = new StringSelection(sb.toString());
		var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(sel, sel);
	}
	
	private String escape(Object val) {
		return val.toString().replace(LINE_BREAK, " ");
	}
}
