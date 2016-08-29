package org.cytoscape.scripting.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;

/*
 * #%L
 * Cytoscape Scripting Impl (scripting-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public abstract class AbstractExecuteScriptTask extends AbstractTask {

	protected final Map<String, ScriptEngineFactory> name2engineMap;
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected final List<String> engineNameList;
	
	protected final ScriptEngineManager manager;

	AbstractExecuteScriptTask(final ScriptEngineManager manager, final CyServiceRegistrar serviceRegistrar) {
		this.manager = manager;
		this.serviceRegistrar = serviceRegistrar;

		this.name2engineMap = new HashMap<>();

		final List<ScriptEngineFactory> engines = manager.getEngineFactories();
		engineNameList = new ArrayList<>();

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
