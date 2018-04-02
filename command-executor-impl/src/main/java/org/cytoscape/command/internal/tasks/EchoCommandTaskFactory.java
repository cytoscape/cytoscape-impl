package org.cytoscape.command.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class EchoCommandTaskFactory extends AbstractTaskFactory {
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new EchoCommandTask());
	}
	
}
