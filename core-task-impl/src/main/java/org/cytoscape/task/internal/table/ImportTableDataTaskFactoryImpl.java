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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.task.internal.table.ImportTableDataTask.TableType;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class ImportTableDataTaskFactoryImpl extends AbstractTableTaskFactory implements ImportDataTableTaskFactory {
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ImportTableDataTaskFactoryImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(final CyTable table) {
		return new TaskIterator(new ImportTableDataTask(table, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(final CyTableReader reader) {
		return new TaskIterator(new ImportTableDataTask(reader, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(
			final CyTable globalTable,
			final boolean selectedNetworksOnly,
			final boolean loadToUnassignedTable,
			final List<CyNetwork> networkList,
			final CyRootNetwork rootNetwork,
			final CyColumn targetJoinColumn,
			final Class<? extends CyIdentifiable> type
	) {
		ListSingleSelection<String> chooser = new ListSingleSelection<>(ImportTableDataTask.NETWORK_COLLECTION,
				ImportTableDataTask.NETWORK_SELECTION, ImportTableDataTask.UNASSIGNED_TABLE);

		final Map<String, Object> m = new HashMap<>();

		if (!loadToUnassignedTable) {
			TableType tableType = getTableType(type);

			if (tableType == null)
				throw new IllegalArgumentException("The specified type " + type + " is not acceptable.");

			ListSingleSelection<TableType> tableTypes = new ListSingleSelection<>(tableType);
			tableTypes.setSelectedValue(tableType);

			List<String> networkNames = new ArrayList<>();

			for (CyNetwork net : networkList) {
				networkNames.add(net.getRow(net).get(CyNetwork.NAME, String.class));
			}

			ListMultipleSelection<String> networksListTunable = new ListMultipleSelection<>(networkNames);
			networksListTunable.setSelectedValues(networkNames);

			List<String> rootNetworkNames = new ArrayList<>();
			ListSingleSelection<String> rootNetworkList = new ListSingleSelection<>();

			if (rootNetwork != null) {
				rootNetworkNames.add(rootNetwork.getRow(rootNetwork).get(CyNetwork.NAME, String.class));
				rootNetworkList = new ListSingleSelection<>(rootNetworkNames);
				rootNetworkList.setSelectedValue(rootNetworkNames.get(0));
			}

			List<String> columnNames = new ArrayList<>();
			ListSingleSelection<String> columnNamesList = new ListSingleSelection<>();

			if (targetJoinColumn != null) {
				columnNames.add(targetJoinColumn.getName());
				columnNamesList = new ListSingleSelection<>(columnNames);
				columnNamesList.setSelectedValue(columnNames.get(0));
			}

			if (selectedNetworksOnly) {
				m.put("DataTypeTargetForNetworkList", tableTypes);
				chooser.setSelectedValue(ImportTableDataTask.NETWORK_SELECTION);
			} else {
				m.put("DataTypeTargetForNetworkCollection", tableTypes);
				chooser.setSelectedValue(ImportTableDataTask.NETWORK_COLLECTION);
			}

			m.put("TargetNetworkList", networksListTunable);
			m.put("KeyColumnForMapping", columnNamesList);
			m.put("TargetNetworkCollection", rootNetworkList);
		} else {
			chooser.setSelectedValue(ImportTableDataTask.UNASSIGNED_TABLE);
		}

		m.put("WhereImportTable", chooser);

		final TunableSetter tunableSetter = serviceRegistrar.getService(TunableSetter.class);
		
		return tunableSetter.createTaskIterator(createTaskIterator(globalTable), m);

	}
	
	private TableType getTableType(Class<? extends CyIdentifiable> type) {
		if (type.equals(TableType.EDGE_ATTR.getType()))
			return TableType.EDGE_ATTR;
		if (type.equals(TableType.NETWORK_ATTR.getType()))
			return TableType.NETWORK_ATTR;
		if (type.equals(TableType.NODE_ATTR.getType()))
			return TableType.NODE_ATTR;

		return null;
	}
}
