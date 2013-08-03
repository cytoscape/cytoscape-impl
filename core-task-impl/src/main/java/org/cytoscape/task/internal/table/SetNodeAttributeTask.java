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
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.NodeTunable;
import org.cytoscape.task.internal.utils.ColumnValueTunable;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.ContainsTunables;

public class SetNodeAttributeTask extends AbstractGetTableDataTask {
	final CyApplicationManager appMgr;
	Map<CyIdentifiable, Map<String, Object>> nodeData;

	@ContainsTunables
	public NodeTunable nodeTunable;

	@ContainsTunables
	public ColumnValueTunable columnTunable;

	public SetNodeAttributeTask(CyTableManager mgr, CyApplicationManager appMgr) {
		super(mgr);
		this.appMgr = appMgr;
		nodeTunable = new NodeTunable(appMgr);
		columnTunable = new ColumnValueTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyNetwork network = nodeTunable.getNetwork();

		CyTable nodeTable = getNetworkTable(network, CyNode.class, columnTunable.getNamespace());

		for (CyNode node: nodeTunable.getNodeList()) {
			int count = setCyIdentifierData(nodeTable, 
		 	                                node,
		 	                                columnTunable.getValueMap(nodeTable));

			taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Set "+count+" node attribute values for node "+getNodeName(nodeTable, node));
		}
	}

}
