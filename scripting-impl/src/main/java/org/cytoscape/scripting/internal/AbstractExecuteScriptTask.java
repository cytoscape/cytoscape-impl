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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.AbstractTask;

public abstract class AbstractExecuteScriptTask extends AbstractTask {

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

		if (engineNameList.isEmpty())
			throw new IllegalStateException("No Scripting Engine is available.");
	}

	@Override
	public void cancel() {
		Thread.currentThread().interrupt();
	}
}
