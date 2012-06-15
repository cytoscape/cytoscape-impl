package org.cytoscape.task.internal.loaddatatable;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
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
		checkTable();
		mappingTask.run(taskMonitor);
	}

	private void checkTable() {

		for(CyTable table: readerTask.getTables())
			if (table.getColumns().size() <= 1)
				throw new IllegalArgumentException("Imported table requires to have two or more columns!" +
				"Check the selected delimiters and columns.");
	}

}
