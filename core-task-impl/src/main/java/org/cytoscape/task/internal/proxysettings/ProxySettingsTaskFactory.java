package org.cytoscape.task.internal.proxysettings;


import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.io.util.StreamUtil;


public class ProxySettingsTaskFactory implements TaskFactory {
	
	private final StreamUtil streamUtil;
	
	public ProxySettingsTaskFactory(StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2,new ProxySettingsTask(streamUtil));
	}
}
