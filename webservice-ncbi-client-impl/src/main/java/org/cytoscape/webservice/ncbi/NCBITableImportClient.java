package org.cytoscape.webservice.ncbi;



import org.cytoscape.io.webservice.TableImportWebServiceClient;
import org.cytoscape.io.webservice.client.AbstractWebServiceClient;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.edit.MapNetworkAttrTaskFactory;
import org.cytoscape.webservice.ncbi.task.ImportTableFromNCBITask;
import org.cytoscape.work.TaskIterator;


public class NCBITableImportClient extends AbstractWebServiceClient implements TableImportWebServiceClient {
	private final CyTableFactory tableFactory;

	private final CyTableManager tableManager;
	private final MapNetworkAttrTaskFactory mapNetworkAttrTF;

	public NCBITableImportClient(final String uri, final String displayName,
	                             final String description,
	                             final CyTableFactory tableFactory,
	                             final CyTableManager tableManager,
								 final MapNetworkAttrTaskFactory mapNetworkAttrTF)
	{
		super(uri, displayName, description);

		this.tableFactory       = tableFactory;
		this.tableManager       = tableManager;
		this.mapNetworkAttrTF   = mapNetworkAttrTF;
	}

	@Override
	public TaskIterator createTaskIterator(Object query) {
		return new TaskIterator(
			new ImportTableFromNCBITask(tableFactory, ((NCBIQuery) query).getIds(),
			                            ((NCBIQuery) query).getCategory(),
			                             tableManager, mapNetworkAttrTF
										));
	}
}
