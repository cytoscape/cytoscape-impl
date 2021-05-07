package org.cytoscape.task.internal.export.network;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadMultipleNetworkFilesTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class LoadMultipleNetworkFilesTaskFactoryImpl extends AbstractTaskFactory
		implements LoadMultipleNetworkFilesTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public LoadMultipleNetworkFilesTaskFactoryImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return createTaskIterator(null, null);
	}

	@Override
	public TaskIterator createTaskIterator(List<File> files, CyRootNetwork rootNetwork) {
		if (files == null || files.isEmpty())
			throw new IllegalArgumentException("'files' must not be empty");
		
		return new TaskIterator(new FindReadersAndLoadFilesTask(files, rootNetwork));
	}
	
	private class FindReadersAndLoadFilesTask extends AbstractTask {
		
		private final List<File> files;
		private final CyRootNetwork rootNetwork;

		public FindReadersAndLoadFilesTask(List<File> files, CyRootNetwork rootNetwork) {
			this.files = files;
			this.rootNetwork = rootNetwork;
		}
		
		@Override
		public void run(TaskMonitor tm) throws Exception {
			tm.setTitle("Find Network Readers");
			tm.setStatusMessage("Finding readers for network files...");
			tm.setProgress(0.0);
			
			Map<String, CyNetworkReader> readers = new LinkedHashMap<>();
			CyNetworkReaderManager netReaderManager = serviceRegistrar.getService(CyNetworkReaderManager.class);
			
			final float total = files.size();
			int count = 1;
			
			for (File f : files) {
				if (cancelled)
					return;
				
				try {
					CyNetworkReader netReader = netReaderManager.getReader(f.toURI(), f.toURI().toString());
					
					if (netReader != null)
						readers.put(f.getName(), netReader);
				} catch (Exception e) {
					logger.warn("Cannot load file", e);
				}
				
				tm.setProgress(count / total);
				count++;
			}

			if (cancelled)
				return;
			
			if (readers.isEmpty()) {
				logger.warn("Failed to find appropriate network readers for the input files.");
				return;
			}
				
			tm.setProgress(1.0);
			insertTasksAfterCurrentTask(new LoadMultipleNetworksTask(readers, rootNetwork, serviceRegistrar));
		}
	}
}
