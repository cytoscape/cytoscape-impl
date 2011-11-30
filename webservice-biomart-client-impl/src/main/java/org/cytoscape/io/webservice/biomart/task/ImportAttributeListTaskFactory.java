package org.cytoscape.io.webservice.biomart.task;

import org.cytoscape.io.webservice.biomart.rest.BiomartRestClient;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ImportAttributeListTaskFactory implements TaskFactory {
	
	final ImportAttributeListTask task;

	public ImportAttributeListTaskFactory(final String datasourceName, final BiomartRestClient client) {
		task = new ImportAttributeListTask(datasourceName, client);
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(task);
	}

}
