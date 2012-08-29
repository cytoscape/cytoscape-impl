package org.cytoscape.task.internal.loaddatatable;


import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.internal.table.UpdateAddedNetworkAttributes;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.LoadTableURLTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;


public class LoadAttributesURLTaskFactoryImpl extends AbstractTaskFactory implements LoadTableURLTaskFactory {
	
	private CyTableReaderManager mgr;
	
	private final TunableSetter tunableSetter; 
	private  final CyNetworkManager netMgr;
	private final CyTableManager tableMgr;
	private final UpdateAddedNetworkAttributes updateAddedNetworkAttributes; 
	private final CyRootNetworkManager rootNetMgr;
	
	public LoadAttributesURLTaskFactoryImpl(CyTableReaderManager mgr, TunableSetter tunableSetter,  final CyNetworkManager netMgr,
			final CyTableManager tabelMgr, final UpdateAddedNetworkAttributes updateAddedNetworkAttributes, final CyRootNetworkManager rootNetMgr) {
		this.mgr = mgr;
		this.tunableSetter = tunableSetter;
		this.netMgr = netMgr;
		this.tableMgr = tabelMgr;
		this.updateAddedNetworkAttributes = updateAddedNetworkAttributes;
		this.rootNetMgr = rootNetMgr;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new LoadAttributesURLTask(mgr, netMgr, tableMgr, updateAddedNetworkAttributes, rootNetMgr));
	}

	@Override
	public TaskIterator createTaskIterator(URL url) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("url", url);

		return tunableSetter.createTaskIterator(this.createTaskIterator(), m); 
	}
}
