package org.cytoscape.scripting.internal;

import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExecuteScriptTaskFactory implements TaskFactory {
	
	private final ScriptEngineManager manager;
	private final CyAppAdapter cyAppAdapter;
	
	public ExecuteScriptTaskFactory(final CyAppAdapter cyAppAdapter) {
		this.manager = new ScriptEngineManager();
		this.cyAppAdapter = cyAppAdapter;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExecuteScriptTask(manager, cyAppAdapter));
	}

}
