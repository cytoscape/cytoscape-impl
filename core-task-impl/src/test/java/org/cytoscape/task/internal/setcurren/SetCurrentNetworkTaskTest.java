package org.cytoscape.task.internal.setcurren;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.internal.setcurrent.SetCurrentNetworkTask;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class SetCurrentNetworkTaskTest {

	@Test
	public void testRun() throws Exception {
		CyApplicationManager applicationManager = mock(CyApplicationManager.class);
		CyNetworkManager netmgr = mock(CyNetworkManager.class);;
		TaskMonitor tm= mock(TaskMonitor.class);
		CyNetwork net= mock(CyNetwork.class);
		
		HashSet<CyNetwork> netSet = new HashSet<CyNetwork>();
		netSet.add(net);
		
		when(netmgr.getNetworkSet()).thenReturn(netSet);

		SetCurrentNetworkTask t = new SetCurrentNetworkTask(applicationManager, netmgr);
		t.run(tm);

		verify(applicationManager, times(1)).setCurrentNetwork(net.getSUID());
	}
}
