package org.cytoscape.webservice.psicquic.task;

import java.util.Collection;
import java.util.Map;

import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchRecoredsTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(SearchRecoredsTask.class);
	
	private final PSICQUICRestClient client;
	private final SearchMode mode;

	private String query;

	private Collection<String> targetServices;

	private Map<String, Long> result;
	
	public SearchRecoredsTask(final PSICQUICRestClient client, final SearchMode mode) {
		this.client = client;
		this.mode = mode;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Searching PSICQUIC Services");
		taskMonitor.setProgress(0.01d);
		if (query == null)
			throw new NullPointerException("Query is null");
		if (targetServices == null)
			throw new NullPointerException("Target service set is null");

		result = client.search(query, targetServices, mode, taskMonitor);
		taskMonitor.setProgress(1.0d);
	}
	
	@Override
	public void cancel() {
		client.cancel();
	}
	
	public void setTargets(final Collection<String> targetServices) {
		this.targetServices = targetServices;
	}

	public void setQuery(final String query) {
		this.query = query;
	}
	
	public Map<String, Long> getResult() {
		return result;
	}

}
