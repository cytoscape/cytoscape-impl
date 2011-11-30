package org.cytoscape.cpath2.internal.task;

import java.net.URL;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class MergeNetworkTaskFactory implements TaskFactory {
	private final URL cpathURL;
	private final CyNetwork cyNetwork;
	private final CPath2Factory factory;

	public MergeNetworkTaskFactory(URL cpathURL, CyNetwork cyNetwork, CPath2Factory factory) {
		this.cpathURL = cpathURL;
		this.cyNetwork = cyNetwork;
		this.factory = factory;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MergeNetworkTask(cpathURL, cyNetwork, factory));
	}

}
