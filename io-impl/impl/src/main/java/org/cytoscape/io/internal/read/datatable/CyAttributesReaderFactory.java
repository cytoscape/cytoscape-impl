package org.cytoscape.io.internal.read.datatable;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractTableReaderFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskIterator;


public class CyAttributesReaderFactory extends AbstractTableReaderFactory {
	private final CyApplicationManager appMgr;
	private final CyNetworkManager netMgr;
	private final CyRootNetworkManager rootNetFact;

	public CyAttributesReaderFactory(final CyFileFilter filter, final CyTableFactory factory,
					 final CyApplicationManager appMgr,
					 final CyNetworkManager netMgr,
					 final CyTableManager tableManager,
					 final CyRootNetworkManager rootNetFact)
	{
		super(filter, factory, tableManager);
		this.appMgr = appMgr;
		this.netMgr = netMgr;
		this.rootNetFact = rootNetFact;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new CyAttributesReader(inputStream, tableFactory, appMgr,
		                                               netMgr, tableManager,rootNetFact));
	}
}
