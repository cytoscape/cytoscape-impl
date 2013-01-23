package org.cytoscape.scripting.internal.command;

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

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.scripting.internal.ExecuteScriptCommandTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

@Command(scope = "cytoscape", name = "script", description = "Execute scripts", detailedDescription = "Execute script in the file.")
public class ExecuteScriptCommand extends OsgiCommandSupport {

	@Argument(index = 0, name = "engine", description = "Name of scripting engine", required = true, multiValued = false)
	String engine;

	@Argument(index = 1, name = "filename", description = "Script file name", required = true, multiValued = false)
	String filename;

	@Argument(index = 2, name = "arg", description = "Optional arguments for the script", required = false, multiValued = true)
	List<String> args;

	private ScriptEngineManager manager = new ScriptEngineManager();
	private TaskManager<?, ?> taskManager;
	private CyAppAdapter cyAppAdapter;

	public void setCyAppAdapter(CyAppAdapter cyAppAdapter) {
		this.cyAppAdapter = cyAppAdapter;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	@Override
	protected Object doExecute() throws Exception {
		final TaskIterator taskItr = new TaskIterator(new ExecuteScriptCommandTask(manager, cyAppAdapter, engine,
				filename, args));

		taskManager.execute(taskItr);

		return null;
	}
}