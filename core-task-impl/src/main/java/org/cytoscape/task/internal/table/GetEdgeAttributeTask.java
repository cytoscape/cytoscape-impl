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
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.task.internal.utils.ColumnListTunable;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.ContainsTunables;

import org.cytoscape.task.internal.utils.DataUtils;

public class GetEdgeAttributeTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	Map<CyIdentifiable, Map<String, Object>> edgeDataMap;

	@ContainsTunables
	public EdgeTunable edgeTunable;

	@ContainsTunables
	public ColumnListTunable columnTunable;

	public GetEdgeAttributeTask(CyTableManager mgr, CyApplicationManager appMgr) {
		super(mgr);
		this.appMgr = appMgr;
		edgeTunable = new EdgeTunable(appMgr);
		columnTunable = new ColumnListTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyNetwork network = edgeTunable.getNetwork();

		CyTable edgeTable = getNetworkTable(network, CyEdge.class, columnTunable.getNamespace());

		edgeDataMap = new HashMap<>();

		for (CyEdge edge: edgeTunable.getEdgeList()) {
		  Map<String, Object> edgeData = getCyIdentifierData(edgeTable, 
			                                                   edge,
			                                                   columnTunable.getColumnNames(edgeTable));

			if (edgeData == null || edgeData.size() == 0)
				continue;

			edgeDataMap.put(edge, edgeData);

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Edge table values for edge "+DataUtils.getEdgeName(edgeTable, edge)+":");
			for (String column: edgeData.keySet()) {
				if (edgeData.get(column) != null)
					taskMonitor.showMessage(TaskMonitor.Level.INFO, "        "+column+"="+DataUtils.convertData(edgeData.get(column)));
			}
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			return DataUtils.convertMapToString(edgeDataMap);
		}
		return edgeDataMap;
	}
	
}
