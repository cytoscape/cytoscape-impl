package org.cytoscape.webservice.psicquic;

import java.awt.Container;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.task.ImportNetworkFromPSICQUICTask;
import org.cytoscape.webservice.psicquic.task.SearchRecoredsTask;
import org.cytoscape.webservice.psicquic.ui.PSICQUICSearchUI;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSICQUICWebServiceClient extends AbstractWebServiceGUIClient implements NetworkImportWebServiceClient,
		SearchWebServiceClient<Set<String>> {

	private static final Logger logger = LoggerFactory.getLogger(PSICQUICWebServiceClient.class);

	// Timeout value for registry manager.
	private static final Long TIMEOUT = 30l;

	private PSICQUICRestClient client;
	private RegistryManager regManager;
	private final CyNetworkManager networkManager;

	private final TaskManager tManager;

	private ImportNetworkFromPSICQUICTask networkTask;
	private final NetworkTaskFactory createViewTaskFactory;

	private SearchRecoredsTask searchTask;
	
	private final OpenBrowser openBrowser;

	public PSICQUICWebServiceClient(final String uri, final String displayName, final String description,
			final CyNetworkFactory networkFactory, final CyNetworkManager networkManager, final TaskManager tManager,
			final NetworkTaskFactory createViewTaskFactory, final OpenBrowser openBrowser) {
		super(uri, displayName, description);

		this.networkManager = networkManager;
		this.tManager = tManager;
		this.createViewTaskFactory = createViewTaskFactory;
		this.openBrowser = openBrowser;

		// Initialize registry manager in different thread.
		initRegmanager(networkFactory);
	}

	private void initRegmanager(CyNetworkFactory factory) {
		final ExecutorService exe = Executors.newCachedThreadPool();
		final long startTime = System.currentTimeMillis();

		// Submit the query for each active service
		final List<InitRegistryManagerTask> tasks = new ArrayList<InitRegistryManagerTask>();
		tasks.add(new InitRegistryManagerTask());

		List<Future<RegistryManager>> futures = null;
		try {
			futures = exe.invokeAll(tasks, TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Initialization interrupted.", e);
			return;
		}

		for (final Future<RegistryManager> future : futures) {
			try {
				regManager = future.get();
			} catch (ExecutionException e) {
				logger.error("Error initialize regManager: ", e);
				return;
			} catch (CancellationException ce) {
				logger.warn("Initialization operation timeout", ce);
				return;
			} catch (InterruptedException e) {
				logger.error("Initialization interrupted.", e);
				return;
			}
		}
		
		client = new PSICQUICRestClient(factory, regManager);
		
		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("RegistryManager initialized in " + sec + " sec.");
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
					SearchMode.MIQL, createViewTaskFactory);

			return new TaskIterator(searchTask, networkTask);
		}
	}

	public Set<CyNetwork> getNetworks() {
		if (networkTask != null)
			return networkTask.getNetworks();
		else
			return new HashSet<CyNetwork>();
	}

	public Set<String> getSearchResult() {
		return null;
	}

	private static final class InitRegistryManagerTask implements Callable<RegistryManager> {
		@Override
		public RegistryManager call() throws Exception {
			return new RegistryManager();
		}
	}

	@Override
	public Container getQueryBuilderGUI() {
		return new PSICQUICSearchUI(networkManager, regManager, client, tManager, createViewTaskFactory, openBrowser);
	}
}
