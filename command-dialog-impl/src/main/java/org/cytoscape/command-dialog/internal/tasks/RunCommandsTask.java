package org.cytoscape.commandDialog.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import org.cytoscape.commandDialog.internal.handlers.CommandHandler;
import org.cytoscape.commandDialog.internal.ui.CommandToolDialog;

public class RunCommandsTask extends AbstractTask {
	CommandToolDialog dialog;
	CommandHandler handler;

	@ProvidesTitle
	public String getTitle() { return "Execute command file"; }
	@Tunable(description="Command File", params="input=true;fileCategory=unspecified")
	public File file;
	
	public RunCommandsTask(CommandToolDialog dialog, CommandHandler handler) {
		super();
		this.dialog = dialog;
		this.handler = handler;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException fnfe) {
			arg0.showMessage(TaskMonitor.Level.ERROR, "No such file or directory: "+file.getPath());
			return;
		} 

		if (dialog != null) {
			// We have a GUI
			dialog.pack();
			dialog.setVisible(true);
			
		} else {
			// Pure command line
		}

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (dialog != null)
					dialog.executeCommand(line);
			}
		} catch (IOException ioe) {
			arg0.showMessage(TaskMonitor.Level.ERROR, "Unexpected I/O error: "+ioe.getMessage());
		}
	}
}
