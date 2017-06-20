package org.cytoscape.task.internal.loadnetwork;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadMultipleNetworkFilesTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

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

public class LoadMultipleNetworkFilesTaskFactoryImpl extends AbstractTaskFactory
		implements LoadMultipleNetworkFilesTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public LoadMultipleNetworkFilesTaskFactoryImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(null, null);
	}

	@Override
	public TaskIterator createTaskIterator(List<File> filesOrDirectories, CyRootNetwork rootNetwork) {
		return createTaskIterator(filesOrDirectories, rootNetwork, null);
	}

	@Override
	public TaskIterator createTaskIterator(List<File> filesOrDirectories, CyRootNetwork rootNetwork,
			TaskObserver observer) {
		List<File> files = new ArrayList<>();
		getOnlyFiles(filesOrDirectories, files);
		
		Map<String, CyNetworkReader> readerMap = new LinkedHashMap<>();
		CyNetworkReaderManager netReaderManager = serviceRegistrar.getService(CyNetworkReaderManager.class);
		
		for (File f : files) {
			CyNetworkReader reader = netReaderManager.getReader(f.toURI(), f.toURI().toString());
			
			if (reader != null)
				readerMap.put(f.getName(), reader);
		}
		
		TaskIterator taskIterator = new TaskIterator();
		
		if (!readerMap.isEmpty())
			taskIterator.append(new LoadMultipleNetworksTask(readerMap, rootNetwork, serviceRegistrar));

		return taskIterator;
	}

	private void getOnlyFiles(List<File> filesOrDirectories, List<File> files) {
		for (File f : filesOrDirectories) {
			if (f.isDirectory()) {
				File[] listFiles = f.listFiles();
				
				if (listFiles != null)
					getOnlyFiles(Arrays.asList(listFiles), files);
			} else {
				files.add(f);
			}
		}
	}
}
