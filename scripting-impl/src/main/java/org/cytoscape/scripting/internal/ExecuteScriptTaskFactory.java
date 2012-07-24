package org.cytoscape.scripting.internal;

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
