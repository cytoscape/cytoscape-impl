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

import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.EdgeTunable;
import org.cytoscape.task.internal.utils.ColumnValueTunable;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.ContainsTunables;

public class SetEdgeAttributeTask extends AbstractGetTableDataTask {
	final CyApplicationManager appMgr;
	Map<CyIdentifiable, Map<String, Object>> edgeData;

	@ContainsTunables
	public EdgeTunable edgeTunable;

	@ContainsTunables
	public ColumnValueTunable columnTunable;

	public SetEdgeAttributeTask(CyTableManager mgr, CyApplicationManager appMgr) {
		super(mgr);
		this.appMgr = appMgr;
		edgeTunable = new EdgeTunable(appMgr);
		columnTunable = new ColumnValueTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyNetwork network = edgeTunable.getNetwork();

		CyTable edgeTable = getNetworkTable(network, CyEdge.class, columnTunable.getNamespace());

		for (CyEdge edge: edgeTunable.getEdgeList()) {
			int count = setCyIdentifierData(edgeTable, 
		 	                                edge,
		 	                                columnTunable.getValueMap(edgeTable));

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Set "+count+" edge attribute values for edge "+getEdgeName(edgeTable, edge));
		}
	}

}
