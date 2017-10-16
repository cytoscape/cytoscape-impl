package org.cytoscape.task.internal.export.network;

import java.io.File;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
public class LoadNetworkFileTask extends AbstractLoadNetworkTask {
	
	@Tunable(description = "Network file to load", 
	         longDescription = "Select a network format file.  This command does not support "+
					                   "csv or Excel files.  Use ``network import file`` for that.",
	         params = "fileCategory=network;input=true")
	public File file;

	@ProvidesTitle
	public String getTitle() {
		return "Load Network from File";
	}
	
	public LoadNetworkFileTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		if (file == null)
			throw new NullPointerException("No file specified.");

		CyNetworkReader reader = serviceRegistrar.getService(CyNetworkReaderManager.class)
				.getReader(file.toURI(), file.getName());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		uri = file.toURI();
		name = file.getName();

		loadNetwork(reader);
	}
}
