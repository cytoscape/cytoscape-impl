package org.cytoscape.task.internal.setcurren;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;
import org.cytoscape.task.internal.setcurrent.SetCurrentNetworkTaskFactoryImpl;


public class SetCurrentNetworkTaskFactoryImplTest {

	@Test
	public void testRun() throws Exception {

		CyApplicationManager appMgr = mock(CyApplicationManager.class);
		CyNetworkManager netmgr = mock(CyNetworkManager.class);;

		SetCurrentNetworkTaskFactoryImpl factory = new SetCurrentNetworkTaskFactoryImpl(appMgr, netmgr);
		
		TaskIterator ti = factory.getTaskIterator();
		assertNotNull(ti);
		
		assertTrue( ti.hasNext() );
		Task t = ti.next();
		assertNotNull( t );		
	}
}
