package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.ColumnListTunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class GetNetworkAttributeTask extends AbstractTableDataTask implements ObservableTask {
	
	@Tunable(description="Network", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         context="nogui")
	public CyNetwork network;

	@ContainsTunables
	public ColumnListTunable columnTunable;

	private Map<String, Object> networkData;
	private CyTable networkTable;

	public GetNetworkAttributeTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		columnTunable = new ColumnListTunable();
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

		networkTable = getNetworkTable(network, CyNetwork.class, columnTunable.getNamespace());
		networkData = getCyIdentifierData(networkTable, network, columnTunable.getColumnNames(networkTable));

		tm.showMessage(TaskMonitor.Level.INFO, "   Attribute values for network "+DataUtils.getNetworkName(network)+":");
		for (String column: networkData.keySet()) {
			if (networkData.get(column) != null)
				tm.showMessage(TaskMonitor.Level.INFO, "        "+column+"="+DataUtils.convertData(networkData.get(column)));
		}
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			return DataUtils.convertMapToString(networkData);
		}
		else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (networkData == null) 
					return "[]";
				else {
					StringBuilder output = new StringBuilder("[");
					CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
					
					List<CyColumn> cyColumn = columnTunable.getColumnList(networkTable);
					CyColumn[] cyColumnArray = cyColumn.size() > 0 ? cyColumn.toArray(new CyColumn[0]) : new CyColumn[]{};
					CyRow row = networkTable.getRow(network.getSUID());
					output.append(" " + cyJSONUtil.toJson(row, cyColumnArray));
					output.append("\n]");
					return output.toString();
				}
			};
			return res;
		}
		return networkData;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Map.class, String.class, JSONResult.class);
	}
}
