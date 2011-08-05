package org.cytoscape.io.webservice.biomart.task;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class BioMartTaskFactory implements TaskFactory{

	private TaskIterator itr;
	
	public BioMartTaskFactory(final Task firstTask) {
		if(firstTask == null)
			throw new NullPointerException("First task is null.");
		
		this.itr = new TaskIterator(firstTask);
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return itr;
	}

}
