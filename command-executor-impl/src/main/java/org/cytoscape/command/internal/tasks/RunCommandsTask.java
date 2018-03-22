package org.cytoscape.command.internal.tasks;

import java.io.File;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class RunCommandsTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() { return "Execute Command File"; }

	public File file;
	@Tunable(description="Command File", required=true, params="input=true;fileCategory=unspecified")
	public File getfile() {
		return file;
	}
	public void setfile(File file) {
		this.file = file;
	}
	

//	// add a new string tunable to specify file command arguments
//	@Tunable(description="Script arguments",
//	         longDescription="Enter the script arguments as key:value pairs separated by commas",
//					 exampleStringValue="arg1:value,arg2:value")
//	public String args;

	
	private final CommandExecutorTaskFactory taskFactory;
	
	public RunCommandsTask(CommandExecutorTaskFactory taskFactory) {
		this.taskFactory = taskFactory;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		TaskIterator tasks = taskFactory.createTaskIterator(file, null);
		insertTasksAfterCurrentTask(tasks);
	}
}
