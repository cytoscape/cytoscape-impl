package org.cytoscape.internal.task;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadMultipleNetworkFilesTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

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

	private final int FILE_COUNT_THRESHOLD = 10;
	
	private final Queue<File> queue;
	private List<File> netFiles;
	private final CyRootNetwork rootNetwork;
	private final boolean confirmLargeList;
	
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * @param files Files or directories. Hidden files are ignored.
	 * @param rootNetwork The target network collection.
	 * @param serviceRegistrar
	 */
	public LoadFileListTask(List<File> files, CyRootNetwork rootNetwork, CyServiceRegistrar serviceRegistrar) {
		this(new ArrayDeque<>(files), new ArrayList<>(), rootNetwork, true, serviceRegistrar);
	}
	
	private LoadFileListTask(
			Queue<File> sourceQueue,
			List<File> netFiles,
			CyRootNetwork rootNetwork,
			boolean confirmLargeList,
			CyServiceRegistrar serviceRegistrar
	) {
		this.queue = sourceQueue;
		this.netFiles = netFiles;
		this.rootNetwork = rootNetwork;
		this.confirmLargeList = confirmLargeList;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Import Files");
		tm.setStatusMessage("Scanning Files...");
		tm.setProgress(-1);
		
		// Collect files first
		while (!queue.isEmpty() && !cancelled) {
			File f = queue.remove();
			
			if (f.isDirectory()) {
				File[] listFiles = f.listFiles();
				
				if (listFiles != null)
					queue.addAll(Arrays.asList(listFiles));
			} else if (f.exists() && f.canRead() && !f.isHidden()) {
				netFiles.add(f);
				
				if (confirmLargeList && netFiles.size() > FILE_COUNT_THRESHOLD) {
					// Too many files? The user has to confirm it then!
					insertTasksAfterCurrentTask(new ConfirmLargeFileListTask());
					return;
				}
			}
		}
		
		if (cancelled)
			return;
		
		int cysCount = 0;
		File cysFile = null;
		
		for (Iterator<File> iter = netFiles.iterator(); iter.hasNext() && !cancelled;) {
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
	
	public class ConfirmLargeFileListTask extends AbstractTask {

		@Tunable(
				description = "<html>The file list contains more than " + FILE_COUNT_THRESHOLD +
				              " files, which may take too long to import.<br />Do you want to continue?</html>",
				params = "ForceSetDirectly=true;ForceSetTitle=Import Files"
		)
		public boolean ok;
		
		@Override
		public void run(TaskMonitor tm) throws Exception {
			// If the user confirm it, execute the original task again, but it won't check the file count this time
			if (ok && ! cancelled)
				insertTasksAfterCurrentTask(
						new LoadFileListTask(queue, netFiles, rootNetwork, false, serviceRegistrar));
		}
	}
}
