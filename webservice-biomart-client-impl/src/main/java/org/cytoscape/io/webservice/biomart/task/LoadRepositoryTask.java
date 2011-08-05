package org.cytoscape.io.webservice.biomart.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class LoadRepositoryTask extends AbstractTask {

	private static final Logger logger = LoggerFactory
			.getLogger(LoadRepositoryTask.class);

	private final BiomartRestClient client;

	private Map<String, Map<String, String>> reg;
	

	private LoadRepositoryResult result;

	// These databases are not compatible with this UI.
	private static final List<String> databaseFilter = new ArrayList<String>();

	static {
		// Database on this list will not appear on the list.
		databaseFilter.add("compara_mart_pairwise_ga_47");
		databaseFilter.add("compara_mart_multiple_ga_47");
		databaseFilter.add("dicty");
		databaseFilter.add("Pancreatic_Expression");
	}

	private Map<String, String> datasourceMap;
	private List<String> dsList;

	public LoadRepositoryTask(final BiomartRestClient client) {
		this.client = client;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws IOException, ParserConfigurationException, SAXException {

		taskMonitor.setTitle("Loading list of available BioMart Services.  Please wait...");
		taskMonitor.setStatusMessage("Loading list of available Marts...");
		
		dsList = new ArrayList<String>();
		datasourceMap = new HashMap<String, String>();

		logger.debug("Loading Repository...");
		
		reg = client.getRegistry();
		taskMonitor.setProgress(0.1);
		final int registryCount = reg.size();
		float increment = 0.9f / registryCount;
		float percentCompleted = 0.1f;

		taskMonitor.setProgress(percentCompleted);
		Map<String, String> datasources;

		for (String databaseName : reg.keySet()) {

			Map<String, String> detail = reg.get(databaseName);

			// Add the datasource if its visible
			if (detail.get("visible").equals("1")
					&& (databaseFilter.contains(databaseName) == false)) {
				String dispName = detail.get("displayName");
				try {
					datasources = client.getAvailableDatasets(databaseName);
				} catch (IOException e) {
					// If timeout/connection error is found, skip the source.
					percentCompleted += increment;
					continue;
				}

				for (String key : datasources.keySet()) {
					final String dataSource = dispName + " - " + datasources.get(key);
					dsList.add(dataSource);
					datasourceMap.put(dataSource, key);
					taskMonitor.setStatusMessage("Loading Data Source: " + dataSource);
				}
			}

			percentCompleted += increment;
			taskMonitor.setProgress(percentCompleted);
		}

		Collections.sort(dsList);
		
		taskMonitor.setStatusMessage("Finished: " + dsList.size());
		taskMonitor.setProgress(1.0);
		
		result = new LoadRepositoryResult(this.datasourceMap, this.dsList);
	}
	
	public LoadRepositoryResult getResult() {
		return result;
	}
}
