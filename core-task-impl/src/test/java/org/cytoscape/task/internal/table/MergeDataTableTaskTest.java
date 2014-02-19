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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.internal.CySubNetworkImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesOnlyTask;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutator;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;
import org.junit.Test;


public class MergeDataTableTaskTest {

	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private final TableTestSupport tableSupport = new TableTestSupport();
	private CyRootNetwork root;

	private CyEventHelper eventHelper = new DummyCyEventHelper();
	private CyNetworkManagerImpl netMgr = new CyNetworkManagerImpl(eventHelper);	
	private final CyRootNetworkManagerImpl rootNetMgr = new CyRootNetworkManagerImpl();
	private final CyTableManagerImpl tableMgr = new CyTableManagerImpl(eventHelper,new CyNetworkTableManagerImpl(),netMgr);
	
	private SyncTunableMutator stm = new SyncTunableMutator();
	SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();

	private TunableSetterImpl ts = new TunableSetterImpl(new SyncTunableMutatorFactory(syncTunableHandlerFactory),  new TunableRecorderManager());
	CyTableManager tabMgr = new CyTableManagerImpl(eventHelper, new CyNetworkTableManagerImpl(), netMgr);
	Properties syncFactoryProp = new Properties();
	
	private CyGroupManager groupMgr = mock(CyGroupManager.class);
	private RenderingEngineManager renderingEngineManager = mock(RenderingEngineManager.class);

	@Before
	public void setUp() throws Exception {
		when(renderingEngineManager.getRenderingEngines(any(View.class))).thenReturn(Collections.EMPTY_LIST);
	}
	
