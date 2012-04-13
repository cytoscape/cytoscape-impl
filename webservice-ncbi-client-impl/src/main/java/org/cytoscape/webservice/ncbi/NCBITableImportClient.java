package org.cytoscape.webservice.ncbi;


import java.util.Set;

import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.client.AbstractWebServiceClient;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.webservice.ncbi.task.ImportTableFromNCBITask;
import org.cytoscape.work.TaskIterator;


public class NCBITableImportClient extends AbstractWebServiceClient implements TableImportWebServiceClient {
	private final CyTableFactory tableFactory;
	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	private final CyTableManager tableManager;
	private final CyRootNetworkManager cyRootNetworkFactory; 

	public NCBITableImportClient(final String uri, final String displayName,
	                             final String description,
	                             final CyTableFactory tableFactory,
	                             final CyNetworkManager networkManager,
	                             final CyApplicationManager applicationManager,
	                             final CyTableManager tableManager,
								 final CyRootNetworkManager cyRootNetworkFactory)
	{
		super(uri, displayName, description);

		this.tableFactory       = tableFactory;
		this.applicationManager = applicationManager;
		this.networkManager     = networkManager;
		this.tableManager       = tableManager;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
	}

	@Override
	public TaskIterator createTaskIterator(Object query) {
		return new TaskIterator(
			new ImportTableFromNCBITask(tableFactory, ((NCBIQuery) query).getIds(),
			                            ((NCBIQuery) query).getCategory(),
			                            networkManager, applicationManager, tableManager,
										cyRootNetworkFactory));
	}
}
