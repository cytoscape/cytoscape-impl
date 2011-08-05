package org.cytoscape.io.webservice.biomart.task;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportFilterTaskFactory implements TaskFactory {

	private final BiomartRestClient client;
	private final String datasourceName;
	
	private Task task;

	public ImportFilterTaskFactory(final String datasourceName, final BiomartRestClient client) {
		this.client = client;
		this.datasourceName = datasourceName;
		task = null;
	}

	@Override
	public TaskIterator getTaskIterator() {
		if(task == null)
			return new TaskIterator(new ImportFilterTask(datasourceName, client));
		else
			return new TaskIterator(task);
	}
	
	public void setTask(Task task) {
		this.task = task;
	}

}
