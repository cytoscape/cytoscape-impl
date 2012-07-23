package org.cytoscape.task.internal.table;


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
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.cytoscape.work.internal.sync.*;


public class MapTableToNetworkTableTaskTest {


	private final NetworkTestSupport support = new NetworkTestSupport();

	
	private CyNetwork net1;
	
	private CyNode node1;
	private CyNode node2;
	
	private String node1Name = "node1";
	private String node2Name = "node2";
	
	private CyTableImpl table1;

	private static CyEventHelper eventHelper = new DummyCyEventHelper();
	private static CyNetworkManagerImpl netMgr = new CyNetworkManagerImpl(eventHelper);	
	private static SyncTunableMutator stm = new SyncTunableMutator();
	private static TunableSetterImpl ts = new  TunableSetterImpl(stm, new TunableRecorderManager());
	SyncTunableHandlerFactory syncTunableHandlerFactory = new SyncTunableHandlerFactory();
	Properties syncFactoryProp = new Properties();
	
	@Test
	public void testMappingTableToNetwork() throws Exception{
		
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		
		net1 = support.getNetwork();
		
		node1 =  net1.addNode();
		node2 =  net1.addNode();
		net1.addEdge(node1, node2, true);
		
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.NAME, node1Name);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyNetwork.NAME, node2Name);
		
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
		UpdateAddedNetworkAttributes up = new UpdateAddedNetworkAttributes(new MapGlobalToLocalTableTaskFactoryImpl(mock(CyTableManager.class), netMgr, ts), new SyncTaskManager(stm));
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
