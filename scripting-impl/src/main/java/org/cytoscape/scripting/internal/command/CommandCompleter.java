package org.cytoscape.scripting.internal.command;

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

import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;

public class CommandCompleter implements Completer {

	private ScriptEngineManager manager;

	/**
	 * @param buffer
	 *            the beginning string typed by the user
	 * @param cursor
	 *            the position of the cursor
	 * @param candidates
	 *            the list of completions proposed to the user
	 */
	public int complete(String buffer, int cursor, List candidates) {
		StringsCompleter delegate = new StringsCompleter();

		final List<ScriptEngineFactory> engines = manager.getEngineFactories();

		for (final ScriptEngineFactory engine : engines) {
			final List<String> names = engine.getNames();
			for (final String name : names)
				delegate.getStrings().add(name);
		}

		return delegate.complete(buffer, cursor, candidates);
	}

}