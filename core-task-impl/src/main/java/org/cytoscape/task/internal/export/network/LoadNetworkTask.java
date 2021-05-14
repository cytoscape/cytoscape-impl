package org.cytoscape.task.internal.export.network;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

/**
 * Specific instance of AbstractLoadNetworkTask that loads a File.
 */
public class LoadNetworkTask extends AbstractLoadNetworkTask {
	
	private CyNetworkReader reader;
	
	@ProvidesTitle
	@Override
	public String getTitle() {
		return "Load Network from File";
	}
	
	public LoadNetworkTask(CyNetworkReader reader, String networkName, CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		this.reader = reader;
		this.name = networkName;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		if (reader == null)
			throw new NullPointerException("No reader specified.");

		loadNetwork(reader);
	}
}
