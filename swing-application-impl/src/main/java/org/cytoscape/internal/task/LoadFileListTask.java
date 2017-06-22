package org.cytoscape.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadMultipleNetworkFilesTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
 * Calls other tasks that loads session or network files, as needed.
 */
public class LoadFileListTask extends AbstractTask {

	private final List<File> files;
	private final CyRootNetwork rootNetwork;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * @param files Files or directories. Hidden files are ignored.
	 * @param rootNetwork The target network collection.
	 * @param serviceRegistrar
	 */
	public LoadFileListTask(List<File> files, CyRootNetwork rootNetwork, CyServiceRegistrar serviceRegistrar) {
		this.files = files;
		this.rootNetwork = rootNetwork;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Import Files");
		tm.setStatusMessage("Scanning Files...");
		tm.setProgress(-1);
		
		// Collect files first
		List<File> netFiles = new ArrayList<>();
		getOnlyFiles(files, netFiles, tm);
		
		if (cancelled)
			return;
		
		int cysCount = 0;
		File cysFile = null;
		
		for (Iterator<File> iter = netFiles.iterator(); iter.hasNext();) {
			if (cancelled)
				return;
			
			File file = iter.next();
			
			if (isCySession(file)) {
				iter.remove();
				cysCount++;
				
				// The dragged list can contain only 1 cys file!
				if (cysCount > 1)
					throw new RuntimeException(
							"The file list cannot contain more than one Cytoscape Session (.cys) file.");

				cysFile = file;
			}
		}
		
		if (cancelled)
			return;
		
		// If there is a session file to be loaded, load standalone network files as new network collections
		if (!netFiles.isEmpty())
			insertTasksAfterCurrentTask(serviceRegistrar.getService(LoadMultipleNetworkFilesTaskFactory.class)
					.createTaskIterator(netFiles, cysFile == null ? rootNetwork : null));
		
		if (cancelled)
			return;
		
		// Load the session file first, if there is one and only one .cys file
		if (cysFile != null)
			insertTasksAfterCurrentTask(
					serviceRegistrar.getService(OpenSessionTaskFactory.class).createTaskIterator(cysFile, true));
	}
	
	private boolean isCySession(File f) {
		return f != null && f.isFile() && f.getName().toLowerCase().endsWith(".cys");
	}
	
	private void getOnlyFiles(List<File> filesOrDirectories, List<File> normalFiles, TaskMonitor tm) {
		for (File f : filesOrDirectories) {
			if (cancelled)
				return;
			
			if (f.isDirectory()) {
				File[] listFiles = f.listFiles();
				
				if (listFiles != null)
					getOnlyFiles(Arrays.asList(listFiles), normalFiles, tm);
			} else if (f.exists() && f.canRead() && !f.isHidden()) {
				normalFiles.add(f);
			}
		}
	}
}
