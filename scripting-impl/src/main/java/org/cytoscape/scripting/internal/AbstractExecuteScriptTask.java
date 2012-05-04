package org.cytoscape.scripting.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.Task;

public abstract class AbstractExecuteScriptTask implements Task {

	protected final Map<String, ScriptEngineFactory> name2engineMap;
	protected final CyAppAdapter cyAppAdapter;
	
	protected final List<String> engineNameList;
	
	protected final ScriptEngineManager manager;

	AbstractExecuteScriptTask(final ScriptEngineManager manager, final CyAppAdapter cyAppAdapter) {
		this.cyAppAdapter = cyAppAdapter;
		this.manager = manager;

		this.name2engineMap = new HashMap<String, ScriptEngineFactory>();

		final List<ScriptEngineFactory> engines = manager.getEngineFactories();
		engineNameList = new ArrayList<String>();

		for (final ScriptEngineFactory engine : engines) {
			final String langName = engine.getLanguageName();
			final String langVersion = engine.getLanguageVersion();
			final String engineName = engine.getEngineName();
			final String engineDescription = langName + " (" + engineName + ", Version " + langVersion + ")";
			engineNameList.add(engineDescription);
			name2engineMap.put(engineDescription, engine);
		}

		if (engineNameList.size() == 0)
			throw new IllegalStateException("No Scripting Engine is available.");
	}

	@Override
	public void cancel() {
		Thread.currentThread().interrupt();
	}
}
