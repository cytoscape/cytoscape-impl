package org.cytoscape.task.internal.title;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class EditNetworkTitleTaskTest {
	@Test
	public void testRun() throws Exception {
		CyNetwork net = mock(CyNetwork.class);
		TaskMonitor tm = mock(TaskMonitor.class);
		CyNetworkManager netMgr = mock(CyNetworkManager.class);

		CyRow r1 =  mock(CyRow.class);

		when(net.getRow(net)).thenReturn(r1);
		when(r1.get("name",String.class)).thenReturn("title");
		
		UndoSupport undoSupport = mock(UndoSupport.class);
					
		EditNetworkTitleTask t = new EditNetworkTitleTask(undoSupport, net, netMgr);
		
		t.run(tm);
		
		verify(r1, times(1)).set("name", "title");

	}
}
