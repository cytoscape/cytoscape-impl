package org.cytoscape.task.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.utils.ColumnTunable;
import org.cytoscape.task.internal.utils.ColumnTypeTunable;
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

public class CreateNodeAttributeTask extends AbstractTableDataTask implements ObservableTask{
	
	@Tunable(description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network;

	@ContainsTunables
	public ColumnTunable columnTunable;

	@ContainsTunables
	public ColumnTypeTunable columnTypeTunable;

	public CreateNodeAttributeTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
		columnTunable = new ColumnTunable();
		columnTypeTunable = new ColumnTypeTunable();
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

		CyTable nodeTable = getNetworkTable(network, CyNode.class, columnTunable.getNamespace());

		try {
			createColumn(nodeTable, columnTunable.getColumnName(), columnTypeTunable.getColumnType(),
					columnTypeTunable.getListElementType());
			success = true;
			
			if (columnTypeTunable.getColumnType() == "list")
				tm.showMessage(TaskMonitor.Level.INFO, "Created new "+columnTypeTunable.getListElementType()+" list column: "+columnTunable.getColumnName());
			else
				tm.showMessage(TaskMonitor.Level.INFO, "Created new "+columnTypeTunable.getColumnType()+" column: "+columnTunable.getColumnName());
		} catch (Exception e) {
			tm.showMessage(TaskMonitor.Level.ERROR, "Unable to create new column: "+e.getMessage());
		}

	}

	@Override
	public Object getResults(Class type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (success) {
					return "{\"columnName\": \"" + columnTunable.getColumnName()+"\"}";
				}
				else {
					return "{}";
				}
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
