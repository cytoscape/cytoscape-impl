package org.cytoscape.task.internal.proxysettings;


import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class ProxySettingsTaskFactory implements TaskFactory {
	
	private final StreamUtil streamUtil;
	private CyProperty<Properties> proxyProperties;
	
	public ProxySettingsTaskFactory(CyProperty<Properties> proxyProperties, StreamUtil streamUtil) {
		this.proxyProperties = proxyProperties;
		this.streamUtil = streamUtil;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2,new ProxySettingsTask(proxyProperties, streamUtil));
	}
}
