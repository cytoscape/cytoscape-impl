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
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.internal.table.ImportDataTableTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;

public class CombineReaderAndMappingTask extends AbstractTask implements TunableValidator {

	@ProvidesTitle
	public String getTitle() {
		return "Import Column From Table";
	}


	
//	@ContainsTunables
//	public MapTableToNetworkTablesTask mappingTask;

	

	@ContainsTunables
	public ImportDataTableTask importTablesTask;
	
	@ContainsTunables
	public CyTableReader readerTask;

	
	public CombineReaderAndMappingTask(CyTableReader readerTask , CyNetworkManager networkManager, final CyRootNetworkManager rootNetMgr){
		this.readerTask = readerTask;
		this.importTablesTask = new ImportDataTableTask(readerTask, rootNetMgr, networkManager);
	//	this.mappingTask = new MapTableToNetworkTablesTask(networkManager, readerTask, updateAddedNetworkAttributes, rootNetMgr);
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if ( readerTask instanceof TunableValidator ) {
			ValidationState readVS = ((TunableValidator)readerTask).getValidationState(errMsg);

			if ( readVS != OK )
				return readVS;
		}
		
		// If MapTableToNetworkTablesTask implemented TunableValidator, then
		// this is what we'd do:
		// return mappingTask.getValidationState(errMsg);
		
		return OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		readerTask.run(taskMonitor);
		this.importTablesTask.run(taskMonitor);
	//	mappingTask.run(taskMonitor);
	}

}
