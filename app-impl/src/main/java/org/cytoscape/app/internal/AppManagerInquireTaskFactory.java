package org.cytoscape.app.internal;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class AppManagerInquireTaskFactory implements TaskFactory{

	Task task;
	public AppManagerInquireTaskFactory(Task task){
		this.task = task;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(task);
	}
}
