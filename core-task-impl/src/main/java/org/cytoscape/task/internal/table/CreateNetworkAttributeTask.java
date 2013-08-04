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
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.internal.utils.ColumnTunable;
import org.cytoscape.task.internal.utils.ColumnTypeTunable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateNetworkAttributeTask extends AbstractTableDataTask {
	final CyApplicationManager appMgr;
	Map<CyIdentifiable, Map<String, Object>> networkData;

	@Tunable(description="Network", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public ColumnTunable columnTunable;

	@ContainsTunables
	public ColumnTypeTunable columnTypeTunable;

	public CreateNetworkAttributeTask(CyTableManager mgr, CyApplicationManager appMgr) {
		super(mgr);
		this.appMgr = appMgr;
		columnTunable = new ColumnTunable();
		columnTypeTunable = new ColumnTypeTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) network = appMgr.getCurrentNetwork();

		CyTable networkTable = getNetworkTable(network, CyNetwork.class, columnTunable.getNamespace());

		try {
			createColumn(networkTable, columnTunable.getColumnName(), 
		               columnTypeTunable.getColumnType(), 
		               columnTypeTunable.getListElementType());

			if (columnTypeTunable.getColumnType() == "list")
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created new "+columnTypeTunable.getListElementType()+" list column: "+columnTunable.getColumnName());
			else
				taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created new "+columnTypeTunable.getColumnType()+" column: "+columnTunable.getColumnName());
		} catch (Exception e) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Unable to create new column: "+e.getMessage());
		}

	}

}