	@Test
	public void mappingToAllNetworksWithSubNetwork() throws Exception{
		//set up tunable mutator
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		
		CyNetwork net1;
		CyNetwork subnet1;
		CyNetwork subnet2;

		String node1Name = "node1";
		String node2Name = "node2";
		
		String row1Name = "node1";
		String row2Name = "node2";

		CyTableImpl table1;
		CyTableImpl table2;
		CyTableImpl table3;
		CyTableImpl table4;
		
		String table1sCol = "col1";
		String table2sCol = "col2";
		String table3sCol = "col3";
		String table4sCol = "col4";
		
		String table1sRow1 = "col1 row1";
		String table1sRow2 = "col1 row2";

		String table2sRow1 = "col2 row1";
		String table2sRow2 = "col2 row2";
		
		String table3sRow1 = "col3 row1";
		String table3sRow2 = "col3 row2";
		
		String table4sRow1 = "col4 row1";
		String table4sRow2 = "col4 row2";
		
		
		
		//creating first network with 2 nodes
		net1 = support.getNetwork();
		net1.getRow(net1).set(CyNetwork.NAME, "net1");
		final CyNode node1 =  net1.addNode();
		final CyNode node2 =  net1.addNode();
		net1.addEdge(node1, node2, true);
		
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.NAME, node1Name);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyNetwork.NAME, node2Name);
		
		netMgr.addNetwork(net1);
		((CySubNetworkImpl) net1).handleEvent(new NetworkAddedEvent(netMgr, net1));

		
		List<CyNetwork> firstnetlist = new ArrayList<CyNetwork>(netMgr.getNetworkSet());
		
		//creating a table for mapping to all networks
		table1 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table1.createColumn(table1sCol, String.class, false);
		
		CyRow row1 = table1.getRow(node1Name);
		row1.set(table1sCol, table1sRow1);
		CyRow row2 = table1.getRow(node2Name);
		row2.set(table1sCol, table1sRow2);
		
		root = rootNetMgr.getRootNetwork(net1);
		List<String> listCols = new ArrayList<String>();
		listCols.add(table1sCol);
		mapping(table1,listCols,"ID", true, net1, root, root.getDefaultNodeTable().getColumn(CyRootNetwork.SHARED_NAME),false);
		//check the mapping by task
		assertNotNull(net1.getDefaultNodeTable().getColumn(table1sCol));
		assertEquals(table1sRow1, net1.getDefaultNodeTable().getRow(node1.getSUID()).get(table1sCol, String.class) );
		
		//creating a sub network to check if gets updated or not
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.SELECTED, true);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyNetwork.SELECTED, true);
		
		NewNetworkSelectedNodesOnlyTask newNetTask = new NewNetworkSelectedNodesOnlyTask(mock(UndoSupport.class), net1, support.getRootNetworkFactory(), viewSupport.getNetworkViewFactory(),
				netMgr, mock(CyNetworkViewManager.class) , mock(CyNetworkNaming.class), mock(VisualMappingManager.class), mock(CyApplicationManager.class), eventHelper, groupMgr, renderingEngineManager);
		
		assertNotNull(newNetTask);
		newNetTask.setTaskIterator(new TaskIterator(newNetTask));
		newNetTask.run(mock(TaskMonitor.class));
		
		
		List<CyNetwork> secondNetList  = new ArrayList<CyNetwork>(netMgr.getNetworkSet());
		secondNetList.removeAll(firstnetlist);
		assertEquals(1, secondNetList.size());

		subnet1 = secondNetList.get(0);
		secondNetList  = new ArrayList<CyNetwork>(netMgr.getNetworkSet());
		
		((CySubNetworkImpl) subnet1).handleEvent(new NetworkAddedEvent(netMgr, subnet1));
		
		
		assertEquals(2, subnet1.getNodeList().size());
		
		assertNotNull(subnet1.getDefaultNodeTable().getColumn(table1sCol));
		//these two tests are failing because the required table update when creating the subnetwork is not handled
		//hence the nodes are there but the related rows in the table are empty
		assertEquals(table1sRow1, subnet1.getRow(node1).get(table1sCol, String.class) );
				
		//creating another table to map to the net1 only
		table2 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table2.createColumn(table2sCol, String.class, false);
		
		CyRow row3 = table2.getRow(node1Name);
		row3.set(table2sCol, table2sRow1);
		CyRow row4 = table2.getRow(node2Name);
		row4.set(table2sCol,table2sRow2);
		
		root = rootNetMgr.getRootNetwork(net1);
		listCols = new ArrayList<String>();
		listCols.add(table2sCol);
		mapping(table2,listCols,"ID", true, net1,root, root.getDefaultNodeTable().getColumn(CyRootNetwork.SHARED_NAME), true);
		//check the mapping by task
		assertNotNull(net1.getDefaultNodeTable().getColumn(table2sCol));
		assertEquals(table2sRow1, net1.getDefaultNodeTable().getRow(node1.getSUID()).get(table2sCol, String.class) );
		
		assertNull(subnet1.getDefaultNodeTable().getColumn(table2sCol)); //subnet1 should not be mapped 

		//creating another subnetwork (subnet2) to check that bot virtual columns will be added
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.SELECTED, true);
		
		NewNetworkSelectedNodesOnlyTask newNetTask2 = new NewNetworkSelectedNodesOnlyTask(mock(UndoSupport.class), net1, support.getRootNetworkFactory(), viewSupport.getNetworkViewFactory(),
				netMgr, mock(CyNetworkViewManager.class) , mock(CyNetworkNaming.class), mock(VisualMappingManager.class), mock(CyApplicationManager.class), eventHelper, groupMgr, renderingEngineManager);
		
		assertNotNull(newNetTask2);
		newNetTask2.setTaskIterator(new TaskIterator(newNetTask2));
		newNetTask2.run(mock(TaskMonitor.class));
		
	
		
		
		List<CyNetwork> thirdNetList  = new ArrayList<CyNetwork>(netMgr.getNetworkSet());
		thirdNetList.removeAll(secondNetList);
		assertEquals(1, thirdNetList.size());
		subnet2 = thirdNetList.get(0);		
		((CySubNetworkImpl) subnet2).handleEvent(new NetworkAddedEvent(netMgr, subnet2));
		
		
		assertEquals(2, subnet2.getNodeList().size());
		
		//check that the new subnetwork has both columns
		assertNotNull(subnet2.getDefaultNodeTable().getColumn(table1sCol));
		assertNotNull(subnet2.getDefaultNodeTable().getColumn(table2sCol));

		//these two tests are failing because the required table update when creating the subnetwork is not handled
		//hence the nodes are there but the related rows in the table are empty
		assertEquals(table1sRow1, subnet2.getRow(node1).get(table1sCol, String.class) );
		assertEquals(table2sRow1, subnet2.getRow(node1).get(table2sCol, String.class) );
		
		//creating another table to map to the net1 only
		table3 = new CyTableImpl("dummy table 3", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table3.createColumn(table2sCol, String.class, false);
		table3.createColumn(table3sCol, String.class, false);
		CyRow rowTemp;
		rowTemp = table3.getRow(row1Name);
		rowTemp.set(table2sCol, table2sRow1);
		rowTemp.set(table3sCol, table3sRow1);
		rowTemp = table3.getRow(row2Name);
		rowTemp.set(table2sCol, table2sRow2);
		rowTemp.set(table3sCol, table3sRow2);
		
		
		table4 = new CyTableImpl("dummy table 4", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table4.createColumn(table2sCol, String.class, false);
		table4.createColumn(table4sCol, String.class, false);
		
		rowTemp = table4.getRow(row1Name);
		rowTemp.set(table2sCol, table2sRow1);
		rowTemp.set(table4sCol, table4sRow1);
		rowTemp = table4.getRow(row2Name);
		rowTemp.set(table2sCol, table2sRow2);
		rowTemp.set(table4sCol, table4sRow2);
		
		tableMgr.addTable(table3);
		
		tableMgr.addTable(table4);
		
		List<String> listCols2 = new ArrayList<String>();
		listCols2.add(table3sCol);
		
		mappingGlobalTable(table3,table4,listCols2,table2sCol,false,null,null,table4.getColumn(table2sCol),false);
		
		assertEquals(table3sRow1, table4.getRow(row1Name).get(table3sCol, String.class) );
		
		assertEquals(table3sRow2, table4.getRow(row2Name).get(table3sCol, String.class) );
	
	}
	
	public void mapping(CyTable sourceTable,List<String> sourceColumnsList, String sourceKeyColumn,boolean mergeColumnVirtual, CyNetwork net,CyRootNetwork rootNet, CyColumn col, boolean selectedOnly) throws Exception{
		
		MergeTablesTaskFactoryImpl mappingTF = new MergeTablesTaskFactoryImpl(tableMgr,netMgr, ts, rootNetMgr);
		List<CyNetwork> nets = new ArrayList<CyNetwork>();
		nets.add(net);
		
		TaskIterator ti = mappingTF.createTaskIterator(sourceTable,null,sourceColumnsList,sourceKeyColumn,mergeColumnVirtual, true,selectedOnly, nets ,rootNet,col, CyNode.class);
		assertNotNull("task iterator is null", ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull("task is null", t);
		t.run(mock(TaskMonitor.class));
	
	}
	
    public void mappingGlobalTable(CyTable sourceTable,CyTable targetTable,List<String> sourceColumnsList, String sourceKeyColumn,boolean mergeColumnVirtual, CyNetwork net,CyRootNetwork rootNet, CyColumn col, boolean selectedOnly) throws Exception{
		
		MergeTablesTaskFactoryImpl mappingTF = new MergeTablesTaskFactoryImpl(tableMgr,netMgr, ts, rootNetMgr);
		List<CyNetwork> nets = new ArrayList<CyNetwork>();
		if(net != null)
			nets.add(net);
		
		TaskIterator ti = mappingTF.createTaskIterator(sourceTable,targetTable,sourceColumnsList,sourceKeyColumn,mergeColumnVirtual, false,selectedOnly, nets ,rootNet,col, CyNode.class);
		assertNotNull("task iterator is null", ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull("task is null", t);
		t.run(mock(TaskMonitor.class));
	
	}
}
