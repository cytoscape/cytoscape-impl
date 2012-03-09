package org.cytoscape.scripting.internal;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Use standard Java scripting mechanism to run script.
 *
 */
public class ExecuteScriptTask implements Task {

	@Tunable(description = "Select a script file:", params = "fileCategory=SCRIPT;input=true")
	public File file;
	
	@Tunable(description = "Select Scripting Language:")
	public ListSingleSelection<String> engineNames;

	private final Map<String, ScriptEngineFactory> name2engineMap;
	private final CyAppAdapter cyAppAdapter;
	
	ExecuteScriptTask(final ScriptEngineManager manager, final CyAppAdapter cyAppAdapter) {
		this.cyAppAdapter = cyAppAdapter;
		
		this.name2engineMap = new HashMap<String, ScriptEngineFactory>();
		
		final List<ScriptEngineFactory> engines = manager.getEngineFactories();
		final List<String> engineNameList = new ArrayList<String>();
		
		for(final ScriptEngineFactory engine: engines) {
			final String langName = engine.getEngineName();
			final String langVersion = engine.getLanguageVersion();
			final String engineName = engine.getEngineName();
			final String engineDescription = langName + " (" + engineName + ", Version " + langVersion + ")";
			engineNameList.add(engineDescription);
			name2engineMap.put(engineDescription, engine);
		}
		
		if(engineNameList.size() == 0)
			throw new IllegalStateException("No Scripting Engine is available.");
		
		engineNames = new ListSingleSelection<String>(engineNameList);
		engineNames.setSelectedValue(engineNameList.get(0));
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		final ScriptEngineFactory engineFactory = name2engineMap.get(engineNames.getSelectedValue());
		final ScriptEngine engine = engineFactory.getScriptEngine();

		// Provide access to CyAppAdapter.
		engine.put("cyAppAdapter", cyAppAdapter);
		
		// Execute
		engine.eval(new FileReader(file));
	}

	@Override
	public void cancel() {
		Thread.currentThread().interrupt();
	}

}
