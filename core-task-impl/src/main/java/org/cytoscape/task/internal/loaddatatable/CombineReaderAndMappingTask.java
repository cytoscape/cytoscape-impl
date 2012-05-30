package org.cytoscape.task.internal.loaddatatable;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class CombineReaderAndMappingTask extends AbstractTask{

	@ProvidesTitle
	public String getTitle() {
		return "Import Attribute From Table";
	}
	

	@ContainsTunables
	public MapTableToNetworkTablesTask mappingTask;
	
	@ContainsTunables
	public CyTableReader readerTask;
	
	
	
	public CombineReaderAndMappingTask(CyTableReader readerTask , CyNetworkManager networkManager){
		this.readerTask = readerTask;
		this.mappingTask = new MapTableToNetworkTablesTask(networkManager, readerTask);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		readerTask.run(taskMonitor);
		mappingTask.run(taskMonitor);
	}
	

	
}
