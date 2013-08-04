package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.Collections;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.task.internal.utils.ColumnTunable;

public class GetNetworkAttributeTask extends AbstractGetTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	Map<String, Object> networkData;

	@Tunable(description="Network", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public ColumnTunable columnTunable;

	public GetNetworkAttributeTask(CyTableManager mgr, CyApplicationManager appMgr) {
		super(mgr);
		this.appMgr = appMgr;
		columnTunable = new ColumnTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) network = appMgr.getCurrentNetwork();

		CyTable networkTable = getNetworkTable(network, CyNetwork.class, columnTunable.getNamespace());

		networkData = getCyIdentifierData(networkTable, 
		                                  network,
		                                  columnTunable.getColumnNames(networkTable));

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Attribute values for network "+getNetworkTitle(network)+":");
		for (String column: networkData.keySet()) {
			if (networkData.get(column) != null)
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+column+"="+convertData(networkData.get(column)));
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			return convertMapToString(networkData);
		}
		return networkData;
	}
}
