package org.cytoscape.task.internal.table;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.ColumnValueTunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

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

public class SetNetworkAttributeTask extends AbstractTableDataTask {
	
	@Tunable(description="Network", context="nogui",
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network;

	@ContainsTunables
	public ColumnValueTunable columnTunable;

	public SetNetworkAttributeTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		columnTunable = new ColumnValueTunable();
	}

	@Override
	public void run(final TaskMonitor tm) {
		if (network == null) {
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if (network == null) {
				tm.showMessage(TaskMonitor.Level.ERROR, "Network must be specified");
				return;
			}
		}

		CyTable networkTable = getNetworkTable(network, CyNetwork.class, columnTunable.getNamespace());
		int count = setCyIdentifierData(networkTable, network, columnTunable.getValueMap(networkTable));

		tm.showMessage(TaskMonitor.Level.INFO,
				"Set " + count + " network table values for network " + DataUtils.getNetworkName(network));
	}
}
