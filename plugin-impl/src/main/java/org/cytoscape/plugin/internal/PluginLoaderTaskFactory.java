package org.cytoscape.plugin.internal;


import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.plugin.CyPluginAdapter;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class PluginLoaderTaskFactory implements TaskFactory {
	
	private final CyPluginAdapter adapter;
	
	// Plugin Jar file URLs
	private final Set<URL> urls;

	PluginLoaderTaskFactory(final CyPluginAdapter adapter) {
		this.adapter = adapter;
		urls = new HashSet<URL>();
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new PluginLoaderTask(adapter, urls));
	}
}
