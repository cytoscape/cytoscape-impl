package org.cytoscape.task.internal.table;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.sync.*;

public class JoinTablesTaskTest {

private final NetworkTestSupport support = new NetworkTestSupport();

	
	private CyNetwork net1;
	private CyRootNetwork root;
	
	private CyNode node1;
	private CyNode node2;
	
	private String node1Name = "node1";
	private String node2Name = "node2";
	
	private CyTableImpl table1;

	private static CyEventHelper eventHelper = new DummyCyEventHelper();
	private static CyNetworkManagerImpl netMgr = new CyNetworkManagerImpl(eventHelper);	
	private static CyRootNetworkManagerImpl rootNetMgr = new CyRootNetworkManagerImpl();
	
	private static SyncTunableMutator stm = new SyncTunableMutator();
	private static SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
	private static TunableSetterImpl ts =new TunableSetterImpl(new SyncTunableMutatorFactory(syncTunableHandlerFactory),  new TunableRecorderManager());
	Properties syncFactoryProp = new Properties();
	
	@Test
	public void testJoinTableToRootNetwork() throws Exception{
		
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		creatNetwork();
		table1 = (CyTableImpl) createTable("col1", "listCol1");
		
		join(table1, net1, root, root.getDefaultNodeTable().getColumn(CyRootNetwork.SHARED_NAME), false);
		
		testJoinTableToRootNetworkColumn();
		testJoinTableToRootNetworkListColumn();
		
		testSubnetworkAfterJointoRoot();
		
	}
	

	public void testJoinTableToRootNetworkColumn(){
		assertNotNull(net1.getDefaultNodeTable().getColumn("col1"));
		assertEquals("col1 row1", net1.getRow(node1).get("col1", String.class) );
		assertTrue(net1.getDefaultNodeTable().getColumn("col1").getVirtualColumnInfo().isVirtual());
		
	}
	
	public void testJoinTableToRootNetworkListColumn(){
		assertNotNull(net1.getDefaultNodeTable().getColumn("listCol1"));
		assertEquals("listRow1-1", net1.getRow(node1).getList("listCol1", String.class).get(0) );
		assertTrue(net1.getDefaultNodeTable().getColumn("listCol1").getVirtualColumnInfo().isVirtual());
	
	}
	

	private void testSubnetworkAfterJointoRoot() {
		List<CyNode> nodes = new ArrayList<CyNode>();
		nodes.add(node1);
		nodes.add(node2);
		
		CyNetwork subNet = root.addSubNetwork(nodes, null);
		
		assertNotNull(subNet.getDefaultNodeTable().getColumn("col1"));
		assertEquals("col1 row1", subNet.getRow(node1).get("col1", String.class) );
		assertTrue(subNet.getDefaultNodeTable().getColumn("col1").getVirtualColumnInfo().isVirtual());

	}

	
	@Test
	public void testJoinTableToSubNetwork() throws Exception{
	
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		creatNetwork();

		CyTable table2 = createTable("col2", "listCol2");
		
		join(table2, net1, null, net1.getDefaultNodeTable().getColumn(CyNetwork.NAME), true);
		testJoinTableToSubNetworkColumns();
		testJoinTableToSubNetworkListColumns();
		
		testSubnetworkAfterJointoSub();
	}

	public void testJoinTableToSubNetworkColumns(){
		assertNotNull(net1.getDefaultNodeTable().getColumn("col2"));
		assertEquals("col1 row1", net1.getRow(node1).get("col2", String.class) );
		assertFalse(net1.getDefaultNodeTable().getColumn("col2").getVirtualColumnInfo().isVirtual());

	}
	
	public void testJoinTableToSubNetworkListColumns(){
		assertNotNull(net1.getDefaultNodeTable().getColumn("listCol2"));
		assertEquals("listRow1-1", net1.getRow(node1).getList("listCol2", String.class).get(0) );
		assertFalse(net1.getDefaultNodeTable().getColumn("listCol2").getVirtualColumnInfo().isVirtual());
		
	}
	
	private void testSubnetworkAfterJointoSub() {
		List<CyNode> nodes = new ArrayList<CyNode>();
		nodes.add(node1);
		nodes.add(node2);
		
		CyNetwork subNet = root.addSubNetwork(nodes, null);
		
		assertNull(subNet.getDefaultNodeTable().getColumn("col1"));
		
	}

	
	public void creatNetwork(){
		net1 = support.getNetwork();
		
		node1 =  net1.addNode();
		node2 =  net1.addNode();
		net1.addEdge(node1, node2, true);
		
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyRootNetwork.SHARED_NAME, node1Name);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyRootNetwork.SHARED_NAME, node2Name);
		
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.NAME, node1Name);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyNetwork.NAME, node2Name);
		
		
		net1.getRow(net1).set(CyNetwork.NAME, "net1");
		
		netMgr.addNetwork(net1);
		
		//check root network
		root = rootNetMgr.getRootNetwork(net1);
		assertEquals(2, root.getSharedNodeTable().getAllRows().size());
	}
	
	public CyTable createTable(String col, String listCol ){
		CyTable table1 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table1.createColumn(col, String.class, false);
		
		CyRow row1 = table1.getRow(node1Name);
		row1.set(col, "col1 row1");
		CyRow row2 = table1.getRow(node2Name);
		row2.set(col, "col1 row2");
		
		List<String> s = new ArrayList<String>();
		s.add("listRow1-1");
		s.add("listRow1-2");
		List<String> s2 = new ArrayList<String>();
		s2.add("listRow2-1");
		s2.add("listRow2-2");

		
		table1.createListColumn(listCol, String.class , false);
		CyRow listRow1 = table1.getRow(node1Name);
		listRow1.set(listCol, s);
		CyRow listRow2 = table1.getRow(node2Name);
		listRow2.set(listCol, s2);
		
		return table1;
	}
	
	public static void join(CyTable table, CyNetwork net, CyRootNetwork rootNet, CyColumn col, boolean selectedOnly) throws Exception{
		JoinTablesTaskTaskFactoryImpl joinTableTF = new JoinTablesTaskTaskFactoryImpl(netMgr, ts, rootNetMgr);
		List<CyNetwork> nets = new ArrayList<CyNetwork>();
		nets.add(net);
		
		TaskIterator ti = joinTableTF.createTaskIterator(table, selectedOnly, nets, rootNet, col , CyNode.class);
		assertNotNull("task iterator is null", ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull("task is null", t);
		t.run(mock(TaskMonitor.class));
	}
}
