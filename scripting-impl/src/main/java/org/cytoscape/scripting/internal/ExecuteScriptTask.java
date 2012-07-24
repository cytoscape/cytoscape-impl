package org.cytoscape.scripting.internal;

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
		final TaskIterator cyCommandTasks = commandExecutorTaskFactoryService.createTaskIterator(file);
		this.insertTasksAfterCurrentTask(cyCommandTasks);
	}
}
