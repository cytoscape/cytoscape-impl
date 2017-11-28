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

import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.task.internal.utils.ColumnListTunable;
import org.cytoscape.task.internal.utils.DataUtils;

public class GetNetworkAttributeTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	Map<String, Object> networkData;

	@Tunable(description="Network", 
	         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
					 exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	         context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public ColumnListTunable columnTunable;

	public CyServiceRegistrar serviceRegistrar;

	private CyTable networkTable;

	public GetNetworkAttributeTask(CyTableManager mgr, CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar) {
		super(mgr);
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
		columnTunable = new ColumnListTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) network = appMgr.getCurrentNetwork();

		networkTable = getNetworkTable(network, CyNetwork.class, columnTunable.getNamespace());


		networkData = getCyIdentifierData(networkTable, 
		                                  network,
		                                  columnTunable.getColumnNames(networkTable));

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Attribute values for network "+DataUtils.getNetworkName(network)+":");
		for (String column: networkData.keySet()) {
			if (networkData.get(column) != null)
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+column+"="+DataUtils.convertData(networkData.get(column)));
		}
	}

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

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Map.class, String.class, JSONResult.class);
	}
}
