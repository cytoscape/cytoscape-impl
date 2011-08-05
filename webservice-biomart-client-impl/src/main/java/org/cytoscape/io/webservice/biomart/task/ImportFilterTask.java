package org.cytoscape.io.webservice.biomart.task;

import java.util.Map;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ImportFilterTask extends AbstractTask {
	
	private final BiomartRestClient client;
	private final String datasourceName;
	
	private Map<String, String> returnValMap;
	
	public ImportFilterTask(final String datasourceName, final BiomartRestClient client) {
		this.client = client;
		this.datasourceName = datasourceName;
	}
	
	public Map<String, String> getFilters() {
		return this.returnValMap;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		returnValMap = client.getFilters(datasourceName, false);
	}


}
