package org.cytoscape.commandDialog.internal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.cytoscape.commandDialog.internal.handlers.CommandHandler;
import org.cytoscape.commandDialog.internal.handlers.MessageHandler;
import org.cytoscape.commandDialog.internal.ui.CommandToolDialog;
import org.cytoscape.commandDialog.internal.ui.ConsoleCommandHandler;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class RunCommandsTask extends AbstractTask {
	
	CommandToolDialog dialog;
	CommandHandler handler;

	@ProvidesTitle
	public String getTitle() { return "Execute Command File"; }
	
	@Tunable(description="Command File", params="input=true;fileCategory=unspecified")
	public File file;
	
	public RunCommandsTask(CommandToolDialog dialog, CommandHandler handler) {
		super();
		this.dialog = dialog;
		this.handler = handler;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		try {
			executeCommandScript(dialog, new ConsoleCommandHandler());
		} catch (FileNotFoundException fnfe) {
			arg0.showMessage(TaskMonitor.Level.ERROR, "No such file or directory: "+file.getPath());
			return;
		} catch (IOException ioe) {
			arg0.showMessage(TaskMonitor.Level.ERROR, "Unexpected I/O error: "+ioe.getMessage());
		}
	}

	public void executeCommandScript(String fileName, CommandToolDialog dialog) {
		file = new File(fileName);
		ConsoleCommandHandler consoleHandler = new ConsoleCommandHandler();
		try {
			executeCommandScript(dialog, consoleHandler);
		} catch (FileNotFoundException fnfe) {
			System.err.println( "No such file or directory: "+file.getPath());
		} catch (IOException ioe) {
			System.err.println( "Unexpected I/O error: "+ioe.getMessage());
		}
	}

	public void executeCommandScript(CommandToolDialog dialog, ConsoleCommandHandler consoleHandler) 
	       throws FileNotFoundException, IOException {
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(file));

		if (dialog != null) {
			// We have a GUI
			dialog.setVisible(true);
		}
			
		String line;
		while ((line = reader.readLine()) != null) {
			if (dialog != null) {
				dialog.executeCommand(line);
			} else {
				consoleHandler.appendCommand(line);
				handler.handleCommand((MessageHandler) consoleHandler, line);
			}
		}
	}
}
