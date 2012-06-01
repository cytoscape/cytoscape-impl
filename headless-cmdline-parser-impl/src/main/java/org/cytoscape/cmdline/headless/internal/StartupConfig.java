package org.cytoscape.cmdline.headless.internal;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.command.CommandExecutorTaskFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.io.File;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupConfig {
	private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);

	private final SynchronousTaskManager taskManager;
	private final CommandExecutorTaskFactory commandFactory;

	private File commandFile = null;
	

	public StartupConfig(CommandExecutorTaskFactory commandFactory, SynchronousTaskManager taskManager)	{
		this.commandFactory = commandFactory;
		this.taskManager = taskManager;
	}

	public void setCommandFile(String args) {
		try{
			commandFile = new File(args);
		} catch(Exception e) {
			logger.error("Could not create command file from string: '" + args + "'",e);
		}
	}

	public void start() {
		if ( commandFile != null )
			taskManager.execute( commandFactory.createTaskIterator( commandFile ) );
	}
}

