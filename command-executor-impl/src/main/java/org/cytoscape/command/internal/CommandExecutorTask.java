package org.cytoscape.command.internal;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
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

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;


import java.util.Map;


public class CommandExecutorTask extends AbstractTask {

	private final CommandExecutorImpl cei;
	private final String namespace;
	private final String command;
	private final Map<String, Object> args;
	private final TaskObserver observer;

	public CommandExecutorTask(String namespace, String command, Map<String, Object> args, 
	                           CommandExecutorImpl cei, TaskObserver observer) {
		super();
		this.namespace = namespace;
		this.command = command;
		this.args = args;
		this.cei = cei;
		this.observer = observer;
	}

	public void run(TaskMonitor tm) throws Exception {
		cei.executeCommand(namespace, command, args, tm, observer);
	}
}
