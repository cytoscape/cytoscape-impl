package org.cytoscape.webservice.psicquic.task;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportNetworkFromPSICQUICTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(ImportNetworkFromPSICQUICTask.class);

	private final PSICQUICRestClient client;
	private final CyNetworkManager manager;
	private final RegistryManager registryManager;
	
	// TaskFactory for creating view
	private final CreateNetworkViewTaskFactory createViewTaskFactory;

	private String query;

	private Collection<String> targetServices;

	private Set<String> searchResult;
	private Map<String, CyNetwork> result;
	
	private SearchRecoredsTask searchTask;
	
	private final SearchMode mode;
	private final boolean toCluster;
	
	
	private volatile boolean canceled = false;
	
	public ImportNetworkFromPSICQUICTask(final String query, final PSICQUICRestClient client,
			final CyNetworkManager manager, final RegistryManager registryManager, final Set<String> searchResult,
			final SearchMode mode, final CreateNetworkViewTaskFactory createViewTaskFactory, final boolean toCluster) {
		this.client = client;
		this.manager = manager;
		this.registryManager = registryManager;
		this.query = query;
		this.toCluster = toCluster;

		this.searchResult = searchResult;
		this.mode = mode;
		this.createViewTaskFactory = createViewTaskFactory;
	}

	public ImportNetworkFromPSICQUICTask(final String query, final PSICQUICRestClient client,
			final CyNetworkManager manager, final RegistryManager registryManager, final SearchRecoredsTask searchTask,
			final SearchMode mode, final CreateNetworkViewTaskFactory createViewTaskFactory) {
		this.client = client;
		this.manager = manager;
		this.registryManager = registryManager;
		this.query = query;
		this.toCluster = false;

		this.searchTask = searchTask;
		this.mode = mode;
		this.createViewTaskFactory = createViewTaskFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Importing Interacitons from PSICQUIC Services");
		taskMonitor.setStatusMessage("Loading interaction from remote service...");
		taskMonitor.setProgress(0.01d);
		
		if(searchResult == null)
			processSearchResult();
		else {
			targetServices = searchResult;
		}
		
		if(searchResult == null)
			throw new NullPointerException("Could not find search result");
		
		if (query == null)
			throw new NullPointerException("Query is null");
		if (targetServices == null)
			throw new NullPointerException("Target service set is null");

		// Switch task type based on the user option.
		if(toCluster) {
			final CyNetwork network = client.importClusteredNetwork(query, targetServices, mode, taskMonitor);
			result = new HashMap<String, CyNetwork>();
			result.put("clustered", network);
		} else
			result = client.importNetwork(query, targetServices, mode, taskMonitor);
		
		if(canceled) {
			result.clear();
			result = null;
			return;
		}
			
		final Date date = new Date();
		final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy/MM/dd K:mm:ss a, z");
		final String suffix = "(" + timestamp.format(date) + ")";
		
		// Register networks to the manager
		final Set<CyNetwork> networks = new HashSet<CyNetwork>();
		
		if (toCluster) {
			final CyNetwork network = result.values().iterator().next();
			if(network != null) {
				network.getRow(network).set(CyNetwork.NAME, "Merged Network " + suffix);
				addNetworkData(network);
				manager.addNetwork(network);
				networks.add(network);
			}
		} else {
			for (String sourceURL : result.keySet()) {
				final CyNetwork network = result.get(sourceURL);
				network.getRow(network).set(CyNetwork.NAME,
						registryManager.getSource2NameMap().get(sourceURL) + " " + suffix);
				addNetworkData(network);
				manager.addNetwork(network);
				networks.add(network);
			}
		}

		logger.debug(networks.size() + " networks created.");
		
		if (!canceled)
			insertTasksAfterCurrentTask(createViewTaskFactory.createTaskIterator(networks));
	}
	
	@Override
	public void cancel() {
		this.canceled = true;
		client.cancel();
	}
	
	private void addNetworkData(final CyNetwork network) {
		network.getRow(network).getTable().createColumn("created by", String.class, true);
		network.getRow(network).set("created by", "PSICQUIC Web Service");
	}

	private void processSearchResult() {
		Map<String, Long> rs = searchTask.getResult();
		targetServices = new HashSet<String>();
		
		for(final String sourceURL: rs.keySet()) {
			final Long interactionCount = rs.get(sourceURL);
			if(interactionCount == 0)
				continue;
			
			targetServices.add(sourceURL);
		}
	}

	public Set<CyNetwork> getNetworks() {
		return new HashSet<CyNetwork>(result.values());
	}

}
