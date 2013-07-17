package org.cytoscape.cmdline.headless.internal;

/*
 * #%L
 * Cytoscape Headless Command Line Parser Impl (headless-cmdline-parser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


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
			taskManager.execute( commandFactory.createTaskIterator( commandFile , null ) );
	}
}

