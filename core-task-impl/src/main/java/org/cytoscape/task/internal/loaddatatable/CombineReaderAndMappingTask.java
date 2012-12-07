package org.cytoscape.task.internal.loaddatatable;

import static org.cytoscape.work.TunableValidator.ValidationState.OK;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.internal.table.JoinTablesTask;
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
	public JoinTablesTask mergeTablesTask;
	
	@ContainsTunables
	public CyTableReader readerTask;

	
	public CombineReaderAndMappingTask(CyTableReader readerTask , CyNetworkManager networkManager, final CyRootNetworkManager rootNetMgr){
		this.readerTask = readerTask;
		this.mergeTablesTask = new JoinTablesTask(readerTask, rootNetMgr, networkManager);
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
		this.mergeTablesTask.run(taskMonitor);
	//	mappingTask.run(taskMonitor);
	}

}
