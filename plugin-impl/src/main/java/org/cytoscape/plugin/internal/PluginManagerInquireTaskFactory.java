package org.cytoscape.plugin.internal;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class PluginManagerInquireTaskFactory implements TaskFactory{

	Task task;
	public PluginManagerInquireTaskFactory(Task task){
		this.task = task;
	}
	
	public TaskIterator getTaskIterator() {
		return new TaskIterator(task);
	}
}
