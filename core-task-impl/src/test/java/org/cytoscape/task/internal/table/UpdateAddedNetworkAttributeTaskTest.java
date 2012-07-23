package org.cytoscape.task.internal.table;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
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
import org.cytoscape.model.internal.CySubNetworkImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.sync.SyncTaskManager;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutator;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.junit.Before;
import org.junit.Test;

public class UpdateAddedNetworkAttributeTaskTest {

	
	private final NetworkTestSupport support = new NetworkTestSupport();
	private final NetworkViewTestSupport viewSupport = new NetworkViewTestSupport();
	private final TableTestSupport tableSupport = new TableTestSupport();


	private CyEventHelper eventHelper = new DummyCyEventHelper();
	private CyNetworkManagerImpl netMgr = new CyNetworkManagerImpl(eventHelper);	
	private SyncTunableMutator stm = new SyncTunableMutator();
	private TunableSetterImpl ts = new  TunableSetterImpl(stm, new TunableRecorderManager());
	CyTableManager tabMgr = new CyTableManagerImpl(eventHelper, new CyNetworkTableManagerImpl(), netMgr);
	private UpdateAddedNetworkAttributes up = new UpdateAddedNetworkAttributes(new MapGlobalToLocalTableTaskFactoryImpl(tabMgr, netMgr, ts), new SyncTaskManager(stm));
	SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
	Properties syncFactoryProp = new Properties();

	@Test
	public void testUpdatingAddedNetwork() throws Exception{
		//set up tunable mutator
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		
		CyNetwork net1;
		CyNetwork net2;
		CyNetwork net3;

		String node1Name = "node1";
		String node2Name = "node2";

		CyTableImpl table1;
		CyTableImpl table2;
		
		String tabel1sCol = "col1";
		String table2sCol = "col2";
		
		String table1sRow1 = "col1 row1";
		String table1sRow2 = "col1 row2";

		String table2sRow1 = "col2 row2";
		String table2sRow2 = "col2 row2";
		
		
		
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
	
		//creating a table for mapping to all networks
		table1 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table1.createColumn(tabel1sCol, String.class, false);
		
		CyRow row1 = table1.getRow(node1Name);
		row1.set(tabel1sCol, table1sRow1);
		CyRow row2 = table1.getRow(node2Name);
		row2.set(tabel1sCol, table1sRow2);
		
		mapping(table1, net1, false);
		//check the mapping by task
		assertNotNull(net1.getDefaultNodeTable().getColumn(tabel1sCol));
		assertEquals(table1sRow1, net1.getDefaultNodeTable().getRow(node1.getSUID()).get(tabel1sCol, String.class) );
		
		//creating a second network to check if gets updated or not
		net2 = support.getNetwork();
		net2.getRow(net2).set(CyNetwork.NAME, "net2");

		final CyNode node3 =  net2.addNode();
		final CyNode node4 =  net2.addNode();
		net2.addEdge(node3, node4, true);
		
		net2.getDefaultNodeTable().getRow(node3.getSUID()).set(CyNetwork.NAME, node1Name);
		net2.getDefaultNodeTable().getRow(node4.getSUID()).set(CyNetwork.NAME, node2Name);
		
		netMgr.addNetwork(net2);
		((CySubNetworkImpl) net2).handleEvent(new NetworkAddedEvent(netMgr, net2));
		tabMgr.addTable(net2.getDefaultNodeTable());
		
		up.handleEvent(new NetworkAddedEvent(netMgr, net2));
		//check if the mapping is done and if the table is updated
		assertNotNull(net2.getDefaultNodeTable().getColumn(tabel1sCol));
		assertEquals(table1sRow1, net2.getDefaultNodeTable().getRow(node3.getSUID()).get(tabel1sCol, String.class) );
		
		//creating another table to map to the second network only
		table2 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table2.createColumn(table2sCol, String.class, false);
		
		CyRow row3 = table2.getRow(node1Name);
		row3.set(table2sCol, table2sRow1);
		CyRow row4 = table2.getRow(node2Name);
		row4.set(table2sCol,table2sRow2);
		
		mapping(table2, net2, true);
		//check the mapping by task
		assertNotNull(net2.getDefaultNodeTable().getColumn(table2sCol));
		assertEquals(table2sRow1, net2.getDefaultNodeTable().getRow(node3.getSUID()).get(table2sCol, String.class) );
		assertNull(net1.getDefaultNodeTable().getColumn(table2sCol)); //net1 should not be mapped

		//creating another network to check that updater will add the first table but not the second to it
		net3 = support.getNetwork();
		net3.getRow(net3).set(CyNetwork.NAME, "net3");

		final CyNode node5 =  net3.addNode();
		final CyNode node6 =  net3.addNode();
		net3.addEdge(node5, node6, true);
		
		net3.getDefaultNodeTable().getRow(node5.getSUID()).set(CyNetwork.NAME, node1Name);
		net3.getDefaultNodeTable().getRow(node6.getSUID()).set(CyNetwork.NAME, node2Name);
		
		netMgr.addNetwork(net3);
		((CySubNetworkImpl) net3).handleEvent(new NetworkAddedEvent(netMgr, net3));
		tabMgr.addTable(net3.getDefaultNodeTable());
		up.handleEvent(new NetworkAddedEvent(netMgr, net3));
		//check the mappings
		assertNotNull(net3.getDefaultNodeTable().getColumn(tabel1sCol));
		assertEquals(table1sRow1, net3.getDefaultNodeTable().getRow(node5.getSUID()).get(tabel1sCol, String.class) );
		assertNull(net3.getDefaultNodeTable().getColumn(table2sCol)); //net3 should not be mapped to table2


	}
	
	
	public void mapping(CyTable table, CyNetwork net, boolean selectedOnly ) throws Exception{
		
		MapTableToNetworkTablesTaskFactoryImpl mappingTF = new MapTableToNetworkTablesTaskFactoryImpl(netMgr, ts, up);
		List<CyNetwork> nets = new ArrayList<CyNetwork>();
		nets.add(net);
		
		TaskIterator ti = mappingTF.createTaskIterator(table, selectedOnly, nets , CyNode.class);
		assertNotNull("task iterator is null", ti);
		
		assertTrue(ti.hasNext());
		Task t = ti.next();
		assertNotNull("task is null", t);
		t.run(mock(TaskMonitor.class));
	
	}
	
}
