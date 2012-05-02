package org.cytoscape.app.internal;


import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class AppLoaderTaskFactory extends AbstractTaskFactory {
	
	private final CySwingAppAdapter adapter;
	 
	// App Jar file URLs
	public static final Set<URL> urls = new HashSet<URL>();

	AppLoaderTaskFactory(final CySwingAppAdapter adapter) {
		this.adapter = adapter;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AppLoaderTask(adapter, urls));
	}
}
