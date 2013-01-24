package org.cytoscape.task.internal.setcurrent;

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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * This class exists for (possible) use in headless mode.  The associated 
 * TaskFactory should not be registered in Swing mode, since this task doesn't 
 * make sense in GUI mode.
 */
public class SetCurrentNetworkTask extends AbstractTask {
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private TaskMonitor taskMonitor;
	
	public SetCurrentNetworkTask(final CyApplicationManager applicationManager,
				     final CyNetworkManager networkManager)
	{
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Verify that we want an essentially random network and that this
		// task shouldn't be NetworkTask instead.
		this.taskMonitor = taskMonitor;
		taskMonitor.setProgress(0.0);
		Object[] setNetworks = networkManager.getNetworkSet().toArray();
		taskMonitor.setProgress(0.3);
		applicationManager.setCurrentNetwork(((CyNetwork) setNetworks[setNetworks.length-1]));
		taskMonitor.setProgress(1.0);
	}
}
