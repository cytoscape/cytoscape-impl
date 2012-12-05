package org.cytoscape.webservice.psicquic;

import java.awt.Container;
import java.util.Map;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.cytoscape.webservice.psicquic.task.ImportNetworkFromPSICQUICTask;
import org.cytoscape.webservice.psicquic.task.SearchRecoredsTask;
import org.cytoscape.webservice.psicquic.ui.PSICQUICSearchUI;
import org.cytoscape.webservice.psicquic.ui.PSIMITagManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSICQUICWebServiceClient extends AbstractWebServiceGUIClient implements NetworkImportWebServiceClient,
		SearchWebServiceClient {

	private static final Logger logger = LoggerFactory.getLogger(PSICQUICWebServiceClient.class);

	private PSICQUICRestClient client;
	private RegistryManager regManager;
	private final CyNetworkManager networkManager;

	private final TaskManager<?, ?> tManager;

	private ImportNetworkFromPSICQUICTask networkTask;
	private final CreateNetworkViewTaskFactory createViewTaskFactory;

	private SearchRecoredsTask searchTask;

	private final OpenBrowser openBrowser;
	private final PSIMI25VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;
	private final PSIMITagManager tagManager;

	public PSICQUICWebServiceClient(final String uri, final String displayName, final String description,
			final CyNetworkFactory networkFactory, final CyNetworkManager networkManager,
			final TaskManager<?, ?> tManager, final CreateNetworkViewTaskFactory createViewTaskFactory,
			final OpenBrowser openBrowser, final MergedNetworkBuilder builder, PSIMI25VisualStyleBuilder vsBuilder, VisualMappingManager vmm, final PSIMITagManager tagManager) {
		super(uri, displayName, description);

		this.networkManager = networkManager;
		this.tManager = tManager;
		this.createViewTaskFactory = createViewTaskFactory;
		this.openBrowser = openBrowser;
		this.vsBuilder = vsBuilder;
		this.vmm = vmm;
		this.tagManager = tagManager;

		regManager = new RegistryManager();
		client = new PSICQUICRestClient(networkFactory, regManager, builder);
	}

	public TaskIterator createTaskIterator(Object query) {
		if (regManager == null)
			throw new NullPointerException("RegistryManager is null");

		if (query == null)
			throw new NullPointerException("Query object is null.");
		else {
			searchTask = new SearchRecoredsTask(client, SearchMode.MIQL);
			final Map<String, String> activeSource = regManager.getActiveServices();
			final String query2 = query.toString();
			searchTask.setQuery(query2);
			searchTask.setTargets(activeSource.values());

			networkTask = new ImportNetworkFromPSICQUICTask(query2, client, networkManager, regManager, searchTask,
					SearchMode.MIQL, createViewTaskFactory, vsBuilder, vmm);

			return new TaskIterator(searchTask, networkTask);
		}
	}

	@Override
	public Container getQueryBuilderGUI() {
		return new PSICQUICSearchUI(networkManager, regManager, client, tManager, createViewTaskFactory, vsBuilder, vmm, tagManager);
	}

	PSICQUICRestClient getRestClient() {
		return client;
	}

	RegistryManager getRegistryManager() {
		return regManager;
	}
}
