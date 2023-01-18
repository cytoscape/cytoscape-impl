package org.cytoscape.internal.command;

import java.util.Arrays;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;

public class CommandTaskRunner {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CommandTaskRunner(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	
	public void runCommand(String command, MessageHandler messageHandler) {
		String tokens[] = command.split(" ");
		if(tokens.length >= 1 && tokens[0].equals("help")) {
			AvailableCommands availableCommands = serviceRegistrar.getService(AvailableCommands.class);
			HelpGenerator helpGenerator = new HelpGenerator(availableCommands);
			helpGenerator.generateHelpHTML(command, messageHandler);
		} else {
			runTasks(command, messageHandler);
		}
	}
	
	
	private void runTasks(String command, MessageHandler messageHandler) {
		CommandExecutorTaskFactory commandFactory = serviceRegistrar.getService(CommandExecutorTaskFactory.class);
		
		TaskIterator taskIterator = commandFactory.createTaskIterator(Arrays.asList(command), new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				Object result = task.getResults(String.class);
				if(result != null) {
					messageHandler.appendResult(result.toString());
				}
			}
			@Override
			public void allFinished(FinishStatus status) {
				messageHandler.appendCommand(status.getType().toString());
			}
		});
		
		while(taskIterator.hasNext()) {
			Task task = taskIterator.next();
			try {
				task.run(new TaskMonitor() {
					
					@Override
					public void showMessage(Level level, String message, int wait) {
            showMessage(level, message);
          }
					@Override
					public void showMessage(Level level, String message) {
						switch(level) {
						default:
						case ERROR:
							messageHandler.appendError(message);
							break;
						case INFO:
							messageHandler.appendMessage(message);
							break;
						case WARN:
							messageHandler.appendWarning(message);
							break;
						}
					}
					
					@Override
					public void setTitle(String title) { }
					@Override
					public void setStatusMessage(String statusMessage) { }
					@Override
					public void setProgress(double progress) {  }
				});
			} catch(Exception e) {
				messageHandler.appendError("Error handling command \"" + e.getMessage());
			}
		}
	}
}
