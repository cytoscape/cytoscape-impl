package org.cytoscape.task.internal.proxysettings;


import java.util.Properties;

import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * Dialog for assigning proxy settings.
 */
public class ProxySettingsTask extends AbstractTask {

	private final StreamUtil streamUtil;
	private CyProperty<Properties> proxyProperties;
	
	public ProxySettingsTask(CyProperty<Properties> proxyProperties, final StreamUtil streamUtil) {
		this.proxyProperties = proxyProperties;
		this.streamUtil = streamUtil;
	}
	
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setProgress(0.01);
		taskMonitor.setTitle("Set proxy server");
		taskMonitor.setStatusMessage("Setting proxy server...");
	
		// We run ProxySeting in another task, because TunableValidator is used. If we run
		// it in the same task, Cytoscape will be frozen during validating process
		ProxySettingsTask2 task = new ProxySettingsTask2(proxyProperties, this.streamUtil);
		
		this.insertTasksAfterCurrentTask(task);

		taskMonitor.setProgress(1.0);
	}
}

