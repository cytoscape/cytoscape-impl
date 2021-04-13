package org.cytoscape.browser.internal.task;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.Mutability;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

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

public final class DeleteTableTask extends AbstractTableTask {

	@ProvidesTitle
	public String getTitle() {
		return "Delete Table";
	}
	
	@Tunable(
			description = "Are you sure you want to delete this table?",
			params = "ForceSetDirectly=true;ForceSetTitle=Delete Table",
			context = "gui"
	)
	public boolean confirm;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public DeleteTableTask(CyTable table, CyServiceRegistrar serviceRegistrar) {
		super(table);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (confirm) {
			tm.setTitle("Delete Table");
			
			if (table.getMutability() == Mutability.MUTABLE) {
				tm.setStatusMessage("Deleting table: " + table + "...");
				serviceRegistrar.getService(CyTableManager.class).deleteTable(table.getSUID());
			} else {
				throw new RuntimeException(table.getMutability() == Mutability.IMMUTABLE_DUE_TO_VIRT_COLUMN_REFERENCES
						? "Cannot delete this table, it is immutable due to virtual column references."
						: "Cannot delete this table, it is immutable.");
			}
		}
	}
}
