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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutator;
import org.cytoscape.work.internal.sync.SyncTunableMutatorFactory;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.junit.Before;
import org.junit.Test;


public class MapTableToNetworkTableTaskTest {


	private final NetworkTestSupport support = new NetworkTestSupport();

	
	private CyNetwork net1;
	
	private CyNode node1;
	private CyNode node2;
	
	private String node1Name = "node1";
	private String node2Name = "node2";
	
	private CyTableImpl table1;

	private static CyEventHelper eventHelper = new DummyCyEventHelper();
	private static CyNetworkNaming namingUtil = mock(CyNetworkNaming.class);
    private static CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
	private static CyNetworkManagerImpl netMgr = new CyNetworkManagerImpl(serviceRegistrar);
	private static CyRootNetworkManagerImpl rootNetMgr = new CyRootNetworkManagerImpl();
	
	private static SyncTunableMutator stm = new SyncTunableMutator();
	private static SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
	private static TunableSetterImpl ts =new TunableSetterImpl(new SyncTunableMutatorFactory(syncTunableHandlerFactory),  new TunableRecorderManager());
	Properties syncFactoryProp = new Properties();
	
	@Before
	public void setUp() throws Exception {
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
        when(serviceRegistrar.getService(CyNetworkNaming.class)).thenReturn(namingUtil);
	}
	
	@Test
	public void testMappingTableToNetwork() throws Exception{
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		
		net1 = support.getNetwork();
		
		node1 =  net1.addNode();
		node2 =  net1.addNode();
		net1.addEdge(node1, node2, true);
		
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyRootNetwork.SHARED_NAME, node1Name);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyRootNetwork.SHARED_NAME, node2Name);
		
		netMgr.addNetwork(net1);
		
		table1 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table1.createColumn("col1", String.class, false);
		
		CyRow row1 = table1.getRow(node1Name);
		row1.set("col1", "col1 row1");
		CyRow row2 = table1.getRow(node2Name);
		row2.set("col1", "col1 row2");
		
		mapping(table1, net1, false);
		
		assertNotNull(net1.getDefaultNodeTable().getColumn("col1"));
		assertEquals("col1 row1", net1.getRow(node1).get("col1", String.class) );

	}
	
	
	public static void mapping(CyTable table, CyNetwork net, boolean selectedOnly) throws Exception{
		MapTableToNetworkTablesTaskFactoryImpl mappingTF = new MapTableToNetworkTablesTaskFactoryImpl(netMgr, ts, rootNetMgr);
		List<CyNetwork> nets = new ArrayList<>();
		nets.add(net);
		
		TaskIterator ti = mappingTF.createTaskIterator(table, selectedOnly, nets , CyNode.class);
		assertNotNull("task iterator is null", ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull("task is null", t);
		t.run(mock(TaskMonitor.class));
	}
}
