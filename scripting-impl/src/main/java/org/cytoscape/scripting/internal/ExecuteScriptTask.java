package org.cytoscape.scripting.internal;

import java.io.File;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Use standard Java scripting mechanism to run script.
 *
 */
public class ExecuteScriptTask extends AbstractExecuteScriptTask {

	@Tunable(description = "Select a script file:", params = "fileCategory=SCRIPT;input=true")
	public File file;
	
	@Tunable(description = "Select Scripting Language:")
	public ListSingleSelection<String> engineNames;

	
	ExecuteScriptTask(final ScriptEngineManager manager, final CyAppAdapter cyAppAdapter) {
		super(manager, cyAppAdapter);
		
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
}
