package org.cytoscape.app.internal;


import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class AppLoaderTaskFactory2 implements TaskFactory {
	
	private AppLoaderTask2 task = null;
	 
	AppLoaderTaskFactory2() {
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(task);
	}
	
	public void setTask(AppLoaderTask2 task){
		this.task = task;
	}
}
