package org.cytoscape.cmdline.headless.internal;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.io.File;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupConfig {
	private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);

	private boolean taskStart = false;
	private final TaskManager taskManager;
	
	private TaskFactory commandFactory;
	private File commandFile;
	private TunableSetter setter;
	

	public StartupConfig(StreamUtil streamUtil, TaskFactory commandFactory, TaskManager taskManager, TunableSetter setter)	{
		this.commandFactory = commandFactory;
		this.taskManager = taskManager;
		this.setter = setter;
	}

	public void setCommandFile(String args) {
		try{
			commandFile = new File(args);
		} catch(Exception e) {
			logger.error(e.toString());
		}
		taskStart = true;
	}

	public void start() {

		// Only proceed if we've specified tasks for execution
		// on the command line.
		if ( !taskStart )
			return;

		ArrayList<TaskIterator> taskIteratorList = new ArrayList<TaskIterator>();
				
		if ( commandFile != null ) 	{
			final Map<String, Object> temp = new HashMap<String, Object>();
			temp.put("file", commandFile);
			taskIteratorList.add(setter.createTaskIterator(commandFactory.createTaskIterator(),temp));
		}

		Task initTask = new DummyTaks();
		TaskIterator taskIterator = new TaskIterator(taskIteratorList.size(), initTask);
		for (int i= taskIteratorList.size()-1; i>= 0 ; i--){
			TaskIterator ti = taskIteratorList.get(i);
			taskIterator.insertTasksAfter(initTask, ti);
		}
		
		taskManager.execute(taskIterator);
		
	}
	
	private class DummyTaks extends AbstractTask{

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			//DO nothing it is a dummy tas just to initiate the iterator
		}
		
	}
}

