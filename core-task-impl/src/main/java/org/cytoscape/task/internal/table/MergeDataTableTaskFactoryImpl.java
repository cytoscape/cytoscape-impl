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

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.task.edit.MergeDataTableTaskFactory;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTask.TableType;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class MergeDataTableTaskFactoryImpl extends AbstractTaskFactory implements MergeDataTableTaskFactory {
	
	private final CyNetworkManager networkManager;
	private final TunableSetter tunableSetter; 
	private final CyRootNetworkManager rootNetMgr;
	private final CyTableManager tabelMgr;
	
	public MergeDataTableTaskFactoryImpl( final CyTableManager tabelMgr,final CyNetworkManager networkManager, final TunableSetter tunableSetter, final CyRootNetworkManager rootNetMgr){
		this.networkManager = networkManager;
		this.tunableSetter = tunableSetter;
		this.rootNetMgr = rootNetMgr;
		this.tabelMgr = tabelMgr;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new MergeDataTableTask(tabelMgr, rootNetMgr, networkManager));
	}
	
	

	@Override
	public TaskIterator createTaskIterator(final CyTable sourceTable,final CyTable targetTable,
			List<String> sourceColumnsList, String sourceKeyColumn,boolean mergeColumnVirtual, boolean mapToNetworks,
			 boolean selectedNetworksOnly,  List<CyNetwork> networkList, 
			CyRootNetwork rootNetwork, CyColumn targetJoinColumn, Class<? extends CyIdentifiable> type) {
		
		
		TableType tableType = getTableType(type);
		if(tableType == null)
			throw new IllegalArgumentException("The specified type " + type + " is not acceptable.");
		ListSingleSelection<TableType> tableTypes = new ListSingleSelection<TableType>(tableType);
		tableTypes.setSelectedValue(tableType);
		
		List<String> networkNames = new ArrayList<String>();
		if(networkList != null)
		{
			for(CyNetwork net: networkList){
				networkNames.add(net.getRow(net).get(CyNetwork.NAME, String.class));
			}
		}
	
		ListMultipleSelection<String> networksListTunable = new ListMultipleSelection<String>(networkNames);
		networksListTunable.setSelectedValues(networkNames);
		
		List<String> rootNetworkNames = new ArrayList<String>();
		ListSingleSelection<String> rootNetworkList = new ListSingleSelection<String>();
		if (rootNetwork != null){
			rootNetworkNames.add( rootNetwork.getRow(rootNetwork).get(CyNetwork.NAME, String.class));
			rootNetworkList = new ListSingleSelection<String>(rootNetworkNames);
			rootNetworkList.setSelectedValue(rootNetworkNames.get(0));
		}

		List<String> columnNames = new ArrayList<String>();
		ListSingleSelection<String> columnNamesList = new ListSingleSelection<String>();
		if (targetJoinColumn != null){
			columnNames.add(targetJoinColumn.getName());
			columnNamesList = new ListSingleSelection<String>(columnNames);
			columnNamesList.setSelectedValue(columnNames.get(0));
		}
		
		final Map<String, Object> m = new HashMap<String, Object>();
		
	    ListSingleSelection<String> chooser = new ListSingleSelection<String>(MergeDataTableTask.NETWORK_COLLECTION,MergeDataTableTask.NETWORK_SELECTION,MergeDataTableTask.UNASSIGNED_TABLE);
		
	    if(mapToNetworks)
	    {
			if(selectedNetworksOnly)
				chooser.setSelectedValue(MergeDataTableTask.NETWORK_SELECTION);
			else
				chooser.setSelectedValue(MergeDataTableTask.NETWORK_COLLECTION);
	    }
	    else
	    	chooser.setSelectedValue(MergeDataTableTask.UNASSIGNED_TABLE);
	    
		ListSingleSelection<CyTable> sourceTableList = new ListSingleSelection<CyTable> (sourceTable);
		sourceTableList.setSelectedValue(sourceTable);
		ListSingleSelection<Object> targetTableList;
		if(targetTable != null)
		{
			targetTableList = new ListSingleSelection<Object> (targetTable);
			targetTableList.setSelectedValue(targetTable);
		}
		else
		{
			targetTableList = new ListSingleSelection<Object> (MergeDataTableTask.NO_TABLES);
			targetTableList.setSelectedValue(MergeDataTableTask.NO_TABLES);
		}
		ListMultipleSelection<String> sourceColNames = new ListMultipleSelection<String>(sourceColumnsList);
		sourceColNames.setSelectedValues(sourceColumnsList);
		ListSingleSelection<String> sourceColumn = new ListSingleSelection<String>(sourceKeyColumn);
		sourceColumn.setSelectedValue(sourceKeyColumn);
		
		
		m.put("MergeType", mergeColumnVirtual);
		m.put("SourceMergeKey", sourceColumn);
		m.put("TargetMergeKey", columnNamesList);
		m.put("SourceTable", sourceTableList);
		m.put("UnassignedTable", targetTableList);
		m.put("SourceMergeColumns", sourceColNames);
		m.put("WhereMergeTable", chooser);
		m.put("TargetNetworkList", networksListTunable);
		m.put("TargetKeyNetworkCollection", columnNamesList);
		m.put("TargetNetworkCollection", rootNetworkList);
		m.put("DataTypeTargetForNetworkCollection", tableTypes);
		m.put("DataTypeTargetForNetworkList", tableTypes);
		
		return tunableSetter.createTaskIterator(createTaskIterator(), m);
		
	}

	private TableType getTableType( Class<? extends CyIdentifiable> type) {
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
