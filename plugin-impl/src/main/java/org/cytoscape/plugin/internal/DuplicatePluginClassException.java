package org.cytoscape.plugin.internal;

public class DuplicatePluginClassException extends PluginException {

	public DuplicatePluginClassException() {
		super("Failed to load duplicate plugin class");
	}

	public DuplicatePluginClassException(String arg0) {
		super(arg0);
	}

	public DuplicatePluginClassException(Throwable arg0) {
		super("Failed to load duplicate plugin class", arg0);
	}

	public DuplicatePluginClassException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
