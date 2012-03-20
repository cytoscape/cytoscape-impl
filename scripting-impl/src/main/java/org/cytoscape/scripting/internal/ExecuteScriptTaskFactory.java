package org.cytoscape.scripting.internal;

import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExecuteScriptTaskFactory extends AbstractTaskFactory {
	
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
