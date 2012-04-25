package org.cytoscape.webservice.ncbi.task;


import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.webservice.ncbi.rest.EntrezRestClient;
import org.cytoscape.webservice.ncbi.ui.AnnotationCategory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImportTableFromNCBITask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(ImportTableFromNCBITask.class);

	private final CyTableFactory tableFactory;
	private final Set<String> idList;
	private final Set<AnnotationCategory> category;
	private final CyTableManager tableManager;
	private final MapTableToNetworkTablesTaskFactory mapNetworkAttrTF;

	public ImportTableFromNCBITask(final CyTableFactory tableFactory, final Set<String> idList,
	                               final Set<AnnotationCategory> category,
	                               final CyTableManager tableManager,
								   final MapTableToNetworkTablesTaskFactory mapNetworkAttrTF)
	{
		this.tableFactory       = tableFactory;
		this.idList             = idList;
		this.category           = category;
		this.tableManager       = tableManager;
		this.mapNetworkAttrTF   = mapNetworkAttrTF;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		logger.debug("Table Import Task Start.");
		EntrezRestClient client = new EntrezRestClient(null, tableFactory, tableManager);
		final CyTable globalTable = client.importDataTable(idList, category, taskMonitor);

		this.insertTasksAfterCurrentTask(mapNetworkAttrTF.createTaskIterator(CyNode.class, globalTable,CyNetwork.NAME));
	}

}
