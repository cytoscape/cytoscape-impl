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

import java.io.File;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * Use standard Java scripting mechanism to run script.
 * 
 */
public class ExecuteScriptTask extends AbstractExecuteScriptTask {

	private static final String CYCOMMAND_TITLE = "Cytoscape Commands";
	
	@Tunable(description = "Script File Name:", params = "fileCategory=SCRIPT;input=true")
	public File file;

	@Tunable(description = "Select Script Type:")
	public ListSingleSelection<String> engineNames;
	
	@ProvidesTitle
	public String getTitle() {
		return "Run Script File";
	}
	
	// For CyCommands
	final CommandExecutorTaskFactory commandExecutorTaskFactoryService;

	ExecuteScriptTask(final ScriptEngineManager manager, final CyAppAdapter cyAppAdapter,
			final CommandExecutorTaskFactory commandExecutorTaskFactoryService) {
		super(manager, cyAppAdapter);
		
		this.commandExecutorTaskFactoryService = commandExecutorTaskFactoryService;

		engineNameList.add(CYCOMMAND_TITLE);
		engineNames = new ListSingleSelection<String>(engineNameList);
		engineNames.setSelectedValue(engineNameList.get(0));
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final String selected = engineNames.getSelectedValue();
		
		// Special case: CyCommand
		if(selected == CYCOMMAND_TITLE) {
			executeCyCommandFile();
			return;
		}
		
		final ScriptEngineFactory engineFactory = name2engineMap.get(engineNames.getSelectedValue());
		final ScriptEngine engine = engineFactory.getScriptEngine();

		// Provide access to CyAppAdapter.
		engine.put("cyAppAdapter", cyAppAdapter);

		// Execute
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			engine.eval(reader);
		} finally {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		}
	}
	
	private void executeCyCommandFile() throws Exception {
		final TaskIterator cyCommandTasks = commandExecutorTaskFactoryService.createTaskIterator(file, null);
		this.insertTasksAfterCurrentTask(cyCommandTasks);
	}
}
