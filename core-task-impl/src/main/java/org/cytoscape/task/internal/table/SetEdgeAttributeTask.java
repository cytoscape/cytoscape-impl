package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.ColumnValueTunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

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

public class SetEdgeAttributeTask extends AbstractTableDataTask implements ObservableTask {
	
	@ContainsTunables
	public EdgeTunable edgeTunable;

	@ContainsTunables
	public ColumnValueTunable columnTunable;

	public SetEdgeAttributeTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		edgeTunable = new EdgeTunable(serviceRegistrar);
		columnTunable = new ColumnValueTunable();
	}

	@Override
	public void run(final TaskMonitor tm) {
		CyNetwork network = edgeTunable.getNetwork();
		CyTable edgeTable = getNetworkTable(network, CyEdge.class, columnTunable.getNamespace());

		for (CyEdge edge : edgeTunable.getEdgeList()) {
			int count = setCyIdentifierData(edgeTable, edge, columnTunable.getValueMap(edgeTable));

			tm.showMessage(TaskMonitor.Level.INFO,
					"   Set " + count + " edge table values for edge " + DataUtils.getEdgeName(edgeTable, edge));
		}
	}

	@Override
	public Object getResults(Class type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return "{}";
			};
			return res;
		}
		return null;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class);
	}
}
