package org.cytoscape.commandDialog.internal.tasks;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class QuitTaskFactory extends AbstractTaskFactory {
	CyShutdown shutdown;
	
	public QuitTaskFactory(CyShutdown shutdown) {
		super();
		this.shutdown = shutdown;
	}
	
	public boolean isReady() {
		return true;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new QuitTask(shutdown));
	}

}
