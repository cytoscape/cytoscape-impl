package org.cytoscape.tableimport.internal.task;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

class FinalStatusMessageUpdateTask extends AbstractTask {

	private final CyTableReader reader;

	FinalStatusMessageUpdateTask(final CyTableReader reader) {
		this.reader = reader;
	}

	@Override
	public void run(final TaskMonitor tm) throws Exception {
		for (CyTable table : reader.getTables())
			tm.setStatusMessage("Successfully loaded table: " + table.getTitle());

		tm.setProgress(1.0);
	}
}
