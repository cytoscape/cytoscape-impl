package org.cytoscape.task.internal.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
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
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.internal.CyNetworkManagerImpl;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CySubNetworkImpl;
import org.cytoscape.model.internal.CyTableImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesOnlyTask;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.internal.sync.SyncTaskManager;
import org.cytoscape.work.internal.sync.SyncTunableHandlerFactory;
import org.cytoscape.work.internal.sync.SyncTunableMutator;
import org.cytoscape.work.internal.sync.TunableRecorderManager;
import org.cytoscape.work.internal.sync.TunableSetterImpl;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class MappingIntegrationTest {


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
	public void mappingToAllNetworksWithSubNetwork() throws Exception{
		//set up tunable mutator
		stm.addTunableHandlerFactory(syncTunableHandlerFactory, syncFactoryProp);
		
		CyNetwork net1;
		CyNetwork subnet1;
		CyNetwork subnet2;

		String node1Name = "node1";
		String node2Name = "node2";

		CyTableImpl table1;
		CyTableImpl table2;
		
		String table1sCol = "col1";
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
				CyTable.SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table1.createColumn(table1sCol, String.class, false);
		
		CyRow row1 = table1.getRow(node1Name);
		row1.set(table1sCol, table1sRow1);
		CyRow row2 = table1.getRow(node2Name);
		row2.set(table1sCol, table1sRow2);
		
		mapping(table1, net1, false);
		//check the mapping by task
		assertNotNull(net1.getDefaultNodeTable().getColumn(table1sCol));
		assertEquals(table1sRow1, net1.getDefaultNodeTable().getRow(node1.getSUID()).get(table1sCol, String.class) );
		
		//creating a sub network to check if gets updated or not
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.SELECTED, true);
		net1.getDefaultNodeTable().getRow(node2.getSUID()).set(CyNetwork.SELECTED, true);
		
		NewNetworkSelectedNodesOnlyTask newNetTask = new NewNetworkSelectedNodesOnlyTask(mock(UndoSupport.class), net1, support.getRootNetworkFactory(), viewSupport.getNetworkViewFactory(),
				netMgr, mock(CyNetworkViewManager.class) , mock(CyNetworkNaming.class), mock(VisualMappingManager.class), mock(CyApplicationManager.class), eventHelper);
		
		assertNotNull(newNetTask);
		newNetTask.setTaskIterator(new TaskIterator(newNetTask));
		newNetTask.run(mock(TaskMonitor.class));
		
		assertEquals(2, netMgr.getNetworkSet().size());
		
		List<CyNetwork> newNetList = new ArrayList<CyNetwork>(netMgr.getNetworkSet());
		subnet1 = newNetList.get(1);
		((CySubNetworkImpl) subnet1).handleEvent(new NetworkAddedEvent(netMgr, subnet1));
		
		assertEquals(2, subnet1.getNodeList().size());
		
		assertNotNull(subnet1.getDefaultNodeTable().getColumn(table1sCol));
		//these two tests are failing because the required table update when creating the subnetwork is not handled
		//hence the nodes are there but the related rows in the table are empty
		assertEquals(table1sRow1, subnet1.getDefaultNodeTable().getRow(subnet1.getNodeList().get(0).getSUID()).get(table1sCol, String.class) );
				
		//creating another table to map to the net1 only
		table2 = new CyTableImpl("dummy table", "ID", String.class, true, true, 
				CyTable.SavePolicy.DO_NOT_SAVE , eventHelper, new InterpreterImpl(), 2);
		table2.createColumn(table2sCol, String.class, false);
		
		CyRow row3 = table2.getRow(node1Name);
		row3.set(table2sCol, table2sRow1);
		CyRow row4 = table2.getRow(node2Name);
		row4.set(table2sCol,table2sRow2);
		
		mapping(table2, net1, true);
		//check the mapping by task
		assertNotNull(net1.getDefaultNodeTable().getColumn(table2sCol));
		assertEquals(table2sRow1, net1.getDefaultNodeTable().getRow(node1.getSUID()).get(table2sCol, String.class) );
		assertNull(subnet1.getDefaultNodeTable().getColumn(table2sCol)); //subnet1 should not be mapped

		//creating another subnetwork (subnet2) to check that bot virtual columns will be added
		net1.getDefaultNodeTable().getRow(node1.getSUID()).set(CyNetwork.SELECTED, true);
		
		NewNetworkSelectedNodesOnlyTask newNetTask2 = new NewNetworkSelectedNodesOnlyTask(mock(UndoSupport.class), net1, support.getRootNetworkFactory(), viewSupport.getNetworkViewFactory(),
				netMgr, mock(CyNetworkViewManager.class) , mock(CyNetworkNaming.class), mock(VisualMappingManager.class), mock(CyApplicationManager.class), eventHelper);
		
		assertNotNull(newNetTask2);
		newNetTask2.setTaskIterator(new TaskIterator(newNetTask2));
		newNetTask2.run(mock(TaskMonitor.class));
		
		assertEquals(3, netMgr.getNetworkSet().size());
		
		newNetList = new ArrayList<CyNetwork>(netMgr.getNetworkSet());
		subnet2 = newNetList.get(0);
		((CySubNetworkImpl) subnet2).handleEvent(new NetworkAddedEvent(netMgr, subnet2));
		
		
		assertEquals(2, subnet2.getNodeList().size());
		
		//check that the new subnetwork has both columns
		assertNotNull(subnet2.getDefaultNodeTable().getColumn(table1sCol));
		assertNotNull(subnet2.getDefaultNodeTable().getColumn(table2sCol));

		//these two tests are failing because the required table update when creating the subnetwork is not handled
		//hence the nodes are there but the related rows in the table are empty
		assertEquals(table1sRow1, subnet2.getDefaultNodeTable().getRow(subnet2.getNodeList().get(0).getSUID()).get(table1sCol, String.class) );
		assertEquals(table2sRow1, subnet2.getDefaultNodeTable().getRow(subnet2.getNodeList().get(0).getSUID()).get(table2sCol, String.class) );
	
	}
	
	public void mapping(CyTable table, CyNetwork net, boolean selectedOnly) throws Exception{
		
		MapTableToNetworkTablesTaskFactoryImpl mappingTF = new MapTableToNetworkTablesTaskFactoryImpl(netMgr, ts);
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
