package org.cytoscape.webservice.ncbi.task;


import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.MapNetworkAttrTask;
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
	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	private final CyTableManager tableManager;
	private final CyRootNetworkFactory cyRootNetworkFactory;

	public ImportTableFromNCBITask(final CyTableFactory tableFactory, final Set<String> idList,
	                               final Set<AnnotationCategory> category,
	                               final CyNetworkManager networkManager,
	                               final CyApplicationManager applicationManager,
	                               final CyTableManager tableManager,
								   final CyRootNetworkFactory cyRootNetworkFactory)
	{
		this.tableFactory       = tableFactory;
		this.idList             = idList;
		this.category           = category;
		this.applicationManager = applicationManager;
		this.networkManager     = networkManager;
		this.tableManager       = tableManager;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		logger.debug("Table Import Task Start.");
		EntrezRestClient client = new EntrezRestClient(null, tableFactory, tableManager);
		final CyTable globalTable = client.importDataTable(idList, category, taskMonitor);

		final MapNetworkAttrTask localMappingTask =
			new MapNetworkAttrTask(CyNode.class, globalTable, networkManager,
			                       applicationManager,cyRootNetworkFactory);
		this.insertTasksAfterCurrentTask(localMappingTask);
	}

}
