package org.cytoscape.command.internal.tasks;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RunCommandsTaskFactory extends AbstractTaskFactory {
	
	private final CommandExecutorTaskFactory taskFactory;
	
	public RunCommandsTaskFactory(CommandExecutorTaskFactory taskFactory) {
		this.taskFactory = taskFactory;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RunCommandsTask(taskFactory));
	}

}
