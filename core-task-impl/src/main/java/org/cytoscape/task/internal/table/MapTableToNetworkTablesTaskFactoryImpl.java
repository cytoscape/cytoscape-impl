package org.cytoscape.task.internal.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask.TableType;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public final class MapTableToNetworkTablesTaskFactoryImpl extends AbstractTableTaskFactory
		implements MapTableToNetworkTablesTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public MapTableToNetworkTablesTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(CyTable globalTable) {
		return new TaskIterator(new MapTableToNetworkTablesTask(globalTable, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(
			CyTable globalTable,
			boolean selectedNetworksOnly,
			List<CyNetwork> networksList,
			Class<? extends CyIdentifiable> type
	) {
		TableType tableType = getTableType(type);
		
		if (tableType == null)
			throw new IllegalArgumentException("The specified type " + type + " is not acceptable.");
		
		ListSingleSelection<TableType> tableTypes = new ListSingleSelection<>(tableType);
		tableTypes.setSelectedValue(tableType);

		List<String> networkNames = new ArrayList<>();
		
		for (CyNetwork net : networksList)
			networkNames.add(net.getRow(net).get(CyNetwork.NAME, String.class));

		ListMultipleSelection<String> networksListTunable = new ListMultipleSelection<>(networkNames);
		networksListTunable.setSelectedValues(networkNames);

		final Map<String, Object> m = new HashMap<>();

		m.put("dataTypeOptions", tableTypes);
		m.put("selectedNetworksOnly", selectedNetworksOnly);
		m.put("networkList", networksListTunable);

		return serviceRegistrar.getService(TunableSetter.class).createTaskIterator(createTaskIterator(globalTable), m);
	}

	private TableType getTableType(Class<? extends CyIdentifiable> type) {
		if (type.equals(TableType.GLOBAL.getType()))
			return TableType.GLOBAL;
		if (type.equals(TableType.EDGE_ATTR.getType()))
			return TableType.EDGE_ATTR;
		if (type.equals(TableType.NETWORK_ATTR.getType()))
			return TableType.NETWORK_ATTR;
		if (type.equals(TableType.NODE_ATTR.getType()))
			return TableType.NODE_ATTR;
		return null;
	}
}
