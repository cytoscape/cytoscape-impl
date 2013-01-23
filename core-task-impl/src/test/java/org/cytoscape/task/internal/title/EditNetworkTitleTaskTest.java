package org.cytoscape.task.internal.title;

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



import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;


public class EditNetworkTitleTaskTest {
	@Test
	public void testRun() throws Exception {
		CyNetwork net = mock(CyNetwork.class);
		TaskMonitor tm = mock(TaskMonitor.class);
		CyNetworkManager netMgr = mock(CyNetworkManager.class);
		CyNetworkNaming cyNetworkNaming = mock(CyNetworkNaming.class);
		
		CyRow r1 =  mock(CyRow.class);

		when(net.getRow(net)).thenReturn(r1);
		when(r1.get("name",String.class)).thenReturn("title");
		
		UndoSupport undoSupport = mock(UndoSupport.class);
					
		EditNetworkTitleTask t = new EditNetworkTitleTask(undoSupport, net, netMgr,cyNetworkNaming);
		
		t.run(tm);
		
		verify(r1, times(1)).set("name", "title");

	}
}
