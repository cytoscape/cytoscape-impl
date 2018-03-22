package org.cytoscape.command.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class SleepCommandTaskFactory extends AbstractTaskFactory {
	
	public boolean isReady() {
		return true;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SleepCommandTask());
	}

}
