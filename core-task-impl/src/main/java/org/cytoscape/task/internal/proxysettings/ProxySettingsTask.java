package org.cytoscape.task.internal.proxysettings;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


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

