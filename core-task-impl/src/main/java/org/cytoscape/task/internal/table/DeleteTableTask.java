package org.cytoscape.task.internal.table;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

	private final CyServiceRegistrar serviceRegistrar;
	
	public DeleteTableTask(CyTable table, CyServiceRegistrar serviceRegistrar) {
		super(table);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Delete Table");
		tm.setStatusMessage("Deleting table " + table + "...");
		
		serviceRegistrar.getService(CyTableManager.class).deleteTable(table.getSUID());
	}
}
