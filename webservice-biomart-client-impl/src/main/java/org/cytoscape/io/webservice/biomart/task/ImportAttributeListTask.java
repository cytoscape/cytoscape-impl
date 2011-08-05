package org.cytoscape.io.webservice.biomart.task;

import java.util.Map;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ImportAttributeListTask extends AbstractTask {
	
	private final BiomartRestClient client;
	private final String datasourceName;
	
	private Map<String, String[]> attributeVals;
	
	public ImportAttributeListTask(final String datasourceName, final BiomartRestClient client) {
		this.datasourceName = datasourceName;
		this.client = client;
	}
	
	public Map<String, String[]> getAttributeValues() {
		return this.attributeVals;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Loading available attributes...");
		taskMonitor.setProgress(0.0);
		this.attributeVals = client.getAttributes(datasourceName);
	}
}
