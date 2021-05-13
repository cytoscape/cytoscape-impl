package org.cytoscape.task.internal.utils;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class TableTunable {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	@Tunable(description="Table", context="nogui", longDescription="Specifies a table by table name. If the prefix ```SUID:``` is used, the table corresponding the SUID will be returned.", exampleStringValue="galFiltered.sif default node")
	public String table;

	public TableTunable(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public String getTableString() {
		return table;
	}

	public CyTable getTable() {
		if (table == null)
			return null;

		CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
		
		if (table.toLowerCase().startsWith("suid:")) {
			String[] tokens = table.split(":");
			CyTable t = tableManager.getTable(Long.parseLong(tokens[1].trim()));
			
			return t;
		} else {
			for (CyTable t : tableManager.getAllTables(true)) {
				if (t.getTitle().equalsIgnoreCase(table))
					return t;
			}
		}
		
		return null;
	}
}
