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

import static org.cytoscape.work.TunableValidator.ValidationState.OK;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.table.ImportTableDataTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;

public class CombineReaderAndMappingTask extends AbstractTask implements TunableValidator {

	@ProvidesTitle
	public String getTitle() {		return "Import Columns From Table";	}

	@ContainsTunables
	public ImportTableDataTask importTableDataTask;
	
	@ContainsTunables
	public CyTableReader tableReader;

	
	public CombineReaderAndMappingTask(final CyTableReader tableReader, final CyServiceRegistrar serviceRegistrar) {
		this.tableReader = tableReader;
		this.importTableDataTask = new ImportTableDataTask(tableReader, serviceRegistrar);
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (tableReader instanceof TunableValidator) {
			ValidationState readVS = ((TunableValidator) tableReader).getValidationState(errMsg);
			if (readVS != OK)		return readVS;
		}

		if (importTableDataTask instanceof TunableValidator) {
			ValidationState readVS = ((TunableValidator) importTableDataTask).getValidationState(errMsg);
			if (readVS != OK)		return readVS;
		}
		return OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		tableReader.run(taskMonitor);
		importTableDataTask.run(taskMonitor);
	}
}
