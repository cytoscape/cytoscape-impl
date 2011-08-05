package org.cytoscape.io.internal.read.datatable;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractTableReaderFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.work.TaskIterator;

public class CyAttributesReaderFactory extends AbstractTableReaderFactory {

	private final CyApplicationManager appMgr;
	private final CyNetworkManager netMgr;

	public CyAttributesReaderFactory(CyFileFilter filter,
			CyTableFactory factory, CyApplicationManager appMgr,
			CyNetworkManager netMgr) {
		super(filter, factory);
		this.appMgr = appMgr;
		this.netMgr = netMgr;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new CyAttributesReader(inputStream,
				tableFactory, appMgr, netMgr));
	}
}
