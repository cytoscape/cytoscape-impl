package org.cytoscape.task.internal.loaddatatable;

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

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AddImportedTableTask extends AbstractTask {

	private static Logger logger = LoggerFactory.getLogger(AddImportedTableTask.class);

	private final CyTableReader reader;
	private final CyServiceRegistrar serviceRegistrar;

	AddImportedTableTask(final CyTableReader reader, final CyServiceRegistrar serviceRegistrar) {
		this.reader = reader;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (reader != null && reader.getTables() != null) {
			final CyTableManager tableMgr = serviceRegistrar.getService(CyTableManager.class);
			
			for (CyTable table : reader.getTables())
				tableMgr.addTable(table);
		} else {
			if (reader == null)
				logger.warn("reader is null.");
			else
				logger.warn("No tables in reader.");
		}
	}
}
