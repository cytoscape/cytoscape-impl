package org.cytoscape.app.internal;


import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class PluginLoaderTaskFactory implements TaskFactory {
	
	private final CyAppAdapter adapter;
	 
	// Plugin Jar file URLs
	public static final Set<URL> urls = new HashSet<URL>();

	PluginLoaderTaskFactory(final CyAppAdapter adapter) {
		this.adapter = adapter;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new PluginLoaderTask(adapter, urls));
	}
}
