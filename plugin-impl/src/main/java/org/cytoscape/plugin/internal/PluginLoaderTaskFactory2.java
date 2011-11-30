package org.cytoscape.plugin.internal;


import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class PluginLoaderTaskFactory2 implements TaskFactory {
	
	private PluginLoaderTask2 task = null;
	 
	PluginLoaderTaskFactory2() {
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(task);
	}
	
	public void setTask(PluginLoaderTask2 task){
		this.task = task;
	}
}
