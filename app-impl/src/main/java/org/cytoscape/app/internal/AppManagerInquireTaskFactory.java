package org.cytoscape.app.internal;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;


public class AppManagerInquireTaskFactory extends AbstractTaskFactory {

	Task task;
	public AppManagerInquireTaskFactory(Task task){
		this.task = task;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(task);
	}
}
