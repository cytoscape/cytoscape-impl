package org.cytoscape.io.internal.read.datatable;


import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractTableReaderFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.work.TaskIterator;


public class CyAttributesReaderFactory extends AbstractTableReaderFactory {
	private final CyApplicationManager appMgr;
	private final CyNetworkManager netMgr;
	private final CyRootNetworkManager rootNetFact;

	public CyAttributesReaderFactory(final CyFileFilter filter, final CyTableFactory factory,
					 final CyApplicationManager appMgr,
					 final CyNetworkManager netMgr,
					 final CyRootNetworkManager rootNetFact)
	{
		super(filter, factory);
		this.appMgr = appMgr;
		this.netMgr = netMgr;
		this.rootNetFact = rootNetFact;
	}

	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new CyAttributesReader(inputStream, tableFactory, appMgr,
		                                               netMgr,rootNetFact));
	}
}
