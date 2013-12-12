package org.cytoscape.commandDialog.internal.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import org.cytoscape.commandDialog.internal.ui.CommandToolDialog;

public class CommandDialogTask extends AbstractTask {
	CommandToolDialog dialog;

	@ProvidesTitle
	public String getTitle() { return "Launching Command Dialog"; }
	
	public CommandDialogTask(CommandToolDialog dialog) {
		super();
		this.dialog = dialog;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// Start the command dialog on the swing thread
		dialog.pack();
		dialog.setVisible(true);
		
	}
}
