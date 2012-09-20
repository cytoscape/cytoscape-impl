package org.cytoscape.task.internal.loaddatatable;


import java.net.URL;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.internal.table.UpdateAddedNetworkAttributes;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;


public class LoadAttributesURLTask extends AbstractLoadAttributesTask {
	
	@Tunable(description="Data Table URL", params="fileCategory=table;input=true")
	public URL url;

	public LoadAttributesURLTask(final CyTableReaderManager mgr,  final CyNetworkManager netMgr, final CyTableManager tableMgr, 
			final UpdateAddedNetworkAttributes updateAddedNetworkAttributes, final CyRootNetworkManager rootNetMgr) {
		super(mgr, netMgr, tableMgr, updateAddedNetworkAttributes, rootNetMgr);
	}

	/**
	 * Executes Task.
	 */
	public void run(final TaskMonitor taskMonitor) throws Exception {

		loadTable(url.toString(), url.toURI(), taskMonitor);
	}
}

