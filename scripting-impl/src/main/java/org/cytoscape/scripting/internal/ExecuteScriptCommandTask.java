package org.cytoscape.scripting.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.TaskMonitor;

public class ExecuteScriptCommandTask extends AbstractExecuteScriptTask {

	private final String engineName;
	private final String filename;
	private final List<String> args;

	public ExecuteScriptCommandTask(final ScriptEngineManager manager, final CyAppAdapter cyAppAdapter,
			final String engineName, final String filename, final List<String> args) {
		super(manager, cyAppAdapter);

		this.engineName = engineName;
		this.filename = filename;
		if(args == null)
			this.args = new ArrayList<String>();
		else
			this.args = args;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final ScriptEngine engine = manager.getEngineByName(engineName);

		// Provide access to CyAppAdapter.
		engine.put("cyAppAdapter", cyAppAdapter);
		
		final String[] argArray = new String[args.size()];
		for(int i=0; i<args.size(); i++) {
			System.out.println("* ARG = " + args.get(i));
			argArray[i] = args.get(i);
		}
		engine.put("args", argArray);
		
		// Execute
		FileReader reader = null;
		try {
			reader = new FileReader(new File(filename));
			engine.eval(reader);
		} catch (FileNotFoundException e) {
			throw new IOException("Could not open the file.", e);
		} finally {
			if(reader != null) {
				reader.close();
				reader = null;
			}
		}
	}

}
