package org.cytoscape.task.internal.setcurren;

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
		
		HashSet<CyNetwork> netSet = new HashSet<>();
		netSet.add(net);
		
		when(netmgr.getNetworkSet()).thenReturn(netSet);

		SetCurrentNetworkTask t = new SetCurrentNetworkTask(applicationManager, netmgr);
		t.run(tm);

		verify(applicationManager, times(1)).setCurrentNetwork(net);
	}
}
