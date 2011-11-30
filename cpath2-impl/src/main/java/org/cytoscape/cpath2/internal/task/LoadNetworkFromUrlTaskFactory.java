package org.cytoscape.cpath2.internal.task;

import java.net.URL;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class LoadNetworkFromUrlTaskFactory implements TaskFactory {

	private URL url;
	private CPath2Factory factory;

	public LoadNetworkFromUrlTaskFactory(URL url, CPath2Factory factory) {
		this.url = url;
		this.factory = factory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new LoadNetworkFromUrlTask(url, factory));
	}

}
