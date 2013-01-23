package org.cytoscape.scripting.internal;

/*
 * #%L
 * Cytoscape Scripting Impl (scripting-impl)
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

import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExecuteScriptTaskFactory extends AbstractTaskFactory {
	
	private final ScriptEngineManager manager;
	private final CyAppAdapter cyAppAdapter;
	private final CommandExecutorTaskFactory commandExecutorTaskFactoryService;
	
	public ExecuteScriptTaskFactory(final CyAppAdapter cyAppAdapter, final CommandExecutorTaskFactory commandExecutorTaskFactoryService) {
		this.manager = new ScriptEngineManager();
		this.cyAppAdapter = cyAppAdapter;
		this.commandExecutorTaskFactoryService = commandExecutorTaskFactoryService;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExecuteScriptTask(manager, cyAppAdapter, commandExecutorTaskFactoryService));
	}

}
