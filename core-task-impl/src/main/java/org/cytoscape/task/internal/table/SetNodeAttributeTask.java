package org.cytoscape.task.internal.table;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.ColumnValueTunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.NodeTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

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

public class SetNodeAttributeTask extends AbstractTableDataTask {
	
	@ContainsTunables
	public NodeTunable nodeTunable;

	@ContainsTunables
	public ColumnValueTunable columnTunable;

	public SetNodeAttributeTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		nodeTunable = new NodeTunable(serviceRegistrar);
		columnTunable = new ColumnValueTunable();
	}

	@Override
	public void run(final TaskMonitor tm) {
		CyNetwork network = nodeTunable.getNetwork();
		CyTable nodeTable = getNetworkTable(network, CyNode.class, columnTunable.getNamespace());

		for (CyNode node : nodeTunable.getNodeList()) {
			int count = setCyIdentifierData(nodeTable, node, columnTunable.getValueMap(nodeTable));

			tm.showMessage(TaskMonitor.Level.INFO,
					"   Set " + count + " node table values for node " + DataUtils.getNodeName(nodeTable, node));
		}
	}
}
