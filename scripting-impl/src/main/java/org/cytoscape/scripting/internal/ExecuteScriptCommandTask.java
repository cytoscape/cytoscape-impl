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
