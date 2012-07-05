/*
 File: LoadNetworkFileTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.task.internal.loadnetwork;

import java.io.File;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * Specific instance of AbstractLoadNetworkTask that loads a File.
 */
public class LoadNetworkFileTask extends AbstractLoadNetworkTask {
	@Tunable(description = "Network file to load", params = "fileCategory=network;input=true")
	public File file;

	@ProvidesTitle
	public String getTitle() {
		return "Load Network from File";
	}
	
	public LoadNetworkFileTask(CyNetworkReaderManager mgr, CyNetworkManager netmgr,
			final CyNetworkViewManager networkViewManager, final Properties props, CyNetworkNaming namingUtil, final VisualMappingManager vmm) {
		super(mgr, netmgr, networkViewManager, props, namingUtil, vmm);
	}

	/**
	 * Executes Task.
	 */
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		if (file == null)
			throw new NullPointerException("No file specified.");

		reader = mgr.getReader(file.toURI(), file.getName());

		if (cancelled)
			return;

		if (reader == null)
			throw new NullPointerException("Failed to find appropriate reader for file: " + file);

		uri = file.toURI();
		name = file.getName();

		loadNetwork(reader);
	}
}
