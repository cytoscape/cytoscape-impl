package org.cytoscape.commandDialog.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import org.cytoscape.commandDialog.internal.handlers.CommandHandler;
import org.cytoscape.commandDialog.internal.ui.CommandToolDialog;

public class RunCommandsTaskFactory extends AbstractTaskFactory {
	CommandToolDialog dialog;
	CommandHandler handler;
	
	public RunCommandsTaskFactory(CommandToolDialog dialog, CommandHandler handler) {
		super();
		this.dialog = dialog;
		this.handler = handler;
	}
	
	public boolean isReady() {
		return true;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RunCommandsTask(dialog, handler));
	}

}
