package org.cytoscape.commandDialog.internal.tasks;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import org.cytoscape.commandDialog.internal.ui.CommandToolDialog;

public class CommandDialogTaskFactory extends AbstractTaskFactory {
	CommandToolDialog dialog;
	
	public CommandDialogTaskFactory(CommandToolDialog dialog) {
		super();
		this.dialog = dialog;
	}
	
	public boolean isReady() {
		return true;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CommandDialogTask(dialog));
	}

}
