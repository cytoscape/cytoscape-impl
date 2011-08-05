package org.cytoscape.plugin.internal;


import org.cytoscape.plugin.CyPluginAdapter;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class PluginLoaderTaskFactory implements TaskFactory {
	CyPluginAdapter adapter;

	PluginLoaderTaskFactory(CyPluginAdapter adapter) {
		this.adapter = adapter;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new PluginLoaderTask(adapter));
	}
}
