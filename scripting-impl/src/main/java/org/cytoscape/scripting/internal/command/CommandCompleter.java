package org.cytoscape.scripting.internal.command;

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