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
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.NodeTunable;
import org.cytoscape.task.internal.utils.ColumnListTunable;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.task.internal.utils.DataUtils;

public class GetNodeAttributeTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	Map<CyIdentifiable, Map<String, Object>> nodeDataMap;

	@ContainsTunables
	public NodeTunable nodeTunable;

	@ContainsTunables
	public ColumnListTunable columnTunable;

	public CyServiceRegistrar serviceRegistrar;

	public CyTable nodeTable;

	public GetNodeAttributeTask(CyTableManager mgr, CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar) {
		super(mgr);
		this.appMgr = appMgr;
		nodeTunable = new NodeTunable(appMgr);
		columnTunable = new ColumnListTunable();
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyNetwork network = nodeTunable.getNetwork();

		nodeTable = getNetworkTable(network, CyNode.class, columnTunable.getNamespace());

		nodeDataMap = new HashMap<CyIdentifiable, Map<String, Object>>();
		
		for (CyNode node: nodeTunable.getNodeList()) {
			Map<String, Object> nodeData = getCyIdentifierData(nodeTable, 
			                                                   node, 
			                                                   columnTunable.getColumnNames(nodeTable));
			if (nodeData == null || nodeData.size() == 0)
				continue;

			nodeDataMap.put(node, nodeData);

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Node table values for node "+DataUtils.getNodeName(nodeTable, node)+":");
			for (String column: nodeData.keySet()) {
				if (nodeData.get(column) != null)
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+column+"="+DataUtils.convertData(nodeData.get(column)));
			}
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			return DataUtils.convertMapToString(nodeDataMap);
		}
		else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (nodeDataMap == null) 
					return "[]";
				else {
					StringBuilder output = new StringBuilder("[");
					CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
					
					List<CyColumn> cyColumn = columnTunable.getColumnList(nodeTable);
					CyColumn[] cyColumnArray = cyColumn.size() > 0 ? cyColumn.toArray(new CyColumn[0]) : new CyColumn[]{};
					int count = nodeDataMap.size();
					for (CyIdentifiable node : nodeDataMap.keySet()) {
						CyRow row = nodeTable.getRow(node.getSUID());
						output.append(" " + cyJSONUtil.toJson(row, cyColumnArray));
						if (count > 1) {
							output.append(",\n");
						}
						count--;
					}
					output.append("\n]");
					return output.toString();
				}
			};
			return res;
		}
		return nodeDataMap;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(Map.class, String.class, JSONResult.class);
	}
	
}
